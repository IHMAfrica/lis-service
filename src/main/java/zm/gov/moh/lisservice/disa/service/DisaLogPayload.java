package zm.gov.moh.lisservice.disa.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisaLogPayload {

    // -----------------------------------------------------------------------
    // Core / patient fields
    // -----------------------------------------------------------------------
    private String orderNumber;
    private Long facilityId;
    private String facilityName;
    private String hmisCode;
    private String clientId;
    private String patientFirstName;
    private String patientSurname;
    private String patientNUPN;
    private String patientPhone;
    private String patientStreetName;
    private String patientHousePlot;
    private String patientPOBOX;
    private String patientDistrict;
    private LocalDate patientDOB;
    private String patientAddressType;
    private int patientAge;
    private String physicianNRC;
    private String physicianFirstName;
    private String physicianLastName;
    private String physicianPhone;
    private Long investigationTestId;
    private String investigationTestName;
    /** Ignored – LOINC is resolved from this application's own DB. */
    private String lonic;
    private String investigationPriority;
    private String specimenName;
    private LocalDateTime investigationSampleCollectionDate;
    private String investigationComments;
    private int gender;
    private Boolean isOnART;
    private int isCovidTest;
    private Boolean isCompositeTest;
    private Boolean isAdmitted;
    private Boolean isPatientDeceased;

    // -----------------------------------------------------------------------
    // VL / ART shared fields
    // -----------------------------------------------------------------------
    private String artNumber;
    private int isBreastFeeding;
    private int isPregnant;
    private LocalDateTime artInitiationDate;
    /** Text description e.g. "Adult First Line" – mapped to code 1/2/3 by Oml021Builder. */
    private String treatmentLine;
    private int lastVisitInMonths;

    // -----------------------------------------------------------------------
    // VL-specific OBX fields
    // -----------------------------------------------------------------------
    /** ARTRP – Repeat (0/1 → N/Y, Context 142) */
    private int artRepeat;
    /** RPRS – Repeat Reason (free text) */
    private String repeatReason;
    /** VADHE – Patient ART Compliant (0/1 → N/Y, Context 142) */
    private int artCompliant;
    /** VLVL – Date of Last VL */
    private LocalDate dateLastVL;
    /** ARTRS – Reason for Viral Load (BASE/EAC/R/T, Context 74 – already a code) */
    private String reasonForVL;
    /** VDRUG – Drug Regimen Used (1–19, Context 76 – already a code) */
    private String drugRegimen;
    /** DRRS – Reason for HIV DR Test (A/B/C/D/I, Context 108 – already a code) */
    private String reasonForDrTest;
    /** EACDO – EAC Completed (0/1 → N/Y, Context 142) */
    private int eacCompleted;
    /** DRREG – Current Regimen (1–19, Context 76 – already a code) */
    private String currentRegimen;
    /** VLVLR – Last VL Result (numeric, free text) */
    private String lastVLResult;

    // -----------------------------------------------------------------------
    // EID-specific OBX fields
    // -----------------------------------------------------------------------
    /** EIDID – EID ID Number (free text) */
    private String eidId;
    /** AZT – AZT + 3TC + NVP (0/1 → N/Y, Context 142) */
    private int aztRegimen;
    /** OTHR – Other Regimen (0/1 → N/Y, Context 142) */
    private int otherRegimen;
    /** IPT – IPT (0/1 → N/Y, Context 142) */
    private int ipt;
    /** CTX – CTX (0/1 → N/Y, Context 142) */
    private int ctx;
    /** EIDEP – EID Entry Point (ART/NUT/OPD/OTH/PAED/TB/U5, Context 119 – already a code) */
    private String eidEntryPoint;
    /** EIDEO – Entry Point Other (free text) */
    private String entryPointOther;
    /** INFNB – Infant Never BreastFed (0/1 → N/Y, Context 142) */
    private int neverBreastFed;
    /** CDHIV – RapidHIV (c) (Context 116 – already a code) */
    private String rapidHivC;
    /** MOHIC – HIV (m) (Context 116 – already a code) */
    private String hivM;
    /** MPMTC – PMTCT (m) (0/1 → N/Y, Context 142) */
    private int pmtctM;
    /** CPMTC – PMTCT (c) (0/1 → N/Y, Context 142) */
    private int pmtctC;
    /** INFBR – Breastfed (Context 116 – already a code: I/N/NO/NR/P/R/U/Y) */
    private String eidBreastfed;
    /** INFN – Weeks since cessation (free text) */
    private String weeksBreastfeeding;
    /** PCRT – PCR Done (Context 116 – already a code) */
    private String pcrDone;
    /** PCRTT – PCR Date */
    private LocalDate pcrDate;
    /** EIDRS – Reason for PCR (CONF/EXP/OTH/UXP, Context 111 – already a code) */
    private String reasonForPcr;
    /** EIDRP – Repeat Sample (0/1 → N/Y, Context 142) */
    private int repeatSample;
    /** MADDR – Mothers Physical Address (free text) */
    private String mothersAddress;
    /** MCGNO – Mother/Caregiver Phone No (free text) */
    private String caregiverPhone;
    /** MSMNO – Mothers SM Card No (free text) */
    private String mothersSmCardNo;
    /** ARTNO (EID) – Mother's ART No (free text) */
    private String mothersArtNo;
    /** MCGFN – Mother/CareGiver First Name (free text) */
    private String caregiverFirstName;
    /** MCGSN – Mother/CareGiver Surname (free text) */
    private String caregiverSurname;
    /** MULBI – Multiple Birth (0/1 → N/Y, Context 142) */
    private int multipleBirth;
    /** BORDR – Birth Order (integer) */
    private int birthOrder;
    /** PCRTR – Previous Result (Context 116 – already a code) */
    private String previousResult;
    /** EIDTS – EID Test Schedule (12M/18M/24M/6M/6W/9M/B, Context 110 – already a code) */
    private String eidTestSchedule;
    /** VDATE (EID) – ART Start Date */
    private LocalDate artStartDate;
    /** EIDRO – Other (PCR) (free text) */
    private String eidOther;

    // -----------------------------------------------------------------------
    // Null-safe accessors used by Oml021Builder
    // -----------------------------------------------------------------------

    public String safeInvestigationComments() {
        return investigationComments != null ? investigationComments : "";
    }

    public boolean isOnArt() {
        return Boolean.TRUE.equals(isOnART);
    }
}
