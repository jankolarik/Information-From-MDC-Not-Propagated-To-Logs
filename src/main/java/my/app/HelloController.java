package my.app;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/hello")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class HelloController {

    static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @Get
    @Produces(MediaType.TEXT_PLAIN)
    public String index() {
        LOGGER.info("Get Hello World");
        return "Hello World";
    }
}
