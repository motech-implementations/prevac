package org.motechproject.prevac.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.motechproject.prevac.domain.Screening;
import org.motechproject.prevac.dto.ScreeningDto;

@Mapper(uses = { DateAndTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScreeningMapper {

    ScreeningMapper INSTANCE = Mappers.getMapper(ScreeningMapper.class);

    @Mappings({
            @Mapping(target = "volunteerId", source = "volunteer.id"),
            @Mapping(target = "clinicId", source = "clinic.id")
    })
    ScreeningDto toDto(Screening screening);
}
