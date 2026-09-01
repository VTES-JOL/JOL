# Testing

Three tiers, by what they need to run.

## 1. Unit tests — pure logic, no database

Plain JUnit 5, `*Test`, no container, no Quarkus boot. Run anywhere:

```bash
./mvnw test -Dtest='ParserServiceTest,GameOutcomeTest,CommandParserTest,GameNamesTest,\
CardRegistryTest,AuthServiceTest,CardSearchServiceTest,DeckImportServiceTest,\
GuavaTablePersistenceTest,TokenServiceTest,DeckNormalizerTest,TournamentDefinitionTest'
```

`AuthServiceTest` / `TokenServiceTest` / `ParserServiceTest` set `ENABLE_TEST_MODE=true`
via `@SetEnvironmentVariable`, which makes the service singletons in-memory-only, so
they still touch no DB.

## 2. JPA tests — real Postgres, real migrations

`net.deckserver.jpa.repository.*`, annotated `@ExtendWith(PostgresJpaExtension.class)`.

`PostgresJpaExtension` (`src/test/java/net/deckserver/testsupport/`):

* starts one `postgres:16-alpine` container per JVM via Testcontainers;
* before **every** test class, runs `flyway clean` + `migrate` over
  `classpath:db/migration` (the real production migrations) — so each class
  starts from a bare, fully-migrated schema and inserts its own rows;
* builds the `jol-test-pu` `EntityManagerFactory`
  (`src/test/resources/META-INF/persistence.xml`) with `hbm2ddl=validate`, i.e.
  the schema is owned by Flyway exactly as in production.

Individual tests wrap their work in a transaction they roll back in
`@AfterEach`.

The declarative fixture `db/testseed/V900__test_fixture.sql` (all names
prefixed `fixture-`) is applied only by tests that opt in with
`PostgresJpaExtension.applyTestSeed()` in `@BeforeAll` — currently
`FixtureDataTest`. It is dropped again by the next class's schema reset, so it
never perturbs the insert-and-assert repository tests.

**Docker required.** When Docker is absent the whole tier is *skipped* (the
extension's `ExecutionCondition`), not failed — so `./mvnw test` still succeeds
locally without Docker, but CI (which has Docker) runs it.

`FixtureDataTest` asserts the `db/testseed` fixture loaded and that the
per-class schema reset works.

Adding an `@Entity`: register it in `persistence.xml`'s `jol-test-pu` `<class>`
list (the main persistence unit auto-discovers; this one does not).

## 3. `quarkus:dev` — disposable bootstrap environment

```bash
ENABLE_CAPTCHA=false ./mvnw quarkus:dev
```

No JDBC URL is configured under `%dev`, so Quarkus Dev Services boots a
throwaway `postgres:16-alpine` and Flyway loads `db/migration` +
`db/devseed/R__dev_bootstrap.sql` (a repeatable migration, so edits re-apply
even against a reused container): **Player1..Player5, password `password`**;
Player1 has `ADMIN` + `TOURNAMENT_ADMIN`.

`%dev.quarkus.datasource.devservices.reuse=true` keeps the container (and its
data) between restarts *if* you have `testcontainers.reuse.enable=true` in
`~/.testcontainers.properties`; otherwise every start is clean.

### Point dev at a real Postgres

```bash
JOL_DB_URL=jdbc:postgresql://host/db JOL_DB_USER=… JOL_DB_PASSWORD=… \
  ./mvnw quarkus:dev -Dquarkus.profile=prodlike
```

`%prodlike` never migrates or cleans the target (`migrate-at-start=false`,
`clean-disabled=true`) and logs any query slower than 50 ms.

`migrate-to-db.sh` / `load-test-fixtures.sh` remain **only** for seeding a real
Postgres from a production JSON snapshot. They are not part of testing or dev.

---

## Deferred (removed from the tree, recover from git history)

The service-level suite that booted an in-memory H2 via the old
`JolServiceExtension` / `JolFixtureLoader` (fed by the ~150-game JSON dump under
`src/test/resources/data`) was removed. It coupled tests to a large opaque
fixture and never exercised the migrations. Bringing it back means driving the
service singletons against a Testcontainers Postgres (they do their own static
transaction management, so `@QuarkusTest` + `@TestTransaction` alone will not
isolate them — that design work is what was deferred).

Removed:

| Area | Classes |
|---|---|
| Services | `PlayerServiceTest`, `DeckServiceTest`, `DeckValidityServiceTest`, `GlobalChatServiceTest`, `HistoryServiceTest`, `MetricsServiceTest`, `PlayerActivityServiceTest`, `PlayerGameActivityServiceTest`, `RefreshTokenServiceTest`, `SiteNotesServiceTest`, `SubscriptionServiceTest`, `TournamentLifecycleTest` |
| Game model | `DoCommandTest`, `JolGameTest`, `GameModelSubmitTest`, `CommandParserTest` (all need the `command-test` game loaded through `GameService`) |
| BDD | `game/model/bdd/**` + `src/test/resources/features/**` |
| Jobs / REST | `RegistrationReconciliationTest`, `rest/MetricsResourceTest` |

Also removed: `com.h2database:h2`, the `jol-repo-test-pu` persistence unit, and
the large fixture reader. `src/test/resources/data/**` is still present (used by
the card-database Builder tests and available for any reinstated suite).
