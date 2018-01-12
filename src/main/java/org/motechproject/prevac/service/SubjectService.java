package org.motechproject.prevac.service;

import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.web.domain.SubjectZetesDto;

import java.util.List;

/**
 * Service interface for CRUD on Subject
 */
public interface SubjectService {

    Subject createOrUpdateForZetes(SubjectZetesDto newSubject);

    Subject findSubjectBySubjectId(String subjectId);

    List<Subject> findModifiedSubjects();

    Subject create(Subject record);

    Subject update(Subject record);
}
