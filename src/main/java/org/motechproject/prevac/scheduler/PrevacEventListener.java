package org.motechproject.prevac.scheduler;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.service.ZetesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrevacEventListener {

    @Autowired
    private ZetesService zetesService;

    @MotechListener(subjects = { PrevacConstants.ZETES_UPDATE_EVENT })
    public void zetesUpdate(MotechEvent event) {
        Object zetesUrl = event.getParameters().get(PrevacConstants.ZETES_URL);
        Object username = event.getParameters().get(PrevacConstants.ZETES_USERNAME);
        Object password = event.getParameters().get(PrevacConstants.ZETES_PASSWORD);
        zetesService.sendUpdatedSubjects(zetesUrl.toString(), username.toString(), password.toString());
    }
}
