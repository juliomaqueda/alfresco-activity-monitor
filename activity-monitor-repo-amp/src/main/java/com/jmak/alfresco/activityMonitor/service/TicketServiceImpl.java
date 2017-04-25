package com.jmak.alfresco.activityMonitor.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;

@Service
public final class TicketServiceImpl implements TicketService {

	private static final Log LOG = LogFactory.getLog(TicketServiceImpl.class);

	private static final String SITE_CLAIM = "site";
	private static final String USER_CLAIM = "user";

	@Autowired
	private JwtService jwtService;


	@Override
	public boolean validateTicket(String ticket) {
		return Objects.nonNull(jwtService.parse(ticket));
	}

	@Override
	public String generateTicketForSite(String site) {
		final Map<String, Object> claims = new HashMap<>();
		claims.put(SITE_CLAIM, site);

		return generateTicketForClaims(claims);
	}

	@Override
	public String generateTicketForUser(String username) {
		final Map<String, Object> claims = new HashMap<>();
		claims.put(USER_CLAIM, username);

		return generateTicketForClaims(claims);
	}

	private String generateTicketForClaims(final Map<String, Object> claims) {
		LOG.debug("Generating activity monitor ticket...");

		final String loggedUser = AuthenticationUtil.getFullyAuthenticatedUser();
		final String ticket = jwtService.generate(loggedUser, claims);
		
		LOG.debug("Activity monitor ticket created for user " + loggedUser + ": " + ticket);

		return ticket;
	}

	@Override
	public String getSiteFromTicket(String ticket) {
		return (String) getClaimFromTicket(SITE_CLAIM, ticket);
	}

	@Override
	public String getUserFromTicket(String ticket) {
		return (String) getClaimFromTicket(USER_CLAIM, ticket);
	}

	private Object getClaimFromTicket(final String claim, final String ticket) {
		final Claims claims = jwtService.parse(ticket);

		if (Objects.nonNull(claims) && claims.containsKey(claim)) {
			return claims.get(claim);
		}

		return null;
	}
}
