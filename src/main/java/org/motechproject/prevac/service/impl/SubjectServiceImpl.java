package org.motechproject.prevac.service.impl;

import org.apache.commons.lang.StringUtils;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.domain.UnscheduledVisit;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.mapper.SubjectMapper;
import org.motechproject.prevac.repository.ClinicDataService;
import org.motechproject.prevac.repository.SubjectDataService;
import org.motechproject.prevac.repository.UnscheduledVisitDataService;
import org.motechproject.prevac.repository.VisitDataService;
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
    private VisitDataService visitDataService;

    @Autowired
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Autowired
    private ClinicDataService clinicDataService;

    @Override
    public Subject createOrUpdateForZetes(SubjectZetesDto newSubject) {

        Subject subjectInDb = findSubjectBySubjectId(newSubject.getSubjectId());

        SubjectMapper subjectMapper = SubjectMapper.INSTANCE;

        if (subjectInDb != null) {
            if (StringUtils.isNotBlank(newSubject.getSiteId()) && !newSubject.getSiteId().equals(subjectInDb.getSiteId())) {
                updateSiteId(newSubject.getSubjectId(), newSubject.getSiteId());
            }

            subjectMapper.updateFromDto(newSubject, subjectInDb);

            subjectInDb = update(subjectInDb);
        } else {
            subjectInDb = create(subjectMapper.fromDto(newSubject));
        }

        return subjectInDb;
    }

    @Override
    public Subject findSubjectBySubjectId(String subjectId) {
        return subjectDataService.findBySubjectId(subjectId);
    }

    @Override
    public List<Subject> findModifiedSubjects() {
        return subjectDataService.findByModified(true);
    }

    @Override
    public Subject create(Subject record) {
        return subjectDataService.create(record);
    }

    @Override
    public Subject update(Subject record) {
        return subjectDataService.update(record);
    }

    private void updateSiteId(String subjectId, String siteId) {
        if (StringUtils.isNotBlank(subjectId) && StringUtils.isNotBlank(siteId)) {
            Clinic clinic = clinicDataService.findByExactSiteId(siteId);

            if (clinic != null) {
                List<Visit> visits = visitDataService.findBySubjectId(subjectId);
                List<UnscheduledVisit> unscheduledVisits = unscheduledVisitDataService.findByParticipantId(subjectId);

                for (Visit details : visits) {
                    details.setClinic(clinic);
                    visitDataService.update(details);
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
