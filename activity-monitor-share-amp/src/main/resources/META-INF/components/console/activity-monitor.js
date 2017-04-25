/**
 * ConsoleActivityMonitor tool component.
 * 
 * @namespace Alfresco
 * @class Alfresco.ConsoleActivityMonitor
 */
(function()
		{
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

		tracerList: undefined,

		dialogMsg: undefined,

		/**
		 * Fired by YUI when parent element is available for scripting
		 * 
		 * @method onReady
		 */
		onReady: function ConsoleActivityMonitor_onReady() {
			var _this = this;
			this.tracerList = Dom.get("activity-tracer");

			function monitoriseSite() {
				var selectedSite = Dom.get("siteList").value;
				_this.startMonitoring(Alfresco.constants.PROXY_URI + "activity-monitor/ticket?site=" + selectedSite);
			}

			Event.on(Dom.get("monitoriseSite"), "click", monitoriseSite);
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
			this.dialogMsg = Alfresco.util.PopupManager.displayMessage({
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

		closeCurrentAdvice: function () {
			if (this.dialogMsg) {
				this.dialogMsg.destroy();
				this.dialogMsg = undefined;
			}
		},

		initializeSocket: function (ticket) {
			var self = this;

			var ws = new WebSocket("ws://localhost:8080/alfresco/activity-monitor?ticket=" + ticket);

			ws.onopen = function() {};

			ws.onmessage = function(message) {
				try {
					var jsonMessage = JSON.parse(message.data);
					
					if (jsonMessage.type == 'message') {
						var activityEntry = document.createElement("LI");

						var action = jsonMessage.action;

						var actionStyle = getActionStyle(jsonMessage.action);

						if (actionStyle) {
							activityEntry.className = actionStyle;
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

				if (action === 'delete') {
					return getDeletionMenssage(jsonMessage);
				}
			}

			function getCreationMenssage(jsonMessage) {
				var date = new Date(jsonMessage.issuedAt);
				var author = jsonMessage.author;
				var node = jsonMessage.node;

				return decoratedDate(date) + ' - User ' + decoratedAuthor(author) + ' created a node: ' + decoratedNode(node);
			}

			function getDeletionMenssage(jsonMessage) {
				var date = new Date(jsonMessage.issuedAt);
				var author = jsonMessage.author;
				var node = jsonMessage.node;

				return decoratedDate(date) + ' - User ' + decoratedAuthor(author) + ' deleted a node: ' + decoratedNodeLocation(node)  ;
			}

			function decoratedDate(date) {
				return '<span class="activity-date" title=\"' + date + '\">' + date.toLocaleDateString() + ' ' + date.toLocaleTimeString() + '</span>';
			}

			function decoratedNode(node) {
				var href = Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef=" + node.nodeRef;
				return '<span class="activity-node"><a href="' + href + '" target="_blank" title="' + node.location + '">' + node.name + '</a></span>';
			}
		
			function decoratedNodeLocation(node) {
				return '<span class="activity-node-location">' + node.location + '/' + node.name + '</span>';
			}

			function decoratedAuthor(author) {
				var href = Alfresco.constants.URL_PAGECONTEXT + 'user/' + author + '/profile';
				return '<span class="activity-author"><a href="' + href + '" target="_blank">' + author + '</a></span>';
			}

			function getActionStyle(operation) {
				if (operation === 'access') return 'operation-access';
				if (operation === 'create') return 'operation-create';
				if (operation === 'modify') return 'operation-modify';
				if (operation === 'delete') return 'operation-delete';

				return '';
			}

//			function postToServer() {
//			ws.send(document.getElementById("msg").value);
//			document.getElementById("msg").value = "";
//			}

//			function closeConnect() {
//			ws.close();
//			}
		}
	});
})();
