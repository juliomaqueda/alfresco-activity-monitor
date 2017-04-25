package com.jmak.alfresco.activityMonitor.kafka;

import javax.websocket.Session;

import org.springframework.stereotype.Service;

import com.jmak.alfresco.activityMonitor.domain.ActivityConsumer;
import com.jmak.alfresco.activityMonitor.service.ActivityConsumerService;

@Service
public final class ActivityConsumerServiceImpl implements ActivityConsumerService {
	
	private static final String KAFKA_SITES_TOPIC_PREFIX = "site-activity-";
	private static final String KAFKA_USERS_TOPIC_PREFIX = "user-activity-";

	@Override
	public ActivityConsumer createActivityConsumerForSite(String site, Session session) {
		return createActivityConsumer(KAFKA_SITES_TOPIC_PREFIX + site, session);
	}

	@Override
	public ActivityConsumer createActivityConsumerForUser(String username, Session session) {
		return createActivityConsumer(KAFKA_USERS_TOPIC_PREFIX + username, session);
	}

	private ActivityConsumer createActivityConsumer(final String topic, final Session session) {
		return new KafkaActivityConsumer(topic, session);
	}

	@Override
	public boolean serviceAvailable() {
		// TODO verify service status
		return true;
	}
}
