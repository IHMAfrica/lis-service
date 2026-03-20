package zm.gov.moh.lisservice.constant;

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
    List<T> content;
    Long totalElements;
    Integer totalPages;
    Integer currentPage;
    Integer size;
    Boolean hasNext;
    Boolean hasPrevious;
}
