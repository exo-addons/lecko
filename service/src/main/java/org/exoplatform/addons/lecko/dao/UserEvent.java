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

})
public class UserEvent {

    @Id
    @SequenceGenerator(name = "SEQ_USERS_EVENTS_EVENT_ID", sequenceName = "SEQ_USERS_EVENTS_EVENT_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_USERS_EVENTS_EVENT_ID")
    @Column(name = "EVENT_ID")
    private long   id;

    @Column(name = "REFERENCED_OBJECT_ID")
    private int referenceObjectId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE")
    private Date date;

    @Column(name = "USER_ID")
    private int userId;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_TYPE_ID", nullable = false)
    private EventType eventType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getReferenceObjectId() {
        return referenceObjectId;
    }

    public void setReferenceObjectId(int referenceObjectId) {
        this.referenceObjectId = referenceObjectId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
