package net.deckserver.servlet;

import net.deckserver.JolAdmin;
import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.cards.SecuredCardLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

    private void setupPlaytestAuth(HttpServletResponse response) {
        logger.info("Setting up playtest auth cookies");
        SecuredCardLoader cardLoader = new SecuredCardLoader("/secured/*");
        try {
            boolean devMode = System.getenv().getOrDefault("TYPE", "dev").equals("dev");
            if (!devMode) {
                String additionalSettings = ";HttpOnly; Domain=deckserver.net; Path=/; Secure;";
                CookiesForCustomPolicy cookies = cardLoader.generateCookies();
                response.addHeader("Set-Cookie", cookies.policyHeaderValue() + additionalSettings);
                response.addHeader("Set-Cookie", cookies.signatureHeaderValue() + additionalSettings);
                response.addHeader("Set-Cookie", cookies.keyPairIdHeaderValue() + additionalSettings);
            }
        } catch (Exception e) {
            logger.error("Unable to set playtest auth cookies", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean authResult = PlayerService.authenticate(username, password);
        if (authResult) {
            request.getSession().setAttribute("meth", username);
            boolean playTester = JolAdmin.INSTANCE.isPlaytester(username);
            if (playTester) {
                setupPlaytestAuth(response);
            }
            response.sendRedirect("/jol/");
        } else {
            response.sendRedirect("/jol/login");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
    }
}
