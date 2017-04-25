package com.jmak.alfresco.activityMonitor.webscript;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jmak.alfresco.activityMonitor.service.TicketService;

@Service("webscript.com.jmak.alfresco.activityMonitor.monitor-ticket.get")
public final class GetMonitorTicketWebScript extends DeclarativeWebScript {

	private static final String SITE_REF_PARAM = "site";
	private static final String USER_REF_PARAM = "username";

	@Autowired
    @Qualifier("siteService")
    private SiteService siteService;

	@Autowired
	private PersonService personService;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private TicketService ticketService;


	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) {
		final String site = req.getParameter(SITE_REF_PARAM);
		final String username = req.getParameter(USER_REF_PARAM);

		String ticket = null;

		if (Objects.nonNull(site) && validateSitePermissions(site)) {
			ticket = ticketService.generateTicketForSite(site);
		}
		else if (Objects.nonNull(username) && validateUserPermissions(username)) {
			ticket = ticketService.generateTicketForUser(username);
		}

		Assert.notNull(ticket, "User has no permissions to access the requested resources.");

		final Map<String, Object> model = new HashMap<>();
		model.put("ticket", ticket);

		return model;
	}

	private boolean validateSitePermissions(final String site) {
		Assert.notNull(siteService.getSite(site), "Site " + site + " does not exist.");
		return authorityService.hasAdminAuthority() || siteService.isSiteAdmin(site);
	}

	private boolean validateUserPermissions(final String username) {
		Assert.isTrue(personService.personExists(username), "User " + username + " does not exist.");
		return authorityService.hasAdminAuthority();
	}
}
