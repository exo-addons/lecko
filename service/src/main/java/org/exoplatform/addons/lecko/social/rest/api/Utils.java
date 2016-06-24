package org.exoplatform.addons.lecko.social.rest.api;

import org.exoplatform.services.security.ConversationState;

public class Utils {
	
	 public static final String API_ACCESS_GROUP    = "/platform/api-access";
	 
	  /**
	   * Check if the authenticated user is a member of the api access group
	   * 
	   * @return boolean
	   */
	  public static boolean isMemberOfAPIAccessGroup() {
	    return ConversationState.getCurrent().getIdentity().isMemberOf(API_ACCESS_GROUP);
	  }

}
