package org.exoplatform.addons.lecko.dao;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 22/06/16.
 */
@Entity(name = "JobStatus")
@ExoEntity
@Table(name = "JOBSTATUS")
@NamedQueries({

        @NamedQuery(name = "JobStatus.findJobStatusByIdentityId",
                query = "SELECT j FROM JobStatus j WHERE j.identityId = :identityId"),
        @NamedQuery(name = "JobStatus.reset",
                query = "DELETE FROM JobStatus")
})
public class JobStatus {

    @Id
    @SequenceGenerator(name="SEQ_JOB_STATUS_ID", sequenceName="SEQ_JOB_STATUS_ID")
    @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_JOB_STATUS_ID")
    @Column(name = "JOBSTATUS_ID")
    private long id;


    @Column(name = "IDENTITY_ID")
    private String      identityId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }
}
