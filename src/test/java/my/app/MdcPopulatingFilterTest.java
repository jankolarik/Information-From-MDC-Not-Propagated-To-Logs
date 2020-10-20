package my.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

/**
 * Tests the {@link MdcPopulatingFilter} class.
 *
 * Specifically, whether the principal name propagates to the log messages.
 */
@MicronautTest(application =  HelloController.class)
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
        appender = new ListAppender("LIST", null, PatternLayout.newBuilder()
                .withPattern("%d{yyyy-MM-dd zzz HH:mm:ss,SSS} [%t] [%X{principal}] %-5level %logger{36} - %msg%n")
                .build(), false, false);
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
    void testPrincipalInLogs() throws InterruptedException {
        try (RxHttpClient httpClient = RxHttpClient.create(embeddedServer.getURL())) {
            // given
            appender.clear();

            //when
            HttpResponse<String> rsp = httpClient.toBlocking().exchange(HttpRequest.GET("/hello")
                            .basicAuth(USERNAME, PASSWORD),
                    String.class);

            // then
            assertEquals(rsp.getStatus(),  HttpStatus.OK);
            awaitMessages();

            boolean principalLogged = appender.getMessages().stream()
                    .allMatch(message -> message.contains(USERNAME));

            assertTrue(principalLogged);
        }
    }

    private void awaitMessages() throws InterruptedException {
        int i = 0;
        while(appender.getMessages().isEmpty() && i < 10) {
            Thread.sleep(500);
            i++;
        }
        assertFalse(appender.getMessages().isEmpty());
    }
}

