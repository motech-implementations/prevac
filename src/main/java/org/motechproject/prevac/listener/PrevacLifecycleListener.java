package org.motechproject.prevac.listener;

import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
import org.motechproject.prevac.domain.Clinic;

public interface PrevacLifecycleListener {

    @InstanceLifecycleListener(value = InstanceLifecycleListenerType.POST_CREATE)
    void addClinicToVisitBookingDetails(Clinic clinic);
}
