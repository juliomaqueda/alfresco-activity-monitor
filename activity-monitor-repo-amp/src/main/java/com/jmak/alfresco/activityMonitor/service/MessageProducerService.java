package com.jmak.alfresco.activityMonitor.service;

import com.jmak.alfresco.activityMonitor.domain.ActivityMessage;

public interface MessageProducerService {

	void publish(ActivityMessage message);
}
