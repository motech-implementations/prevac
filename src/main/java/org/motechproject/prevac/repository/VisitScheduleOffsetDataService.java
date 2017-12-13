package org.motechproject.prevac.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.prevac.domain.VisitScheduleOffset;
import org.motechproject.prevac.domain.enums.VisitType;

import java.util.List;

public interface VisitScheduleOffsetDataService extends MotechDataService<VisitScheduleOffset> {

    @Lookup
    VisitScheduleOffset findByVisitTypeAndStageId(
            @LookupField(name = "visitType") VisitType visitType,
            @LookupField(name = "stageId") Long stageId);

    @Lookup
    List<VisitScheduleOffset> findByStageId(@LookupField(name = "stageId") Long stageId);
}
