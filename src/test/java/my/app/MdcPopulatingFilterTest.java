package my.app;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link MdcPopulatingFilter} class.
 *
 * Specifically, whether the principal name propagates to the log messages.
 */
@MicronautTest(application =  HelloController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MdcPopulatingFilterTest {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String LOGGER_NAME = "HTTP_ACCESS_LOGGER";

    @Inject
    private EmbeddedServer embeddedServer;

    private LoggerContext ctx;
    private ListAppender appender;

    @BeforeEach
    void setupLogging() {
        ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        appender = config.getAppender("LIST");
        appender.start();
        ctx.getLogger(LOGGER_NAME)
                .addAppender(appender);

        ctx.updateLoggers();
    }

    @AfterEach
    void clear() {
        ctx.getLogger(LOGGER_NAME)
                .removeAppender(appender);
        appender.stop();
        appender.clear();
        ctx.updateLoggers();
    }

    @Test
    void testPrincipalInLogs() {
        try (RxHttpClient httpClient = RxHttpClient.create(embeddedServer.getURL())) {
            // given
            appender.clear();

            //when
            HttpResponse<String> rsp = httpClient.toBlocking().exchange(HttpRequest.GET("/hello")
                            .basicAuth(USERNAME, PASSWORD),
                    String.class);

            // then
            assertEquals(rsp.getStatus(),  HttpStatus.OK);

            List<String> messages = appender.getMessages();

            assertFalse(messages.isEmpty());

            boolean principalLogged = messages.stream()
                    .allMatch(message -> message.contains(USERNAME));

            assertTrue(principalLogged);
        }
    }
}

