package org.motechproject.prevac.helper;

import org.joda.time.LocalDate;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.enums.ScreeningStatus;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.exception.LimitationExceededException;
import org.motechproject.prevac.repository.ScreeningDataService;
import org.motechproject.prevac.repository.UnscheduledVisitDataService;
import org.motechproject.prevac.repository.VisitBookingDetailsDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VisitLimitationHelper {

    @Autowired
    private ScreeningDataService screeningDataService;

    @Autowired
    private VisitBookingDetailsDataService visitBookingDetailsDataService;

    @Autowired
    private UnscheduledVisitDataService unscheduledVisitDataService;

    public void checkCapacityForUnscheduleVisit(LocalDate date, Clinic clinic, Long id) {
        checkCapacity(date, clinic, null, id, null);
    }

    public void checkCapacityForScreening(LocalDate date, Clinic clinic, Long id) {
        checkCapacity(date, clinic, id, null, null);
    }

    public void checkCapacityForVisitBookingDetails(LocalDate date, Clinic clinic, Long id) {
        checkCapacity(date, clinic, null, null, id);
    }

    private void checkCapacity(LocalDate date, Clinic clinic, Long screeningId, Long unscheduledVisitId, Long visitBookingDetailsId) {
        if (clinic != null && date != null) {
            int screeningCount = (int) screeningDataService.countFindByClinicIdDateAndScreeningIdAndStatus(date, clinic.getId(), screeningId, ScreeningStatus.ACTIVE);
            int unscheduledVisitCount = (int) unscheduledVisitDataService.countFindByClinicIdAndDateAndVisitId(date, clinic.getId(), unscheduledVisitId);
            int visitBookingDetailsCount = (int) visitBookingDetailsDataService.countFindByBookingPlannedDateAndClinicIdAndVisitId(date, clinic.getId(), visitBookingDetailsId);
            int visitCount = screeningCount + visitBookingDetailsCount + unscheduledVisitCount;
            if (visitCount >= clinic.getMaxCapacityByDay()) {
                throw new LimitationExceededException("The clinic capacity limit for this day has been reached");
            }
        }
    }

    public int getMaxVisitCountForVisitType(VisitType visitType, Clinic clinic) {  //NO CHECKSTYLE CyclomaticComplexity
        switch (visitType) {
            case PRIME_VACCINATION_DAY:
                return clinic.getMaxPrimeVisits();
            case PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT:
                return clinic.getMaxPrimeFirstFollowUpVisits();
            case PRIME_VACCINATION_SECOND_FOLLOW_UP_VISIT:
                return clinic.getMaxPrimeSecondFollowUpVisits();
            case BOOST_VACCINATION_DAY:
                return clinic.getMaxBoosterVisits();
            case BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT:
                return clinic.getMaxBoosterFirstFollowUpVisits();
            case BOOST_VACCINATION_SECOND_FOLLOW_UP_VISIT:
                return clinic.getMaxBoosterSecondFollowUpVisits();
            case BOOST_VACCINATION_THIRD_FOLLOW_UP_VISIT:
                return clinic.getMaxBoosterThirdFollowUpVisits();
            case FIRST_LONG_TERM_FOLLOW_UP_VISIT:
                return clinic.getMaxFirstLongTermFollowUpVisits();
            case SECOND_LONG_TERM_FOLLOW_UP_VISIT:
                return clinic.getMaxSecondLongTermFollowUpVisits();
            case THIRD_LONG_TERM_FOLLOW_UP_VISIT:
                return clinic.getMaxThirdLongTermFollowUpVisits();
            case FOURTH_LONG_TERM_FOLLOW_UP_VISIT:
                return clinic.getMaxFourthLongTermFollowUpVisits();
            case FIFTH_LONG_TERM_FOLLOW_UP_VISIT:
                return clinic.getMaxFifthLongTermFollowUpVisits();
            case SIXTH_LONG_TERM_FOLLOW_UP_VISIT:
                return clinic.getMaxSixthLongTermFollowUpVisits();
            case SEVENTH_LONG_TERM_FOLLOW_UP_VISIT:
                return clinic.getMaxSeventhLongTermFollowUpVisits();
            case THIRD_VACCINATION_DAY:
                return clinic.getMaxThirdVaccinationVisits();
            case FIRST_POST_THIRD_VACCINATION_VISIT:
                return clinic.getMaxFirstPostThirdVaccinationVisits();
            case SECOND_POST_THIRD_VACCINATION_VISIT:
                return clinic.getMaxSecondPostThirdVaccinationVisits();
            case THIRD_POST_THIRD_VACCINATION_VISIT:
                return clinic.getMaxThirdPostThirdVaccinationVisits();
            case FOURTH_POST_THIRD_VACCINATION_VISIT:
                return clinic.getMaxFourthPostThirdVaccinationVisits();
            case FIFTH_POST_THIRD_VACCINATION_VISIT:
                return clinic.getMaxFifthPostThirdVaccinationVisits();
            default:
                throw new IllegalArgumentException(String.format("Cannot find max visits number in Clinic for Visit Type: %s",
                        visitType.getDisplayValue()));
        }
    }
}
