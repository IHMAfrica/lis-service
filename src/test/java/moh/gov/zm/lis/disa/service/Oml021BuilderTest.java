package moh.gov.zm.lis.disa.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Oml021BuilderTest {

    private static final String LOINC_VL = "20447-9";
    private static final String LOINC_EID = "44871-2";

    private final Oml021Builder builder = new Oml021Builder();

    private DisaLogPayload payload(String testName) {
        DisaLogPayload p = new DisaLogPayload();
        p.setOrderNumber("ORD-1001");
        p.setFacilityName("UTH");
        p.setHmisCode("H12345");
        p.setInvestigationTestName(testName);
        p.setInvestigationPriority("R");
        p.setSpecimenName("Plasma");
        p.setInvestigationSampleCollectionDate(LocalDateTime.of(2026, 7, 9, 8, 30));
        p.setGender(2);
        p.setPatientAge(32);
        return p;
    }

    private List<String> segments(String hl7) {
        return Arrays.stream(hl7.split("\r")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    @Test
    void mshCarriesControlId_sendingMflAndReceivingLab() throws Exception {
        ResolvedDisaLog resolved = new ResolvedDisaLog(payload("Full Blood Count"), "LUS01", "58410-2", "504010");

        String hl7 = builder.encode(builder.build(resolved, "test-msg-control-id"));
        String msh = segments(hl7).getFirst();

        assertThat(msh).startsWith("MSH|^~\\&|CarePro|");
        assertThat(msh).contains("UTH^504010^URI");     // MSH-4 sending facility: name ^ mfl
        assertThat(msh).contains("DISA*LAB|LUS01|");     // MSH-5 receiving app ^ MSH-6 lab code
        assertThat(msh).contains("|test-msg-control-id|"); // MSH-10
        assertThat(msh).contains("OML^O21^OML_O21");
    }

    @Test
    void nonMappedLoinc_producesSingleOrderWithOneOrcAndObr() throws Exception {
        ResolvedDisaLog resolved = new ResolvedDisaLog(payload("Full Blood Count"), "LUS01", "58410-2", "504010");

        List<String> segments = segments(builder.encode(builder.build(resolved, "mc-1")));
        List<String> orcs = segments.stream().filter(s -> s.startsWith("ORC")).toList();
        List<String> obrs = segments.stream().filter(s -> s.startsWith("OBR")).toList();

        assertThat(orcs).hasSize(1);
        assertThat(obrs).hasSize(1);
        assertThat(orcs.getFirst()).startsWith("ORC|NW|ORD-1001");
        assertThat(obrs.getFirst()).contains("58410-2");
        assertThat(segments.stream().anyMatch(s -> s.startsWith("SPM"))).isTrue();
    }

    @Test
    void vlPanelPopulatesOrcForEveryOrderGroup() throws Exception {
        ResolvedDisaLog resolved = new ResolvedDisaLog(payload("Viral Load"), "LUS01", LOINC_VL, "504010");

        List<String> segments = segments(builder.encode(builder.build(resolved, "mc-2")));
        List<String> orcs = segments.stream().filter(s -> s.startsWith("ORC")).toList();
        List<String> obrs = segments.stream().filter(s -> s.startsWith("OBR")).toList();

        assertThat(orcs).hasSize(2);
        assertThat(obrs).hasSize(2);
        assertThat(orcs).allSatisfy(orc -> assertThat(orc).startsWith("ORC|NW|ORD-1001"));
        assertThat(obrs.get(0)).contains("47245-6"); // VL panel code
        assertThat(obrs.get(1)).contains(LOINC_VL);   // resolved LOINC
        // VL panel emits OBX question segments
        assertThat(segments.stream().anyMatch(s -> s.startsWith("OBX"))).isTrue();
    }

    @Test
    void eidPanelPopulatesOrcForEveryOrderGroup() throws Exception {
        ResolvedDisaLog resolved = new ResolvedDisaLog(payload("Early Infant Diagnosis"), "LUS01", LOINC_EID, "504010");

        List<String> segments = segments(builder.encode(builder.build(resolved, "mc-3")));
        List<String> orcs = segments.stream().filter(s -> s.startsWith("ORC")).toList();
        List<String> obrs = segments.stream().filter(s -> s.startsWith("OBR")).toList();

        assertThat(orcs).hasSize(2);
        assertThat(obrs).hasSize(2);
        assertThat(orcs).allSatisfy(orc -> assertThat(orc).startsWith("ORC|NW|ORD-1001"));
        assertThat(obrs.get(0)).contains("55277-8"); // EID panel code
        assertThat(obrs.get(1)).contains(LOINC_EID);
    }

    @Test
    void fallsBackToHmisCodeWhenMflBlank() throws Exception {
        ResolvedDisaLog resolved = new ResolvedDisaLog(payload("Full Blood Count"), "LUS01", "58410-2", "");

        String msh = segments(builder.encode(builder.build(resolved, "mc-4"))).getFirst();

        assertThat(msh).contains("UTH^H12345^URI"); // universal id falls back to hmisCode
    }
}
