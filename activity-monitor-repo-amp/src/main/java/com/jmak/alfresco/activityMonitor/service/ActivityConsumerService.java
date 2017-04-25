package com.jmak.alfresco.activityMonitor.service;

import javax.websocket.Session;

import com.jmak.alfresco.activityMonitor.domain.ActivityConsumer;

public interface ActivityConsumerService {

	boolean serviceAvailable();

	ActivityConsumer createActivityConsumerForSite(String site, Session session);

	ActivityConsumer createActivityConsumerForUser(String username, Session session);
}
