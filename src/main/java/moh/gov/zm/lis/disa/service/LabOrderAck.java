package moh.gov.zm.lis.disa.service;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Fields extracted from an HL7 lab-order acknowledgement (ACK).
 *
 * @param messageControlId       MSH-10 of the ACK
 * @param ackDate                date component of MSH-7
 * @param ackTime                time component of MSH-7
 * @param sendingFacilityLabCode MSH-4.1 — the acknowledging laboratory
 * @param receivingFacilityMflCode MSH-6.1 — the ordering facility (MFL code)
 * @param ackCode                MSA-1 (AA / AE / AR)
 * @param refMessageControlId    MSA-2 — the original order's MSH-10 (order_status.message_control_id)
 * @param textMessage            MSA-3
 */
public record LabOrderAck(
        String messageControlId,
        LocalDate ackDate,
        LocalTime ackTime,
        String sendingFacilityLabCode,
        String receivingFacilityMflCode,
        String ackCode,
        String refMessageControlId,
        String textMessage) {
}
