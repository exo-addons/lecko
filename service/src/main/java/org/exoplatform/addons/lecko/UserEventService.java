package org.exoplatform.addons.lecko;

import org.exoplatform.addons.lecko.dao.UserEvent;
import org.exoplatform.addons.lecko.dao.UserEventHandler;
import org.picocontainer.Startable;

import java.util.List;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 02/10/17.
 */
public class UserEventService implements Startable {

    private UserEventHandler userEventHandler;

    public UserEventService () {
        userEventHandler = new UserEventHandler();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public List<UserEvent> findEventsByObjectId(String objectId) {
        return userEventHandler.findEventsByObjectId(objectId);
    }

    public void storeEvent(String eventType) {

    }
}
