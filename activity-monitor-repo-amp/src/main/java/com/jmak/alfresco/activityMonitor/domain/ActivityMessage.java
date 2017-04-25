package com.jmak.alfresco.activityMonitor.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.util.Pair;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ActivityMessage {
	
	private enum Types { status, message }
	public enum Status { connected, disconnected, error }
	public enum Actions { create, update, delete, navigation }

	private Types type;
	private Status status;
	private Actions action;
	private Date issuedAt;
	private String author;
	private String site;
	private NodeInfo node;
	private Map<String, Pair<String, String>> properties;

	private ActivityMessage () {}

	public Types getType() {
		return type;
	}

	public Status getStatus() {
		return status;
	}

	public Actions getAction() {
		return action;
	}

	public Date getIssuedAt() {
		return issuedAt;
	}

	public String getAuthor() {
		return author;
	}

	public String getSite() {
		return site;
	}

	public NodeInfo getNode() {
		return node;
	}

	public Map<String, Pair<String, String>> getProperties() {
		return properties;
	}


	public static final class ActionMessage {

		private Actions action;
		private Date issuedAt;
		private String author;
		private String site;
		private NodeInfo node;
		private Map<String, Pair<String, String>> properties;

		public ActionMessage (Actions action) {
			this.action = action;
		}

		public ActionMessage issuedAt(final Date issued) {
			this.issuedAt = issued;
			return this;
		}
		
		public ActionMessage issuedBy(final String author) {
			this.author = author;
			return this;
		}

		public ActionMessage onSite(final String site) {
			this.site = site;
			return this;
		}

		public ActionMessage overNode(final NodeInfo nodeInfo) {
			this.node = nodeInfo;
			return this;
		}

		public ActionMessage withPropertyChange(final String key, final String oldValue, final String newValue) {
			if (Objects.isNull(properties)) {
				properties = new HashMap<>();
			}

			properties.put(key, new Pair<>(oldValue, newValue));

			return this;
		}

		public ActivityMessage build() {
			final ActivityMessage message = new ActivityMessage();
			message.type = Types.message;
			message.action = action;
			message.issuedAt = issuedAt;
			message.author = author;
			message.site = site;
			message.node = node;
			message.properties = properties;

			return message;
		}
	}


	public static final class StatusMessage {

		public ActivityMessage build(final Status status) {
			final ActivityMessage message = new ActivityMessage();
			message.type = Types.status;
			message.status = status;

			return message;
		}
	}
}
