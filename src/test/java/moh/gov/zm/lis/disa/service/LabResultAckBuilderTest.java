package moh.gov.zm.lis.disa.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LabResultAckBuilderTest {

    private final LabResultAckBuilder builder = new LabResultAckBuilder();

    private LabResultMessage message() {
        return LabResultMessage.builder()
                .messageControlId("ORU-CTRL-001")
                .sendingFacilityName("Kanyama Level 1")
                .build();
    }

    private List<String> segments(String hl7) {
        return Arrays.stream(hl7.split("\r")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private String segment(String hl7, String name) {
        return segments(hl7).stream().filter(s -> s.startsWith(name + "|")).findFirst().orElseThrow();
    }

    @Test
    void buildAcceptProducesAaSuccessAck() {
        String hl7 = builder.buildAccept(message());

        String msh = segment(hl7, "MSH");
        String msa = segment(hl7, "MSA");

        assertThat(msh).contains("ACK^R01^ACK");   // MSH-9
        assertThat(msh).contains("SmartCare+");     // MSH-3 sending application
        assertThat(msh).contains("DISA*LAB");       // MSH-5 receiving application
        assertThat(msh).contains("Kanyama Level 1");// MSH-6 receiving facility (echoes the sender)
        assertThat(msa).startsWith("MSA|AA|ORU-CTRL-001|Success");
    }

    @Test
    void buildErrorAckCarriesAeCodeAndText() {
        String hl7 = builder.build(message(), "AE", "Error processing lab result");

        assertThat(segment(hl7, "MSA")).startsWith("MSA|AE|ORU-CTRL-001|Error processing lab result");
    }

    @Test
    void msa2EchoesTheAcknowledgedControlId() {
        LabResultMessage m = LabResultMessage.builder().messageControlId("XYZ-999").build();

        // MSA-2 must reference the ORU's MSH-10 per the HL7 ACK convention.
        assertThat(segment(builder.buildAccept(m), "MSA")).contains("|XYZ-999|");
    }

    @Test
    void freshMessageControlIdGeneratedForTheAckItself() {
        String hl7 = builder.buildAccept(message());
        String msh = segment(hl7, "MSH");
        // MSH-10 of the ACK is a fresh id, distinct from the acknowledged message's id.
        assertThat(msh).doesNotContain("ORU-CTRL-001");
    }
}
