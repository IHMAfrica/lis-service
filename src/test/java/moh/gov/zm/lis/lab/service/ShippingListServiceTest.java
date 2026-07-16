package moh.gov.zm.lis.lab.service;

import moh.gov.zm.lis.lab.entity.Laboratory;
import moh.gov.zm.lis.lab.entity.ShippingOrder;
import moh.gov.zm.lis.lab.repository.ShippingOrderRepository;
import moh.gov.zm.lis.ref.entity.Facility;
import moh.gov.zm.lis.ref.service.FacilityService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShippingListServiceTest {

    private static final String MFL = "504010";
    private static final String LAB = "LUS01";

    @Mock private ShippingOrderRepository shippingOrderRepository;
    @Mock private FacilityService facilityService;
    @Mock private LaboratoryService laboratoryService;

    private ShippingListService service() {
        return new ShippingListService(shippingOrderRepository, facilityService, laboratoryService);
    }

    private ShippingOrder order(String id, String name, int age, String sex) {
        return ShippingOrder.builder()
                .orderId(id).fullName(name).age(age).sex(sex).testType("HIVVL")
                .requestedDate("2026-07-10").specimenCollectedDate("2026-07-10")
                .createdAt(OffsetDateTime.now()).build();
    }

    private List<String> allStrings(Sheet sheet) {
        List<String> out = new ArrayList<>();
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    out.add(cell.getStringCellValue());
                }
            }
        }
        return out;
    }

    private Workbook render(List<ShippingOrder> orders) {
        when(facilityService.cachedAll()).thenReturn(Mono.just(List.of(
                Facility.builder().mflCode(MFL).name("Chinsali District Hospital").build())));
        when(laboratoryService.cachedAll()).thenReturn(Mono.just(List.of(
                Laboratory.builder().labCode(LAB).labName("ZCD- Chinsali General Hospital").build())));
        when(shippingOrderRepository.findByMflCodeAndLabCodeAndCreatedAtBetweenOrderByCreatedAtAsc(
                eq(MFL), eq(LAB), any(), any())).thenReturn(Flux.fromIterable(orders));
        byte[] bytes = service().generate(MFL, LAB, null, null).block();
        assertThat(bytes).isNotNull();
        try {
            return new XSSFWorkbook(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void producesValidWorkbookWithHeaderFacilityLabAndRows() throws Exception {
        try (Workbook wb = render(List.of(order("ORD-1", "James Phiri", 23, "M"),
                order("ORD-2", "Jane Mwale", 32, "F")))) {
            Sheet sheet = wb.getSheetAt(0);
            List<String> strings = allStrings(sheet);

            // titles + scoping
            assertThat(strings).contains("Republic of Zambia", "Ministry of Health", "Shipping List",
                    "From", "Chinsali District Hospital", "Refer To", "ZCD- Chinsali General Hospital");
            // table headers
            assertThat(strings).contains("S/N", "Lab No", "Full names", "Age", "Sex",
                    "Test Type", "Requested date", "Specimen collected date");
            // data
            assertThat(strings).contains("ORD-1", "James Phiri", "HIVVL", "ORD-2", "Jane Mwale");
            // sign-off footer
            assertThat(strings).contains("Sender (Facility)", "Courier", "Receiver (Hub Lab)", "No. of samples");
        }
    }

    @Test
    void embedsCoatOfArmsImage() throws Exception {
        try (Workbook wb = render(List.of(order("ORD-1", "James Phiri", 23, "M")))) {
            assertThat(wb.getAllPictures()).as("coat of arms embedded").isNotEmpty();
        }
    }

    @Test
    void rendersHeaderEvenWithNoOrders() throws Exception {
        try (Workbook wb = render(List.of())) {
            List<String> strings = allStrings(wb.getSheetAt(0));
            assertThat(strings).contains("Shipping List", "S/N", "Lab No");
            // sample count on the sender row is zero
            assertThat(strings).contains("Sender (Facility)");
        }
    }
}
