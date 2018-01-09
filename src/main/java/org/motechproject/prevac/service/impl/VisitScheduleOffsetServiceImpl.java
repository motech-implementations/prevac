package org.motechproject.prevac.service.impl;

import org.motechproject.prevac.domain.VisitScheduleOffset;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.exception.VisitScheduleException;
import org.motechproject.prevac.repository.VisitScheduleOffsetDataService;
import org.motechproject.prevac.service.VisitScheduleOffsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("visitScheduleOffsetService")
public class VisitScheduleOffsetServiceImpl implements VisitScheduleOffsetService {

    private static final int VISIT_SCHEDULE_OFFSET_SIZE = 12;

    @Autowired
    private VisitScheduleOffsetDataService visitScheduleOffsetDataService;

    @Override
    public VisitScheduleOffset findByVisitType(VisitType visitType) {
        return visitScheduleOffsetDataService.findByVisitType(visitType);
    }

    @Override
    public List<VisitScheduleOffset> getAll() {
        return visitScheduleOffsetDataService.retrieveAll();
    }

    @Override
    public Map<VisitType, VisitScheduleOffset> getAllAsMap() {
        Map<VisitType, VisitScheduleOffset> visitTypeMap = new HashMap<>();
        List<VisitScheduleOffset> visitScheduleOffsetList = visitScheduleOffsetDataService.retrieveAll();

        if (visitScheduleOffsetList == null || visitScheduleOffsetList.isEmpty()) {
            throw new VisitScheduleException("Visit Schedule Offsets don't exist");
        } else if (visitScheduleOffsetList.size() != VISIT_SCHEDULE_OFFSET_SIZE) {
            throw new VisitScheduleException("There should be exact 12 Visit Schedule Offsets");
        }

        for (VisitScheduleOffset offset : visitScheduleOffsetList) {
            visitTypeMap.put(offset.getVisitType(), offset);
        }

        return visitTypeMap;
    }
}
