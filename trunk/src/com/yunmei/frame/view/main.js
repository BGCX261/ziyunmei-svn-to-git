var welcome = '';
var nodeId = '1001';
var nodeURL = $.linkURL('sysWorkflowService.todoList');
var nodeText = '待办任务';
var postDS = new Ext.data.JsonStore( {
	fields : [ 'id', 'name' ],
	data : window.top.user.posts
});
function doSelect(comb, rec) {
	if (window.top.user.posts.length != 1)
		$.forward('sysAuthService.changePost', window.top.user.id, rec.id);
}
Ext.onReady(function() {
			new Ext.Viewport(
					{
						enableTabScroll : true,
						layout : "border",
						items : [
								{
									height : 56,
									region : 'north',
									layout : 'hbox',
									plain : true,
									defaults : {
										baseCls : 'x-plain'
									},
									layoutConfig : {
										padding : '5',
										pack : 'end',
										align : 'middle'
									},
									bodyStyle : 'background-image: url(images/logon/header.jpg); margin : 1 0 0 0px;background-repeat: no-repeat;',
									items : [ {
										xtype : 'label',
										text : window.top.user.name
									}, {
										xtype : 'combo',
										store : postDS,
										valueField : 'id',
										width : 80,
										mode : 'local',
										triggerAction : 'all',
										readOnly : true,
										valueNotFoundText : '临时人员',
										listeners : {
											"select" : doSelect
										},
										value : window.top.user.defaultPost,
										displayField : 'name',
										maxHeight : 120
									} ]
								}, {
									id : 'leftPanel',
									title : "左导航",
									region : "west",
									collapsible : true,
									width : 200,
									split : true,
									layout : "accordion",
									autoScroll : true,
									layoutConfig : {
										animate : true
									},
									items : [ {
										title : '用户菜单',
										xtype : 'treepanel',
										autoScroll : true,
										animate : true,
										containerScroll : true,
										rootVisible : false,
										root : {
											id : '-1'
										},
										listeners : {
											"click" : treeClick,
											"beforeload" : beforeLoad
										}
									}, {
										title : '最近所览'
									}, {
										title : '书签'
									} ]

								}, {
									id : 'tabpanel',
									xtype : 'tabpanel',
									activeTab : 0,
									region : "center",
									listeners : {
										"contextmenu" : onContextMenu
									}
								} ]
					});

			var tabs = Ext.getCmp('tabpanel');
		//	$.mask();
			tabs.add( {
				id : nodeId,
				xtype : "panel",
				title : nodeText,
				layout : "fit",
				closable : true,
				listeners : {
					"afterrender" : function() {
						//$.unmask();
					}
				},
				html : "<iframe id='" + nodeId + "_frame' src=" + nodeURL
						+ " frameborder=0 width=100% height=100%></iframe>"
			});
			tabs.setActiveTab(nodeId);
		});
function beforeLoad(node) {
	node.getOwnerTree().loader.dataUrl = $.treeURL('sysAuthService.findMenus',
			node.id);
}
function treeClick(node, event) {
	if (event)
		event.stopEvent();
	var tabs = Ext.getCmp('tabpanel');
	if (!node.isLeaf())
		return;
	var id = 'tab_' + node.id;
	var url = $.linkURL(node.attributes['url']);
	var text = node.text;
	var panel = tabs.getItem(id);
	if (panel) {
		tabs.remove(id);
		panel.destroy();
	}
	$.mask();
	tabs.add( {
		id : id,
		xtype : "panel",
		title : text,
		layout : "fit",
		closable : true,
		listeners : {
			"afterrender" : function() {
				$.unmask();
			}
		},
		html : "<iframe id='" + nodeId + "_frame' src=" + url
				+ " frameborder=0 width=100% height=100%></iframe>"
	});
	tabs.setActiveTab(id);
}
var menu;
function onContextMenu(ts, item, e) {
	ts.activate(item);
	if (!menu) {
		menu = new Ext.menu.Menu( [ {
			id : ts.id + "_close",
			text : '关闭当前页',
			handler : function() {
				var item = ts.getActiveTab();
				ts.remove(item);
			}
		}, {
			id : ts.id + "_others",
			text : '关闭其他页',
			handler : function() {
				ts.items.each(function(item2) {
					var item = ts.getActiveTab();
					if (item2.closable && item.id != item2.id) {
						ts.remove(item2);
					}
				});
			}
		}, {
			id : ts.id + "_all",
			text : '关闭所有',
			handler : function() {
				ts.items.each(function(item2) {
					if (item2.closable) {
						ts.remove(item2);
					}
				});
			}
		}, '-', {
			text : '最大化窗口',
			handler : function() {
				if (!Ext.getCmp('leftPanel').collapsed) {
					Ext.getCmp('leftPanel').collapse(true);
				}
				if (!Ext.getCmp('topPanel').collapsed) {
					Ext.getCmp('topPanel').collapse(true);
				}
			}
		}, {
			text : '复原窗口',
			handler : function() {
				if (Ext.getCmp('leftPanel').collapsed) {
					Ext.getCmp('leftPanel').expand(true);
				}
				if (Ext.getCmp('topPanel').collapsed) {
					Ext.getCmp('topPanel').expand(true);
				}
			}
		} ]);
	}
	menu.showAt(e.getPoint());
}
