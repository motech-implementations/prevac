package org.motechproject.prevac.service;

import org.motechproject.prevac.domain.VisitScheduleOffset;
import org.motechproject.prevac.domain.enums.VisitType;

import java.util.List;
import java.util.Map;


public interface VisitScheduleOffsetService {

    VisitScheduleOffset findByVisitType(VisitType visitType);

    List<VisitScheduleOffset> getAll();

    Map<VisitType, VisitScheduleOffset> getAllAsMap();
}
