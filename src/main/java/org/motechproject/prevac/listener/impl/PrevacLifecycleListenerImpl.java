package org.motechproject.prevac.listener.impl;

import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.listener.PrevacLifecycleListener;
import org.motechproject.prevac.repository.VisitBookingDetailsDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("prevacLifecycleListener")
public class PrevacLifecycleListenerImpl implements PrevacLifecycleListener {

    @Autowired
    private VisitBookingDetailsDataService visitBookingDetailsDataService;

    @Override
    public void addClinicToVisitBookingDetails(Clinic clinic) {
        List<Visit> visits = visitBookingDetailsDataService.findByExactParticipantSiteId(clinic.getSiteId());

        for (Visit details : visits) {
            details.setClinic(clinic);
            visitBookingDetailsDataService.update(details);
        }
    }
}
