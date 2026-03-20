package zm.gov.moh.lisservice.constant;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListResponse<T> {
    List<T> content;
    Integer totalElements;
}
