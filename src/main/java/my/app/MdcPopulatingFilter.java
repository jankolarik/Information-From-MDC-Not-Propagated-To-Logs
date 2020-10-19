package my.app;

import io.micronaut.core.annotation.Order;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;

import java.security.Principal;

/**
 * Filter that propagates HTTP principal attribute to {@link MDC}.
 */
@Filter("/**")
@Order(1)
public class MdcPopulatingFilter extends  OncePerRequestHttpServerFilter{
    private static final String PRINCIPAL = "principal";

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        MDC.put(PRINCIPAL, request.getUserPrincipal().map(Principal::getName).orElse("unknown"));
        return Publishers.map(chain.proceed(request), response -> response);
    }
}

