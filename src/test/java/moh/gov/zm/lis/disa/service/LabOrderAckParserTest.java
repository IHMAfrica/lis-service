package moh.gov.zm.lis.disa.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class LabOrderAckParserTest {

    private final LabOrderAckParser parser = new LabOrderAckParser();

    private static String ack(String msh7, String msa) {
        return "MSH|^~\\&|DISA*LAB|ZSJ|SmartCare|3129|" + msh7
                + "||ACK^O21^ACK|6SRPW477-T5POH49ZKWN|P^T|2.5||||NE|ZMB\r" + msa;
    }

    @Test
    void parsesAllFieldsFromSampleAck() throws Exception {
        String hl7 = ack("20260709130902", "MSA|AA|1dfab517-6a27-446c-a180-b7bd00bcd6b0|No Ward specified (PV1.3-1); |");

        LabOrderAck result = parser.parse(hl7);

        assertThat(result.messageControlId()).isEqualTo("6SRPW477-T5POH49ZKWN");
        assertThat(result.sendingFacilityLabCode()).isEqualTo("ZSJ");
        assertThat(result.receivingFacilityMflCode()).isEqualTo("3129");
        assertThat(result.ackCode()).isEqualTo("AA");
        assertThat(result.refMessageControlId()).isEqualTo("1dfab517-6a27-446c-a180-b7bd00bcd6b0");
        assertThat(result.textMessage()).isEqualTo("No Ward specified (PV1.3-1);");
        assertThat(result.ackDate()).isEqualTo(LocalDate.of(2026, 7, 9));
        assertThat(result.ackTime()).isEqualTo(LocalTime.of(13, 9, 2));
    }

    @Test
    void parsesDateOnlyMsh7() throws Exception {
        LabOrderAck result = parser.parse(ack("20260709", "MSA|AA|ref|ok|"));

        assertThat(result.ackDate()).isEqualTo(LocalDate.of(2026, 7, 9));
        assertThat(result.ackTime()).isNull();
    }

    @Test
    void parsesHourMinuteMsh7() throws Exception {
        LabOrderAck result = parser.parse(ack("202607091309", "MSA|AA|ref|ok|"));

        assertThat(result.ackTime()).isEqualTo(LocalTime.of(13, 9, 0));
    }

    @Test
    void handlesTimezoneSuffixInMsh7() throws Exception {
        LabOrderAck result = parser.parse(ack("20260709130902+0200", "MSA|AA|ref|ok|"));

        assertThat(result.ackDate()).isEqualTo(LocalDate.of(2026, 7, 9));
        assertThat(result.ackTime()).isEqualTo(LocalTime.of(13, 9, 2));
    }

    @Test
    void missingTextMessageIsNull() throws Exception {
        LabOrderAck result = parser.parse(ack("20260709130902", "MSA|AR|ref|"));

        assertThat(result.ackCode()).isEqualTo("AR");
        assertThat(result.textMessage()).isNull();
    }
}
