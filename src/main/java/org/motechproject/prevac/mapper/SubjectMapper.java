package org.motechproject.prevac.mapper;

import org.joda.time.LocalDate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.web.domain.SubjectZetesDto;

@Mapper(uses = { EnumsMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SubjectMapper {

    public abstract Subject fromDto(SubjectZetesDto subjectZetesDto);

    public abstract void updateFromDto(SubjectZetesDto subjectZetesDto, @MappingTarget Subject subject);

    @AfterMapping
    protected void calculateYearOfBirth(SubjectZetesDto dto, @MappingTarget Subject subject) {
        Integer currentYear = new LocalDate().getYear();
        subject.setYearOfBirth(currentYear - dto.getAge());
    }

}
