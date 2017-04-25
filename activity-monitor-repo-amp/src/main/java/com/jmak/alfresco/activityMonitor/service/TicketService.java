package com.jmak.alfresco.activityMonitor.service;

public interface TicketService {

	boolean validateTicket(String ticket);

	String generateTicketForSite(String site);

	String generateTicketForUser(String username);

	String getSiteFromTicket(String ticket);

	String getUserFromTicket(String ticket);
}
