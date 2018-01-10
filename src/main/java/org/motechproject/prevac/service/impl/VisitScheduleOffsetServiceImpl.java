package org.motechproject.prevac.service.impl;

import org.motechproject.prevac.domain.VisitScheduleOffset;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.repository.VisitScheduleOffsetDataService;
import org.motechproject.prevac.service.VisitScheduleOffsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("visitScheduleOffsetService")
public class VisitScheduleOffsetServiceImpl implements VisitScheduleOffsetService {

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

        if (visitScheduleOffsetList != null) {
            for (VisitScheduleOffset offset : visitScheduleOffsetList) {
                visitTypeMap.put(offset.getVisitType(), offset);
            }
        }

        return visitTypeMap;
    }
}
