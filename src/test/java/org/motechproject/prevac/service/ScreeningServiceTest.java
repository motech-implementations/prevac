package org.motechproject.prevac.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.Screening;
import org.motechproject.prevac.domain.enums.ScreeningStatus;
import org.motechproject.prevac.dto.ScreeningDto;
import org.motechproject.prevac.domain.Volunteer;
import org.motechproject.prevac.exception.LimitationExceededException;
import org.motechproject.prevac.helper.VisitLimitationHelper;
import org.motechproject.prevac.repository.ClinicDataService;
import org.motechproject.prevac.repository.ScreeningDataService;
import org.motechproject.prevac.repository.VolunteerDataService;
import org.motechproject.prevac.service.impl.ScreeningServiceImpl;
import org.motechproject.commons.date.model.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.initMocks;

public class ScreeningServiceTest {

    @InjectMocks
    private ScreeningService screeningService = new ScreeningServiceImpl();

    @Mock
    private ScreeningDataService screeningDataService;

    @Mock
    private VolunteerDataService volunteerDataService;

    @Mock
    private ClinicDataService clinicDataService;

    @Mock
    private VisitLimitationHelper visitLimitationHelper;

    @Rule
    public ExpectedException thrown = ExpectedException.none(); //NO CHECKSTYLE VisibilityModifier

    private Clinic clinic;

    @Before
    public void setUp() {
        initMocks(this);
        clinic = createClinic("siteId", "location", 1, 5, 2, 9, 8, 7, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
        clinic.setId(1L);
    }

    @Test
    public void shouldThrowLimitationExceededExceptionWhenNumberOfRoomsLimitIsReached() {
        when(clinicDataService.findById(1L)).thenReturn(clinic);
        when(volunteerDataService.create(any(Volunteer.class))).thenReturn(new Volunteer());
        List<Screening> screenings = new ArrayList<>(Arrays.asList(
                createScreeningVisit(2L, clinic, new LocalDate(2217, 1, 1), new Time(11, 0), new Time(12, 0))
        ));

        when(screeningDataService.findByClinicIdAndDateAndStatus(1L, new LocalDate(2217, 1, 1), ScreeningStatus.ACTIVE)).thenReturn(screenings);

        ScreeningDto screeningDto = createScreeningDto(null, "1", "2217-1-1", "11:00"); // todo Id may be unwanted here
        Screening screening = new Screening();
        screening.setId(1L);

        thrown.expect(LimitationExceededException.class);
        thrown.expectMessage("Too many visits at the same time");
        screeningService.addOrUpdate(screeningDto, false);

    }

    @Test
    public void shouldThrowLimitationExceededExceptionWhenTypeOfVisitLimitIsReached() {
        when(clinicDataService.findById(1L)).thenReturn(clinic);
        when(volunteerDataService.create(any(Volunteer.class))).thenReturn(new Volunteer());
        List<Screening> screenings = new ArrayList<>(Arrays.asList(
                createScreeningVisit(2L, clinic, new LocalDate(2217, 1, 1), new Time(10, 0), new Time(11, 0)),
                createScreeningVisit(3L, clinic, new LocalDate(2217, 1, 1), new Time(9, 0), new Time(10, 0))
        ));

        when(screeningDataService.findByClinicIdAndDateAndStatus(1L, new LocalDate(2217, 1, 1), ScreeningStatus.ACTIVE)).thenReturn(screenings);

        ScreeningDto screeningDto = createScreeningDto(null, "1", "2217-1-1", "12:00");
        Screening screening = new Screening();
        screening.setId(1L);

        thrown.expect(LimitationExceededException.class);
        thrown.expectMessage("The booking limit for this type of visit has been reached");
        screeningService.addOrUpdate(screeningDto, false);
    }

    @Test
    public void shouldCheckCapacityLimitFirst() {
        when(clinicDataService.findById(1L)).thenReturn(clinic);
        when(volunteerDataService.create(any(Volunteer.class))).thenReturn(new Volunteer());
        List<Screening> screenings = new ArrayList<>(Arrays.asList(
                createScreeningVisit(2L, clinic, new LocalDate(2217, 1, 1), new Time(10, 0), new Time(11, 0)),
                createScreeningVisit(3L, clinic, new LocalDate(2217, 1, 1), new Time(9, 0), new Time(10, 0))
        ));

        when(screeningDataService.findByClinicIdAndDateAndStatus(1L, new LocalDate(2217, 1, 1), ScreeningStatus.ACTIVE)).thenReturn(screenings);

        doThrow(new LimitationExceededException("The clinic capacity limit for this day has been reached")).when(visitLimitationHelper).checkCapacityForScreening(any(LocalDate.class), any(Clinic.class), anyLong());

        ScreeningDto screeningDto = createScreeningDto(null, "1", "2217-1-1", "12:00");
        Screening screening = new Screening();
        screening.setId(1L);

        thrown.expect(LimitationExceededException.class);
        thrown.expectMessage("The clinic capacity limit for this day has been reached");
        screeningService.addOrUpdate(screeningDto, false);
    }

    @Test
    public void shouldNotCheckLimitationWhenIgnoreLimitationVariableIsTrue() {
        when(clinicDataService.findById(1L)).thenReturn(clinic);
        when(volunteerDataService.create(any(Volunteer.class))).thenReturn(new Volunteer());
        List<Screening> screenings = new ArrayList<>(Arrays.asList(
                createScreeningVisit(2L, clinic, new LocalDate(2217, 1, 1), new Time(10, 0), new Time(11, 0)),
                createScreeningVisit(3L, clinic, new LocalDate(2217, 1, 1), new Time(9, 0), new Time(10, 0))
        ));

        when(screeningDataService.findByClinicIdAndDateAndStatus(1L, new LocalDate(2217, 1, 1), ScreeningStatus.ACTIVE)).thenReturn(screenings);

        ScreeningDto screeningDto = createScreeningDto(null, "1", "2217-1-1", "12:00");
        Screening screening = new Screening();
        screening.setId(1L);

        screeningService.addOrUpdate(screeningDto, true);

        verify(visitLimitationHelper, never()).checkCapacityForScreening(any(LocalDate.class), any(Clinic.class), anyLong());
        verify(screeningDataService, times(1)).create(any(Screening.class));
    }

    private Screening createScreeningVisit(Long id, Clinic clinic, LocalDate date, Time startTime, Time endTime) {
        Screening screening = new Screening();
        screening.setId(id);
        screening.setClinic(clinic);
        screening.setDate(date);
        screening.setStartTime(startTime);
        screening.setEndTime(endTime);
        return screening;
    }

    private ScreeningDto createScreeningDto(String id, String clinicId, String date, String startTime) {
        ScreeningDto screeningDto = new ScreeningDto();
        screeningDto.setId(id);
        screeningDto.setClinicId(clinicId);
        screeningDto.setDate(date);
        screeningDto.setStartTime(startTime);
        return screeningDto;
    }

    private Clinic createClinic(String siteId, String location, Integer numberOfRooms, Integer maxCapacityByDay, Integer maxScreeningVisits, //NO CHECKSTYLE ParameterNumber
                                Integer maxPrimeVisits, Integer maxPrimeFirstFollowUpVisits, Integer maxPrimeSecondFollowUpVisits, Integer maxPrimeThirdFollowUpVisits,
                                Integer maxBoosterVisits, Integer maxBoosterFirstFollowUpVisits,
                                Integer maxThreeMonthsPostPrimeVisits, Integer maxSixMonthsPostPrimeVisits, Integer maxTwelveMonthsPostPrimeVisits,
                                Integer maxTwentyFourMonthsPostPrimeVisits, Integer maxThirtySixMonthsPostPrimeVisits,
                                Integer maxFortyEightMonthsPostPrimeVisits, Integer maxSixtyMonthsPostPrimeVisits) {
        Clinic newClinic = new Clinic();
        newClinic.setSiteId(siteId);
        newClinic.setLocation(location);
        newClinic.setNumberOfRooms(numberOfRooms);
        newClinic.setMaxCapacityByDay(maxCapacityByDay);
        newClinic.setMaxScreeningVisits(maxScreeningVisits);
        newClinic.setMaxPrimeVisits(maxPrimeVisits);
        newClinic.setMaxPrimeFirstFollowUpVisits(maxPrimeFirstFollowUpVisits);
        newClinic.setMaxPrimeSecondFollowUpVisits(maxPrimeSecondFollowUpVisits);
        newClinic.setMaxPrimeThirdFollowUpVisits(maxPrimeThirdFollowUpVisits);
        newClinic.setMaxBoosterVisits(maxBoosterVisits);
        newClinic.setMaxBoosterFirstFollowUpVisits(maxBoosterFirstFollowUpVisits);
        newClinic.setMaxThreeMonthsPostPrimeVisits(maxThreeMonthsPostPrimeVisits);
        newClinic.setMaxSixMonthsPostPrimeVisits(maxSixMonthsPostPrimeVisits);
        newClinic.setMaxTwelveMonthsPostPrimeVisits(maxTwelveMonthsPostPrimeVisits);
        newClinic.setMaxTwentyFourMonthsPostPrimeVisits(maxTwentyFourMonthsPostPrimeVisits);
        newClinic.setMaxThirtySixMonthsPostPrimeVisits(maxThirtySixMonthsPostPrimeVisits);
        newClinic.setMaxFortyEightMonthsPostPrimeVisits(maxFortyEightMonthsPostPrimeVisits);
        newClinic.setMaxSixtyMonthsPostPrimeVisits(maxSixtyMonthsPostPrimeVisits);
        return newClinic;
    }

}
