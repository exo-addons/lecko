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
package org.exoplatform.addons.lecko.dao;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 22/06/16.
 */
@Entity(name = "JobStatus")
@ExoEntity
@Table(name = "LECKO_JOB_STATUS")
@NamedQueries({

    @NamedQuery(name = "JobStatus.findJobStatusByIdentityId", query = "SELECT j FROM JobStatus j WHERE j.identityId = :identityId"),
    @NamedQuery(name = "JobStatus.reset", query = "DELETE FROM JobStatus") })
public class JobStatus {

  @Id
  @SequenceGenerator(name = "SEQ_TASK_LECKO_JOB_STATUS_JOBSTATUS_ID", sequenceName = "SEQ_TASK_LECKO_JOB_STATUS_JOBSTATUS_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_TASK_LECKO_JOB_STATUS_JOBSTATUS_ID")
  @Column(name = "JOBSTATUS_ID")
  private long   id;

  @Column(name = "IDENTITY_ID")
  private String identityId;

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
