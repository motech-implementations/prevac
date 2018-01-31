package org.motechproject.prevac.utils;


import org.joda.time.LocalDate;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.enums.VisitType;

public final class VisitUtil {

    private VisitUtil() {
    }

    public static Visit createVisit(Subject subject, VisitType type, LocalDate date,
                                    LocalDate projectedDate, String owner, Clinic clinic) {
        Visit visit = new Visit();
        visit.setSubject(subject);
        visit.setType(type);
        visit.setDate(date);
        visit.setDateProjected(projectedDate);
        visit.setOwner(owner);
        visit.setClinic(clinic);

        return visit;
    }
}
