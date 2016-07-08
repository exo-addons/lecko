package org.exoplatform.addons.lecko;

import org.exoplatform.addons.lecko.dao.JobStatus;
import org.exoplatform.addons.lecko.dao.JobStatusHandler;
import org.picocontainer.Startable;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 22/06/16.
 *
 * Service used to store and check if an entity is already treaten in the current dump
 */
public class JobStatusService implements Startable {

    private JobStatusHandler jobStatusHandler;

    public JobStatusService(){
        jobStatusHandler=new JobStatusHandler();
    }

    public void storeStatus(String identityId) {
        JobStatus jobStatus = new JobStatus();
        jobStatus.setIdentityId(identityId);
        jobStatusHandler.create(jobStatus);
    }


    public JobStatus findByIdentityId(String identityId){
        return jobStatusHandler.findJobStatusByIdentityId(identityId);
    }


    public boolean resetStatus() {
        return jobStatusHandler.resetStatus();
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public Long countStatus()  {
        return jobStatusHandler.count();

    }
}
