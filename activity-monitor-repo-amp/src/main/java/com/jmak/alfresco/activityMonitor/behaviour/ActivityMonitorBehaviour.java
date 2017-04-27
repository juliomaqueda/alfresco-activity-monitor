package com.jmak.alfresco.activityMonitor.behaviour;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.jmak.alfresco.activityMonitor.domain.ActivityMessage;
import com.jmak.alfresco.activityMonitor.domain.ActivityMessage.ActionMessage;
import com.jmak.alfresco.activityMonitor.service.MessageProducerService;
import com.jmak.alfresco.activityMonitor.util.NodeUtils;

@Service
public final class ActivityMonitorBehaviour implements OnCreateNodePolicy, OnUpdatePropertiesPolicy, BeforeDeleteNodePolicy {

	private static final Log LOG = LogFactory.getLog(ActivityMonitorBehaviour.class);

	@Autowired
	private PolicyComponent policyComponent;

	@Autowired
	@Qualifier("nodeService")
	private NodeService nodeService;

	@Autowired
	private MessageProducerService messageService;

	@Autowired
	private NodeUtils nodeUtils;

	@Resource(name = "nonCheckedProperties")
    private List<QName> nonCheckedProperties;


	@PostConstruct
	public void init() {
		policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, OnCreateNodePolicy.QNAME.getLocalName(), Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_CONTENT,
				new JavaBehaviour(this, OnUpdatePropertiesPolicy.QNAME.getLocalName(), Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, BeforeDeleteNodePolicy.QNAME.getLocalName(), Behaviour.NotificationFrequency.FIRST_EVENT));
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		final NodeRef nodeRef = childAssocRef.getChildRef();

		try {
			if (nodeService.exists(nodeRef)) {
				final QName nodeType = nodeService.getType(nodeRef);
				if (!nodeType.equals(ContentModel.TYPE_THUMBNAIL)) {
					final ActivityMessage message = new ActivityMessage.ActionMessage(ActivityMessage.Actions.create)
							.overNode(nodeUtils.getNodeInfo(nodeRef))
							.issuedBy((String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR))
							.issuedAt((Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED))
							.onSite(nodeUtils.getSiteName(nodeRef))
							.build();

					messageService.publish(message);
				}
			}
		}
		catch (Exception e) {
			LOG.error("An error occurred while generating the ActivityMessage for new node " + nodeRef, e);
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		try {
			if (nodeService.exists(nodeRef)) {
				final QName nodeType = nodeService.getType(nodeRef);
				if (!nodeType.equals(ContentModel.TYPE_THUMBNAIL)) {
					final Map<String, Pair<String, String>> updatedProperties = getUpdatedProperties(before, after);

					if (!updatedProperties.isEmpty()) {
						final ActionMessage message = new ActivityMessage.ActionMessage(ActivityMessage.Actions.update)
								.overNode(nodeUtils.getNodeInfo(nodeRef))
								.issuedBy((String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR))
								.issuedAt((Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED))
								.onSite(nodeUtils.getSiteName(nodeRef));

						for (final Entry<String, Pair<String, String>> updatedProperty : updatedProperties.entrySet()) {
							message.withPropertyChange(updatedProperty.getKey(), updatedProperty.getValue().getFirst(), updatedProperty.getValue().getSecond());
						}

						messageService.publish(message.build());
					}
				}
			}
		}
		catch (Exception e) {
			LOG.error("An error occurred while generating the ActivityMessage for deleted node " + nodeRef, e);
		}
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		try {
			if (nodeService.exists(nodeRef)) {
				final QName nodeType = nodeService.getType(nodeRef);
				if (!nodeType.equals(ContentModel.TYPE_THUMBNAIL)) {
					final ActivityMessage message = new ActivityMessage.ActionMessage(ActivityMessage.Actions.delete)
							.overNode(nodeUtils.getNodeInfo(nodeRef))
							.issuedBy((String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR))
							.issuedAt((Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED))
							.onSite(nodeUtils.getSiteName(nodeRef))
							.build();
					
					messageService.publish(message);
				}
			}
		}
		catch (Exception e) {
			LOG.error("An error occurred while generating the ActivityMessage for deleted node " + nodeRef, e);
		}
	}

	private Map<String, Pair<String, String>> getUpdatedProperties(final Map<QName, Serializable> before, final Map<QName, Serializable> after) {
		final Map<String, Pair<String, String>> editedProperties = new HashMap<>();

		for (Entry<QName, Serializable> currentProperty : after.entrySet()) {
			if (!nonCheckedProperties.contains(currentProperty.getKey())) {
				//Edited properties
				if (before.containsKey(currentProperty.getKey())) {
					final Serializable newProperty = currentProperty.getValue();
					final Serializable oldProperty = before.get(currentProperty.getKey());

					final String newValue = Objects.nonNull(newProperty)? newProperty.toString() : null;
					final String oldValue = Objects.nonNull(oldProperty)? oldProperty.toString() : null;

					if (Objects.nonNull(newValue) || Objects.nonNull(oldValue)) {
						if (Objects.isNull(newValue) || !newValue.equals(oldValue)) {
							editedProperties.put(currentProperty.getKey().getLocalName(), new Pair<String, String>(oldValue, newValue));
						}
					}
				}
				//Added properties
				else {
					final Serializable newProperty = currentProperty.getValue();
					
					if (Objects.nonNull(newProperty)) {
						editedProperties.put(currentProperty.getKey().getLocalName(), new Pair<String, String>(null, newProperty.toString()));
					}
				}
			}
		}

		for (Entry<QName, Serializable> currentProperty : before.entrySet()) {
			//Removed properties
			if (!nonCheckedProperties.contains(currentProperty.getKey()) && !after.containsKey(currentProperty.getKey())) {
				final Serializable oldProperty = currentProperty.getValue();

				if (Objects.nonNull(oldProperty)) {
					editedProperties.put(currentProperty.getKey().getLocalName(), new Pair<String, String>(oldProperty.toString(), null));
				}
			}
		}

		return editedProperties;
	}
}
