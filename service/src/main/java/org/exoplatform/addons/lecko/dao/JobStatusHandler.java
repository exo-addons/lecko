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
/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.addons.lecko.dao;

import javax.persistence.*;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class JobStatusHandler extends GenericDAOJPAImpl<JobStatus, Long> {

  private static final Log LOG = ExoLogger.getLogger("org.exoplatform.addons.lecko.dao.JobStatusHandler");


  public JobStatus findJobStatusByIdentityId(String identityId) {
    if (identityId == null || identityId.isEmpty()) {
      return null;
    }
    EntityManager em = getEntityManager();

    // System.out.println("Transaction isActive ?
    // "+em.getTransaction().isActive());
    // EntityTransaction et = em.getTransaction();
    // et.begin();
    Query query = em.createNamedQuery("JobStatus.findJobStatusByIdentityId", JobStatus.class);
    // et.commit();
    query.setParameter("identityId", identityId);
    try {
      return (JobStatus) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (PersistenceException e) {

      LOG.error("Exception when accessing DB : {}",e);
      return null;
    }
  }

  public boolean resetStatus() {
    deleteAll();
    return true;
  }

  public JobStatus findJobStatusByIdentityIdAndProvider(String identityId, String providerId) {
    if (identityId == null || identityId.isEmpty()) {
      return null;
    }
    EntityManager em = getEntityManager();

    // System.out.println("Transaction isActive ?
    // "+em.getTransaction().isActive());
    // EntityTransaction et = em.getTransaction();
    // et.begin();
    Query query = em.createNamedQuery("JobStatus.findJobStatusByIdentityIdAndProviderId", JobStatus.class);
    // et.commit();
    query.setParameter("identityId", identityId);
    query.setParameter("providerId",providerId);
    try {
      return (JobStatus) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (PersistenceException e) {

      LOG.error("Exception when accessing DB : {}",e);
      return null;
    }
  }
}
