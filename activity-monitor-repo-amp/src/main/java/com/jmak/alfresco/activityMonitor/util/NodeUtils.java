package com.jmak.alfresco.activityMonitor.util;

import java.util.Objects;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.jmak.alfresco.activityMonitor.domain.NodeInfo;

@Service
public class NodeUtils {

	@Autowired
	@Qualifier("nodeService")
	private NodeService nodeService;

	@Autowired
    @Qualifier("siteService")
    private SiteService siteService;

	@Autowired
	private PermissionService permissionService;


	public NodeInfo getNodeInfo(final NodeRef nodeRef) {
		if (nodeService.exists(nodeRef)) {
			final String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			final String description = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
			final String type = nodeService.getType(nodeRef).getLocalName();

			final Path nodePath = nodeService.getPath(nodeRef);nodeService.getType(nodeRef);
			final String location = nodePath.toDisplayPath(nodeService, permissionService);

			return new NodeInfo(name, nodeRef.toString(), description, type, location);
		}

		return null;
	}

	public String getSiteName(final NodeRef nodeRef) {
		final SiteInfo siteInfo = AuthenticationUtil.runAs(new RunAsWork<SiteInfo>() {
		    @Override
		    public SiteInfo doWork() throws Exception {
		        return siteService.getSite(nodeRef);
		    };
		}, AuthenticationUtil.getSystemUserName());

		return Objects.nonNull(siteInfo)? siteInfo.getShortName() : null;
	}
}
