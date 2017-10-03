package org.exoplatform.addons.lecko.dao;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 30/08/17.
 */
@Entity(name = "UserEvent")
@ExoEntity
@Table(name = "USERS_EVENTS")
@NamedQueries({
    @NamedQuery(name = "UserEvent.findEventsByObjectId", query = "SELECT ue FROM UserEvent ue WHERE ue.referenceObjectId = :referenceObjectId")

})
public class UserEvent {

    @Id
    @SequenceGenerator(name = "SEQ_USERS_EVENTS_EVENT_ID", sequenceName = "SEQ_USERS_EVENTS_EVENT_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_USERS_EVENTS_EVENT_ID")
    @Column(name = "EVENT_ID")
    private long   id;

    @Column(name = "REFERENCED_OBJECT_ID")
    private String referenceObjectId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE")
    private Date date;

    @Column(name = "USER_ID")
    private String userId;


    @Column(name = "EVENT_TYPE")
    private String eventType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReferenceObjectId() {
        return referenceObjectId;
    }

    public void setReferenceObjectId(String referenceObjectId) {
        this.referenceObjectId = referenceObjectId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public enum eventType {
        CREATE,LIKE;
    }
}
