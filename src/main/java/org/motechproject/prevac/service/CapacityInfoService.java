package org.motechproject.prevac.service;

import org.motechproject.prevac.dto.CapacityInfoDto;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;

public interface CapacityInfoService {

    Records<CapacityInfoDto> getCapacityInfoRecords(GridSettings settings);
}
