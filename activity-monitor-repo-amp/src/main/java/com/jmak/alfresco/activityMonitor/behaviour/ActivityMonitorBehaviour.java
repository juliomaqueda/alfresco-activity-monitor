package com.jmak.alfresco.activityMonitor.behaviour;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.jmak.alfresco.activityMonitor.domain.ActivityMessage;
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

		if (nodeService.exists(nodeRef)) {
			final QName nodeType = nodeService.getType(nodeRef);
			if (!nodeType.equals(ContentModel.TYPE_THUMBNAIL)) {
				try {
					final ActivityMessage message = new ActivityMessage.ActionMessage(ActivityMessage.Actions.create)
							.overNode(nodeUtils.getNodeInfo(nodeRef))
							.issuedBy((String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR))
							.issuedAt((Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED))
							.onSite(nodeUtils.getSiteName(nodeRef))
							.build();

					messageService.publish(message);
				}
				catch (Exception e) {
					LOG.error("An error occurred while generating the ActivityMessage for new node " + nodeRef, e);
				}
			}
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (nodeService.exists(nodeRef)) {

		}
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		if (nodeService.exists(nodeRef)) {
			final QName nodeType = nodeService.getType(nodeRef);
			if (!nodeType.equals(ContentModel.TYPE_THUMBNAIL)) {
				try {
					final ActivityMessage message = new ActivityMessage.ActionMessage(ActivityMessage.Actions.delete)
							.overNode(nodeUtils.getNodeInfo(nodeRef))
							.issuedBy((String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR))
							.issuedAt((Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED))
							.onSite(nodeUtils.getSiteName(nodeRef))
							.build();
					
					messageService.publish(message);
				}
				catch (Exception e) {
					LOG.error("An error occurred while generating the ActivityMessage for deleted node " + nodeRef, e);
				}
			}
		}
	}
}
