package org.motechproject.prevac.service;

import org.motechproject.prevac.dto.PrimeVaccinationScheduleDto;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;

import java.io.IOException;
import java.util.List;

public interface PrimeVaccinationScheduleService {

    Records<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords(
            GridSettings settings) throws IOException;

    PrimeVaccinationScheduleDto createOrUpdateWithDto(PrimeVaccinationScheduleDto visitDto,
                                                      Boolean ignoreLimitation);

    List<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords();
}
