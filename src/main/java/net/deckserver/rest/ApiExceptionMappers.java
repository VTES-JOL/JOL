package net.deckserver.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

/**
 * Consistent structured error bodies for the REST API.
 *
 * <p>Before this, an unknown / stale game id surfaced as
 * {@code IllegalArgumentException} from {@code GameService.getNameByGameId} and
 * every game endpoint 500'd; service {@code IllegalStateException}s (deliberate
 * validation refusals) also 500'd or were caught ad hoc. These mappers give
 * every one of them the right status and one body shape:
 *
 * <pre>{@code { "code": "not_found", "message": "No game with id: …" } }</pre>
 *
 * <p>Deliberately targeted, not a catch-all: a bare {@code Exception} /
 * {@code Throwable} mapper would also swallow Quarkus security exceptions and
 * genuine server faults. Anything not listed here still falls through to
 * Quarkus's default 500 handler <em>with its stack trace logged</em>. The
 * {@code submit} / {@code end-turn} "rejected" path (D8/D11) is a 200 with a
 * flag — not an exception — so it is untouched.
 */
final class ApiExceptionMappers {

    private ApiExceptionMappers() {}

    /** The single error body shape. {@code code} is a stable machine token; {@code message} is human text. */
    public record ApiError(String code, String message) {}

    static String codeFor(int status) {
        return switch (status) {
            case 400 -> "bad_request";
            case 401 -> "unauthorized";
            case 403 -> "forbidden";
            case 404 -> "not_found";
            case 405 -> "method_not_allowed";
            case 409 -> "conflict";
            case 415 -> "unsupported_media_type";
            case 422 -> "unprocessable";
            default -> status >= 500 ? "server_error" : "error";
        };
    }

    static Response json(Response.Status status, String code, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError(code, message == null ? status.getReasonPhrase() : message))
                .build();
    }
}

/** Unknown / not-found domain object (services throw {@link NoSuchElementException} for this). → 404 */
@Provider
class NoSuchElementExceptionMapper implements ExceptionMapper<NoSuchElementException> {
    private static final Logger log = LoggerFactory.getLogger(NoSuchElementExceptionMapper.class);

    @Override
    public Response toResponse(NoSuchElementException e) {
        log.debug("404 (not found): {}", e.getMessage());
        return ApiExceptionMappers.json(Response.Status.NOT_FOUND, "not_found", e.getMessage());
    }
}

/** Bad caller input that reached the service layer as a raw {@link IllegalArgumentException}. → 400 */
@Provider
class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    private static final Logger log = LoggerFactory.getLogger(IllegalArgumentExceptionMapper.class);

    @Override
    public Response toResponse(IllegalArgumentException e) {
        log.debug("400 (bad request): {}", e.getMessage());
        return ApiExceptionMappers.json(Response.Status.BAD_REQUEST, "bad_request", e.getMessage());
    }
}

/**
 * A deliberate engine / service refusal that wasn't converted to a typed
 * response earlier (e.g. {@code JolAdmin.registerDeck} validation,
 * {@code GameService.loadSnapshot} missing turn). → 409. Logged at WARN with the
 * stack because this type is ambiguous — an unexpected one is still worth seeing.
 */
@Provider
class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
    private static final Logger log = LoggerFactory.getLogger(IllegalStateExceptionMapper.class);

    @Override
    public Response toResponse(IllegalStateException e) {
        log.warn("409 (conflict) from IllegalStateException: {}", e.getMessage(), e);
        return ApiExceptionMappers.json(Response.Status.CONFLICT, "conflict", e.getMessage());
    }
}

/**
 * Every {@code jakarta.ws.rs} exception a resource throws directly
 * ({@code NotFoundException}, {@code BadRequestException}, {@code ForbiddenException},
 * {@code ClientErrorException(status)}, …): keep its status, but replace the
 * default empty / HTML body with the same {@code {code,message}} shape. Does NOT
 * match {@code io.quarkus.security.*} exceptions (those aren't
 * {@code WebApplicationException} and keep Quarkus's own auth handling).
 */
@Provider
class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger log = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException e) {
        Response original = e.getResponse();
        int status = original != null ? original.getStatus() : 500;
        // A resource that already built its own entity body keeps it untouched.
        if (original != null && original.hasEntity()) {
            return original;
        }
        if (status >= 500) {
            log.warn("{} from {}: {}", status, e.getClass().getSimpleName(), e.getMessage(), e);
        } else {
            log.debug("{} ({}): {}", status, ApiExceptionMappers.codeFor(status), e.getMessage());
        }
        Response.Status resolved = Response.Status.fromStatusCode(status);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiExceptionMappers.ApiError(
                        ApiExceptionMappers.codeFor(status),
                        e.getMessage() != null ? e.getMessage()
                                : (resolved != null ? resolved.getReasonPhrase() : "Error")))
                .build();
    }
}
