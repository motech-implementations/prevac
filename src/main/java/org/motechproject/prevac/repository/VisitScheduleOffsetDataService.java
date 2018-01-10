package org.motechproject.prevac.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.prevac.domain.VisitScheduleOffset;
import org.motechproject.prevac.domain.enums.VisitType;

public interface VisitScheduleOffsetDataService extends MotechDataService<VisitScheduleOffset> {

    @Lookup
    VisitScheduleOffset findByVisitType(
            @LookupField(name = "visitType") VisitType visitType);
}
