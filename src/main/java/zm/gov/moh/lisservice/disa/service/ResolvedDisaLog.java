package zm.gov.moh.lisservice.disa.service;

/**
 * Wraps a {@link DisaLogPayload} with values resolved from this application's
 * own reference data, replacing fields that should NOT be trusted from the
 * external service:
 * <ul>
 *   <li>{@code labCode}   – resolved via hmisCode → facility → facility_laboratory_map → laboratory_test → laboratory.lab_code</li>
 *   <li>{@code loincCode} – resolved via investigation_test_name → lab.test.loinc_code</li>
 *   <li>{@code mflCode}   – resolved from ref.facility.mfl_code (the payload does not carry this)</li>
 * </ul>
 */
public record ResolvedDisaLog(DisaLogPayload message, String labCode, String loincCode, String mflCode) {
}
