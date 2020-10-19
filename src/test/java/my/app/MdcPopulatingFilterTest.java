package my.app;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests the {@link MdcPopulatingFilter} class.
 *
 * Specifically, whether the principal name propagates to the log messages.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MdcPopulatingFilterTest {
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String PRINCIPAL = "principal";

    @Inject
    private EmbeddedServer embeddedServer;

    private LoggerContext ctx;

    private Logger logger;
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @BeforeEach
    void setupLogging() {
        ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = LoggerFactory.getLogger(HelloController.class);
        listAppender.start();
        ctx.getLogger(logger.getName()).addAppender(listAppender);
    }

    @AfterEach
    void clear() {
        listAppender.stop();
    }

    @Test
    void testPrincipalInLogs() {
        try (RxHttpClient httpClient = RxHttpClient.create(embeddedServer.getURL())) {
            //when
            HttpResponse<String> rsp = httpClient.toBlocking().exchange(HttpRequest.GET("/hello")
                            .basicAuth(USERNAME, PASSWORD),
                    String.class);

            // then
            assertEquals(rsp.getStatus(),  HttpStatus.OK);

            assertEquals(1, listAppender.list.size());
            assertNotNull(listAppender.list.get(0).getMDCPropertyMap().get(PRINCIPAL));
            assertEquals(USERNAME, listAppender.list.get(0).getMDCPropertyMap().get(PRINCIPAL));
        }
    }
}

