package moh.gov.zm.lis.print.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

public interface BarcodePrintDTO {
    @Data
    @Builder
    @Schema(description = "Outcome of a barcode print job")
    class PrintResult {
        @Schema(description = "The lab order the barcode encodes", example = "ORD-1001")
        private String orderId;
        @Schema(description = "Number of label strips printed", example = "3")
        private int strips;
        @Schema(description = "Printer the job was sent to", example = "10.0.0.42:9100")
        private String printer;
        @Schema(description = "Job status", example = "PRINTED")
        private String status;
    }
}
