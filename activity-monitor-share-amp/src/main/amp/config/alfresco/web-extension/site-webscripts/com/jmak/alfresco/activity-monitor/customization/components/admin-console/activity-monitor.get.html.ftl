<@markup id="css" >
   <#-- CSS Dependencies -->
   <#include "/org/alfresco/components/form/form.css.ftl"/>
   <@link href="${url.context}/res/components/console/activity-monitor.css" group="console"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <#include "/org/alfresco/components/form/form.js.ftl"/>
   <@script src="${url.context}/res/components/console/consoletool.js" group="console"/>
   <@script src="${url.context}/res/components/console/activity-monitor.js" group="console"/>
   <@script src="${url.context}/res/modules/simple-dialog.js" group="console"/>
   <@script src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="console"/>
</@>

<@markup id="widgets">
   <@createWidgets group="console"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>
      
      	<div id="activity-monitor">
			<div class="row">
				<p>
					Site: 
					<select id="siteList">
						<option value="sample">Sample</option>
					</select>
					<input type="button" id="${el}-start-monitor" value="Start monitoring" />
					<input type="button" id="${el}-stop-monitor" value="Stop monitoring" />
					<input type="button" id="${el}-clear-monitor" value="Clear" />
				</p>
			</div>
			
			<div id="activity-monitor-buttons" class="row">
				<input id="${el}-creations-view" class="create" type="checkbox" name="1" value="1" checked /> 
				<input id="${el}-updates-view" type="checkbox" name="2" value="1" checked /> 
				<input id="${el}-deletions-view" type="checkbox" name="3" value="1" checked /> 
			</div>

			<div id="activity-monitor-tracer">
				<ul id="activity-tracer"></ul>
			</div>
		</div>
   </@>
</@>
