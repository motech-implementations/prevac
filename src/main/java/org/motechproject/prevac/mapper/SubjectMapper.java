package org.motechproject.prevac.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.web.domain.SubjectZetesDto;

@Mapper(uses = { EnumsMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {

    SubjectMapper INSTANCE = Mappers.getMapper(SubjectMapper.class);

    Subject fromDto(SubjectZetesDto subjectZetesDto);

    void updateFromDto(SubjectZetesDto subjectZetesDto, @MappingTarget Subject subject);
}
