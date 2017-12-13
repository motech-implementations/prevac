package org.motechproject.prevac.service;

import org.motechproject.prevac.dto.CapacityReportDto;
import org.motechproject.prevac.web.domain.GridSettings;

import java.util.List;

public interface ReportService {

    List<CapacityReportDto> generateCapacityReports(GridSettings settings);
}
