package org.motechproject.prevac.service;

import org.motechproject.prevac.domain.Screening;
import org.motechproject.prevac.dto.ScreeningDto;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;

import java.io.IOException;

public interface ScreeningService {

    Records<Screening> getScreenings(GridSettings gridSettings) throws IOException;

    Screening addOrUpdate(ScreeningDto screeningDto, Boolean ignoreLimitation);

    ScreeningDto getScreeningById(Long id);

    void cancelScreening(Long id);

    void activateScreening(Long id, Boolean ignoreLimitation);

    void completeScreening(Long id);
}
