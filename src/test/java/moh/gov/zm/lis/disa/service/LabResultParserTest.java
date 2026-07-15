package moh.gov.zm.lis.disa.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LabResultParserTest {

    private final LabResultParser parser = new LabResultParser();

    // MSH: MSH-3=sending app, MSH-4=name^labCode^URI, MSH-6=ordering mfl, MSH-10=control id
    private static final String MSH =
            "MSH|^~\\&|DISA*LAB|Kanyama Lab^LUS01^URI|SmartCare+|504010|20260703130000||ORU^R01|MC-123|P|2.5";
    private static final String PID = "PID|1||PATIENT-001^^^^MR||Doe^John^M||19900101|M";
    private static final String PV1 = "PV1|1||^^^H12345";

    /** Build a segment with fields at the given 1-based indices; gaps are empty. */
    private static String seg(String name, int size, Map<Integer, String> fields) {
        String[] a = new String[size + 1];
        a[0] = name;
        for (int i = 1; i <= size; i++) {
            a[i] = fields.getOrDefault(i, "");
        }
        return String.join("|", a);
    }

    private static Map<Integer, String> f(Object... pairs) {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            m.put((Integer) pairs[i], (String) pairs[i + 1]);
        }
        return m;
    }

    private static String join(String... segments) {
        return String.join("\r", segments);
    }

    @Test
    void parsesHeaderPatientAndOrderIdentifiers() {
        String orc = seg("ORC", 5, f(1, "RE", 2, "ORD-1001", 3, "FIL-9"));
        String obr = seg("OBR", 25, f(2, "ORD-1001", 3, "FIL-9",
                4, "20447-9^HIV Viral Load^LN", 7, "20260701080000", 14, "20260702000000", 25, "F"));
        String obx = seg("OBX", 14, f(1, "1", 2, "NM", 3, "20447-9^HIV Viral Load^LN",
                5, "1200", 6, "copies/mL", 7, "<40", 8, "H", 11, "F", 14, "20260703120000"));

        LabResultMessage m = parser.parse(join(MSH, PID, PV1, orc, obr, obx));

        assertThat(m.getMessageControlId()).isEqualTo("MC-123");
        assertThat(m.getLabCode()).isEqualTo("LUS01");                 // MSH-4 comp 2
        assertThat(m.getSendingFacilityName()).isEqualTo("Kanyama Lab"); // MSH-4 comp 1
        assertThat(m.getOrderingMflCode()).isEqualTo("504010");        // MSH-6 comp 1
        assertThat(m.getOrderingHmisCode()).isEqualTo("H12345");       // PV1-3 comp 4
        assertThat(m.getPatientIdentifier()).isEqualTo("PATIENT-001");
        assertThat(m.getPatientSex()).isEqualTo("M");
        assertThat(m.getPlacerOrderNumber()).isEqualTo("ORD-1001");
        assertThat(m.getFillerOrderNumber()).isEqualTo("FIL-9");
        assertThat(m.getTestLoinc()).isEqualTo("20447-9");
        assertThat(m.getResultStatus()).isEqualTo("F");
        assertThat(m.getSpecimenCollectedAt()).isNotNull();
        assertThat(m.getSpecimenReceivedAt()).isNotNull();
    }

    @Test
    void numericObservationParsedAsResult() {
        String obr = seg("OBR", 25, f(4, "20447-9^Viral Load^LN", 25, "F"));
        String obx = seg("OBX", 14, f(1, "1", 2, "NM", 3, "20447-9^Viral Load^LN", 5, "1200", 6, "copies/mL", 11, "F"));

        LabResultMessage m = parser.parse(join(MSH, obr, obx));

        assertThat(m.messageKind()).isEqualTo(LabResultMessage.KIND_RESULT);
        assertThat(m.hasMeaningfulObservations()).isTrue();
        assertThat(m.getObservations()).hasSize(1);
        assertThat(m.getObservations().getFirst().getNumericValue()).isEqualByComparingTo(new BigDecimal("1200"));
        assertThat(m.getObservations().getFirst().getUnits()).isEqualTo("copies/mL");
    }

    @Test
    void nonNumericNumericValueKeepsTextButNoNumber() {
        // "< 40" cannot be parsed to a number: numericValue is null, textual value retained.
        String obr = seg("OBR", 25, f(4, "20447-9^Viral Load^LN", 25, "F"));
        String obx = seg("OBX", 14, f(1, "1", 2, "NM", 3, "20447-9^Viral Load^LN", 5, "< 40", 11, "F"));

        LabResultMessage m = parser.parse(join(MSH, obr, obx));

        LabResultMessage.Observation o = m.getObservations().getFirst();
        assertThat(o.getValue()).isEqualTo("< 40");
        assertThat(o.getNumericValue()).isNull();
        assertThat(m.hasMeaningfulObservations()).isTrue();
    }

    @Test
    void orcOnlyStatusUpdateHasNoMeaningfulObservations() {
        String orc = seg("ORC", 5, f(1, "SC", 2, "ORD-2002", 5, "IP"));
        String obr = seg("OBR", 25, f(2, "ORD-2002", 25, "I"));

        LabResultMessage m = parser.parse(join(MSH, orc, obr));

        assertThat(m.messageKind()).isEqualTo(LabResultMessage.KIND_STATUS_UPDATE);
        assertThat(m.hasMeaningfulObservations()).isFalse();
    }

    @Test
    void noLoincIdentifierNormalisedToNull() {
        String obr = seg("OBR", 25, f(4, "NO LOINC^Unmapped^LN", 25, "F"));
        String obx = seg("OBX", 14, f(1, "1", 2, "ST", 3, "NO LOINC^Unmapped^LN", 5, "Positive", 11, "F"));

        LabResultMessage m = parser.parse(join(MSH, obr, obx));

        assertThat(m.getTestLoinc()).isNull();
        assertThat(m.getObservations().getFirst().getLoinc()).isNull();
        assertThat(m.getObservations().getFirst().getValue()).isEqualTo("Positive");
    }

    @Test
    void whollyEmptyObxIsSkipped() {
        String obr = seg("OBR", 25, f(4, "20447-9^Viral Load^LN", 25, "F"));
        String emptyObx = seg("OBX", 14, f(1, "1", 2, "NM"));
        String realObx = seg("OBX", 14, f(1, "2", 2, "NM", 3, "20447-9^Viral Load^LN", 5, "50", 11, "F"));

        LabResultMessage m = parser.parse(join(MSH, obr, emptyObx, realObx));

        assertThat(m.getObservations()).hasSize(1);
        assertThat(m.getObservations().getFirst().getSetId()).isEqualTo(2);
    }

    @Test
    void missingMshControlIdIsNull() {
        String noId = "MSH|^~\\&|DISA*LAB|Lab^L1^URI|SmartCare+|504010|20260703130000||ORU^R01||P|2.5";
        String obr = seg("OBR", 25, f(4, "20447-9^VL^LN", 25, "F"));

        LabResultMessage m = parser.parse(join(noId, obr));

        assertThat(m.getMessageControlId()).isNull();
    }
}
