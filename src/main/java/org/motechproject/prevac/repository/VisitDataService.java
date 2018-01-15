package org.motechproject.prevac.repository;

import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.enums.VisitType;

import java.util.List;
import java.util.Set;

public interface VisitDataService extends MotechDataService<Visit> {

    @Lookup(name = "Find By exact Participant Id")
    List<Visit> findBySubjectId(
            @LookupField(name = "subject.subjectId") String subjectId);

    @Lookup
    List<Visit> findByPlannedDateClinicIdAndVisitType(
            @LookupField(name = "dateProjected", customOperator = Constants.Operators.EQ) LocalDate plannedDate,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type);

    @Lookup
    List<Visit> findByVisitPlannedDateAndClinicIdAndVisitId(
            @LookupField(name = "dateProjected", customOperator = Constants.Operators.EQ) LocalDate plannedDate,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id);

    long countFindByVisitPlannedDateAndClinicIdAndVisitId(
            @LookupField(name = "dateProjected", customOperator = Constants.Operators.EQ) LocalDate plannedDate,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id);

    @Lookup
    List<Visit> findByClinicIdVisitPlannedDateAndType(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "dateProjected") LocalDate plannedDate,
            @LookupField(name = "type") VisitType type);

    @Lookup
    List<Visit> findByClinicIdAndPlannedVisitDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    long countFindByClinicIdAndPlannedVisitDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    @Lookup
    List<Visit> findByClinicIdVisitTypeAndPlannedVisitDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    long countFindByClinicIdVisitTypeAndPlannedVisitDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    @Lookup
    List<Visit> findByClinicIdAndPlannedVisitDate(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "dateProjected") LocalDate date);

    long countFindByClinicIdAndPlannedVisitDate(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "dateProjected") LocalDate date);

    @Lookup
    List<Visit> findByClinicIdVisitTypeAndPlannedVisitDate(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") LocalDate date);

    long countFindByClinicIdVisitTypeAndPlannedVisitDate(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") LocalDate date);

    @Lookup
    List<Visit> findByExactParticipantSiteId(
            @LookupField(name = "subject.siteId") String siteId);

    @Lookup
    List<Visit> findByVisitTypeAndActualDateLess(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date", customOperator = Constants.Operators.LT) LocalDate date);

    /**
     * UI Lookups
     */

    @Lookup
    List<Visit> findByParticipantId(@LookupField(name = "subject.subjectId",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);

    @Lookup
    List<Visit> findByParticipantName(@LookupField(name = "subject.name",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name);

    @Lookup
    List<Visit> findByVisitType(@LookupField(name = "type") VisitType type);

    @Lookup
    List<Visit> findByClinicLocation(@LookupField(name = "clinic.location",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location);

    @Lookup
    List<Visit> findByVisitActualDate(
            @LookupField(name = "date") LocalDate date);

    @Lookup
    List<Visit> findByVisitActualDateRange(
            @LookupField(name = "date") Range<LocalDate> date);

    /**
     * Prime Vaccination Screen Lookups
     */

    @Lookup
    List<Visit> findByParticipantNamePrimeVaccinationDateAndVisitTypeAndPlannedVisitDate(
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "type") VisitType visitType,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate dateProjected);

    @Lookup
    List<Visit> findByParticipantIdVisitTypeAndParticipantPrimeVaccinationDateAndNameAndPlannedVisitDate(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") VisitType visitType,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByParticipantNamePrimeVaccinationDateAndVisitTypeAndPlannedVisitDateRange(
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "type") VisitType visitType,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByParticipantIdVisitTypeAndParticipantPrimeVaccinationDateAndNameAndPlannedVisitDateRange(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") VisitType visitType,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    /**
     * Reschedule Screen Lookups
     */

    @Lookup
    List<Visit> findByVisitTypeSetAndPlannedDate(
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    @Lookup
    List<Visit> findByParticipantIdAndVisitTypeSetAndPlannedDate(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByParticipantIdAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByParticipantNameAndVisitTypeSetAndPlannedDate(
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByParticipantNameAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitTypeAndPlannedDate(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByVisitTypeAndPlannedDateRange(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByClinicLocationAndVisitTypeSetAndPlannedDate(
            @LookupField(name = "clinic.location",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByClinicLocationAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "clinic.location",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitActualDateAndVisitTypeSetAndPlannedDate(
            @LookupField(name = "date") LocalDate date,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByVisitActualDateAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "date") LocalDate date,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);


    @Lookup
    List<Visit> findByVisitActualDateRangeAndVisitTypeSetAndPlannedDate(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByVisitActualDateRangeAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

}
