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

public interface VisitBookingDetailsDataService extends MotechDataService<Visit> {

    @Lookup(name = "Find By exact Participant Id")
    List<Visit> findBySubjectId(
            @LookupField(name = "subject.subjectId") String subjectId);

    @Lookup
    Visit findByParticipantIdAndVisitType(
            @LookupField(name = "subject.subjectId") String subjectId,
            @LookupField(name = "type") VisitType type);

    @Lookup
    List<Visit> findByPlannedDateClinicIdAndVisitType(
            @LookupField(name = "dateProjected", customOperator = Constants.Operators.EQ) LocalDate bookingPlannedDate,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type);

    @Lookup
    List<Visit> findByBookingPlannedDateAndClinicIdAndVisitId(
            @LookupField(name = "bookingPlannedDate", customOperator = Constants.Operators.EQ) LocalDate bookingPlannedDate,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id);

    long countFindByBookingPlannedDateAndClinicIdAndVisitId(
            @LookupField(name = "bookingPlannedDate", customOperator = Constants.Operators.EQ) LocalDate bookingPlannedDate,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id);

    @Lookup
    List<Visit> findByClinicIdVisitPlannedDateAndType(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "dateProjected") LocalDate plannedDate,
            @LookupField(name = "type") VisitType type);

    @Lookup
    List<Visit> findByClinicIdAndBookingPlannedDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "bookingPlannedDate") Range<LocalDate> date);

    long countFindByClinicIdAndBookingPlannedDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "bookingPlannedDate") Range<LocalDate> date);

    @Lookup
    List<Visit> findByClinicIdVisitTypeAndBookingPlannedDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "bookingPlannedDate") Range<LocalDate> date);

    long countFindByClinicIdVisitTypeAndBookingPlannedDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    @Lookup
    List<Visit> findByClinicIdAndBookingPlannedDate(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "bookingPlannedDate") LocalDate date);

    long countFindByClinicIdAndBookingPlannedDate(@LookupField(name = "clinic.id") Long clinicId,
                                                  @LookupField(name = "bookingPlannedDate") LocalDate date);

    @Lookup
    List<Visit> findByClinicIdVisitTypeAndBookingPlannedDate(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "bookingPlannedDate") LocalDate date);

    long countFindByClinicIdVisitTypeAndBookingPlannedDate(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "bookingPlannedDate") LocalDate date);

    @Lookup
    List<Visit> findByExactParticipantSiteId(
            @LookupField(name = "subject.siteId") String siteId);

    @Lookup
    List<Visit> findByVisitTypeAndActualDateLess(@LookupField(name = "type") VisitType type,
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

    @Lookup
    List<Visit> findByVisitPlannedDate(
            @LookupField(name = "dateProjected") LocalDate date);

    @Lookup
    List<Visit> findByVisitPlannedDateRange(
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    @Lookup
    List<Visit> findByVisitTypeAndParticipantPrimeVaccinationDateAndName(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name);

    /**
     * Prime Vaccination Screen Lookups
     */

    @Lookup
    List<Visit> findByParticipantNamePrimeVaccinationDateAndVisitTypeAndBookingPlannedDate(
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "type") VisitType visitType,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate dateProjected);

    @Lookup
    List<Visit> findByParticipantIdVisitTypeAndParticipantPrimeVaccinationDateAndNameAndBookingPlannedDate(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") VisitType visitType,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "bookingPlannedDate",
                    customOperator = Constants.Operators.NEQ) LocalDate bookingPlannedDate);

    @Lookup
    List<Visit> findByParticipantNamePrimeVaccinationDateAndVisitTypeAndBookingPlannedDateRange(
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "type") VisitType visitType,
            @LookupField(name = "dateProjected") Range<LocalDate> bookingPlannedDate);

    @Lookup
    List<Visit> findByParticipantIdVisitTypeAndParticipantPrimeVaccinationDateAndNameAndBookingPlannedDateRange(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") VisitType visitType,
            @LookupField(name = "subject.primerVaccinationDate",
                    customOperator = Constants.Operators.EQ) LocalDate primerVaccinationDate,
            @LookupField(name = "subject.name",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name,
            @LookupField(name = "bookingPlannedDate") Range<LocalDate> bookingPlannedDate);

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
