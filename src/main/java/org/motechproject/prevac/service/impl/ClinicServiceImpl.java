package org.motechproject.prevac.service.impl;

import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.repository.ClinicDataService;
import org.motechproject.prevac.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("clinicService")
public class ClinicServiceImpl implements ClinicService {

    @Autowired
    private ClinicDataService clinicDataService;

    @Override
    public List<Clinic> getClinics() {
        return clinicDataService.retrieveAll();
    }
}
