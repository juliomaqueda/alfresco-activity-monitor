package com.jmak.alfresco.activityMonitor.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;

import com.jmak.alfresco.activityMonitor.domain.ActivityConsumer;
import com.jmak.alfresco.activityMonitor.domain.ActivityMessage;
import com.jmak.alfresco.activityMonitor.service.ActivityConsumerService;
import com.jmak.alfresco.activityMonitor.service.TicketService;
import com.jmak.alfresco.activityMonitor.websocket.common.JsonEncoder;
import com.jmak.alfresco.activityMonitor.websocket.common.WebSocketConfigurator;

@ServerEndpoint(
	value = "/activity-monitor",
	configurator = WebSocketConfigurator.class,
	encoders = {JsonEncoder.class}
)
public final class ActivityMonitorWebSocket {

	private static final String TICKET_PARAM = "ticket";
	private static final Map<String, ActivityConsumer> SESSION_MAP = new HashMap<>();

	@Autowired
	private TicketService ticketService;

	@Autowired
	private ActivityConsumerService activityConsumerService;


	@OnOpen
	public void onOpen(Session peer) throws IOException, EncodeException {
		ActivityConsumer consumer = null;

		final Map<String, List<String>> params = peer.getRequestParameterMap();

		if (Objects.nonNull(peer.getId()) && params.containsKey(TICKET_PARAM)) {
			final String ticket = params.get(TICKET_PARAM).get(0);

			if (activityConsumerService.serviceAvailable() && ticketService.validateTicket(ticket)) {
				final String site = ticketService.getSiteFromTicket(ticket);

				if (Objects.nonNull(site)) {
					consumer = activityConsumerService.createActivityConsumerForSite(site, peer);
				}
				else {
					final String user = ticketService.getUserFromTicket(ticket);

					if (Objects.nonNull(user)) {
						consumer = activityConsumerService.createActivityConsumerForUser(user, peer);
					}
				}

				if (Objects.nonNull(site)) {
					SESSION_MAP.put(peer.getId(), consumer);
					consumer.start();
				}
			}
		}

		if (Objects.isNull(consumer)) {
			peer.getBasicRemote().sendObject(new ActivityMessage.StatusMessage().build(ActivityMessage.Status.error));
			peer.close();
		}
	}

	@OnMessage
	public void onMessage(String message) {}

	@OnClose
	public void onClose(Session peer) {
		final ActivityConsumer activeConsumer = SESSION_MAP.get(peer.getId());

		if (Objects.nonNull(activeConsumer)) {
			SESSION_MAP.remove(peer.getId());
			activeConsumer.finalize();
		}
	}
}
