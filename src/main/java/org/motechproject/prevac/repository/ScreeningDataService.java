package org.motechproject.prevac.repository;

import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.prevac.domain.Screening;
import org.motechproject.prevac.domain.enums.ScreeningStatus;

import java.util.List;

public interface ScreeningDataService extends MotechDataService<Screening> {

    @Lookup
    List<Screening> findByDate(@LookupField(name = "date") Range<LocalDate> dateRange);

    @Lookup
    List<Screening> findByClinicIdDateAndScreeningIdAndStatus(
            @LookupField(name = "date") LocalDate date,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id,
            @LookupField(name = "status") ScreeningStatus status);

    long countFindByClinicIdDateAndScreeningIdAndStatus(@LookupField(name = "date") LocalDate date,
                                                        @LookupField(name = "clinic.id") Long clinicId,
                                                        @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id,
                                                        @LookupField(name = "status") ScreeningStatus status);

    @Lookup
    List<Screening> findByClinicLocationAndDate(@LookupField(name = "date") Range<LocalDate> date,
                                                @LookupField(name = "clinic.location", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location);

    @Lookup
    List<Screening> findByClinicLocation(
            @LookupField(name = "clinic.location", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location);

    @Lookup
    List<Screening> findByBookingId(@LookupField(name = "volunteer.id") Long bookingId);

    @Lookup
    List<Screening> findByBookingIdAndDate(@LookupField(name = "date") Range<LocalDate> date,
                                           @LookupField(name = "volunteer.id") Long bookingId);

    @Lookup
    List<Screening> findByClinicIdAndDateRangeAndStatus(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "status") ScreeningStatus status);

    long countFindByClinicIdAndDateRangeAndStatus(@LookupField(name = "clinic.id") Long clinicId,
                                                  @LookupField(name = "date") Range<LocalDate> date,
                                                  @LookupField(name = "status") ScreeningStatus status);

    @Lookup
    List<Screening> findByClinicIdAndDateAndStatus(@LookupField(name = "clinic.id") Long clinicId,
                                                   @LookupField(name = "date") LocalDate date,
                                                   @LookupField(name = "status") ScreeningStatus status);

    long countFindByClinicIdAndDateAndStatus(@LookupField(name = "clinic.id") Long clinicId,
                                             @LookupField(name = "date") LocalDate date,
                                             @LookupField(name = "status") ScreeningStatus status);
}
