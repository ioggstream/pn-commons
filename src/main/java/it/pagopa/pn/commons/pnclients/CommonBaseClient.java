package it.pagopa.pn.commons.pnclients;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class CommonBaseClient {

    protected CommonBaseClient(){
    }

    @Value("${pn.log.trace-id-header}")
    private String traceIdHeader;

    protected WebClient.Builder enrichBuilder(WebClient.Builder builder){
        return enrichBuilderWithTraceId(builder);
    }

    private WebClient.Builder enrichBuilderWithTraceId(WebClient.Builder builder){
        return builder
                .filters(filterList -> filterList.add(ExchangeFilterFunction.ofRequestProcessor(this::traceIdFilter)));
    }

    private Mono<ClientRequest> traceIdFilter(ClientRequest request) {
        String traceId = MDC.get("trace_id");
        if (StringUtils.hasText(traceIdHeader) && StringUtils.hasText(traceId))
        {
            return Mono.just(ClientRequest.from(request)
                    .header(traceIdHeader, traceId)
                    .build());
        }
        else
        {
            log.warn("missing trace_id");
            return Mono.just(request);
        }

    }

}
