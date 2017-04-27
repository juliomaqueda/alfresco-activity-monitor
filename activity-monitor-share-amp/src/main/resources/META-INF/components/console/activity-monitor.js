/**
 * ConsoleActivityMonitor tool component.
 * 
 * @namespace Alfresco
 * @class Alfresco.ConsoleActivityMonitor
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML, $date = function $date(date, format) {
		return Alfresco.util.formatDate(Alfresco.util.fromISO8601(date), format)
	};

	/**
	 * ConsoleActivityMonitor tool component constructor.
	 * 
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {Alfresco.ConsoleTagManagement} The new component instance
	 * @constructor
	 */
	Alfresco.ConsoleActivityMonitor = function TagManagement_constructor(htmlId) {
		Alfresco.ConsoleActivityMonitor.superclass.constructor.call(this, "Alfresco.ConsoleActivityMonitor", htmlId, [ "button", "container", "datasource", "datatable", "paginator", "history", "animation" ]);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(Alfresco.ConsoleActivityMonitor, Alfresco.component.Base, {

		tracerList: null,

		currentSocket: null,

		statusDialog: null,

		showCreations: true,
		showUpdates: true,
		showDeletions: true,

		/**
		 * Fired by YUI when parent element is available for scripting
		 * 
		 * @method onReady
		 */
		onReady: function ConsoleActivityMonitor_onReady() {
			var _this = this;
			this.tracerList = Dom.get("activity-tracer");

			var startMonitorButton = Alfresco.util.createYUIButton(this, "start-monitor", monitoriseSite);

			function monitoriseSite() {
				startMonitorButton.set("disabled", true);
				stopMonitorButton.set("disabled", false);

				var selectedSite = Dom.get("siteList").value;
				_this.startMonitoring(Alfresco.constants.PROXY_URI + "activity-monitor/ticket?site=" + selectedSite);
			}

			var stopMonitorButton = Alfresco.util.createYUIButton(this, "stop-monitor", stopMonitor);
			stopMonitorButton.set("disabled", true);

			function stopMonitor() {
				_this.finalizeSocket();
				_this.adviceDisconnected();
				startMonitorButton.set("disabled", false);
				stopMonitorButton.set("disabled", true);
			}

			var clearMonitorButton = Alfresco.util.createYUIButton(this, "clear-monitor", clearMonitor);

			function clearMonitor() {
				_this.tracerList.innerHTML = '';
			}
			
			var showCreationsButton = new YAHOO.widget.Button(this.id + "-creations-view", { label:"Creations", checked:true });
			var showUpdatesButton = new YAHOO.widget.Button(this.id + "-updates-view", { label:"Updates", checked:true });
			var showDeletionsButton = new YAHOO.widget.Button(this.id + "-deletions-view", { label:"Deletions", checked:true });

			function toggleMonitorView(viewButton, classSelector) {
				var activityMessages = document.querySelectorAll("." + classSelector);

				if (activityMessages.length > 0) {
					if (viewButton.get("checked")) {
						for (activityMessage in activityMessages) {
							if (activityMessages[activityMessage].classList) {
								activityMessages[activityMessage].classList.remove("hidden");
							}
						}
					}
					else {
						for (activityMessage in activityMessages) {
							if (activityMessages[activityMessage].classList) {
								activityMessages[activityMessage].classList.add("hidden");
							}
						}
					}
				}

				return viewButton.get("checked");
			}

			function toggleCreations() {
				_this.showCreations = toggleMonitorView(showCreationsButton, _this.getActionStyle('create'));
			}

			function toggleUpdates() {
				_this.showUpdates = toggleMonitorView(showUpdatesButton, _this.getActionStyle('update'));
			}

			function toggleDeletions() {
				_this.showDeletions = toggleMonitorView(showDeletionsButton, _this.getActionStyle('delete'));
			}

			showCreationsButton.on("click", toggleCreations);
			showUpdatesButton.on("click", toggleUpdates);
			showDeletionsButton.on("click", toggleDeletions);
		},

		startMonitoring: function (ticketUrl) {
			var self = this;

			this.adviceConnecting();

			Alfresco.util.Ajax.request({
				url : ticketUrl,
				method : Alfresco.util.Ajax.GET,
				successCallback : {
					fn : function (response, config) {
						var ticket = response.json.ticket;
						self.initializeSocket(ticket);
					},
					scope : this
				},
				failureCallback : {
					fn : function () {
						self.adviceConnectionError();
					},
					scope : this
				}
			});
		},

		adviceConnecting: function () {
			this.statusDialog = Alfresco.util.PopupManager.displayMessage({
			    text : "Connecting to the server...",
			    spanClass : "wait",
			    displayTime : 0
			});
		},

		adviceConnectionError: function () {
			this.closeCurrentAdvice();
			Alfresco.util.PopupManager.displayMessage({
				text : "Error - Connection not established"
			});
		},

		adviceConnected: function () {
			this.closeCurrentAdvice();
			Alfresco.util.PopupManager.displayMessage({
				text : "Connection established successfully"
			});
		},

		adviceDisconnected: function () {
			this.closeCurrentAdvice();
			Alfresco.util.PopupManager.displayMessage({
				text : "Connection closed successfully"
			});
		},

		closeCurrentAdvice: function () {
			if (this.statusDialog) {
				this.statusDialog.destroy();
				this.statusDialog = undefined;
			}
		},

		initializeSocket: function (ticket) {
			var self = this;

			this.currentSocket = new WebSocket("ws://localhost:8080/alfresco/activity-monitor?ticket=" + ticket);

			this.currentSocket.onopen = function() {};

			this.currentSocket.onmessage = function(message) {
				try {
					var jsonMessage = JSON.parse(message.data);
					
					if (jsonMessage.type == 'message') {
						var activityEntry = document.createElement("LI");

						var action = jsonMessage.action;

						var actionStyle = self.getActionStyle(action);

						if (actionStyle) {
							activityEntry.classList.add(actionStyle);
						}

						var statusStyle = self.getStatusStyle(action);

						if (statusStyle) {
							activityEntry.classList.add(statusStyle);
						}

						var actionText = createMessageForAction(action, jsonMessage);

						self.tracerList.appendChild(activityEntry);

						activityEntry.innerHTML = actionText;
					}
					else if (jsonMessage.type == 'status') {
						if (jsonMessage.status == 'connected') {
							self.adviceConnected();
						}
					}
				}
				catch(err) {
					
				}
			};

			function createMessageForAction(action, jsonMessage) {
				if (action === 'create') {
					return getCreationMenssage(jsonMessage);
				}

				if (action === 'update') {
					return getUpdateMenssage(jsonMessage);
				}

				if (action === 'delete') {
					return getDeletionMenssage(jsonMessage);
				}

				if (action === 'navigation') {
					return getNavigationMenssage(jsonMessage);
				}
			}

			function getCreationMenssage(message) {
				var date = new Date(message.issuedAt);
				var author = message.author;
				var node = message.node;

				return decoratedDate(date) + ' - User ' + decoratedAuthor(author) + ' created a node of type ' + decoratedNodeType(node.type) + ': ' + decoratedNode(node);
			}

			function getUpdateMenssage(message) {
				var date = new Date(message.issuedAt);
				var author = message.author;
				var node = message.node;
				var properties = message.properties;

				return decoratedDate(date) + ' - Node ' + decoratedNode(node) + ' was updated by ' + decoratedAuthor(author) + ':' + decoratedProperties(properties);
			}

			function getDeletionMenssage(message) {
				var date = new Date(message.issuedAt);
				var author = message.author;
				var node = message.node;

				return decoratedDate(date) + ' - User ' + decoratedAuthor(author) + ' deleted a node: ' + decoratedNodeLocation(node);
			}

			function getNavigationMenssage(message) {
				var date = new Date(message.issuedAt);
				var author = message.author;
				var node = message.node;

				return decoratedDate(date) + ' - User ' + decoratedAuthor(author) + ' accessed the next path: ' + decoratedNodeLocation(node)  ;
			}

			function decoratedDate(date) {
				return '<span class="activity-date" title=\"' + date + '\">' + date.toLocaleDateString() + ' ' + date.toLocaleTimeString() + '</span>';
			}

			function decoratedNode(node) {
				var href = Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef=" + node.nodeRef;
				return '<span class="activity-node"><a href="' + href + '" target="_blank" title="' + node.location + '">' + node.name + '</a></span>';
			}

			function decoratedNodeType(type) {
				return '<span class="activity-node-type">' + type + '</span>';
			}

			function decoratedNodeLocation(node) {
				return '<span class="activity-node-location">' + node.location + '/' + node.name + '</span>';
			}

			function decoratedAuthor(author) {
				var href = Alfresco.constants.URL_PAGECONTEXT + 'user/' + author + '/profile';
				return '<span class="activity-author"><a href="' + href + '" target="_blank">' + author + '</a></span>';
			}

			function decoratedProperties(properties) {
				var text = '<p class="activity-properties">';

				Object.keys(properties).forEach(function (key, index) {
					var before = properties[key].first;
					var after = properties[key].second;

					if (before || after) {
						text += '<span class="activity-property">' + key + ': ' + 
							(before? before + ' -> ' : '') + 
							(after? after : ' (removed)') + 
							(before? '' : ' (created)') +
							'</span>';
					}
				});

				text += '</p>';

				return text;
			}
		},

		finalizeSocket: function () {
			if (this.currentSocket) {
				this.currentSocket.close();
				this.currentSocket = null;

				this.adviceDisconnected();
			}
		},

		getActionStyle: function (operation) {
			if (operation === 'navigation') return 'operation-navigation';
			if (operation === 'create') return 'operation-create';
			if (operation === 'update') return 'operation-update';
			if (operation === 'delete') return 'operation-delete';

			return '';
		},

		getStatusStyle: function (operation) {
			if ((operation === 'create' && !this.showCreations) ||
				(operation === 'update' && !this.showUpdates) ||
				(operation === 'delete' && !this.showDeletions)) {
				return 'hidden';
			}

			return '';
		}
	});
})();
