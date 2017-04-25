package com.jmak.alfresco.activityMonitor.webscript;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ActivityMonitorGet extends DeclarativeWebScript {

	private static final Log LOGGER = LogFactory.getLog(ActivityMonitorGet.class);


	@Override
	protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
		final Map<String, Object> model = new HashMap<>();

		try {
			final String targetSite = "sample"; //req.getParameter("site");
//			final String targetUser = req.getParameter("username");

			model.put("site", targetSite);
//			model.put("username", targetUser);
		}
		catch (Exception e) {
			LOGGER.warn("Error while retrieving the node versions", e);
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error while retrieving the node versions", e);
		}

		return model;
	}
}
