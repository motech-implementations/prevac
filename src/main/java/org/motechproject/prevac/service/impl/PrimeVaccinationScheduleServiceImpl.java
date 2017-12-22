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
        validateDate(dto);

        Visit primeVisit = visitBookingDetailsDataService.findById(dto.getVisitBookingDetailsId());
        // We have an update
        if (primeVisit != null) {
            Visit screeningVisit = getScreeningDetails(primeVisit);
            if (screeningVisit == null) {
                throw new IllegalArgumentException("Cannot save, because details for Visit not found");
            }
            return new PrimeVaccinationScheduleDto(updateVisitWithDto(primeVisit, screeningVisit, dto));
        }

        Subject subject = subjectDataService.findBySubjectId(dto.getParticipantId());

        List<Visit> visits = subject.getVisits();
        visits.add(visitBookingDetailsDataService.create(createScreeningVisitFromDto(dto, subject)));
        primeVisit = visitBookingDetailsDataService.create(createPrimeVacVisitFromDto(dto, subject));
        visits.add(primeVisit);

        Clinic clinic = primeVisit.getClinic();

        if (clinic != null && !ignoreLimitation) {
            checkNumberOfPatients(dto, clinic);
        }

        return new PrimeVaccinationScheduleDto(primeVisit);
    }

    @Override
    public List<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords() {
        List<PrimeVaccinationScheduleDto> primeVacDtos = new ArrayList<>();
        // Get all subjects
        List<Subject> subjects = subjectDataService.findByPrimerVaccinationDate(null);

        for (Subject subject : subjects) {
            if (subject.getVisits().isEmpty()) {
                primeVacDtos.add(new PrimeVaccinationScheduleDto(subject));
            }
        }

        return primeVacDtos;
    }

    private Visit createScreeningVisitFromDto(PrimeVaccinationScheduleDto dto, Subject subject) {
        Visit screeningVisit = new Visit();
        screeningVisit.setType(VisitType.SCREENING);
        screeningVisit.setSubject(subject);
        screeningVisit.setDate(dto.getBookingScreeningActualDate());
        return screeningVisit;
    }

    private Visit createPrimeVacVisitFromDto(PrimeVaccinationScheduleDto dto, Subject subject) {
        Visit primeVisit = new Visit();
        primeVisit.setSubject(subject);
        primeVisit.setType(VisitType.PRIME_VACCINATION_DAY);
        primeVisit.setStartTime(dto.getStartTime());
        primeVisit.setEndTime(calculateEndTime(dto.getStartTime()));
        primeVisit.setDateProjected(dto.getDate());
        primeVisit.getSubject().setFemaleChildBearingAge(dto.getFemaleChildBearingAge());
        primeVisit.setIgnoreDateLimitation(dto.getIgnoreDateLimitation());
        return primeVisit;
    }

    private Visit updateVisitWithDto(Visit primeDetails, Visit screeningDetails,
                                     PrimeVaccinationScheduleDto dto) {
        primeDetails.setStartTime(dto.getStartTime());
        primeDetails.setEndTime(calculateEndTime(dto.getStartTime()));
        primeDetails.setDateProjected(dto.getDate());
        primeDetails.getSubject().setFemaleChildBearingAge(dto.getFemaleChildBearingAge());
        primeDetails.setIgnoreDateLimitation(dto.getIgnoreDateLimitation());

        screeningDetails.setDate(dto.getBookingScreeningActualDate());

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

    private Time calculateEndTime(Time startTime) {
        int endTimeHour = (startTime.getHour() + PrevacConstants.TIME_OF_THE_VISIT) % PrevacConstants.MAX_TIME_HOUR;
        return new Time(endTimeHour, startTime.getMinute());
    }
}
