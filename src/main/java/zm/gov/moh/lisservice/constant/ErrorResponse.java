package zm.gov.moh.lisservice.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder
@Schema(description = "Error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @Schema(description = "Error code", example = "RESOURCE_NOT_FOUND")
    String errorCode;
    @Schema(description = "Error message", example = "User not found with id: 1234")
    String message;
    @Schema(description = "Timestamp", example = "2025-09-25T12:34:56.789")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();
    @Schema(description = "Request path", example = "/api/v1/users/1234")
    String path;
    @Schema(description = "Field errors", example = "[{\"field\":\"username\",\"message\":\"Username is required\"}]")
    Map<String, String> fieldErrors;
    @Schema(description = "Trace ID for debugging", example = "a1b2c3d4-e5f6-7890")
    String traceId;
}
