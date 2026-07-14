package moh.gov.zm.lis.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "Paged response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {
    @Schema(description = "List of items", example = "[{\"id\":1,\"name\":\"Item 1\"}, ...]")
    List<T> content;

    @Schema(description = "Total number of items", example = "100")
    Long totalElements;

    @Schema(description = "Total number of pages", example = "10")
    Integer totalPages;

    @Schema(description = "Current page number", example = "1")
    Integer currentPage;

    @Schema(description = "Number of items per page", example = "20")
    Integer size;

    Boolean hasNext;

    Boolean hasPrevious;
}
