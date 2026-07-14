package moh.gov.zm.lis.lab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Generated;

public interface LabResultDTO {
   @JsonInclude(Include.NON_NULL)
   @Schema(
      description = "A received lab result"
   )
   public static class LabResultResponse {
      private UUID id;
      private String messageControlId;
      private String placerOrderNumber;
      private String fillerOrderNumber;
      private String labCode;
      private String orderingMflCode;
      private String orderingHmisCode;
      private String patientIdentifier;
      private String patientName;
      private LocalDate patientDob;
      private String patientSex;
      private String testLoinc;
      private String testName;
      private String resultStatus;
      private String messageKind;
      private OffsetDateTime specimenCollectedAt;
      private String reconciliationStatus;
      private String matchMethod;
      private Integer candidateCount;
      private String reviewStatus;
      private UUID reviewedBy;
      private OffsetDateTime reviewedAt;
      private String reviewNote;
      private Integer version;
      private Boolean isCurrent;
      private String forwardStatus;
      private OffsetDateTime receivedAt;
      private List<ObservationResponse> observations;

      @Generated
      public static LabResultResponseBuilder builder() {
         return new LabResultResponseBuilder();
      }

      @Generated
      public UUID getId() {
         return this.id;
      }

      @Generated
      public String getMessageControlId() {
         return this.messageControlId;
      }

      @Generated
      public String getPlacerOrderNumber() {
         return this.placerOrderNumber;
      }

      @Generated
      public String getFillerOrderNumber() {
         return this.fillerOrderNumber;
      }

      @Generated
      public String getLabCode() {
         return this.labCode;
      }

      @Generated
      public String getOrderingMflCode() {
         return this.orderingMflCode;
      }

      @Generated
      public String getOrderingHmisCode() {
         return this.orderingHmisCode;
      }

      @Generated
      public String getPatientIdentifier() {
         return this.patientIdentifier;
      }

      @Generated
      public String getPatientName() {
         return this.patientName;
      }

      @Generated
      public LocalDate getPatientDob() {
         return this.patientDob;
      }

      @Generated
      public String getPatientSex() {
         return this.patientSex;
      }

      @Generated
      public String getTestLoinc() {
         return this.testLoinc;
      }

      @Generated
      public String getTestName() {
         return this.testName;
      }

      @Generated
      public String getResultStatus() {
         return this.resultStatus;
      }

      @Generated
      public String getMessageKind() {
         return this.messageKind;
      }

      @Generated
      public OffsetDateTime getSpecimenCollectedAt() {
         return this.specimenCollectedAt;
      }

      @Generated
      public String getReconciliationStatus() {
         return this.reconciliationStatus;
      }

      @Generated
      public String getMatchMethod() {
         return this.matchMethod;
      }

      @Generated
      public Integer getCandidateCount() {
         return this.candidateCount;
      }

      @Generated
      public String getReviewStatus() {
         return this.reviewStatus;
      }

      @Generated
      public UUID getReviewedBy() {
         return this.reviewedBy;
      }

      @Generated
      public OffsetDateTime getReviewedAt() {
         return this.reviewedAt;
      }

      @Generated
      public String getReviewNote() {
         return this.reviewNote;
      }

      @Generated
      public Integer getVersion() {
         return this.version;
      }

      @Generated
      public Boolean getIsCurrent() {
         return this.isCurrent;
      }

      @Generated
      public String getForwardStatus() {
         return this.forwardStatus;
      }

      @Generated
      public OffsetDateTime getReceivedAt() {
         return this.receivedAt;
      }

      @Generated
      public List<ObservationResponse> getObservations() {
         return this.observations;
      }

      @Generated
      public void setId(final UUID id) {
         this.id = id;
      }

      @Generated
      public void setMessageControlId(final String messageControlId) {
         this.messageControlId = messageControlId;
      }

      @Generated
      public void setPlacerOrderNumber(final String placerOrderNumber) {
         this.placerOrderNumber = placerOrderNumber;
      }

      @Generated
      public void setFillerOrderNumber(final String fillerOrderNumber) {
         this.fillerOrderNumber = fillerOrderNumber;
      }

      @Generated
      public void setLabCode(final String labCode) {
         this.labCode = labCode;
      }

      @Generated
      public void setOrderingMflCode(final String orderingMflCode) {
         this.orderingMflCode = orderingMflCode;
      }

      @Generated
      public void setOrderingHmisCode(final String orderingHmisCode) {
         this.orderingHmisCode = orderingHmisCode;
      }

      @Generated
      public void setPatientIdentifier(final String patientIdentifier) {
         this.patientIdentifier = patientIdentifier;
      }

      @Generated
      public void setPatientName(final String patientName) {
         this.patientName = patientName;
      }

      @Generated
      public void setPatientDob(final LocalDate patientDob) {
         this.patientDob = patientDob;
      }

      @Generated
      public void setPatientSex(final String patientSex) {
         this.patientSex = patientSex;
      }

      @Generated
      public void setTestLoinc(final String testLoinc) {
         this.testLoinc = testLoinc;
      }

      @Generated
      public void setTestName(final String testName) {
         this.testName = testName;
      }

      @Generated
      public void setResultStatus(final String resultStatus) {
         this.resultStatus = resultStatus;
      }

      @Generated
      public void setMessageKind(final String messageKind) {
         this.messageKind = messageKind;
      }

      @Generated
      public void setSpecimenCollectedAt(final OffsetDateTime specimenCollectedAt) {
         this.specimenCollectedAt = specimenCollectedAt;
      }

      @Generated
      public void setReconciliationStatus(final String reconciliationStatus) {
         this.reconciliationStatus = reconciliationStatus;
      }

      @Generated
      public void setMatchMethod(final String matchMethod) {
         this.matchMethod = matchMethod;
      }

      @Generated
      public void setCandidateCount(final Integer candidateCount) {
         this.candidateCount = candidateCount;
      }

      @Generated
      public void setReviewStatus(final String reviewStatus) {
         this.reviewStatus = reviewStatus;
      }

      @Generated
      public void setReviewedBy(final UUID reviewedBy) {
         this.reviewedBy = reviewedBy;
      }

      @Generated
      public void setReviewedAt(final OffsetDateTime reviewedAt) {
         this.reviewedAt = reviewedAt;
      }

      @Generated
      public void setReviewNote(final String reviewNote) {
         this.reviewNote = reviewNote;
      }

      @Generated
      public void setVersion(final Integer version) {
         this.version = version;
      }

      @Generated
      public void setIsCurrent(final Boolean isCurrent) {
         this.isCurrent = isCurrent;
      }

      @Generated
      public void setForwardStatus(final String forwardStatus) {
         this.forwardStatus = forwardStatus;
      }

      @Generated
      public void setReceivedAt(final OffsetDateTime receivedAt) {
         this.receivedAt = receivedAt;
      }

      @Generated
      public void setObservations(final List<ObservationResponse> observations) {
         this.observations = observations;
      }

      @Generated
      public boolean equals(final Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof LabResultResponse)) {
            return false;
         } else {
            LabResultResponse other = (LabResultResponse)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$candidateCount = this.getCandidateCount();
               Object other$candidateCount = other.getCandidateCount();
               if (this$candidateCount == null) {
                  if (other$candidateCount != null) {
                     return false;
                  }
               } else if (!this$candidateCount.equals(other$candidateCount)) {
                  return false;
               }

               Object this$version = this.getVersion();
               Object other$version = other.getVersion();
               if (this$version == null) {
                  if (other$version != null) {
                     return false;
                  }
               } else if (!this$version.equals(other$version)) {
                  return false;
               }

               Object this$isCurrent = this.getIsCurrent();
               Object other$isCurrent = other.getIsCurrent();
               if (this$isCurrent == null) {
                  if (other$isCurrent != null) {
                     return false;
                  }
               } else if (!this$isCurrent.equals(other$isCurrent)) {
                  return false;
               }

               Object this$id = this.getId();
               Object other$id = other.getId();
               if (this$id == null) {
                  if (other$id != null) {
                     return false;
                  }
               } else if (!this$id.equals(other$id)) {
                  return false;
               }

               Object this$messageControlId = this.getMessageControlId();
               Object other$messageControlId = other.getMessageControlId();
               if (this$messageControlId == null) {
                  if (other$messageControlId != null) {
                     return false;
                  }
               } else if (!this$messageControlId.equals(other$messageControlId)) {
                  return false;
               }

               Object this$placerOrderNumber = this.getPlacerOrderNumber();
               Object other$placerOrderNumber = other.getPlacerOrderNumber();
               if (this$placerOrderNumber == null) {
                  if (other$placerOrderNumber != null) {
                     return false;
                  }
               } else if (!this$placerOrderNumber.equals(other$placerOrderNumber)) {
                  return false;
               }

               Object this$fillerOrderNumber = this.getFillerOrderNumber();
               Object other$fillerOrderNumber = other.getFillerOrderNumber();
               if (this$fillerOrderNumber == null) {
                  if (other$fillerOrderNumber != null) {
                     return false;
                  }
               } else if (!this$fillerOrderNumber.equals(other$fillerOrderNumber)) {
                  return false;
               }

               Object this$labCode = this.getLabCode();
               Object other$labCode = other.getLabCode();
               if (this$labCode == null) {
                  if (other$labCode != null) {
                     return false;
                  }
               } else if (!this$labCode.equals(other$labCode)) {
                  return false;
               }

               Object this$orderingMflCode = this.getOrderingMflCode();
               Object other$orderingMflCode = other.getOrderingMflCode();
               if (this$orderingMflCode == null) {
                  if (other$orderingMflCode != null) {
                     return false;
                  }
               } else if (!this$orderingMflCode.equals(other$orderingMflCode)) {
                  return false;
               }

               Object this$orderingHmisCode = this.getOrderingHmisCode();
               Object other$orderingHmisCode = other.getOrderingHmisCode();
               if (this$orderingHmisCode == null) {
                  if (other$orderingHmisCode != null) {
                     return false;
                  }
               } else if (!this$orderingHmisCode.equals(other$orderingHmisCode)) {
                  return false;
               }

               Object this$patientIdentifier = this.getPatientIdentifier();
               Object other$patientIdentifier = other.getPatientIdentifier();
               if (this$patientIdentifier == null) {
                  if (other$patientIdentifier != null) {
                     return false;
                  }
               } else if (!this$patientIdentifier.equals(other$patientIdentifier)) {
                  return false;
               }

               Object this$patientName = this.getPatientName();
               Object other$patientName = other.getPatientName();
               if (this$patientName == null) {
                  if (other$patientName != null) {
                     return false;
                  }
               } else if (!this$patientName.equals(other$patientName)) {
                  return false;
               }

               Object this$patientDob = this.getPatientDob();
               Object other$patientDob = other.getPatientDob();
               if (this$patientDob == null) {
                  if (other$patientDob != null) {
                     return false;
                  }
               } else if (!this$patientDob.equals(other$patientDob)) {
                  return false;
               }

               Object this$patientSex = this.getPatientSex();
               Object other$patientSex = other.getPatientSex();
               if (this$patientSex == null) {
                  if (other$patientSex != null) {
                     return false;
                  }
               } else if (!this$patientSex.equals(other$patientSex)) {
                  return false;
               }

               Object this$testLoinc = this.getTestLoinc();
               Object other$testLoinc = other.getTestLoinc();
               if (this$testLoinc == null) {
                  if (other$testLoinc != null) {
                     return false;
                  }
               } else if (!this$testLoinc.equals(other$testLoinc)) {
                  return false;
               }

               Object this$testName = this.getTestName();
               Object other$testName = other.getTestName();
               if (this$testName == null) {
                  if (other$testName != null) {
                     return false;
                  }
               } else if (!this$testName.equals(other$testName)) {
                  return false;
               }

               Object this$resultStatus = this.getResultStatus();
               Object other$resultStatus = other.getResultStatus();
               if (this$resultStatus == null) {
                  if (other$resultStatus != null) {
                     return false;
                  }
               } else if (!this$resultStatus.equals(other$resultStatus)) {
                  return false;
               }

               Object this$messageKind = this.getMessageKind();
               Object other$messageKind = other.getMessageKind();
               if (this$messageKind == null) {
                  if (other$messageKind != null) {
                     return false;
                  }
               } else if (!this$messageKind.equals(other$messageKind)) {
                  return false;
               }

               Object this$specimenCollectedAt = this.getSpecimenCollectedAt();
               Object other$specimenCollectedAt = other.getSpecimenCollectedAt();
               if (this$specimenCollectedAt == null) {
                  if (other$specimenCollectedAt != null) {
                     return false;
                  }
               } else if (!this$specimenCollectedAt.equals(other$specimenCollectedAt)) {
                  return false;
               }

               Object this$reconciliationStatus = this.getReconciliationStatus();
               Object other$reconciliationStatus = other.getReconciliationStatus();
               if (this$reconciliationStatus == null) {
                  if (other$reconciliationStatus != null) {
                     return false;
                  }
               } else if (!this$reconciliationStatus.equals(other$reconciliationStatus)) {
                  return false;
               }

               Object this$matchMethod = this.getMatchMethod();
               Object other$matchMethod = other.getMatchMethod();
               if (this$matchMethod == null) {
                  if (other$matchMethod != null) {
                     return false;
                  }
               } else if (!this$matchMethod.equals(other$matchMethod)) {
                  return false;
               }

               Object this$reviewStatus = this.getReviewStatus();
               Object other$reviewStatus = other.getReviewStatus();
               if (this$reviewStatus == null) {
                  if (other$reviewStatus != null) {
                     return false;
                  }
               } else if (!this$reviewStatus.equals(other$reviewStatus)) {
                  return false;
               }

               Object this$reviewedBy = this.getReviewedBy();
               Object other$reviewedBy = other.getReviewedBy();
               if (this$reviewedBy == null) {
                  if (other$reviewedBy != null) {
                     return false;
                  }
               } else if (!this$reviewedBy.equals(other$reviewedBy)) {
                  return false;
               }

               Object this$reviewedAt = this.getReviewedAt();
               Object other$reviewedAt = other.getReviewedAt();
               if (this$reviewedAt == null) {
                  if (other$reviewedAt != null) {
                     return false;
                  }
               } else if (!this$reviewedAt.equals(other$reviewedAt)) {
                  return false;
               }

               Object this$reviewNote = this.getReviewNote();
               Object other$reviewNote = other.getReviewNote();
               if (this$reviewNote == null) {
                  if (other$reviewNote != null) {
                     return false;
                  }
               } else if (!this$reviewNote.equals(other$reviewNote)) {
                  return false;
               }

               Object this$forwardStatus = this.getForwardStatus();
               Object other$forwardStatus = other.getForwardStatus();
               if (this$forwardStatus == null) {
                  if (other$forwardStatus != null) {
                     return false;
                  }
               } else if (!this$forwardStatus.equals(other$forwardStatus)) {
                  return false;
               }

               Object this$receivedAt = this.getReceivedAt();
               Object other$receivedAt = other.getReceivedAt();
               if (this$receivedAt == null) {
                  if (other$receivedAt != null) {
                     return false;
                  }
               } else if (!this$receivedAt.equals(other$receivedAt)) {
                  return false;
               }

               Object this$observations = this.getObservations();
               Object other$observations = other.getObservations();
               if (this$observations == null) {
                  if (other$observations != null) {
                     return false;
                  }
               } else if (!this$observations.equals(other$observations)) {
                  return false;
               }

               return true;
            }
         }
      }

      @Generated
      protected boolean canEqual(final Object other) {
         return other instanceof LabResultResponse;
      }

      @Generated
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $candidateCount = this.getCandidateCount();
         result = result * 59 + ($candidateCount == null ? 43 : $candidateCount.hashCode());
         Object $version = this.getVersion();
         result = result * 59 + ($version == null ? 43 : $version.hashCode());
         Object $isCurrent = this.getIsCurrent();
         result = result * 59 + ($isCurrent == null ? 43 : $isCurrent.hashCode());
         Object $id = this.getId();
         result = result * 59 + ($id == null ? 43 : $id.hashCode());
         Object $messageControlId = this.getMessageControlId();
         result = result * 59 + ($messageControlId == null ? 43 : $messageControlId.hashCode());
         Object $placerOrderNumber = this.getPlacerOrderNumber();
         result = result * 59 + ($placerOrderNumber == null ? 43 : $placerOrderNumber.hashCode());
         Object $fillerOrderNumber = this.getFillerOrderNumber();
         result = result * 59 + ($fillerOrderNumber == null ? 43 : $fillerOrderNumber.hashCode());
         Object $labCode = this.getLabCode();
         result = result * 59 + ($labCode == null ? 43 : $labCode.hashCode());
         Object $orderingMflCode = this.getOrderingMflCode();
         result = result * 59 + ($orderingMflCode == null ? 43 : $orderingMflCode.hashCode());
         Object $orderingHmisCode = this.getOrderingHmisCode();
         result = result * 59 + ($orderingHmisCode == null ? 43 : $orderingHmisCode.hashCode());
         Object $patientIdentifier = this.getPatientIdentifier();
         result = result * 59 + ($patientIdentifier == null ? 43 : $patientIdentifier.hashCode());
         Object $patientName = this.getPatientName();
         result = result * 59 + ($patientName == null ? 43 : $patientName.hashCode());
         Object $patientDob = this.getPatientDob();
         result = result * 59 + ($patientDob == null ? 43 : $patientDob.hashCode());
         Object $patientSex = this.getPatientSex();
         result = result * 59 + ($patientSex == null ? 43 : $patientSex.hashCode());
         Object $testLoinc = this.getTestLoinc();
         result = result * 59 + ($testLoinc == null ? 43 : $testLoinc.hashCode());
         Object $testName = this.getTestName();
         result = result * 59 + ($testName == null ? 43 : $testName.hashCode());
         Object $resultStatus = this.getResultStatus();
         result = result * 59 + ($resultStatus == null ? 43 : $resultStatus.hashCode());
         Object $messageKind = this.getMessageKind();
         result = result * 59 + ($messageKind == null ? 43 : $messageKind.hashCode());
         Object $specimenCollectedAt = this.getSpecimenCollectedAt();
         result = result * 59 + ($specimenCollectedAt == null ? 43 : $specimenCollectedAt.hashCode());
         Object $reconciliationStatus = this.getReconciliationStatus();
         result = result * 59 + ($reconciliationStatus == null ? 43 : $reconciliationStatus.hashCode());
         Object $matchMethod = this.getMatchMethod();
         result = result * 59 + ($matchMethod == null ? 43 : $matchMethod.hashCode());
         Object $reviewStatus = this.getReviewStatus();
         result = result * 59 + ($reviewStatus == null ? 43 : $reviewStatus.hashCode());
         Object $reviewedBy = this.getReviewedBy();
         result = result * 59 + ($reviewedBy == null ? 43 : $reviewedBy.hashCode());
         Object $reviewedAt = this.getReviewedAt();
         result = result * 59 + ($reviewedAt == null ? 43 : $reviewedAt.hashCode());
         Object $reviewNote = this.getReviewNote();
         result = result * 59 + ($reviewNote == null ? 43 : $reviewNote.hashCode());
         Object $forwardStatus = this.getForwardStatus();
         result = result * 59 + ($forwardStatus == null ? 43 : $forwardStatus.hashCode());
         Object $receivedAt = this.getReceivedAt();
         result = result * 59 + ($receivedAt == null ? 43 : $receivedAt.hashCode());
         Object $observations = this.getObservations();
         result = result * 59 + ($observations == null ? 43 : $observations.hashCode());
         return result;
      }

      @Generated
      public String toString() {
         String var10000 = String.valueOf(this.getId());
         return "LabResultDTO.LabResultResponse(id=" + var10000 + ", messageControlId=" + this.getMessageControlId() + ", placerOrderNumber=" + this.getPlacerOrderNumber() + ", fillerOrderNumber=" + this.getFillerOrderNumber() + ", labCode=" + this.getLabCode() + ", orderingMflCode=" + this.getOrderingMflCode() + ", orderingHmisCode=" + this.getOrderingHmisCode() + ", patientIdentifier=" + this.getPatientIdentifier() + ", patientName=" + this.getPatientName() + ", patientDob=" + String.valueOf(this.getPatientDob()) + ", patientSex=" + this.getPatientSex() + ", testLoinc=" + this.getTestLoinc() + ", testName=" + this.getTestName() + ", resultStatus=" + this.getResultStatus() + ", messageKind=" + this.getMessageKind() + ", specimenCollectedAt=" + String.valueOf(this.getSpecimenCollectedAt()) + ", reconciliationStatus=" + this.getReconciliationStatus() + ", matchMethod=" + this.getMatchMethod() + ", candidateCount=" + this.getCandidateCount() + ", reviewStatus=" + this.getReviewStatus() + ", reviewedBy=" + String.valueOf(this.getReviewedBy()) + ", reviewedAt=" + String.valueOf(this.getReviewedAt()) + ", reviewNote=" + this.getReviewNote() + ", version=" + this.getVersion() + ", isCurrent=" + this.getIsCurrent() + ", forwardStatus=" + this.getForwardStatus() + ", receivedAt=" + String.valueOf(this.getReceivedAt()) + ", observations=" + String.valueOf(this.getObservations()) + ")";
      }

      @Generated
      public LabResultResponse() {
      }

      @Generated
      public LabResultResponse(final UUID id, final String messageControlId, final String placerOrderNumber, final String fillerOrderNumber, final String labCode, final String orderingMflCode, final String orderingHmisCode, final String patientIdentifier, final String patientName, final LocalDate patientDob, final String patientSex, final String testLoinc, final String testName, final String resultStatus, final String messageKind, final OffsetDateTime specimenCollectedAt, final String reconciliationStatus, final String matchMethod, final Integer candidateCount, final String reviewStatus, final UUID reviewedBy, final OffsetDateTime reviewedAt, final String reviewNote, final Integer version, final Boolean isCurrent, final String forwardStatus, final OffsetDateTime receivedAt, final List<ObservationResponse> observations) {
         this.id = id;
         this.messageControlId = messageControlId;
         this.placerOrderNumber = placerOrderNumber;
         this.fillerOrderNumber = fillerOrderNumber;
         this.labCode = labCode;
         this.orderingMflCode = orderingMflCode;
         this.orderingHmisCode = orderingHmisCode;
         this.patientIdentifier = patientIdentifier;
         this.patientName = patientName;
         this.patientDob = patientDob;
         this.patientSex = patientSex;
         this.testLoinc = testLoinc;
         this.testName = testName;
         this.resultStatus = resultStatus;
         this.messageKind = messageKind;
         this.specimenCollectedAt = specimenCollectedAt;
         this.reconciliationStatus = reconciliationStatus;
         this.matchMethod = matchMethod;
         this.candidateCount = candidateCount;
         this.reviewStatus = reviewStatus;
         this.reviewedBy = reviewedBy;
         this.reviewedAt = reviewedAt;
         this.reviewNote = reviewNote;
         this.version = version;
         this.isCurrent = isCurrent;
         this.forwardStatus = forwardStatus;
         this.receivedAt = receivedAt;
         this.observations = observations;
      }

      @Generated
      public static class LabResultResponseBuilder {
         @Generated
         private UUID id;
         @Generated
         private String messageControlId;
         @Generated
         private String placerOrderNumber;
         @Generated
         private String fillerOrderNumber;
         @Generated
         private String labCode;
         @Generated
         private String orderingMflCode;
         @Generated
         private String orderingHmisCode;
         @Generated
         private String patientIdentifier;
         @Generated
         private String patientName;
         @Generated
         private LocalDate patientDob;
         @Generated
         private String patientSex;
         @Generated
         private String testLoinc;
         @Generated
         private String testName;
         @Generated
         private String resultStatus;
         @Generated
         private String messageKind;
         @Generated
         private OffsetDateTime specimenCollectedAt;
         @Generated
         private String reconciliationStatus;
         @Generated
         private String matchMethod;
         @Generated
         private Integer candidateCount;
         @Generated
         private String reviewStatus;
         @Generated
         private UUID reviewedBy;
         @Generated
         private OffsetDateTime reviewedAt;
         @Generated
         private String reviewNote;
         @Generated
         private Integer version;
         @Generated
         private Boolean isCurrent;
         @Generated
         private String forwardStatus;
         @Generated
         private OffsetDateTime receivedAt;
         @Generated
         private List<ObservationResponse> observations;

         @Generated
         LabResultResponseBuilder() {
         }

         @Generated
         public LabResultResponseBuilder id(final UUID id) {
            this.id = id;
            return this;
         }

         @Generated
         public LabResultResponseBuilder messageControlId(final String messageControlId) {
            this.messageControlId = messageControlId;
            return this;
         }

         @Generated
         public LabResultResponseBuilder placerOrderNumber(final String placerOrderNumber) {
            this.placerOrderNumber = placerOrderNumber;
            return this;
         }

         @Generated
         public LabResultResponseBuilder fillerOrderNumber(final String fillerOrderNumber) {
            this.fillerOrderNumber = fillerOrderNumber;
            return this;
         }

         @Generated
         public LabResultResponseBuilder labCode(final String labCode) {
            this.labCode = labCode;
            return this;
         }

         @Generated
         public LabResultResponseBuilder orderingMflCode(final String orderingMflCode) {
            this.orderingMflCode = orderingMflCode;
            return this;
         }

         @Generated
         public LabResultResponseBuilder orderingHmisCode(final String orderingHmisCode) {
            this.orderingHmisCode = orderingHmisCode;
            return this;
         }

         @Generated
         public LabResultResponseBuilder patientIdentifier(final String patientIdentifier) {
            this.patientIdentifier = patientIdentifier;
            return this;
         }

         @Generated
         public LabResultResponseBuilder patientName(final String patientName) {
            this.patientName = patientName;
            return this;
         }

         @Generated
         public LabResultResponseBuilder patientDob(final LocalDate patientDob) {
            this.patientDob = patientDob;
            return this;
         }

         @Generated
         public LabResultResponseBuilder patientSex(final String patientSex) {
            this.patientSex = patientSex;
            return this;
         }

         @Generated
         public LabResultResponseBuilder testLoinc(final String testLoinc) {
            this.testLoinc = testLoinc;
            return this;
         }

         @Generated
         public LabResultResponseBuilder testName(final String testName) {
            this.testName = testName;
            return this;
         }

         @Generated
         public LabResultResponseBuilder resultStatus(final String resultStatus) {
            this.resultStatus = resultStatus;
            return this;
         }

         @Generated
         public LabResultResponseBuilder messageKind(final String messageKind) {
            this.messageKind = messageKind;
            return this;
         }

         @Generated
         public LabResultResponseBuilder specimenCollectedAt(final OffsetDateTime specimenCollectedAt) {
            this.specimenCollectedAt = specimenCollectedAt;
            return this;
         }

         @Generated
         public LabResultResponseBuilder reconciliationStatus(final String reconciliationStatus) {
            this.reconciliationStatus = reconciliationStatus;
            return this;
         }

         @Generated
         public LabResultResponseBuilder matchMethod(final String matchMethod) {
            this.matchMethod = matchMethod;
            return this;
         }

         @Generated
         public LabResultResponseBuilder candidateCount(final Integer candidateCount) {
            this.candidateCount = candidateCount;
            return this;
         }

         @Generated
         public LabResultResponseBuilder reviewStatus(final String reviewStatus) {
            this.reviewStatus = reviewStatus;
            return this;
         }

         @Generated
         public LabResultResponseBuilder reviewedBy(final UUID reviewedBy) {
            this.reviewedBy = reviewedBy;
            return this;
         }

         @Generated
         public LabResultResponseBuilder reviewedAt(final OffsetDateTime reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
         }

         @Generated
         public LabResultResponseBuilder reviewNote(final String reviewNote) {
            this.reviewNote = reviewNote;
            return this;
         }

         @Generated
         public LabResultResponseBuilder version(final Integer version) {
            this.version = version;
            return this;
         }

         @Generated
         public LabResultResponseBuilder isCurrent(final Boolean isCurrent) {
            this.isCurrent = isCurrent;
            return this;
         }

         @Generated
         public LabResultResponseBuilder forwardStatus(final String forwardStatus) {
            this.forwardStatus = forwardStatus;
            return this;
         }

         @Generated
         public LabResultResponseBuilder receivedAt(final OffsetDateTime receivedAt) {
            this.receivedAt = receivedAt;
            return this;
         }

         @Generated
         public LabResultResponseBuilder observations(final List<ObservationResponse> observations) {
            this.observations = observations;
            return this;
         }

         @Generated
         public LabResultResponse build() {
            return new LabResultResponse(this.id, this.messageControlId, this.placerOrderNumber, this.fillerOrderNumber, this.labCode, this.orderingMflCode, this.orderingHmisCode, this.patientIdentifier, this.patientName, this.patientDob, this.patientSex, this.testLoinc, this.testName, this.resultStatus, this.messageKind, this.specimenCollectedAt, this.reconciliationStatus, this.matchMethod, this.candidateCount, this.reviewStatus, this.reviewedBy, this.reviewedAt, this.reviewNote, this.version, this.isCurrent, this.forwardStatus, this.receivedAt, this.observations);
         }

         @Generated
         public String toString() {
            String var10000 = String.valueOf(this.id);
            return "LabResultDTO.LabResultResponse.LabResultResponseBuilder(id=" + var10000 + ", messageControlId=" + this.messageControlId + ", placerOrderNumber=" + this.placerOrderNumber + ", fillerOrderNumber=" + this.fillerOrderNumber + ", labCode=" + this.labCode + ", orderingMflCode=" + this.orderingMflCode + ", orderingHmisCode=" + this.orderingHmisCode + ", patientIdentifier=" + this.patientIdentifier + ", patientName=" + this.patientName + ", patientDob=" + String.valueOf(this.patientDob) + ", patientSex=" + this.patientSex + ", testLoinc=" + this.testLoinc + ", testName=" + this.testName + ", resultStatus=" + this.resultStatus + ", messageKind=" + this.messageKind + ", specimenCollectedAt=" + String.valueOf(this.specimenCollectedAt) + ", reconciliationStatus=" + this.reconciliationStatus + ", matchMethod=" + this.matchMethod + ", candidateCount=" + this.candidateCount + ", reviewStatus=" + this.reviewStatus + ", reviewedBy=" + String.valueOf(this.reviewedBy) + ", reviewedAt=" + String.valueOf(this.reviewedAt) + ", reviewNote=" + this.reviewNote + ", version=" + this.version + ", isCurrent=" + this.isCurrent + ", forwardStatus=" + this.forwardStatus + ", receivedAt=" + String.valueOf(this.receivedAt) + ", observations=" + String.valueOf(this.observations) + ")";
         }
      }
   }

   @JsonInclude(Include.NON_NULL)
   @Schema(
      description = "A single observation (OBX) of a lab result"
   )
   public static class ObservationResponse {
      private Integer setId;
      private String valueType;
      private String loinc;
      private String localCode;
      private String text;
      private String value;
      private BigDecimal numericValue;
      private String units;
      private String referenceRange;
      private String abnormalFlags;
      private String status;
      private OffsetDateTime observedAt;

      @Generated
      public static ObservationResponseBuilder builder() {
         return new ObservationResponseBuilder();
      }

      @Generated
      public Integer getSetId() {
         return this.setId;
      }

      @Generated
      public String getValueType() {
         return this.valueType;
      }

      @Generated
      public String getLoinc() {
         return this.loinc;
      }

      @Generated
      public String getLocalCode() {
         return this.localCode;
      }

      @Generated
      public String getText() {
         return this.text;
      }

      @Generated
      public String getValue() {
         return this.value;
      }

      @Generated
      public BigDecimal getNumericValue() {
         return this.numericValue;
      }

      @Generated
      public String getUnits() {
         return this.units;
      }

      @Generated
      public String getReferenceRange() {
         return this.referenceRange;
      }

      @Generated
      public String getAbnormalFlags() {
         return this.abnormalFlags;
      }

      @Generated
      public String getStatus() {
         return this.status;
      }

      @Generated
      public OffsetDateTime getObservedAt() {
         return this.observedAt;
      }

      @Generated
      public void setSetId(final Integer setId) {
         this.setId = setId;
      }

      @Generated
      public void setValueType(final String valueType) {
         this.valueType = valueType;
      }

      @Generated
      public void setLoinc(final String loinc) {
         this.loinc = loinc;
      }

      @Generated
      public void setLocalCode(final String localCode) {
         this.localCode = localCode;
      }

      @Generated
      public void setText(final String text) {
         this.text = text;
      }

      @Generated
      public void setValue(final String value) {
         this.value = value;
      }

      @Generated
      public void setNumericValue(final BigDecimal numericValue) {
         this.numericValue = numericValue;
      }

      @Generated
      public void setUnits(final String units) {
         this.units = units;
      }

      @Generated
      public void setReferenceRange(final String referenceRange) {
         this.referenceRange = referenceRange;
      }

      @Generated
      public void setAbnormalFlags(final String abnormalFlags) {
         this.abnormalFlags = abnormalFlags;
      }

      @Generated
      public void setStatus(final String status) {
         this.status = status;
      }

      @Generated
      public void setObservedAt(final OffsetDateTime observedAt) {
         this.observedAt = observedAt;
      }

      @Generated
      public boolean equals(final Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ObservationResponse)) {
            return false;
         } else {
            ObservationResponse other = (ObservationResponse)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$setId = this.getSetId();
               Object other$setId = other.getSetId();
               if (this$setId == null) {
                  if (other$setId != null) {
                     return false;
                  }
               } else if (!this$setId.equals(other$setId)) {
                  return false;
               }

               Object this$valueType = this.getValueType();
               Object other$valueType = other.getValueType();
               if (this$valueType == null) {
                  if (other$valueType != null) {
                     return false;
                  }
               } else if (!this$valueType.equals(other$valueType)) {
                  return false;
               }

               Object this$loinc = this.getLoinc();
               Object other$loinc = other.getLoinc();
               if (this$loinc == null) {
                  if (other$loinc != null) {
                     return false;
                  }
               } else if (!this$loinc.equals(other$loinc)) {
                  return false;
               }

               Object this$localCode = this.getLocalCode();
               Object other$localCode = other.getLocalCode();
               if (this$localCode == null) {
                  if (other$localCode != null) {
                     return false;
                  }
               } else if (!this$localCode.equals(other$localCode)) {
                  return false;
               }

               Object this$text = this.getText();
               Object other$text = other.getText();
               if (this$text == null) {
                  if (other$text != null) {
                     return false;
                  }
               } else if (!this$text.equals(other$text)) {
                  return false;
               }

               Object this$value = this.getValue();
               Object other$value = other.getValue();
               if (this$value == null) {
                  if (other$value != null) {
                     return false;
                  }
               } else if (!this$value.equals(other$value)) {
                  return false;
               }

               Object this$numericValue = this.getNumericValue();
               Object other$numericValue = other.getNumericValue();
               if (this$numericValue == null) {
                  if (other$numericValue != null) {
                     return false;
                  }
               } else if (!this$numericValue.equals(other$numericValue)) {
                  return false;
               }

               Object this$units = this.getUnits();
               Object other$units = other.getUnits();
               if (this$units == null) {
                  if (other$units != null) {
                     return false;
                  }
               } else if (!this$units.equals(other$units)) {
                  return false;
               }

               Object this$referenceRange = this.getReferenceRange();
               Object other$referenceRange = other.getReferenceRange();
               if (this$referenceRange == null) {
                  if (other$referenceRange != null) {
                     return false;
                  }
               } else if (!this$referenceRange.equals(other$referenceRange)) {
                  return false;
               }

               Object this$abnormalFlags = this.getAbnormalFlags();
               Object other$abnormalFlags = other.getAbnormalFlags();
               if (this$abnormalFlags == null) {
                  if (other$abnormalFlags != null) {
                     return false;
                  }
               } else if (!this$abnormalFlags.equals(other$abnormalFlags)) {
                  return false;
               }

               Object this$status = this.getStatus();
               Object other$status = other.getStatus();
               if (this$status == null) {
                  if (other$status != null) {
                     return false;
                  }
               } else if (!this$status.equals(other$status)) {
                  return false;
               }

               Object this$observedAt = this.getObservedAt();
               Object other$observedAt = other.getObservedAt();
               if (this$observedAt == null) {
                  if (other$observedAt != null) {
                     return false;
                  }
               } else if (!this$observedAt.equals(other$observedAt)) {
                  return false;
               }

               return true;
            }
         }
      }

      @Generated
      protected boolean canEqual(final Object other) {
         return other instanceof ObservationResponse;
      }

      @Generated
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $setId = this.getSetId();
         result = result * 59 + ($setId == null ? 43 : $setId.hashCode());
         Object $valueType = this.getValueType();
         result = result * 59 + ($valueType == null ? 43 : $valueType.hashCode());
         Object $loinc = this.getLoinc();
         result = result * 59 + ($loinc == null ? 43 : $loinc.hashCode());
         Object $localCode = this.getLocalCode();
         result = result * 59 + ($localCode == null ? 43 : $localCode.hashCode());
         Object $text = this.getText();
         result = result * 59 + ($text == null ? 43 : $text.hashCode());
         Object $value = this.getValue();
         result = result * 59 + ($value == null ? 43 : $value.hashCode());
         Object $numericValue = this.getNumericValue();
         result = result * 59 + ($numericValue == null ? 43 : $numericValue.hashCode());
         Object $units = this.getUnits();
         result = result * 59 + ($units == null ? 43 : $units.hashCode());
         Object $referenceRange = this.getReferenceRange();
         result = result * 59 + ($referenceRange == null ? 43 : $referenceRange.hashCode());
         Object $abnormalFlags = this.getAbnormalFlags();
         result = result * 59 + ($abnormalFlags == null ? 43 : $abnormalFlags.hashCode());
         Object $status = this.getStatus();
         result = result * 59 + ($status == null ? 43 : $status.hashCode());
         Object $observedAt = this.getObservedAt();
         result = result * 59 + ($observedAt == null ? 43 : $observedAt.hashCode());
         return result;
      }

      @Generated
      public String toString() {
         Integer var10000 = this.getSetId();
         return "LabResultDTO.ObservationResponse(setId=" + var10000 + ", valueType=" + this.getValueType() + ", loinc=" + this.getLoinc() + ", localCode=" + this.getLocalCode() + ", text=" + this.getText() + ", value=" + this.getValue() + ", numericValue=" + String.valueOf(this.getNumericValue()) + ", units=" + this.getUnits() + ", referenceRange=" + this.getReferenceRange() + ", abnormalFlags=" + this.getAbnormalFlags() + ", status=" + this.getStatus() + ", observedAt=" + String.valueOf(this.getObservedAt()) + ")";
      }

      @Generated
      public ObservationResponse() {
      }

      @Generated
      public ObservationResponse(final Integer setId, final String valueType, final String loinc, final String localCode, final String text, final String value, final BigDecimal numericValue, final String units, final String referenceRange, final String abnormalFlags, final String status, final OffsetDateTime observedAt) {
         this.setId = setId;
         this.valueType = valueType;
         this.loinc = loinc;
         this.localCode = localCode;
         this.text = text;
         this.value = value;
         this.numericValue = numericValue;
         this.units = units;
         this.referenceRange = referenceRange;
         this.abnormalFlags = abnormalFlags;
         this.status = status;
         this.observedAt = observedAt;
      }

      @Generated
      public static class ObservationResponseBuilder {
         @Generated
         private Integer setId;
         @Generated
         private String valueType;
         @Generated
         private String loinc;
         @Generated
         private String localCode;
         @Generated
         private String text;
         @Generated
         private String value;
         @Generated
         private BigDecimal numericValue;
         @Generated
         private String units;
         @Generated
         private String referenceRange;
         @Generated
         private String abnormalFlags;
         @Generated
         private String status;
         @Generated
         private OffsetDateTime observedAt;

         @Generated
         ObservationResponseBuilder() {
         }

         @Generated
         public ObservationResponseBuilder setId(final Integer setId) {
            this.setId = setId;
            return this;
         }

         @Generated
         public ObservationResponseBuilder valueType(final String valueType) {
            this.valueType = valueType;
            return this;
         }

         @Generated
         public ObservationResponseBuilder loinc(final String loinc) {
            this.loinc = loinc;
            return this;
         }

         @Generated
         public ObservationResponseBuilder localCode(final String localCode) {
            this.localCode = localCode;
            return this;
         }

         @Generated
         public ObservationResponseBuilder text(final String text) {
            this.text = text;
            return this;
         }

         @Generated
         public ObservationResponseBuilder value(final String value) {
            this.value = value;
            return this;
         }

         @Generated
         public ObservationResponseBuilder numericValue(final BigDecimal numericValue) {
            this.numericValue = numericValue;
            return this;
         }

         @Generated
         public ObservationResponseBuilder units(final String units) {
            this.units = units;
            return this;
         }

         @Generated
         public ObservationResponseBuilder referenceRange(final String referenceRange) {
            this.referenceRange = referenceRange;
            return this;
         }

         @Generated
         public ObservationResponseBuilder abnormalFlags(final String abnormalFlags) {
            this.abnormalFlags = abnormalFlags;
            return this;
         }

         @Generated
         public ObservationResponseBuilder status(final String status) {
            this.status = status;
            return this;
         }

         @Generated
         public ObservationResponseBuilder observedAt(final OffsetDateTime observedAt) {
            this.observedAt = observedAt;
            return this;
         }

         @Generated
         public ObservationResponse build() {
            return new ObservationResponse(this.setId, this.valueType, this.loinc, this.localCode, this.text, this.value, this.numericValue, this.units, this.referenceRange, this.abnormalFlags, this.status, this.observedAt);
         }

         @Generated
         public String toString() {
            Integer var10000 = this.setId;
            return "LabResultDTO.ObservationResponse.ObservationResponseBuilder(setId=" + var10000 + ", valueType=" + this.valueType + ", loinc=" + this.loinc + ", localCode=" + this.localCode + ", text=" + this.text + ", value=" + this.value + ", numericValue=" + String.valueOf(this.numericValue) + ", units=" + this.units + ", referenceRange=" + this.referenceRange + ", abnormalFlags=" + this.abnormalFlags + ", status=" + this.status + ", observedAt=" + String.valueOf(this.observedAt) + ")";
         }
      }
   }

   @Schema(
      description = "Clinician review decision (note optional, typically for rejections)"
   )
   public static class ReviewDecisionRequest {
      private @Size(
   max = 1000
) String note;

      @Generated
      public static ReviewDecisionRequestBuilder builder() {
         return new ReviewDecisionRequestBuilder();
      }

      @Generated
      public String getNote() {
         return this.note;
      }

      @Generated
      public void setNote(final String note) {
         this.note = note;
      }

      @Generated
      public boolean equals(final Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ReviewDecisionRequest)) {
            return false;
         } else {
            ReviewDecisionRequest other = (ReviewDecisionRequest)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$note = this.getNote();
               Object other$note = other.getNote();
               if (this$note == null) {
                  if (other$note != null) {
                     return false;
                  }
               } else if (!this$note.equals(other$note)) {
                  return false;
               }

               return true;
            }
         }
      }

      @Generated
      protected boolean canEqual(final Object other) {
         return other instanceof ReviewDecisionRequest;
      }

      @Generated
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $note = this.getNote();
         result = result * 59 + ($note == null ? 43 : $note.hashCode());
         return result;
      }

      @Generated
      public String toString() {
         return "LabResultDTO.ReviewDecisionRequest(note=" + this.getNote() + ")";
      }

      @Generated
      public ReviewDecisionRequest() {
      }

      @Generated
      public ReviewDecisionRequest(final String note) {
         this.note = note;
      }

      @Generated
      public static class ReviewDecisionRequestBuilder {
         @Generated
         private String note;

         @Generated
         ReviewDecisionRequestBuilder() {
         }

         @Generated
         public ReviewDecisionRequestBuilder note(final String note) {
            this.note = note;
            return this;
         }

         @Generated
         public ReviewDecisionRequest build() {
            return new ReviewDecisionRequest(this.note);
         }

         @Generated
         public String toString() {
            return "LabResultDTO.ReviewDecisionRequest.ReviewDecisionRequestBuilder(note=" + this.note + ")";
         }
      }
   }
}
