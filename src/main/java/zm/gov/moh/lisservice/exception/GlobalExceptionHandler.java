package zm.gov.moh.lisservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.ErrorResponse;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Order(-2)
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String traceId = UUID.randomUUID().toString();
        String path = exchange.getRequest().getPath().value();

        ErrorResponse errorResponse = buildErrorResponse(ex, path, traceId);

        int statusCode = determineStatusCode(ex);

        if (statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            log.error("Server error - TraceId: {}, Path: {}", traceId, path, ex);
        } else if (statusCode >= HttpStatus.BAD_REQUEST.value()) {
            log.warn("Client error - TraceId: {}, Path: {}, Error: {}", traceId, path, ex.getMessage());
        }

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(statusCode));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(
                Mono.fromCallable(() -> {
                    try {
                        byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);

                        return exchange.getResponse()
                                .bufferFactory()
                                .wrap(bytes);
                    } catch (Exception e) {
                        log.error("Error serializing error response", e);

                        String fallback = "{\"errorCode\":\"INTERNAL_ERROR\"," +"\"message\":\"An error occurred\"}";

                        return exchange.getResponse()
                                .bufferFactory()
                                .wrap(fallback.getBytes(StandardCharsets.UTF_8));
                    }
                })
        );
    }

    private ErrorResponse buildErrorResponse(Throwable ex, String path, String traceId) {
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .traceId(traceId)
                .path(path);

        if (ex instanceof BaseException baseEx) {
            builder.errorCode(baseEx.getErrorCode())
                    .message(baseEx.getMessage());

            if (ex instanceof ValidationException valEx) {
                builder.fieldErrors(valEx.getFieldErrors());
            }
        } else if (ex instanceof ResponseStatusException rsEx) {
            builder.errorCode("HTTP_" + rsEx.getStatusCode().value())
                    .message(rsEx.getReason() != null ? rsEx.getReason() : rsEx.getMessage());
        } else {
            builder.errorCode("INTERNAL_SERVER_ERROR")
                    .message("An unexpected error occurred");
        }

        return builder.build();
    }

    private int determineStatusCode(Throwable ex) {
        if (ex instanceof BaseException baseEx) {
            return baseEx.getHttpStatus();
        } else if (ex instanceof ResponseStatusException rsEx) {
            return rsEx.getStatusCode().value();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
