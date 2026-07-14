package moh.gov.zm.lis.disa.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses an HL7 v2.5 ORU^R01 lab result into a {@link LabResultMessage}.
 * "NO LOINC" identifiers are normalised to null; specimen collection time is taken
 * from OBR-7 and falls back to the {@code SPECDT} token in the free-text NTE.
 */
@Component
public class LabResultParser {
    private static final String NO_LOINC = "NO LOINC";
    // NTE free text, e.g. "SPECDT:2026061708:00 \F\ RCVDT:2026070200:00 \F\ Priority:R"
    private static final Pattern SPECDT = Pattern.compile("SPECDT:(\\d{8})(\\d{2}):(\\d{2})");
    private static final Pattern RCVDT = Pattern.compile("RCVDT:(\\d{8})(\\d{2}):(\\d{2})");
    private static final Pattern PRIORITY = Pattern.compile("Priority:([A-Za-z])");

    public LabResultMessage parse(String hl7) {
        Hl7v2 msg = Hl7v2.parse(hl7);

        String placer = firstNonBlank(msg.field("ORC", 2), msg.field("OBR", 2));
        String filler = firstNonBlank(msg.field("ORC", 3), msg.field("OBR", 3));
        String usi = msg.field("OBR", 4);

        String nte = notes(msg);
        OffsetDateTime specimenCollected = firstTs(msg.field("OBR", 7), match(SPECDT, nte));
        OffsetDateTime specimenReceived = firstTs(msg.field("OBR", 14), match(RCVDT, nte));

        return LabResultMessage.builder()
                .messageControlId(emptyToNull(msg.field("MSH", 10)))
                .messageDateTime(parseTs(msg.field("MSH", 7)))
                .labCode(emptyToNull(Hl7v2.component(msg.field("MSH", 4), 2)))
                .sendingFacilityName(emptyToNull(Hl7v2.component(msg.field("MSH", 4), 1)))
                .orderingMflCode(emptyToNull(Hl7v2.component(msg.field("MSH", 6), 1)))
                .orderingHmisCode(emptyToNull(Hl7v2.component(msg.field("PV1", 3), 4)))
                .patientIdentifier(emptyToNull(Hl7v2.component(msg.field("PID", 3), 1)))
                .patientName(patientName(msg.field("PID", 5)))
                .patientDob(parseDate(msg.field("PID", 7)))
                .patientSex(emptyToNull(msg.field("PID", 8)))
                .placerOrderNumber(emptyToNull(placer))
                .fillerOrderNumber(emptyToNull(filler))
                .orderControl(emptyToNull(msg.field("ORC", 1)))
                .orderStatusCode(emptyToNull(msg.field("ORC", 5)))
                .testLoinc(loinc(Hl7v2.component(usi, 1)))
                .testName(emptyToNull(Hl7v2.component(usi, 2)))
                .resultStatus(emptyToNull(msg.field("OBR", 25)))
                .specimenCollectedAt(specimenCollected)
                .specimenReceivedAt(specimenReceived)
                .priority(match(PRIORITY, nte))
                .observations(observations(msg))
                .build();
    }

    private List<LabResultMessage.Observation> observations(Hl7v2 msg) {
        List<LabResultMessage.Observation> list = new ArrayList<>();
        for (String[] obx : msg.all("OBX")) {
            String identifier = Hl7v2.field(obx, 3);
            String value = Hl7v2.unescape(Hl7v2.field(obx, 5));
            if (isBlank(identifier) && isBlank(value)) {
                continue; // wholly empty OBX
            }
            String valueType = Hl7v2.field(obx, 2);
            list.add(LabResultMessage.Observation.builder()
                    .setId(parseInt(Hl7v2.field(obx, 1)))
                    .valueType(emptyToNull(valueType))
                    .loinc(loinc(Hl7v2.component(identifier, 1)))
                    .text(emptyToNull(Hl7v2.component(identifier, 2)))
                    .localCode(emptyToNull(Hl7v2.component(identifier, 4)))
                    .value(emptyToNull(value))
                    .numericValue(numeric(valueType, value))
                    .units(emptyToNull(Hl7v2.component(Hl7v2.field(obx, 6), 1)))
                    .referenceRange(emptyToNull(Hl7v2.field(obx, 7)))
                    .abnormalFlags(emptyToNull(Hl7v2.field(obx, 8)))
                    .status(emptyToNull(Hl7v2.field(obx, 11)))
                    .observedAt(parseTs(Hl7v2.field(obx, 14)))
                    .build());
        }
        return list;
    }

    private String notes(Hl7v2 msg) {
        StringBuilder sb = new StringBuilder();
        for (String[] nte : msg.all("NTE")) {
            sb.append(Hl7v2.unescape(Hl7v2.field(nte, 3))).append(' ');
        }
        return sb.toString();
    }

    private static BigDecimal numeric(String valueType, String value) {
        if (!"NM".equalsIgnoreCase(valueType) || isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null; // e.g. "< 40" — keep the text value only
        }
    }

    private static String loinc(String code) {
        return isBlank(code) || NO_LOINC.equalsIgnoreCase(code) ? null : code;
    }

    private static String patientName(String xpn) {
        if (isBlank(xpn)) {
            return null;
        }
        String surname = Hl7v2.component(xpn, 1);
        String given = Hl7v2.component(xpn, 2);
        String middle = Hl7v2.component(xpn, 3);
        String name = (given + " " + middle + " " + surname).replaceAll("\\s+", " ").trim();
        return name.isEmpty() ? null : name;
    }

    // ---- date/time helpers -----------------------------------------------------

    private static OffsetDateTime firstTs(String primary, String fallback) {
        OffsetDateTime ts = parseTs(primary);
        return ts != null ? ts : parseTs(fallback);
    }

    /** HL7 TS: CCYYMMDD[HHMM[SS]], optional timezone suffix. Interpreted as UTC. */
    private static OffsetDateTime parseTs(String value) {
        String d = digits(value);
        if (d == null || d.length() < 8) {
            return null;
        }
        int year = Integer.parseInt(d.substring(0, 4));
        int month = Integer.parseInt(d.substring(4, 6));
        int day = Integer.parseInt(d.substring(6, 8));
        int hour = d.length() >= 10 ? Integer.parseInt(d.substring(8, 10)) : 0;
        int minute = d.length() >= 12 ? Integer.parseInt(d.substring(10, 12)) : 0;
        int second = d.length() >= 14 ? Integer.parseInt(d.substring(12, 14)) : 0;
        try {
            return LocalDateTime.of(year, month, day, hour, minute, second).atOffset(ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseDate(String value) {
        String d = digits(value);
        if (d == null || d.length() < 8) {
            return null;
        }
        try {
            return LocalDate.of(Integer.parseInt(d.substring(0, 4)),
                    Integer.parseInt(d.substring(4, 6)), Integer.parseInt(d.substring(6, 8)));
        } catch (Exception e) {
            return null;
        }
    }

    private static String digits(String value) {
        if (isBlank(value)) {
            return null;
        }
        String core = value.trim().split("[+-]", 2)[0].replace(":", "");
        return core.chars().allMatch(Character::isDigit) ? core : core.replaceAll("\\D", "");
    }

    private static String match(Pattern p, String text) {
        Matcher m = p.matcher(text);
        if (!m.find()) {
            return null;
        }
        return m.groupCount() == 1 ? m.group(1) : m.group(1) + m.group(2) + m.group(3);
    }

    private static Integer parseInt(String value) {
        try {
            return isBlank(value) ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String firstNonBlank(String a, String b) {
        return !isBlank(a) ? a : b;
    }

    private static String emptyToNull(String s) {
        return isBlank(s) ? null : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
