package moh.gov.zm.lis.disa.service;

import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.parser.PipeParser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Builds the HL7 v2.5 acknowledgement (ACK^R01) SmartCare returns to DISA*LAB for a
 * received lab result, following the normal HL7 ACK convention: MSA-2 echoes the
 * acknowledged message's control id (the ORU MSH-10) and MSA-1 is the accept code.
 */
@Component
public class LabResultAckBuilder {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String SENDING_APPLICATION = "SmartCare+";
    private static final String RECEIVING_APPLICATION = "DISA*LAB";

    /** Application-accept ACK for a successfully received result. */
    public String buildAccept(LabResultMessage result) {
        return build(result, "AA", "Success");
    }

    public String build(LabResultMessage result, String ackCode, String textMessage) {
        try {
            ACK ack = new ACK();

            MSH msh = ack.getMSH();
            msh.getFieldSeparator().setValue("|");
            msh.getEncodingCharacters().setValue("^~\\&");
            msh.getSendingApplication().getNamespaceID().setValue(SENDING_APPLICATION);
            msh.getSendingFacility().getUniversalIDType().setValue("URI"); // MSH-4 = ^^URI
            msh.getReceivingApplication().getNamespaceID().setValue(RECEIVING_APPLICATION);
            if (result.getSendingFacilityName() != null) {
                msh.getReceivingFacility().getNamespaceID().setValue(result.getSendingFacilityName());
            }
            msh.getDateTimeOfMessage().getTime().setValue(now());
            msh.getMessageType().getMessageCode().setValue("ACK");
            msh.getMessageType().getTriggerEvent().setValue("R01");
            msh.getMessageType().getMessageStructure().setValue("ACK");
            msh.getMessageControlID().setValue(UUID.randomUUID().toString());
            msh.getProcessingID().getProcessingID().setValue("P");
            msh.getProcessingID().getProcessingMode().setValue("T");
            msh.getVersionID().getVersionID().setValue("2.5");
            msh.getSequenceNumber().setValue("1");
            msh.getApplicationAcknowledgmentType().setValue("NE");
            msh.getCountryCode().setValue("ZMB");

            MSA msa = ack.getMSA();
            msa.getAcknowledgmentCode().setValue(ackCode);
            msa.getMessageControlID().setValue(result.getMessageControlId());
            msa.getTextMessage().setValue(textMessage);

            return new PipeParser().encode(ack);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build HL7 ACK^R01 for result '"
                    + result.getMessageControlId() + "': " + e.getMessage(), e);
        }
    }

    private static String now() {
        return LocalDateTime.now(ZoneOffset.UTC).format(TS);
    }
}
