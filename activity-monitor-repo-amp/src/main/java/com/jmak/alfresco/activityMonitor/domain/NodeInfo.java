package com.jmak.alfresco.activityMonitor.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class NodeInfo {

	private final String name;
	private final String nodeRef;
	private final String description;
	private final String type;
	private final String location;


	public NodeInfo (final String name, final String nodeRef, final String description, final String type, final String location) {
		this.name = name;
		this.nodeRef = nodeRef;
		this.description = description;
		this.type = type;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public String getNodeRef() {
		return nodeRef;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	public String getLocation() {
		return location;
	}
}
