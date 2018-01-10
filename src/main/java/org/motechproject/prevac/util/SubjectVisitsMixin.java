package org.motechproject.prevac.util;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.util.serializer.CustomVisitListSerializer;

import java.util.List;

public abstract class SubjectVisitsMixin {

    @JsonSerialize(using = CustomVisitListSerializer.class)
    public abstract List<Visit> getVisits();
}
