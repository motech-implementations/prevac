package org.motechproject.prevac.service;

import org.joda.time.LocalDate;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.web.domain.SubjectZetesDto;

import java.util.List;

/**
 * Service interface for CRUD on Subject
 */
public interface SubjectService {

    Subject createOrUpdateForZetes(SubjectZetesDto newSubject);

    List<Subject> findSubjectByName(String name);

    Subject findSubjectBySubjectId(String subjectId);

    Subject findSubjectById(Long id);

    List<Subject> findByStageId(Long stageId);

    List<Subject> findModifiedSubjects();

    List<Subject> getAll();

    void delete(Subject record);

    void deleteAll();

    Subject create(Subject record);

    Subject update(Subject record);

    List<Subject> findSubjectsPrimerVaccinatedAtDay(LocalDate date);

    List<Subject> findSubjectsBoosterVaccinatedAtDay(LocalDate date);

    LocalDate findOldestPrimerVaccinationDate();
}
