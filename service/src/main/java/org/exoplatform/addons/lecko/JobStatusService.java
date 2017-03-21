/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This file is part of Lecko Analytics Add-on - Service.
 *
 * Lecko Analytics Add-on - Service is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * Lecko Analytics Add-on - Service software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Lecko Analytics Add-on - Service; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addons.lecko;

import org.exoplatform.addons.lecko.dao.JobStatus;
import org.exoplatform.addons.lecko.dao.JobStatusHandler;
import org.picocontainer.Startable;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 22/06/16.
 * Service used to store and check if an entity is already treaten in the
 * current dump
 */
public class JobStatusService implements Startable {

  private JobStatusHandler jobStatusHandler;

  public JobStatusService() {
    jobStatusHandler = new JobStatusHandler();
  }

  public void storeStatus(String identityId) {
    JobStatus jobStatus = new JobStatus();
    jobStatus.setIdentityId(identityId);
    jobStatusHandler.create(jobStatus);
  }

  public JobStatus findByIdentityId(String identityId) {
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

  public Long countStatus() {
    return jobStatusHandler.count();

  }
}
