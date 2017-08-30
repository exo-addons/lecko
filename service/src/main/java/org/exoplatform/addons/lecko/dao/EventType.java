package org.exoplatform.addons.lecko.dao;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 30/08/17.
 */
@Entity(name = "EventType")
@ExoEntity
@Table(name = "USERS_EVENTS_TYPE")
@NamedQueries({

})
public class EventType {


    @Id
    @SequenceGenerator(name = "SEQ_USERS_EVENTS_TYPE_EVENT_TYPE_ID", sequenceName = "SEQ_USERS_EVENTS_TYPE_EVENT_TYPE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_USERS_EVENTS_TYPE_EVENT_TYPE_ID")
    @Column(name = "EVENT_TYPE_ID")
    private long   id;

    @Column(name = "NAME")
    private String name;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
