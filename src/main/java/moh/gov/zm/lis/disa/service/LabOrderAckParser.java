package moh.gov.zm.lis.disa.service;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Parses an HL7 v2.5 lab-order acknowledgement into a {@link LabOrderAck}.
 * Uses HAPI's {@link Terser} (version-agnostic field access) with validation
 * disabled, so real-world ACKs with minor deviations still parse.
 */
@Component
public class LabOrderAckParser {
    private final PipeParser parser;

    public LabOrderAckParser() {
        HapiContext context = new DefaultHapiContext();
        context.setValidationContext(ValidationContextFactory.noValidation());
        this.parser = context.getPipeParser();
    }

    public LabOrderAck parse(String hl7) throws Exception {
        Message message = parser.parse(hl7);
        Terser t = new Terser(message);

        String messageDateTime = t.get("/MSH-7");

        return new LabOrderAck(
                trimToNull(t.get("/MSH-10")),
                datePart(messageDateTime),
                timePart(messageDateTime),
                trimToNull(t.get("/MSH-4-1")),
                trimToNull(t.get("/MSH-6-1")),
                trimToNull(t.get("/MSA-1")),
                trimToNull(t.get("/MSA-2")),
                trimToNull(t.get("/MSA-3")));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** HL7 TS is {@code CCYYMMDD[HHMM[SS]]}, optionally with a timezone suffix. */
    private static String digits(String ts) {
        if (ts == null || ts.isBlank()) {
            return null;
        }
        String core = ts.trim().split("[+-]", 2)[0];
        return core.chars().allMatch(Character::isDigit) ? core : core.replaceAll("\\D", "");
    }

    private static LocalDate datePart(String ts) {
        String d = digits(ts);
        if (d == null || d.length() < 8) {
            return null;
        }
        return LocalDate.of(
                Integer.parseInt(d.substring(0, 4)),
                Integer.parseInt(d.substring(4, 6)),
                Integer.parseInt(d.substring(6, 8)));
    }

    private static LocalTime timePart(String ts) {
        String d = digits(ts);
        if (d == null || d.length() < 10) {
            return null;
        }
        int hour = Integer.parseInt(d.substring(8, 10));
        int minute = d.length() >= 12 ? Integer.parseInt(d.substring(10, 12)) : 0;
        int second = d.length() >= 14 ? Integer.parseInt(d.substring(12, 14)) : 0;
        return LocalTime.of(hour, minute, second);
    }
}
