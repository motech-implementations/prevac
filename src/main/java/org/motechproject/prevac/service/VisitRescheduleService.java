package org.motechproject.prevac.service;

import org.motechproject.prevac.dto.VisitRescheduleDto;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;

import java.io.IOException;

public interface VisitRescheduleService {

    Records<VisitRescheduleDto> getVisitsRecords(GridSettings settings) throws IOException;

    VisitRescheduleDto saveVisitReschedule(VisitRescheduleDto visitRescheduleDto,
                                           Boolean ignoreLimitation);
}
