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

/**
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class JobStatusHandler extends GenericDAOJPAImpl<JobStatus, Long> {

    public JobStatus findJobStatusByIdentityId(String identityId) {
        if (identityId == null || identityId.isEmpty()) {
            return null;
        }
        EntityManager em = getEntityManager();

        //System.out.println("Transaction isActive ? "+em.getTransaction().isActive());
        //EntityTransaction et = em.getTransaction();
        //et.begin();
        Query query = em.createNamedQuery("JobStatus.findJobStatusByIdentityId", JobStatus.class);
        //et.commit();
        query.setParameter("identityId", identityId);
        try {
            return (JobStatus)query.getSingleResult();
        } catch(NoResultException e) {
            return null;
        } catch (PersistenceException e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean resetStatus() {
        deleteAll();
        return true;
    }
}

