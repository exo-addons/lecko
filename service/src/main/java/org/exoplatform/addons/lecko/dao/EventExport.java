package org.exoplatform.addons.lecko.dao;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 30/08/17.
 */
@Entity(name = "EventExport")
@ExoEntity
@Table(name = "LECKO_EVENTS_EXPORT")
@NamedQueries({

})

public class EventExport {

    @Id
    @SequenceGenerator(name = "SEQ_LECKO_EVENTS_EXPORT_EVENT_EXPORT_ID", sequenceName = "SEQ_LECKO_EVENTS_EXPORT_EVENT_EXPORT_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_LECKO_EVENTS_EXPORT_EVENT_EXPORT_ID")
    @Column(name = "EVENT_EXPORT_ID")
    private long   id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private UserEvent event;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EXPORTED_DATE")
    private Date date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserEvent getEvent() {
        return event;
    }

    public void setEvent(UserEvent event) {
        this.event = event;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
