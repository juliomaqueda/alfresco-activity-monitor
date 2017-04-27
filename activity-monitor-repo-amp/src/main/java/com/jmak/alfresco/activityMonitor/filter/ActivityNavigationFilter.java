package com.jmak.alfresco.activityMonitor.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jmak.alfresco.activityMonitor.domain.ActivityMessage;
import com.jmak.alfresco.activityMonitor.domain.NodeInfo;
import com.jmak.alfresco.activityMonitor.service.MessageProducerService;

//@Component("activityNavigation")
//@WebFilter(urlPatterns = {"/s/slingshot/doclib2/doclist/all/*"}, description = "Activity monitor filter")
public class ActivityNavigationFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(ActivityNavigationFilter.class);

	private static final String FILTERED_URL = "/s/slingshot/doclib2/doclist/all/";

	@Autowired
	private MessageProducerService messageService;


	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) req;

		try {
			final String loggedUser = AuthenticationUtil.getFullyAuthenticatedUser();

			final ActivityMessage message = new ActivityMessage.ActionMessage(ActivityMessage.Actions.navigation)
					.issuedBy(loggedUser)
					.issuedAt(new Date())
					.overNode(getNodeInfo(request))
					.build();

			messageService.publish(message);
		}
		catch (Exception e) {
			LOG.error("An error occurred while generating the ActivityMessage for navigation", e);
		}

		chain.doFilter(request, res);
	}

	@Override
	public void destroy() {}

	private NodeInfo getNodeInfo(final HttpServletRequest request) {
		NodeInfo nodeInfo = null;

		try {
			String requestedPath = request.getRequestURI().replace(request.getContextPath() + FILTERED_URL, "");
			requestedPath = URLDecoder.decode(requestedPath, Charsets.UTF_8.name());

			//TODO no comments...
			nodeInfo = new NodeInfo("", null, null, null, requestedPath);
		}
		catch (Exception e) {
			LOG.error("An error occurred while generating the ActivityMessage for navigation", e);
		}

		return nodeInfo; 
	}
}
