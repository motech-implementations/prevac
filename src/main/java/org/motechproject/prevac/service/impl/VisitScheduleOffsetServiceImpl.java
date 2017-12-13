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
    public VisitScheduleOffset findByVisitTypeAndStageId(VisitType visitType, Long stageId) {
        return visitScheduleOffsetDataService.findByVisitTypeAndStageId(visitType, stageId);
    }

    @Override
    public List<VisitScheduleOffset> getAll() {
        return visitScheduleOffsetDataService.retrieveAll();
    }

    @Override
    public Map<VisitType, VisitScheduleOffset> getAsMapByStageId(Long stageId) {
        Map<VisitType, VisitScheduleOffset> visitScheduleOffsetMap = new HashMap<>();
        List<VisitScheduleOffset> visitScheduleOffsetList = visitScheduleOffsetDataService.findByStageId(stageId);

        if (visitScheduleOffsetList != null) {
            for (VisitScheduleOffset offset : visitScheduleOffsetList) {
                visitScheduleOffsetMap.put(offset.getVisitType(), offset);
            }
        }

        return visitScheduleOffsetMap;
    }

    @Override
    public Map<Long, Map<VisitType, VisitScheduleOffset>> getAllAsMap() {
        Map<Long, Map<VisitType, VisitScheduleOffset>> visitScheduleOffsetMap = new HashMap<>();
        Map<VisitType, VisitScheduleOffset> visitTypeMap;
        List<VisitScheduleOffset> visitScheduleOffsetList = visitScheduleOffsetDataService.retrieveAll();

        if (visitScheduleOffsetList != null) {
            for (VisitScheduleOffset offset : visitScheduleOffsetList) {
                visitTypeMap = visitScheduleOffsetMap.get(offset.getStageId());

                if (visitTypeMap == null) {
                    visitTypeMap = new HashMap<>();
                    visitScheduleOffsetMap.put(offset.getStageId(), visitTypeMap);
                }

                visitTypeMap.put(offset.getVisitType(), offset);
            }
        }

        return visitScheduleOffsetMap;
    }
}
