package zm.gov.moh.lisservice.disa.service;

import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.group.OML_O21_OBSERVATION_REQUEST;
import ca.uhn.hl7v2.model.v25.group.OML_O21_ORDER;
import ca.uhn.hl7v2.model.v25.message.OML_O21;
import ca.uhn.hl7v2.model.v25.segment.*;
import ca.uhn.hl7v2.parser.PipeParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Slf4j
public class Oml021Builder {

    private static final int FEMALE = 2;

    private static final String LOINC_VL  = "20447-9";
    private static final String LOINC_EID = "44871-2";

    private static final DateTimeFormatter TS_FMT   = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd");

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public OML_O21 build(ResolvedDisaLog resolved) throws Exception {
        DisaLogPayload p = resolved.message();
        String universalId = !resolved.mflCode().isBlank() ? resolved.mflCode() : p.getHmisCode();

        OML_O21 oml = new OML_O21();
        setMsh(oml.getMSH(), p, universalId, resolved.labCode());
        setSft(oml.getSFT(0));
        setPid(oml.getPATIENT().getPID(), p);
        setPv1(oml.getPATIENT().getPATIENT_VISIT().getPV1(), p, universalId);
        setOrders(oml, resolved);
        return oml;
    }

    public String encode(OML_O21 oml) throws Exception {
        return new PipeParser().encode(oml);
    }

    // -----------------------------------------------------------------------
    // MSH
    // -----------------------------------------------------------------------

    private void setMsh(MSH msh, DisaLogPayload p, String universalId, String labCode) throws Exception {
        msh.getFieldSeparator().setValue("|");
        msh.getEncodingCharacters().setValue("^~\\&");
        msh.getSendingApplication().getNamespaceID().setValue("CarePro");
        msh.getSendingFacility().getNamespaceID().setValue(p.getFacilityName());
        msh.getSendingFacility().getUniversalID().setValue(universalId);
        msh.getSendingFacility().getUniversalIDType().setValue("URI");
        msh.getReceivingApplication().getNamespaceID().setValue("DISA*LAB");
        msh.getReceivingFacility().getNamespaceID().setValue(labCode);
        msh.getDateTimeOfMessage().getTime().setValue(now());
        msh.getMessageType().getMessageCode().setValue("OML");
        msh.getMessageType().getTriggerEvent().setValue("O21");
        msh.getMessageType().getMessageStructure().setValue("OML_O21");
        msh.getMessageControlID().setValue(UUID.randomUUID().toString());
        msh.getSequenceNumber().setValue("1");
        msh.getVersionID().getVersionID().setValue("2.5");
        msh.getProcessingID().getProcessingID().setValue("P");
        msh.getProcessingID().getProcessingMode().setValue("T");
        msh.getApplicationAcknowledgmentType().setValue("AL");
        msh.getCountryCode().setValue("ZMB");
    }

    // -----------------------------------------------------------------------
    // SFT
    // -----------------------------------------------------------------------

    private void setSft(SFT sft) throws Exception {
        sft.getSoftwareVendorOrganization().getOrganizationName().setValue("moh.gov.zm");
        sft.getSoftwareCertifiedVersionOrReleaseNumber().setValue("v2.0");
        sft.getSoftwareProductName().setValue("CarePro");
        sft.getSoftwareBinaryID().setValue("CarePro:$v2.0");
    }

    // -----------------------------------------------------------------------
    // PID
    // -----------------------------------------------------------------------

    private void setPid(PID pid, DisaLogPayload p) throws Exception {
        String nupn = p.getPatientNUPN();
        pid.getSetIDPID().setValue("1");
        pid.getPatientID().getIDNumber().setValue(nupn);
        pid.getLastUpdateDateTime().getTime().setValue(now());

        var idList = pid.getPatientIdentifierList(0);
        idList.getIDNumber().setValue(nupn);
        idList.getIdentifierTypeCode().setValue("MR");
        idList.getAssigningAuthority().getNamespaceID().setValue("zm.gov.moh.sc");
        idList.getAssigningAuthority().getUniversalID().setValue("api_nupn");
        idList.getAssigningAuthority().getUniversalIDType().setValue("api_nupn");

        var name = pid.getPatientName(0);
        name.getFamilyName().getSurname().setValue(p.getPatientSurname());
        name.getGivenName().setValue(p.getPatientFirstName());

        pid.getDateTimeOfBirth().getTime().setValue(fmtDate(p.getPatientDOB()));
        pid.getAdministrativeSex().setValue(p.getGender() == 1 ? "M" : "F");

        var addr = pid.getPatientAddress(0);
        addr.getStreetAddress().getStreetName().setValue(p.getPatientStreetName());
        addr.getStreetAddress().getDwellingNumber().setValue(p.getPatientHousePlot());
        addr.getZipOrPostalCode().setValue(p.getPatientPOBOX());
        addr.getCity().setValue(p.getPatientDistrict());
        addr.getAddressType().setValue(p.getPatientAddressType());
        addr.getEffectiveDate().getTime().setValue(now());

        pid.getPhoneNumberHome(0).getTelephoneNumber().setValue(p.getPatientPhone());
    }

    // -----------------------------------------------------------------------
    // PV1
    // -----------------------------------------------------------------------

    private void setPv1(PV1 pv1, DisaLogPayload p, String universalId) throws Exception {
        pv1.getSetIDPV1().setValue("1");
        pv1.getPatientClass().setValue("O");
        pv1.getAssignedPatientLocation().getPointOfCare().setValue(universalId);
        pv1.getAssignedPatientLocation().getLocationDescription().setValue(p.getFacilityName());
        pv1.getAdmitDateTime().getTime().setValue(now());

        var doctor = pv1.getReferringDoctor(0);
        doctor.getIDNumber().setValue(p.getPhysicianNRC());
        doctor.getGivenName().setValue(p.getPhysicianFirstName());
        doctor.getFamilyName().getSurname().setValue(p.getPhysicianLastName());
    }

    // -----------------------------------------------------------------------
    // Orders
    // -----------------------------------------------------------------------

    private void setOrders(OML_O21 oml, ResolvedDisaLog resolved) throws Exception {
        DisaLogPayload p = resolved.message();
        String loincCode = resolved.loincCode();
        boolean isMappedLoinc = LOINC_VL.equals(loincCode) || LOINC_EID.equals(loincCode);

        int count = 0;
        OML_O21_ORDER order = oml.getORDER(count);

        // ORC
        ORC orc = order.getORC();
        orc.getOrderControl().setValue("NW");
        orc.getPlacerOrderNumber().getEntityIdentifier().setValue(p.getOrderNumber());
        orc.getDateTimeOfTransaction().getTime()
                .setValue(fmtDateTime(p.getInvestigationSampleCollectionDate()));

        OML_O21_OBSERVATION_REQUEST obsReq = order.getOBSERVATION_REQUEST();
        OBR obr = obsReq.getOBR();

        if (isMappedLoinc) {
            // First OBR: panel code (47245-6 for VL, 55277-8 for EID)
            obr.getSetIDOBR().setValue(String.valueOf(count + 1));
            obr.getPlacerOrderNumber().getEntityIdentifier().setValue(p.getOrderNumber());
            obr.getUniversalServiceIdentifier().getText().setValue(p.getInvestigationTestName());
            obr.getPriorityOBR().setValue(p.getInvestigationPriority());
            obr.getRequestedDateTime().getTime().setValue(now());
            obr.getObservationDateTime().getTime().setValue(now());

            if (LOINC_VL.equals(loincCode)) {
                obr.getUniversalServiceIdentifier().getIdentifier().setValue("47245-6");
                setVlObx(oml, order, p);
            } else {
                obr.getUniversalServiceIdentifier().getIdentifier().setValue("55277-8");
                setEidObx(oml, order, p);
            }
            obr.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue("LOINC");

            count++;
            order = oml.getORDER(count);
            obsReq = order.getOBSERVATION_REQUEST();
            obr = obsReq.getOBR();
        }

        // Final OBR: resolved LOINC code + NTE + SPM
        obr.getSetIDOBR().setValue(String.valueOf(count + 1));
        obr.getPlacerOrderNumber().getEntityIdentifier().setValue(p.getOrderNumber());
        obr.getUniversalServiceIdentifier().getIdentifier().setValue(loincCode);
        obr.getUniversalServiceIdentifier().getText().setValue(p.getInvestigationTestName());
        obr.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue("LOINC");
        obr.getPriorityOBR().setValue(p.getInvestigationPriority());
        obr.getRequestedDateTime().getTime().setValue(now());
        obr.getObservationDateTime().getTime().setValue(now());

        if (!p.safeInvestigationComments().isBlank()) {
            obsReq.getNTE(0).getComment(0).setValue(p.safeInvestigationComments());
        }

        setSpecimen(obsReq, p, loincCode);
    }

    // -----------------------------------------------------------------------
    // SPM
    // -----------------------------------------------------------------------

    private void setSpecimen(OML_O21_OBSERVATION_REQUEST obsReq,
                             DisaLogPayload p, String loincCode) throws Exception {
        SPM spm = obsReq.getSPECIMEN(0).getSPM();
        spm.getSetIDSPM().setValue("1");
        String collectionTs = fmtDateTime(p.getInvestigationSampleCollectionDate());

        if (LOINC_EID.equals(loincCode)) {
            spm.getSpecimenType().getIdentifier().setValue("DBS");
            spm.getSpecimenType().getText().setValue("Dry Blood Spot");
        } else if (LOINC_VL.equals(loincCode)) {
            spm.getSpecimenType().getIdentifier().setValue("B");
            spm.getSpecimenType().getText().setValue("Blood");
        } else {
            String code = mapSpecimenCode(p.getSpecimenName());
            spm.getSpecimenType().getIdentifier().setValue(code);
            spm.getSpecimenType().getText().setValue(p.getSpecimenName());
        }

        spm.getSpecimenCollectionDateTime().getRangeStartDateTime().getTime().setValue(collectionTs);
        spm.getSpecimenReceivedDateTime().getTime().setValue(collectionTs);
    }

    private String mapSpecimenCode(String name) {
        if (name == null) return "";
        return switch (name) {
            case "DBS"        -> "DBS";
            case "WholeBlood" -> "WB";
            case "Plasma"     -> "P";
            default           -> name;
        };
    }

    // -----------------------------------------------------------------------
    // VL OBX (LOINC panel 47245-6)
    //
    // Coded fields use the Code column from the context tables.
    // int 0/1 fields are mapped to N/Y (Context 142).
    // -----------------------------------------------------------------------

    private void setVlObx(OML_O21 oml, OML_O21_ORDER order, DisaLogPayload p) throws Exception {
        OML_O21_OBSERVATION_REQUEST obsReq = order.getOBSERVATION_REQUEST();
        int i = 0;

        // AGECT – Age Category (Context 109: A=Adult, P=Paediatric, X=Adolescent)
        i = addObx(oml, obsReq, i, "AGECT", "Age Category", ageCategory(p.getPatientAge()));

        // ARTNO – ART No (free text, only if on ART)
        if (p.isOnArt()) {
            i = addObxIfPresent(oml, obsReq, i, "ARTNO", "ART No", p.getArtNumber());
        }

        // ARTPR – Pregnant (Context 87: N/Y; only applicable to females)
        // ARTBR – Breastfeeding (Context 87: N/Y; only applicable to females)
        if (p.getGender() == FEMALE) {
            i = addObx(oml, obsReq, i, "ARTPR", "Pregnant",     yn(p.getIsPregnant()));
            i = addObx(oml, obsReq, i, "ARTBR", "Breastfeeding", yn(p.getIsBreastFeeding()));
        }

        // VDATE – ART Initiation date (ccyymmdd)
        if (p.isOnArt() && p.getArtInitiationDate() != null) {
            i = addObxIfPresent(oml, obsReq, i, "VDATE", "ART Initiation",
                    fmtDate(p.getArtInitiationDate().toLocalDate()));
        }

        // ARTRP – Repeat (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "ARTRP", "Repeat", yn(p.getArtRepeat()));

        // RPRS – Repeat Reason (free text)
        i = addObxIfPresent(oml, obsReq, i, "RPRS", "Repeat Reason", p.getRepeatReason());

        // VADHE – Patient ART Compliant (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "VADHE", "Patient ART Compliant", yn(p.getArtCompliant()));

        // VLINE – Treatment Line (Context 107: 1/2/3)
        i = addObxIfPresent(oml, obsReq, i, "VLINE", "Treatment Line",
                treatmentLineCode(p.getTreatmentLine()));

        // VLVL – Date of Last VL (ccyymmdd)
        i = addObxIfPresent(oml, obsReq, i, "VLVL", "Date of Last VL",
                fmtDate(p.getDateLastVL()));

        // ARTRS – Reason for Viral Load (Context 74: BASE/EAC/R/T – already a code)
        i = addObxIfPresent(oml, obsReq, i, "ARTRS", "Reason for Viral Load", p.getReasonForVL());

        // VDRUG – Drug Regimen Used (Context 76: 1–19 – already a code)
        i = addObxIfPresent(oml, obsReq, i, "VDRUG", "Drug Regimen Used", p.getDrugRegimen());

        // DRRS – Reason for HIV DR Test (Context 108: A/B/C/D/I – already a code)
        i = addObxIfPresent(oml, obsReq, i, "DRRS", "Reason for HIV DR Test", p.getReasonForDrTest());

        // EACDO – EAC Completed (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "EACDO", "EAC Completed", yn(p.getEacCompleted()));

        // DRREG – Current Regimen (Context 76: 1–19 – already a code)
        i = addObxIfPresent(oml, obsReq, i, "DRREG", "Current Regimen", p.getCurrentRegimen());

        // VLVLR – Last VL Result (numeric free text)
        i = addObxIfPresent(oml, obsReq, i, "VLVLR", "Last VL Result", p.getLastVLResult());
    }

    // -----------------------------------------------------------------------
    // EID OBX (LOINC panel 55277-8)
    //
    // Coded fields use the Code column from the context tables.
    // int 0/1 fields are mapped to N/Y (Context 142 unless noted).
    // -----------------------------------------------------------------------

    private void setEidObx(OML_O21 oml, OML_O21_ORDER order, DisaLogPayload p) throws Exception {
        OML_O21_OBSERVATION_REQUEST obsReq = order.getOBSERVATION_REQUEST();
        int i = 0;

        // EIDID – EID ID No (free text)
        i = addObxIfPresent(oml, obsReq, i, "EIDID", "EID IDNo", p.getEidId());

        // AZT – AZT + 3TC + NVP (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "AZT", "AZT + 3TC + NVP", yn(p.getAztRegimen()));

        // OTHR – Other Regimen (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "OTHR", "Other Regimen", yn(p.getOtherRegimen()));

        // IPT (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "IPT", "IPT", yn(p.getIpt()));

        // CTX (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "CTX", "CTX", yn(p.getCtx()));

        // EIDEP – EID Entry Point (Context 119: ART/NUT/OPD/OTH/PAED/TB/U5 – already a code)
        i = addObxIfPresent(oml, obsReq, i, "EIDEP", "EID Entry Point", p.getEidEntryPoint());

        // EIDEO – Entry Point Other (free text)
        i = addObxIfPresent(oml, obsReq, i, "EIDEO", "Entry Point Other", p.getEntryPointOther());

        // INFNB – Never BreastFed (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "INFNB", "Never BreastFed", yn(p.getNeverBreastFed()));

        // CDHIV – RapidHIV (c) (Context 116 – already a code)
        i = addObxIfPresent(oml, obsReq, i, "CDHIV", "RapidHIV (c)", p.getRapidHivC());

        // MOHIC – HIV (m) (Context 116 – already a code)
        i = addObxIfPresent(oml, obsReq, i, "MOHIC", "HIV (m)", p.getHivM());

        // MPMTC – PMTCT (m) (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "MPMTC", "PMTCT (m)", yn(p.getPmtctM()));

        // CPMTC – PMTCT (c) (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "CPMTC", "PMTCT (c)", yn(p.getPmtctC()));

        // INFBR – Breastfed (Context 116 – already a code from payload field eidBreastfed)
        i = addObxIfPresent(oml, obsReq, i, "INFBR", "Breastfed", p.getEidBreastfed());

        // INFN – Weeks since cessation (free text)
        i = addObxIfPresent(oml, obsReq, i, "INFN", "Weeks since cessation", p.getWeeksBreastfeeding());

        // PCRT – PCR Done (Context 116 – already a code)
        i = addObxIfPresent(oml, obsReq, i, "PCRT", "PCR Done", p.getPcrDone());

        // PCRTT – PCR Date (ccyymmdd)
        i = addObxIfPresent(oml, obsReq, i, "PCRTT", "PCR Date", fmtDate(p.getPcrDate()));

        // EIDRS – Reason for PCR (Context 111: CONF/EXP/OTH/UXP – already a code)
        i = addObxIfPresent(oml, obsReq, i, "EIDRS", "Reason for PCR", p.getReasonForPcr());

        // EIDRP – Repeat Sample (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "EIDRP", "Repeat Sample", yn(p.getRepeatSample()));

        // Caregiver / mother fields (all free text)
        i = addObxIfPresent(oml, obsReq, i, "MADDR", "Mothers Physical Address",  p.getMothersAddress());
        i = addObxIfPresent(oml, obsReq, i, "MCGNO", "Mother/Caregiver Phone No", p.getCaregiverPhone());
        i = addObxIfPresent(oml, obsReq, i, "MSMNO", "Mothers ID No",             p.getMothersSmCardNo());
        i = addObxIfPresent(oml, obsReq, i, "ARTNO", "Mother's ART No",           p.getMothersArtNo());
        i = addObxIfPresent(oml, obsReq, i, "MCGFN", "Mother/CareGiver First Name", p.getCaregiverFirstName());
        i = addObxIfPresent(oml, obsReq, i, "MCGSN", "Mother/CareGiver Surname",  p.getCaregiverSurname());

        // MULBI – Multiple Birth (Context 142: N/Y)
        i = addObx(oml, obsReq, i, "MULBI", "Multiple Birth", yn(p.getMultipleBirth()));

        // BORDR – Birth Order (integer, only when meaningful)
        if (p.getBirthOrder() > 0) {
            i = addObx(oml, obsReq, i, "BORDR", "Birth Order", String.valueOf(p.getBirthOrder()));
        }

        // PCRTR – Previous Result (Context 116 – already a code)
        i = addObxIfPresent(oml, obsReq, i, "PCRTR", "Previous Result", p.getPreviousResult());

        // EIDTS – EID Test Schedule (Context 110: 12M/18M/24M/6M/6W/9M/B – already a code)
        i = addObxIfPresent(oml, obsReq, i, "EIDTS", "EID Test Schedule", p.getEidTestSchedule());

        // VDATE – ART Start Date (ccyymmdd)
        i = addObxIfPresent(oml, obsReq, i, "VDATE", "ART Start Date", fmtDate(p.getArtStartDate()));

        // EIDRO – Other (PCR) (free text)
        i = addObxIfPresent(oml, obsReq, i, "EIDRO", "Other (PCR)", p.getEidOther());
    }

    // -----------------------------------------------------------------------
    // OBX helpers
    // -----------------------------------------------------------------------

    private int addObx(OML_O21 oml, OML_O21_OBSERVATION_REQUEST obsReq,
                       int index, String id, String text, String value) throws Exception {
        OBX obx = obsReq.getOBSERVATION(index).getOBX();
        obx.getSetIDOBX().setValue(String.valueOf(index + 1));
        obx.getValueType().setValue("ST");
        obx.getObservationIdentifier().getIdentifier().setValue(id);
        obx.getObservationIdentifier().getText().setValue(text);
        Varies varies = obx.getObservationValue(0);
        ST st = new ST(oml);
        st.setValue(value);
        varies.setData(st);
        obx.getObservationResultStatus().setValue("F");
        return index + 1;
    }

    /** Only writes the OBX when value is non-blank – avoids empty segments. */
    private int addObxIfPresent(OML_O21 oml, OML_O21_OBSERVATION_REQUEST obsReq,
                                int index, String id, String text, String value) throws Exception {
        if (value == null || value.isBlank()) return index;
        return addObx(oml, obsReq, index, id, text, value);
    }

    // -----------------------------------------------------------------------
    // Code-mapping helpers
    // -----------------------------------------------------------------------

    /**
     * Maps the treatmentLine text from the payload (e.g. "Adult First Line") to
     * the Context 107 code (1, 2, or 3).  If the value is already a bare digit
     * it is returned as-is.
     */
    private String treatmentLineCode(String treatmentLine) {
        if (treatmentLine == null || treatmentLine.isBlank()) return "";
        String t = treatmentLine.trim();
        if (t.equals("1") || t.equals("2") || t.equals("3")) return t;
        String lower = t.toLowerCase();
        if (lower.contains("first")  || lower.contains("1st")) return "1";
        if (lower.contains("second") || lower.contains("2nd")) return "2";
        if (lower.contains("third")  || lower.contains("3rd")) return "3";
        return "";
    }

    /**
     * Derives the Context 109 age-category code from the patient age:
     * P = Paediatric (&lt; 15), X = Adolescent (15–24), A = Adult (≥ 25).
     */
    private String ageCategory(int age) {
        if (age < 15) return "P";
        if (age <= 24) return "X";
        return "A";
    }

    /** Converts an int flag (0/1) to Context 142 Y/N code. */
    private String yn(int flag) {
        return flag == 1 ? "Y" : "N";
    }

    // -----------------------------------------------------------------------
    // Date / time formatting
    // -----------------------------------------------------------------------

    private String now() {
        return LocalDateTime.now(ZoneOffset.UTC).format(TS_FMT);
    }

    private String fmtDateTime(LocalDateTime ldt) {
        return ldt != null ? ldt.format(TS_FMT) : "";
    }

    private String fmtDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "";
    }
}
