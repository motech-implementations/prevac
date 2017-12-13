package org.motechproject.prevac.service;

import org.motechproject.prevac.dto.UnscheduledVisitDto;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;

import java.io.IOException;

public interface UnscheduledVisitService {

    Records<UnscheduledVisitDto> getUnscheduledVisitsRecords(GridSettings settings) throws IOException;

    UnscheduledVisitDto addOrUpdate(UnscheduledVisitDto unscheduledVisitDto,
                                    Boolean ignoreLimitation);
}
