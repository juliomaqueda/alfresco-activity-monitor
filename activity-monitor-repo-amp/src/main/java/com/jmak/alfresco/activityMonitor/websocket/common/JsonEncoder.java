package com.jmak.alfresco.activityMonitor.websocket.common;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmak.alfresco.activityMonitor.domain.ActivityMessage;
import com.jmak.alfresco.activityMonitor.domain.NodeInfo;

public final class JsonEncoder implements Encoder.TextStream<ActivityMessage> {

	private static final Log LOG = LogFactory.getLog(JsonEncoder.class);

	private static ObjectMapper mapper;


	@Override
	public void init(EndpointConfig config) {}

	@Override
	public void encode(ActivityMessage activityMessage, Writer writer) throws EncodeException, IOException {
		getMapper().writeValue(writer, activityMessage);
	}

	public static String encode(final ActivityMessage activityMessage) {
		String message = null;

		try {
			message = getMapper().writeValueAsString(activityMessage);
		} catch (JsonProcessingException e) {
			LOG.error("The activity message could not be encoded");
		}

		return message;
	}

	@Override
	public void destroy() {}

	private static ObjectMapper getMapper() {
		if (mapper == null) {
			mapper = (new ThreadLocal<ObjectMapper>() {
				@Override
				protected ObjectMapper initialValue() {
					return new ObjectMapper();
				}
			}).get();
		}

		return mapper;
	}

	//TODO delete asap
	public static void main(String[] args) throws JsonProcessingException {
		JsonEncoder enc = new JsonEncoder();

		ActivityMessage msg = new ActivityMessage.ActionMessage(ActivityMessage.Actions.update)
				.issuedBy("Pepe")
				.issuedAt(new Date())
				.withPropertyChange("name", "nombre antiguo", "nombre nuevo")
				.withPropertyChange("desc", "desc antigua", "desc nueva")
				.overNode(new NodeInfo("name", "nodeRef", null, "type", ""))
				.build();
		
		String cad = enc.getMapper().writeValueAsString(msg);
		
		System.out.println(cad);
	}
}
