package org.motechproject.prevac.service.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.dto.PrimeVaccinationScheduleDto;
import org.motechproject.prevac.exception.LimitationExceededException;
import org.motechproject.prevac.helper.VisitLimitationHelper;
import org.motechproject.prevac.repository.SubjectDataService;
import org.motechproject.prevac.repository.VisitBookingDetailsDataService;
import org.motechproject.prevac.service.LookupService;
import org.motechproject.prevac.service.PrimeVaccinationScheduleService;
import org.motechproject.prevac.util.QueryParamsBuilder;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("primeVaccinationScheduleService")
public class PrimeVaccinationScheduleServiceImpl implements PrimeVaccinationScheduleService {

    @Autowired
    private VisitBookingDetailsDataService visitBookingDetailsDataService;

    @Autowired
    private VisitLimitationHelper visitLimitationHelper;

    @Autowired
    private SubjectDataService subjectDataService;

    @Autowired
    private LookupService lookupService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Records<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords(GridSettings settings) throws IOException {
        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(settings, getFields(settings.getFields()));
        return lookupService.getEntities(PrimeVaccinationScheduleDto.class,
                Visit.class, settings.getLookup(), settings.getFields(), queryParams);
    }

    @Override
    public PrimeVaccinationScheduleDto createOrUpdateWithDto(PrimeVaccinationScheduleDto dto, Boolean ignoreLimitation) {
        Visit primeDetails = visitBookingDetailsDataService.findById(dto.getVisitBookingDetailsId());
        Visit screeningDetails = getScreeningDetails(primeDetails);

        if (primeDetails == null || screeningDetails == null) {
            throw new IllegalArgumentException("Cannot save, because details for Visit not found");
        }

        Clinic clinic = primeDetails.getClinic();

        validateDate(dto);

        if (clinic != null && !ignoreLimitation) {
            checkNumberOfPatients(dto, clinic);
        }

        return new PrimeVaccinationScheduleDto(updateVisitWithDto(primeDetails, screeningDetails, dto));
    }

    @Override
    public List<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords() {
        List<PrimeVaccinationScheduleDto> primeVacDtos = new ArrayList<>();
        createEmptyVisitsForSubjectWithoutPrimeVacDate();
        List<Visit> detailsList = visitBookingDetailsDataService
                .findByParticipantNamePrimeVaccinationDateAndVisitTypeAndBookingPlannedDateEq(".", null, VisitType.PRIME_VACCINATION_DAY, null);

        for (Visit details : detailsList) {
            primeVacDtos.add(new PrimeVaccinationScheduleDto(details));
        }

        return primeVacDtos;
    }

    private Visit updateVisitWithDto(Visit primeDetails, Visit screeningDetails,
                                     PrimeVaccinationScheduleDto dto) {
        primeDetails.setStartTime(dto.getStartTime());
        primeDetails.setEndTime(calculateEndTime(dto.getStartTime()));
        primeDetails.setBookingPlannedDate(dto.getDate());
        primeDetails.getSubject().setFemaleChildBearingAge(dto.getFemaleChildBearingAge());
        primeDetails.setIgnoreDateLimitation(dto.getIgnoreDateLimitation());

        screeningDetails.setBookingActualDate(dto.getBookingScreeningActualDate());

        visitBookingDetailsDataService.update(screeningDetails);
        return visitBookingDetailsDataService.update(primeDetails);
    }

    private Map<String, Object> getFields(String json) throws IOException {
        if (json == null) {
            return null;
        } else {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }
    }

    private void checkNumberOfPatients(PrimeVaccinationScheduleDto dto, Clinic clinic) {

        List<Visit> visits = visitBookingDetailsDataService.findByBookingPlannedDateClinicIdAndVisitType(dto.getDate(),
                clinic.getId(), VisitType.PRIME_VACCINATION_DAY);

        visitLimitationHelper.checkCapacityForVisitBookingDetails(dto.getDate(), clinic, dto.getVisitBookingDetailsId());
        if (visits != null) {
            int numberOfRooms = clinic.getNumberOfRooms();
            int maxVisits = clinic.getMaxPrimeVisits();
            int patients = 0;

            for (Visit visit : visits) {
                if (visit.getId().equals(dto.getVisitBookingDetailsId())) {
                    maxVisits++;
                } else {
                    Time startTime = dto.getStartTime();
                    Time endTime = calculateEndTime(startTime);

                    if (startTime.isBefore(visit.getStartTime())) {
                        if (visit.getStartTime().isBefore(endTime)) {
                            patients++;
                        }
                    } else {
                        if (startTime.isBefore(visit.getEndTime())) {
                            patients++;
                        }
                    }
                }
            }

            if (visits.size() >= maxVisits) {
                throw new LimitationExceededException("The booking limit for this type of visit has been reached");
            }
            if (patients >= numberOfRooms) {
                throw new LimitationExceededException("Too many visits at the same time");
            }
        }
    }

    private void validateDate(PrimeVaccinationScheduleDto dto) {
        if (dto.getBookingScreeningActualDate() == null) {
            throw new IllegalArgumentException("Screening Date cannot be empty");
        }
        if (dto.getDate() == null) {
            throw new IllegalArgumentException("Prime Vaccination Planned Date cannot be empty");
        }
        if (dto.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The date can not be in past");
        }

        if (!dto.getIgnoreDateLimitation()) {

            LocalDate actualScreeningDate = dto.getBookingScreeningActualDate();

            LocalDate earliestDate = dto.getFemaleChildBearingAge() != null && dto.getFemaleChildBearingAge()
                    ? actualScreeningDate.plusDays(PrevacConstants.EARLIEST_DATE_IF_FEMALE_CHILD_BEARING_AGE)
                    : actualScreeningDate.plusDays(PrevacConstants.EARLIEST_DATE);
            LocalDate latestDate = actualScreeningDate.plusDays(PrevacConstants.LATEST_DATE);

            if (dto.getDate().isBefore(earliestDate) || dto.getDate().isAfter(latestDate)) {
                throw new IllegalArgumentException(String.format("The date should be between %s and %s but is %s",
                        earliestDate, latestDate, dto.getDate()));
            }
        }
    }

    private Visit getScreeningDetails(Visit visit) {
        if (visit != null) {
            for (Visit details : visit.getSubject().getVisits()) {
                if (VisitType.SCREENING.equals(details.getType())) {
                    return details;
                }
            }
        }
        return null;
    }

    private synchronized void createEmptyVisitsForSubjectWithoutPrimeVacDate() {
        List<Subject> subjects = subjectDataService.findByPrimerVaccinationDate(null);
        for (Subject subject : subjects) {
            Visit screeningVisit = visitBookingDetailsDataService.findByParticipantIdAndVisitType(subject.getSubjectId(), VisitType.SCREENING);
            Visit primeVisit = visitBookingDetailsDataService.findByParticipantIdAndVisitType(subject.getSubjectId(), VisitType.PRIME_VACCINATION_DAY);
            Visit followUpVisit = visitBookingDetailsDataService.findByParticipantIdAndVisitType(subject.getSubjectId(), VisitType.PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT);

            List<Visit> visits = subject.getVisits();
            if (screeningVisit == null) {
                screeningVisit = new Visit();
                screeningVisit.setType(VisitType.SCREENING);
                screeningVisit.setSubject(subject);
                visits.add(screeningVisit);
                visitBookingDetailsDataService.create(screeningVisit);
            }
            if (primeVisit == null) {
                primeVisit = new Visit();
                primeVisit.setSubject(subject);
                primeVisit.setType(VisitType.PRIME_VACCINATION_DAY);
                visits.add(primeVisit);
                visitBookingDetailsDataService.create(primeVisit);
            }
            if (followUpVisit == null) {
                followUpVisit = new Visit();
                followUpVisit.setSubject(subject);
                followUpVisit.setType(VisitType.PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT);
                visits.add(followUpVisit);
                visitBookingDetailsDataService.create(followUpVisit);
            }
        }
    }

    private Time calculateEndTime(Time startTime) {
        int endTimeHour = (startTime.getHour() + PrevacConstants.TIME_OF_THE_VISIT) % PrevacConstants.MAX_TIME_HOUR;
        return new Time(endTimeHour, startTime.getMinute());
    }
}
