package moh.gov.zm.lis.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.common.ErrorResponse;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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

        MappedError mapped = mapException(ex, path, traceId);

        if (mapped.status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            log.error("Server error - TraceId: {}, Path: {}", traceId, path, ex);
        } else if (mapped.status >= HttpStatus.BAD_REQUEST.value()) {
            log.warn("Client error - TraceId: {}, Path: {}, Status: {}, Error: {}",
                    traceId, path, mapped.status, ex.getMessage());
        }

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(mapped.status));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(
                Mono.fromCallable(() -> {
                    try {
                        byte[] bytes = objectMapper.writeValueAsBytes(mapped.response);
                        return exchange.getResponse().bufferFactory().wrap(bytes);
                    } catch (Exception e) {
                        log.error("Error serializing error response", e);
                        String fallback = "{\"errorCode\":\"INTERNAL_ERROR\",\"message\":\"An error occurred\"}";
                        return exchange.getResponse().bufferFactory()
                                .wrap(fallback.getBytes(StandardCharsets.UTF_8));
                    }
                })
        );
    }

    private MappedError mapException(Throwable ex, String path, String traceId) {
        ErrorResponse.ErrorResponseBuilder b = ErrorResponse.builder().traceId(traceId).path(path);

        return switch (ex) {

            case ValidationException valEx -> {
                b.errorCode(valEx.getErrorCode())
                        .message(valEx.getMessage())
                        .fieldErrors(valEx.getFieldErrors());
                yield new MappedError(valEx.getHttpStatus(), b.build());
            }
            case BaseException baseEx -> {
                b.errorCode(baseEx.getErrorCode()).message(baseEx.getMessage());
                yield new MappedError(baseEx.getHttpStatus(), b.build());
            }

            case InvalidBearerTokenException ibtEx -> {
                b.errorCode("INVALID_TOKEN").message("Bearer token is invalid or has expired");
                yield new MappedError(HttpStatus.UNAUTHORIZED.value(), b.build());
            }
            case AuthenticationException authEx -> {
                b.errorCode("UNAUTHORIZED").message("Authentication is required to access this resource");
                yield new MappedError(HttpStatus.UNAUTHORIZED.value(), b.build());
            }

            case AccessDeniedException ignored -> {
                b.errorCode("ACCESS_DENIED").message("You do not have permission to perform this action");
                yield new MappedError(HttpStatus.FORBIDDEN.value(), b.build());
            }

            case WebExchangeBindException bindEx -> {
                Map<String, String> fieldErrors = new HashMap<>();
                bindEx.getBindingResult().getFieldErrors()
                        .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
                b.errorCode("VALIDATION_ERROR").message("Validation failed").fieldErrors(fieldErrors);
                yield new MappedError(HttpStatus.BAD_REQUEST.value(), b.build());
            }

            case ConstraintViolationException cvEx -> {
                Map<String, String> fieldErrors = new HashMap<>();
                cvEx.getConstraintViolations().forEach(cv -> {
                    String field = cv.getPropertyPath().toString();

                    int dot = field.lastIndexOf('.');
                    fieldErrors.put(dot >= 0 ? field.substring(dot + 1) : field, cv.getMessage());
                });
                b.errorCode("VALIDATION_ERROR").message("Validation failed").fieldErrors(fieldErrors);
                yield new MappedError(HttpStatus.BAD_REQUEST.value(), b.build());
            }

            case WebClientResponseException wcEx -> {
                int upstreamStatus = wcEx.getStatusCode().value();
                if (upstreamStatus == HttpStatus.UNAUTHORIZED.value()
                        || upstreamStatus == HttpStatus.BAD_REQUEST.value()) {

                    b.errorCode("INVALID_CREDENTIALS").message("Invalid credentials or token");
                    yield new MappedError(HttpStatus.UNAUTHORIZED.value(), b.build());
                } else if (upstreamStatus == HttpStatus.CONFLICT.value()) {
                    b.errorCode("CONFLICT").message("A resource with the provided details already exists");
                    yield new MappedError(HttpStatus.CONFLICT.value(), b.build());
                } else if (upstreamStatus == HttpStatus.NOT_FOUND.value()) {
                    b.errorCode("UPSTREAM_NOT_FOUND").message("The requested resource was not found upstream");
                    yield new MappedError(HttpStatus.NOT_FOUND.value(), b.build());
                } else {
                    log.error("Upstream service error - status: {}, body: {}",
                            upstreamStatus, wcEx.getResponseBodyAsString());
                    b.errorCode("UPSTREAM_ERROR").message("An upstream service returned an unexpected error");
                    yield new MappedError(HttpStatus.BAD_GATEWAY.value(), b.build());
                }
            }

            case MethodNotAllowedException methodEx -> {
                b.errorCode("METHOD_NOT_ALLOWED")
                        .message("HTTP method " + methodEx.getHttpMethod() + " is not supported for this endpoint");
                yield new MappedError(HttpStatus.METHOD_NOT_ALLOWED.value(), b.build());
            }
            case ServerWebInputException inputEx -> {
                b.errorCode("BAD_REQUEST")
                        .message(inputEx.getReason() != null ? inputEx.getReason() : "Invalid request input");
                yield new MappedError(HttpStatus.BAD_REQUEST.value(), b.build());
            }
            case ResponseStatusException rsEx -> {
                b.errorCode("HTTP_" + rsEx.getStatusCode().value())
                        .message(rsEx.getReason() != null ? rsEx.getReason() : rsEx.getMessage());
                yield new MappedError(rsEx.getStatusCode().value(), b.build());
            }

            case DataIntegrityViolationException ignored -> {
                b.errorCode("CONSTRAINT_VIOLATION")
                        .message("The request violates a data integrity constraint (e.g. a duplicate or an unknown reference)");
                yield new MappedError(HttpStatus.CONFLICT.value(), b.build());
            }

            default -> {
                b.errorCode("INTERNAL_SERVER_ERROR").message("An unexpected error occurred");
                yield new MappedError(HttpStatus.INTERNAL_SERVER_ERROR.value(), b.build());
            }
        };
    }

    private record MappedError(int status, ErrorResponse response) {}
}
