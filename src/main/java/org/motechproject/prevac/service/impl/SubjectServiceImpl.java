package org.motechproject.prevac.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.util.Order;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.domain.UnscheduledVisit;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.mapper.SubjectMapper;
import org.motechproject.prevac.repository.ClinicDataService;
import org.motechproject.prevac.repository.SubjectDataService;
import org.motechproject.prevac.repository.UnscheduledVisitDataService;
import org.motechproject.prevac.repository.VisitBookingDetailsDataService;
import org.motechproject.prevac.service.SubjectService;
import org.motechproject.prevac.web.domain.SubjectZetesDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the {@link org.motechproject.prevac.service.SubjectService} interface. Uses
 * {@link org.motechproject.prevac.repository.SubjectDataService} in order to retrieve and persist records.
 */
@Service("subjectService")
public class SubjectServiceImpl implements SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Autowired
    private SubjectDataService subjectDataService;

    @Autowired
    private VisitBookingDetailsDataService visitBookingDetailsDataService;

    @Autowired
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Autowired
    private ClinicDataService clinicDataService;

    @Override
    public Subject createOrUpdateForZetes(SubjectZetesDto newSubject) {

        Subject subjectInDb = findSubjectBySubjectId(newSubject.getSubjectId());

        if (subjectInDb != null) {
            if (StringUtils.isNotBlank(newSubject.getSiteId()) && !newSubject.getSiteId().equals(subjectInDb.getSiteId())) {
                updateSiteId(newSubject.getSubjectId(), newSubject.getSiteId());
            }

            SubjectMapper.INSTANCE.updateFromDto(newSubject, subjectInDb);

            subjectInDb = update(subjectInDb);
        } else {
            subjectInDb = create(SubjectMapper.INSTANCE.fromDto(newSubject));
        }

        return subjectInDb;
    }

    @Override
    public List<Subject> findSubjectByName(String name) {
        return subjectDataService.findByName(name);
    }

    @Override
    public Subject findSubjectBySubjectId(String subjectId) {
        return subjectDataService.findBySubjectId(subjectId);
    }

    @Override
    public Subject findSubjectById(Long id) {
        return subjectDataService.findById(id);
    }

    @Override
    public List<Subject> findByStageId(Long stageId) {
        return subjectDataService.findByStageId(stageId);
    }

    @Override
    public List<Subject> findModifiedSubjects() {
        return subjectDataService.findByModified(true);
    }

    @Override
    public List<Subject> getAll() {
        return subjectDataService.retrieveAll();
    }

    @Override
    public Subject create(Subject record) {
        return subjectDataService.create(record);
    }

    @Override
    public Subject update(Subject record) {
        return subjectDataService.update(record);
    }

    @Override
    public List<Subject> findSubjectsPrimerVaccinatedAtDay(LocalDate date) {
        return subjectDataService.findByPrimerVaccinationDate(date);
    }

    @Override
    public List<Subject> findSubjectsBoosterVaccinatedAtDay(LocalDate date) {
        return subjectDataService.findByBoosterVaccinationDate(date);
    }

    @Override
    public LocalDate findOldestPrimerVaccinationDate() {
        QueryParams queryParams = new QueryParams(new Order("primerVaccinationDate", Order.Direction.ASC));
        List<Subject> subjects = subjectDataService.retrieveAll(queryParams);
        if (subjects != null && !subjects.isEmpty()) {
            for (Subject subject : subjects) {
                if (subject.getPrimerVaccinationDate() != null) {
                    return subject.getPrimerVaccinationDate();
                }
            }
        }
        return LocalDate.now().minusDays(1);
    }

    @Override
    public void delete(Subject record) {
        subjectDataService.delete(record);
    }

    @Override
    public void deleteAll() {
        subjectDataService.deleteAll();
    }

    private void updateSiteId(String subjectId, String siteId) {
        if (StringUtils.isNotBlank(subjectId) && StringUtils.isNotBlank(siteId)) {
            Clinic clinic = clinicDataService.findByExactSiteId(siteId);

            if (clinic != null) {
                List<Visit> visits = visitBookingDetailsDataService.findBySubjectId(subjectId);
                List<UnscheduledVisit> unscheduledVisits = unscheduledVisitDataService.findByParticipantId(subjectId);

                for (Visit details : visits) {
                    details.setClinic(clinic);
                    visitBookingDetailsDataService.update(details);
                }
                for (UnscheduledVisit unscheduledVisit : unscheduledVisits) {
                    unscheduledVisit.setClinic(clinic);
                    unscheduledVisitDataService.update(unscheduledVisit);
                }
            } else {
                LOGGER.warn("Cannot find Clinic with siteId: {} for Subject with id: {}", siteId, subjectId);
            }
        }
    }
}
