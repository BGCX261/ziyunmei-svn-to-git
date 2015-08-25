var tree;
var currNodeId;
var jobnode, oldParent, jobNewParent;
// ---重写vtype,实现两次密码输入是否相同验证 开始 09-11-09
// 添加验证--------------------------------------
Ext.apply(Ext.form.VTypes, {
	password : function(val, field) {
		if (field.confirmTo) {
			var pwd = Ext.get(field.confirmTo);
			if (val.trim() == pwd.getValue().trim()) {
				return true;
			}
			return false;
		}
	}
});
// 添加验证--------------------------------------
var organTypeDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'organ_type'),
	fields : [ 'name', 'value' ]
});
var sexDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'sex'),
	fields : [ 'name', 'value' ]
});
var sexWinDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'sex'),
	fields : [ 'name', 'value' ]
});
var organTypeWinDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'organ_type'),
	fields : [ 'name', 'value' ]
});
var educationUserDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'education'),
	fields : [ 'name', 'value' ]
});
educationUserDS.load();
// ----------------------岗位定义----------------------------------------
var postDS = new Ext.data.JsonStore( {
	url : $.URL('sysOrganService.findSubNodes'),
	fields : [ 'id', 'name', 'remark' ]
});
var postCM = new Ext.grid.ColumnModel( [ {
	header : "ID",
	dataIndex : "id",
	width : 70
}, {
	header : "岗位名",
	dataIndex : "name",
	width : 120
}, {
	header : "备注",
	dataIndex : "remark",
	width : 210
} ]);
// ---------------------人员定义------------------------------------------------
var userSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true
});
var userDS = new Ext.data.JsonStore( {
	url : $.URL('sysOrganService.findSubNodes'),
	fields : [ 'id', 'name', 'code', 'login', 'password', 'subject',
			'education', 'birthday', 'tphone', 'mphone', 'remark' ]
});
var userCM = new Ext.grid.ColumnModel( [ {
	header : "ID",
	dataIndex : "id",
	width : 50
}, {
	header : "编码",
	dataIndex : "code",
	width : 80
}, {
	header : "用户名",
	dataIndex : "name",
	width : 120
}, {
	header : "登录名",
	dataIndex : "login",
	width : 120
}, {
	header : "专业",
	dataIndex : "subject",
	width : 120
}, {
	header : "最高学历",
	dataIndex : "education",
	width : 120,
	renderer : function(value) {
		var rec = educationUserDS.getAt(educationUserDS.find("value", value));
		return rec ? rec.data.name : '';
	}
}, {
	header : "手机",
	dataIndex : "mphone",
	width : 120
}, {
	header : "座机",
	dataIndex : "tphone",
	width : 120
}, {
	header : "备注",
	dataIndex : "remark",
	width : 120
} ]);
// ---------------------岗位角色信息-------------------------------------
var postRoleDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findRolesByPostId'),
	fields : [ 'id', 'name', 'remark' ]
});
var postRoleNotDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findNotRolesByPostId'),
	fields : [ 'id', 'name', 'remark' ]
});

var postRoleCM = new Ext.grid.ColumnModel( [ {
	header : "ID",
	dataIndex : "id",
	width : 70
}, {
	header : "角色名",
	dataIndex : "name",
	width : 120
}, {
	header : "备注",
	dataIndex : "remark",
	width : 120
} ]);
// ---------------------局室信息-------------------------------------
var organDS = new Ext.data.JsonStore( {
	url : $.URL('sysOrganService.findSubNodes'),
	fields : [ 'id', 'name', 'code', 'address', 'email', 'type', 'fax',
			'phone', 'responsibility', 'state', 'website' ]
});

var organCM = new Ext.grid.ColumnModel( [ {
	header : "ID",
	dataIndex : "id",
	width : 70
}, {
	header : "名称",
	dataIndex : "name",
	width : 120
}, {
	header : "编码",
	dataIndex : "code",
	width : 120
}, {
	header : "类型",
	dataIndex : "type",
	width : 120
}, {
	header : "主要职责",
	dataIndex : "responsibility",
	width : 120
}, {
	header : "地址",
	dataIndex : "address",
	width : 120
}, {
	header : "电话",
	dataIndex : "phone",
	width : 120
}, {
	header : "邮箱",
	dataIndex : "email",
	width : 120
}, {
	header : "传真",
	dataIndex : "fax",
	width : 120
}, {
	header : "网址",
	dataIndex : "website",
	width : 120
}, {
	header : "状态",
	dataIndex : "state",
	width : 120
}, {
	header : "备注",
	dataIndex : "remark",
	width : 120
} ]);
// -------------------------------用户角色信息-------------------------------------------------------
var userRoleDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findRolesByUserId'),
	fields : [ 'name', 'remark', 'type' ]
});
var userRoleCM = new Ext.grid.ColumnModel( [ {
	header : "角色名",
	dataIndex : "name",
	width : 120
}, {
	header : "类型",
	dataIndex : "type",
	width : 120
}, {
	header : "备注",
	dataIndex : "remark",
	width : 120
} ]);
// -------------------------------局室面板-------------------------------------------------
var organPanel = new Ext.FormPanel( {
	title : '局室信息',
	bodyStyle : 'padding:5px',
	frame : 'true',
	tbar : [
			{
				text : "增加",
				url : 'sysOrganService.insertUser',
				handler : showAddMenu

			},
			{
				text : '删除',
				url : 'sysOrganService.deleteNode',
				handler : deleteNode
			},
			{
				text : '保存',
				url : 'sysOrganService.updateUser',
				handler : function() {
					if (!organPanel.getForm().isValid())
						return;
					updatePanel('sysOrganService.updateOrgan', $
							.getFormValues(organPanel), 'C');
					$.msg('保存成功');
				}
			} ],
	items : [ {
		layout : 'column',
		border : false,
		defaults : {
			layout : 'form',
			border : false,
			columnWidth : .5
		},
		items : [ {
			items : [ {
				name : 'id',
				xtype : 'hidden'
			}, {
				name : 'code',
				xtype : 'textfield',
				fieldLabel : '编码',
				allowBlank : false,
				vtype : 'alphanum',
				anchor : '95%'
			}, {
				name : 'type',
				anchor : '95%',
				fieldLabel : '类型',
				xtype : 'combo',
				store : organTypeDS,
				valueField : 'value',
				readOnly : true,
				triggerAction : 'all',
				displayField : 'name',
				maxHeight : 230
			}, {
				name : 'admin',
				anchor : '95%',
				fieldLabel : '主管领导',
				xtype : 'combo',
				mode : 'local',
				readOnly : true,
				triggerAction : 'all',
				maxHeight : 230,
				tree : {
					url : 'sysOrganService.findUserTree',
					id : '-1',
					leaf : true,
					value : 'user'
				},
				listeners : {
					"select" : comboSelect
				},
				onSelect : Ext.emptyFn
			}, {
				name : 'address',
				xtype : 'textfield',
				fieldLabel : '地址',
				anchor : '95%'
			}, {
				name : 'phone',
				xtype : 'textfield',
				fieldLabel : '固定电话',
				vtype : 'phone',
				anchor : '95%'
			}, {
				name : 'fax',
				xtype : 'textfield',
				fieldLabel : '传真',
				vtype : 'phone',
				anchor : '95%'
			} ]
		}, {
			items : [ {
				name : 'name',
				xtype : 'textfield',
				fieldLabel : '名称',
				allowBlank : false,
				vtype : 'name',
				anchor : '95%'
			}, {
				name : 'state',
				xtype : 'textfield',
				fieldLabel : '状态',
				anchor : '95%'
			}, {
				name : 'responsibility',
				xtype : 'textfield',
				fieldLabel : '主要职责',
				anchor : '95%'
			}, {
				name : 'email',
				xtype : 'textfield',
				fieldLabel : '邮箱',
				vtype : 'email',
				anchor : '95%'
			}, {
				name : 'website',
				xtype : 'textfield',
				fieldLabel : '网址',
				vtype : 'url',
				anchor : '95%'
			}, {
				name : 'mphone',
				xtype : 'textfield',
				fieldLabel : '手机',
				vtype : 'mphone',
				anchor : '95%'
			} ]
		} ]
	}, {
		id : 'organTab',
		xtype : 'tabpanel',
		plain : true,
		height : 400,
		defaultType : 'grid',
	    activeTab: 0,
		defaults : {
			bodyStyle : 'padding:10px'
		},
		items : [ {
			id : 'organGrid',
			title : "子局室信息",
			height : 200,
			listeners : {
				activate : tabChange
			},
			ds : organDS,
			cm : organCM
		}, {
			title : "岗位信息",
			height : 200,
			listeners : {
				activate : tabChange
			},
			ds : postDS,
			cm : postCM
		}, {
			title : "人员信息",
			height : 200,
			listeners : {
				activate : tabChange
			},
			ds : userDS,
			cm : userCM
		} ]
	} ]
});
// -------------------------------岗位面板-------------------------------------------------
var postRoleGrid = new Ext.grid.GridPanel( {
	title : "已有角色",
	ddGroup : 'roleGroup',
	enableDragDrop : true,
	height : 300,
	columnWidth : .5,
	ds : postRoleDS,
	cm : postRoleCM
});
var postRoleNotGrid = new Ext.grid.GridPanel( {
	title : "未有角色",
	ddGroup : 'roleNotGroup',
	enableDragDrop : true,
	height : 300,
	columnWidth : .5,
	ds : postRoleNotDS,
	cm : postRoleCM
});
var roleGridTarget;
var roleNotGridTarget;
var postPanel = new Ext.FormPanel( {
	title : '岗位信息',
	bodyStyle : 'padding:5px',
	hidden : true,
	frame : 'true',
	tbar : [
			{
				text : "增加",
				url : 'sysOrganService.insertUser',
				handler : showAddMenu
			},
			{
				text : '删除',
				url : 'sysOrganService.deleteNode',
				handler : deleteNode
			},
			{
				text : '保存',
				url : 'sysOrganService.updateUser',
				handler : function() {
					if (!postPanel.getForm().isValid())
						return;
					updatePanel('sysOrganService.updatePost', $
							.getFormValues(postPanel), 'P');
					$.msg('操作成功');
				}
			} ],
	items : [ {
		layout : 'column',
		border : false,
		items : [ {
			columnWidth : .5,
			layout : 'form',
			border : false,
			items : [ {
				name : 'id',
				xtype : 'hidden'
			}, {
				name : 'name',
				fieldLabel : '岗位名',
				xtype : 'textfield',
				vtype : 'name',
				allowBlank : false,
				anchor : '95%'
			}, {
				name : 'remark',
				xtype : 'textfield',
				fieldLabel : '备注',
				anchor : '95%'
			} ]
		}, {
			columnWidth : .5,
			layout : 'form',
			border : false,
			items : [ {
				name : 'admin',
				anchor : '95%',
				fieldLabel : '负责人',
				xtype : 'combo',
				mode : 'local',
				triggerAction : 'all',
				maxHeight : 230,
				tree : {
					url : 'sysOrganService.findUserTree',
					id : '-1',
					leaf : true,
					value : 'user'
				},
				listeners : {
					"select" : comboSelect
				},
				onSelect : Ext.emptyFn
			} ]
		} ]
	}, {
		id : 'postTab',
		xtype : 'tabpanel',
		plain : true,
		height : 400,
		defaultType : 'grid',
		defaults : {
			bodyStyle : 'padding:10px'
		},
		items : [ {
			xtype : 'panel',
			title : "岗位角色信息",
			defaultType : 'grid',
			layout : 'column',
			height : 360,
			listeners : {
				activate : tabChange
			},
			items : [ postRoleGrid, postRoleNotGrid ]
		}, {
			title : "人员信息",
			height : 360,
			listeners : {
				activate : tabChange
			},
			store : userDS,
			sm : userSM,
			cm : userCM
		} ]
	} ]
});
// ---------------------------------用户面板-----------------------------------------------------
var userPanel = new Ext.FormPanel( {
	title : '用户信息',
	bodyStyle : 'padding:5px',
	hidden : true,
	frame : 'true',
	id : 'userform',
	tbar : [
			{
				url : 'sysOrganService.alterPassword',
				text : '修改密码',
				handler : changeUserPassword
			},
			{
				text : '删除',
				url : 'sysOrganService.deleteNode',
				handler : deleteNode
			},
			{
				text : '保存',
				url : 'sysOrganService.updateUser',
				handler : function() {
					var formPanel = this.ownerCt.ownerCt;
					if (!formPanel.getForm().isValid())
						return;
					updatePanel('sysOrganService.updateUser', $
							.getFormValues(userPanel), 'U');
					$.msg('保存成功');
				}
			} ],
	items : [ {
		layout : 'column',
		border : false,
		items : [ {
			columnWidth : .5,
			layout : 'form',
			border : false,
			items : [ {
				name : 'id',
				xtype : 'hidden'
			}, {
				name : 'password',
				xtype : 'hidden'
			}, {
				name : 'name',
				xtype : 'textfield',
				fieldLabel : '姓名',
				vtype : 'name',
				allowBlank : false,
				anchor : '95%'
			}, {
				name : 'login',
				xtype : 'textfield',
				fieldLabel : '登录名',
				anchor : '95%',
				allowBlank : false,
				vtype : 'alphanum'
			}, {
				name : 'education',
				xtype : 'textfield',
				fieldLabel : '最高学历',
				anchor : '95%'
			}, {
				name : 'tphone',
				xtype : 'textfield',
				fieldLabel : '固定电话',
				vtype : 'phone',
				anchor : '95%'
			}, {
				name : 'email',
				xtype : 'textfield',
				fieldLabel : '邮箱',
				vtype : 'email',
				anchor : '95%'
			} ]
		}, {
			columnWidth : .5,
			layout : 'form',
			border : false,
			items : [ {
				name : 'code',
				xtype : 'textfield',
				fieldLabel : '编码',
				allowBlank : false,
				vtype : 'alphanum',
				anchor : '95%'
			}, {
				name : 'sex',
				anchor : '95%',
				fieldLabel : '性别',
				xtype : 'combo',
				store : sexDS,
				readOnly : true,
				valueField : 'value',
				triggerAction : 'all',
				displayField : 'name',
				maxHeight : 230
			}, {
				name : 'subject',
				xtype : 'textfield',
				fieldLabel : '专业',
				anchor : '95%'
			}, {
				name : 'mphone',
				xtype : 'textfield',
				fieldLabel : '手机',
				vtype : 'phone',
				anchor : '95%'
			} ]
		} ]
	}, {
		id : 'userTab',
		xtype : 'tabpanel',
		plain : true,
		height : 235,
		defaultType : 'grid',
		defaults : {
			bodyStyle : 'padding:10px'
		},
		items : [ {
			title : "用户角色信息",
			height : 200,
			width : 300,
			listeners : {
				activate : tabChange
			},
			ds : userRoleDS,
			cm : userRoleCM
		} ]
	} ]
});
var organItem = {
	text : '创建同级局室!',
	handler : function() {
		var node = tree.getSelectionModel().getSelectedNode();
		showOrganWindow(!currNodeId ? -1 : node.parentNode.id);
	}
};
var downOrganItem = {
	text : '创建下级局室!',
	handler : function() {
		showOrganWindow(currNodeId);
	}
};
var postItem = {
	text : '创建同级岗位!',
	handler : function() {
		var node = tree.getSelectionModel().getSelectedNode();
		showPostWindow(node.parentNode.id);
	}
};
var downPostItem = {
	text : '创建下级岗位!',
	handler : function() {
		showPostWindow(currNodeId);
	}
};
var userItem = {
	text : '添加人员',
	handler : function() {
		showUserWindow(currNodeId);
	}
};
function comboSelect(combo, rec) {

}
var addMenu = new Ext.menu.Menu( {
	listeners : {
		"mouseout" : function(n, e) {
			var srcxy = e.getXY();
			var xy = addMenu.getPosition();
			var size = addMenu.getSize();
			if (srcxy[0] + 10 > xy[0] && srcxy[1] + 10 > xy[1]
					&& srcxy[0] + 10 < xy[0] + size.width
					&& srcxy[1] + 10 < xy[1] + size.height) {
			} else {
				addMenu.hide();
			}
		}
	}
});
Ext.onReady(function() {
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'under';
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : "border",
		items : [ {
			id : "menu-div",
			region : "west",
			width : 200,
			collapsible : true
		}, {
			region : "center",
			items : [ organPanel, postPanel, userPanel ]
		} ]
	});
	tree = new Ext.tree.TreePanel( {
		el : 'menu-div',
		useArrows : false,
		autoScroll : true,
		enableDD : true,
		animate : true,
		containerScroll : true,
		rootVisible : false,
		listeners : {
			"click" : treeClick,
			"beforeload" : function(node) {
				tree.loader.dataUrl = $.treeURL(
						'sysOrganService.findOrganTree', node.id);
			},
			"load" : function(node) {
				if (!tree.getSelectionModel().getSelectedNode()) {
					if (tree.root.firstChild) {
						treeClick(tree.root.firstChild);
						tree.root.firstChild.select();
						setTimeout(function() {
							tree.root.firstChild.select();
						}, 100);
					} else
						showOrganWindow(-1);
				}
			},
			"beforemovenode" : moveNode,
			"contextmenu" : showAddMenu,
			"nodedragover" : dragOver
		},
		root : {
			id : '-1'
		}
	});
	tree.render();
});
var roleWin = new Ext.Window( {
	title : '职位类型',
	frame : true,
	width : 260,
	height : 130,
	buttonAlign : 'center',
	closable : false,
	plain : true,
	modal : true,
	defaults : {
		baseCls : 'x-plain'
	},
	bodyStyle : 'padding: 20px',
	layout : 'column',
	items : [ {
		columnWidth : .5,
		xtype : 'radio',
		name : 'sort',
		value : 'worker',
		checked : true,
		labelStyle : 'padding: 30 30 0 0px',
		boxLabel : '调 岗'
	}, {
		columnWidth : .5,
		xtype : 'radio',
		name : 'sort',
		value : 'partworker',
		boxLabel : '兼 职'
	} ],
	buttons : [ {
		text : '确定',
		handler : doMove
	}, {
		text : '取消',
		handler : function() {
			roleWin.hide();
		}
	} ]
});

function moveNode(tree, node, oldp, newp, index) {
	jobnode = node;
	oldParent = oldp;
	newParent = newp;
	if (newParent.attributes['type'] != 'P')
		return false;
	roleWin.show();
	return false;
}
function doMove(btn) {
	var currNode = new Ext.tree.TreeNode( {
		text : jobnode.text,
		icon : 'images/user.gif',
		type : jobnode.attributes['type']
	});
	if (roleWin.find('name', 'sort')[0].getValue()) {// 调岗
		$.call("sysOrganService.changePost", jobnode.id, newParent.id,
				function() {
					currNode.id = jobnode.id;
					oldParent.removeChild(jobnode);
					newParent.appendChild(currNode);
				});
	} else {
		$.call("sysOrganService.insertUser2Post", jobnode.id, newParent.id,
				function(id) {
					currNode.id = id;
					newParent.appendChild(currNode);
				});
	}
	roleWin.hide();
}
function dragOver(e) {
	var n = e.target;
	if (n.leaf) {
		n.leaf = false;
	}
	if (e.dropNode.parentNode.id == n.id || n.attributes["type"] != 'P'
			|| e.dropNode.attributes["type"] != 'U')
		return false;
	return true;
}
function treeClick(node, event) {
	if (!!event) {
		event.stopEvent();
	}
	currNodeId = node.id;
	var type = node.attributes['type'];
	var res = $.call("sysOrganService.getNode", node.id);
	var tab;
	if (type == 'C') {
		postPanel.hide();
		userPanel.hide();
		organPanel.show();
		tab = organPanel.findById("organTab");
		organTypeDS.load( {
			callback : function() {
				var userId = res.result.admin;
				var combo = organPanel.find("name", "admin")[0];
				var value;
				var text;
				if (userId) {
					delete res.result.admin;
					var userResult = $.call("sysOrganService.getUser", userId);
					value = userId;
					text = userResult.result.name;
				} else {
					value = '';
					text = '';
				}
				organPanel.getForm().reset();
				combo.valueNotFoundText = text;
				combo.setValue(value);
				res.result.type = res.result.type || '';
				organPanel.getForm().setValues(res.result);
			}
		});
	} else if (type == 'P') {
		organPanel.hide();
		userPanel.hide();
		postPanel.show();
		postPanel.getForm().setValues(res.result);
		tab = postPanel.findById("postTab");
		var userId = res.result.admin;
		var combo = postPanel.find("name", "admin")[0];
		var value;
		var text;
		if (userId) {
			delete res.result.admin;
			var userResult = $.call("sysOrganService.getUser", userId);
			value = userId;
			text = userResult.result.name;
		} else {
			value = '';
			text = '';
		}
		postPanel.getForm().reset();
		if (value != '') {
			combo.valueNotFoundText = text;
			combo.setValue(value);
		}
		postPanel.getForm().setValues(res.result);
	} else if (type == 'U') {
		organPanel.hide();
		postPanel.hide();
		userPanel.show();
		userPanel.getForm().setValues(res.result);
		tab = userPanel.findById("userTab");
		sexDS.load( {
			callback : function() {
				res.result.sex = res.result.sex || '';
				userPanel.getForm().reset();
				userPanel.getForm().setValues(res.result);
			}
		});
	}
	tab.setActiveTab(0);
	tabChange(tab.getActiveTab());
	if (type == 'P')
		enableDrop();
}
function enableDrop() {
	if (roleGridTarget == null)
		roleGridTarget = new Ext.dd.DropTarget(
				postRoleGrid.getView().el.dom.childNodes[0].childNodes[1], {
					ddGroup : 'roleNotGroup',
					copy : true,
					notifyDrop : function(ddSource, e, data) {
						function addRow(record, index, allItems) {
							var postId = postPanel.getForm().findField('id')
									.getValue();
							$.call("sysOrganService.savePostRole", {
								post : postId,
								role : record.id
							});
							postRoleDS.add(record);
							ddSource.grid.store.remove(record);
						}
						Ext.each(ddSource.dragData.selections, addRow);
						return (true);
					}
				});
	if (roleNotGridTarget == null)
		roleNotGridTarget = new Ext.dd.DropTarget(
				postRoleNotGrid.getView().el.dom.childNodes[0].childNodes[1], {
					ddGroup : 'roleGroup',
					copy : true,
					notifyDrop : function(ddSource, e, data) {
						function addRow(record, index, allItems) {
							var postId = postPanel.getForm().findField('id')
									.getValue();
							$.call("sysOrganService.deletePostRole", {
								post : postId,
								role : record.id
							});
							postRoleNotDS.add(record);
							ddSource.grid.store.remove(record);
						}
						Ext.each(ddSource.dragData.selections, addRow);
						return (true);
					}
				});

}
function tabChange(tab) {
	if (tab.title == '岗位信息') {
		postDS.load( {
			params : [ currNodeId, 'P' ]
		});
	} else if (tab.title == '人员信息') {
		userDS.load( {
			params : [ currNodeId, 'U' ]
		});
	} else if (tab.title == '岗位角色信息') {
		var postId = postPanel.getForm().findField('id').getValue();
		postRoleNotDS.load( {
			params : postId
		});
		postRoleDS.load( {
			params : [ postId ]
		});
	} else if (tab.title == '用户角色信息') {
		var userId = userPanel.getForm().findField('id').getValue();
		userRoleDS.load( {
			params : userId
		});
	} else if (tab.title == '子局室信息') {
		organDS.load( {
			params : [ currNodeId, 'C' ]
		});
	}
}

function showAddMenu(n, e) {
	var node;
	if (n.parentNode) {
		node = n;
		node.select();
		currNodeId = node.id;
	} else
		node = tree.getSelectionModel().getSelectedNode();
	if (!tree.getRootNode().firstChild) {
		addMenu.removeAll();
		addMenu.addMenuItem(organItem);
	} else if (!!node) {
		if (node.attributes["type"] == 'C') {
			addMenu.removeAll();
			addMenu.addMenuItem(organItem);
			addMenu.addMenuItem(downOrganItem);
			addMenu.addMenuItem(downPostItem);
		} else if (node.attributes["type"] == 'P') {
			addMenu.removeAll();
			addMenu.addMenuItem(postItem);
			addMenu.addMenuItem(userItem);
		} else {
			return;
		}
	} else {
		addMenu.removeAll();
		addMenu.addMenuItem(organItem);
		addMenu.addMenuItem(downOrganItem);
	}
	addMenu.showAt(e.xy);
}
function showUserWindow(parent) {
	var userWindow = new Ext.Window( {
		title : '用户信息',
		layout : 'fit',
		width : 600,
		height : 300,
		modal : true,
		bodyStyle : 'padding:5px;',
		maximizable : false,
		closeAction : 'close',
		closable : false,
		plain : true,
		items : [ {
			bodyStyle : 'padding:5px',
			xtype : 'form',
			frame : 'true',
			items : [ {
				layout : 'column',
				border : false,
				items : [ {
					columnWidth : .5,
					layout : 'form',
					border : false,
					items : [ {
						name : 'id',
						xtype : 'hidden'
					}, {
						name : 'name',
						xtype : 'textfield',
						fieldLabel : '姓名',
						vtype : 'name',
						allowBlank : false,
						anchor : '95%'
					}, {
						name : 'login',// 09-11-09 添加验证
						xtype : 'textfield',
						fieldLabel : '登录名',
						anchor : '95%',
						vtype : 'alphanum',
						allowBlank : false
					}, {
						xtype : 'textfield',
						fieldLabel : '密码',
						name : 'password',
						id : 'password',
						minLength : 6,
						maxLength : 20,
						inputType : 'password',
						allowBlank : false,
						anchor : '95%'

					}, {
						xtype : 'textfield',
						fieldLabel : '确认密码',
						name : 'confirmpassword',
						id : 'confirmpassword',
						inputType : 'password',
						vtype : 'password',
						vtypeText : "两次密码不一致！",
						confirmTo : 'password',
						allowBlank : false,
						anchor : '95%'

					}, {
						name : 'tphone',
						xtype : 'textfield',
						fieldLabel : '固定电话',
						vtype : 'phone',
						anchor : '95%'
					}, {
						name : 'email',
						xtype : 'textfield',
						fieldLabel : '邮箱',
						vtype : 'email',
						anchor : '95%'
					} ]
				}, {
					columnWidth : .5,
					layout : 'form',
					border : false,
					items : [ {
						name : 'code',
						xtype : 'textfield',
						fieldLabel : '编码',
						allowBlank : false,
						vtype : 'alphanum',
						anchor : '95%'
					}, {
						name : 'sex',
						anchor : '95%',
						fieldLabel : '性别',
						xtype : 'combo',
						store : sexWinDS,
						valueField : 'value',
						triggerAction : 'all',
						readOnly : true,
						displayField : 'name',
						maxHeight : 230
					}, {
						name : 'education',
						xtype : 'textfield',
						fieldLabel : '最高学历',
						anchor : '95%'
					}, {
						name : 'subject',
						xtype : 'textfield',
						fieldLabel : '专业',
						anchor : '95%'
					}, {
						name : 'mphone',
						xtype : 'textfield',
						fieldLabel : '手机',
						vtype : 'phone',
						anchor : '95%'
					} ]
				} ]
			} ],
			buttons : [
					{
						text : '保存退出',
						handler : function() {
							var formPanel = this.ownerCt.ownerCt;
							if (!formPanel.getForm().isValid())
								return;
							var values = $.getFormValues(formPanel);
							insertNode('sysOrganService.insertUser', values,
									'U', parent);
							userWindow.close();
						}
					},
					{
						text : '保存继续',
						handler : function() {
							var formPanel = this.ownerCt.ownerCt;
							if (!formPanel.getForm().isValid())
								return;
							var values = $.getFormValues(formPanel);
							insertNode('sysOrganService.insertUser', values,
									'U', parent);
							formPanel.getForm().reset();
						}
					}, {
						text : '关闭',
						handler : function() {
							userWindow.close();
						}
					} ]
		} ]
	});
	userWindow.show();
	if (typeof parent == 'object')
		userWindow.findByType('form')[0].getForm().setValues(parent);
}

function showPostWindow(parent) {
	var postWindow = new Ext.Window( {
		title : '添加组织结构',
		layout : 'fit',
		width : 600,
		height : 300,
		plain : true,
		bodyStyle : 'padding:5px;',
		maximizable : false,
		modal : true,
		closeAction : 'close',
		items : [ {
			title : '岗位信息',
			bodyStyle : 'padding:5px',
			xtype : 'form',
			frame : 'true',
			items : [ {
				layout : 'column',
				border : false,
				items : [ {
					columnWidth : .5,
					layout : 'form',
					border : false,
					items : [ {
						name : 'id',
						xtype : 'hidden'
					}, {
						name : 'name',
						fieldLabel : '岗位名',
						xtype : 'textfield',
						vtype : 'name',
						allowBlank : false
					}, {
						name : 'remark',
						xtype : 'textfield',
						fieldLabel : '备注',
						anchor : '95%'
					} ]
				}, {
					columnWidth : .5,
					layout : 'form',
					border : false,
					items : [ {
						name : 'admin',
						fieldLabel : '负责人',
						anchor : '95%',
						xtype : 'combo',
						mode : 'local',
						allowBlank : true,
						triggerAction : 'all',
						maxHeight : 230,
						tree : {
							url : 'sysOrganService.findUserTree',
							id : '-1',
							leaf : true,
							value : 'user'
						},
						onSelect : Ext.emptyFn
					} ]
				} ]
			} ],
			buttons : [
					{
						text : '保存退出',
						handler : function() {
							var formPanel = this.ownerCt.ownerCt;
							if (!formPanel.getForm().isValid())
								return;
							var values = $.getFormValues(formPanel);
							insertNode('sysOrganService.insertPost', values,
									'P', parent);
							postWindow.close();
						}
					},
					{
						text : '保存&继续',
						handler : function() {
							var formPanel = this.ownerCt.ownerCt;
							if (!formPanel.getForm().isValid())
								return;
							var values = $.getFormValues(formPanel);
							insertNode('sysOrganService.insertPost', values,
									'P', parent);
							formPanel.getForm().reset();
						}
					}, {
						text : '关闭',
						handler : function() {
							postWindow.close();
						}
					} ]
		} ]
	});
	postWindow.show();
}
function showOrganWindow(parent) {
	var organWindow = new Ext.Window( {
		title : '添加组织结构',
		layout : 'fit',
		width : 600,
		height : 300,
		bodyStyle : 'padding:5px;',
		maximizable : false,
		closeAction : 'close',
		closable : false,
		collapsible : true,
		modal : true,
		items : [ {
			title : '局室信息',
			bodyStyle : 'padding:5px',
			xtype : 'form',
			frame : 'true',
			items : [ {
				layout : 'column',
				border : false,
				items : [ {
					columnWidth : .5,
					layout : 'form',
					border : false,
					items : [ {
						name : 'id',
						xtype : 'hidden'
					}, {
						name : 'code',
						xtype : 'textfield',
						fieldLabel : '编码',
						allowBlank : false,
						vtype : 'alphanum',
						anchor : '95%'
					}, {
						name : 'type',
						anchor : '95%',
						fieldLabel : '类型',
						xtype : 'combo',
						readOnly : true,
						store : organTypeDS,
						valueField : 'value',
						triggerAction : 'all',
						displayField : 'name',
						maxHeight : 230
					}, {
						name : 'admin',
						fieldLabel : '主管领导',
						anchor : '95%',
						xtype : 'combo',
						readOnly : true,
						mode : 'local',
						triggerAction : 'all',
						maxHeight : 230,
						tree : {
							url : 'sysOrganService.findUserTree',
							id : '-1',
							leaf : true
						},
						listeners : {
							"select" : comboSelect
						},
						onSelect : Ext.emptyFn
					}, {
						name : 'address',
						xtype : 'textfield',
						fieldLabel : '地址',
						anchor : '95%'
					}, {
						name : 'phone',// 09-11-09 添加验证
						xtype : 'textfield',
						fieldLabel : '固定电话',
						vtype : 'phone',
						anchor : '95%'
					}, {
						xtype : 'textfield',
						fieldLabel : '传真',
						vtype : 'phone',
						anchor : '95%'
					} ]
				}, {
					columnWidth : .5,
					layout : 'form',
					border : false,
					items : [ {
						name : 'name',
						xtype : 'textfield',
						fieldLabel : '名称',
						vtype : 'name',
						allowBlank : false,
						anchor : '95%'
					}, {
						name : 'state',
						xtype : 'textfield',
						fieldLabel : '状态',
						anchor : '95%'
					}, {
						name : 'responsibility',
						xtype : 'textfield',
						fieldLabel : '主要职责',
						anchor : '95%'
					}, {
						name : 'email',
						xtype : 'textfield',
						fieldLabel : '邮箱',
						vtype : 'email',
						anchor : '95%'
					}, {
						name : 'website',
						xtype : 'textfield',
						fieldLabel : '网址',
						vtype : 'url',
						anchor : '95%'
					} ]
				} ]
			} ],
			buttons : [
					{
						text : '保存退出',
						handler : function() {
							var formPanel = this.ownerCt.ownerCt;
							if (!formPanel.getForm().isValid())
								return;
							var values = $.getFormValues(formPanel);
							insertNode('sysOrganService.insertOrgan', values,
									'C', parent);
							organWindow.close();
						}
					},
					{
						text : '保存&继续',
						handler : function() {
							var formPanel = this.ownerCt.ownerCt;
							if (!formPanel.getForm().isValid())
								return;
							var values = $.getFormValues(formPanel);
							insertNode('sysOrganService.insertOrgan', values,
									'C', parent);
							formPanel.getForm().reset();
						}
					}, {
						text : '关闭',
						handler : function() {
							organWindow.close();
						}
					} ]
		} ]
	});
	organWindow.show();
	if (typeof parent == 'object')
		organWindow.findByType('form')[0].getForm().setValues(parent);
}
function deleteNode(btn) {
	Ext.Msg.confirm('信息', '确定要删除？', function(sure) {
		if (sure == 'yes') {
			var currNode2 = tree.getSelectionModel().getSelectedNode();
			if (currNode2.hasChildNodes()) {
				$.msg('该节点还有子节点,不能删除');
				return;
			}
			$.call(btn.url, currNodeId, function() {
				var currNode = tree.getSelectionModel().getSelectedNode();
				if (currNode.parentNode) {
					treeClick(currNode.parentNode);
					currNode.parentNode.select();
				}
				currNode.remove();
				$.msg('操作成功');
			});
		}
	});
}

function updatePanel(url, values, type) {
	var res = $.call(url, values);
	if (typeof res.result == 'object') {
		for ( var i = 0; i < res.result.length; i++) {
			var node = tree.getNodeById(res.result[i]);
			if (node) {
				node.setText(values.name);
			}
		}
	} else {
		var node = tree.getNodeById(res.result);
		if (node) {
			node.setText(values.name);
		}
	}
}
function insertNode(url, values, type, parent) {
	var res = $.call(url, values, parent);
	if (res.result["error"]) {
		return;
	}
	values["id"] = res.result["objId"];
	var icon = null;
	switch (type) {
	case 'C':
		icon = "images/comp.gif";
		if (!!currNodeId && currNodeId == parent)
			organDS.insert(0, new Ext.data.Record(values, res.result["objId"]));
		break;
	case 'P':
		icon = "images/post.gif";
		if (!!currNodeId && currNodeId == parent)
			postDS.insert(0, new Ext.data.Record(values, res.result["objId"]));
		break;
	case 'U':
		icon = "images/user.gif";
		if (!!currNodeId && currNodeId == parent)
			userDS.insert(0, new Ext.data.Record(values, res.result["objId"]));
	}
	var parentNode = tree.getNodeById(parent);
	if (!parentNode.isExpanded()) {
		parentNode.expand(false);
	}
	parentNode.appendChild(new Ext.tree.TreeNode( {
		id : res.result["treeId"],
		text : values.name,
		type : type,
		icon : icon
	}));
}
function changeUserPassword() {
	passwordwindow.show();
}
var passwordwindow = new Ext.Window( {
	title : '修改密码',
	frame : true,
	resizable : false,
	width : 300,
	height : 140,
	plain : true,
	layout : 'form',
	labelAlign : "right",
	buttonAlign : 'center',
	closable : true,
	bodyStyle : 'padding: 10px',
	closeAction : 'hide',
	items : [ {
		xtype : 'form',
		id : 'pform',
		plain : true,
		baseCls : 'x-plain',
		defaults : {
			xtype : 'textfield',
			width : 160
		},
		labelWidth : 65,
		items : [ {
			xtype : 'textfield',
			fieldLabel : '设定密码',
			msgTarget : 'side',
			id : 'newpassword',
			name : 'newpassword',
			minLength : 6,
			maxLength : 20,
			inputType : 'password',
			allowBlank : false
		}, {
			xtype : 'textfield',
			fieldLabel : '确认密码',
			msgTarget : 'side',
			id : 'conpassword',
			name : 'conpassword',
			inputType : 'password',
			vtype : 'password',
			vtypeText : "两次输入的密码不一致！",
			confirmTo : 'newpassword',
			allowBlank : false
		} ]
	} ],
	buttons : [
			{
				text : '确定',
				handler : function() {
					var pform = Ext.getCmp('pform');
					var uform = Ext.getCmp('userform');
					if (!pform.getForm().isValid())
						return;
					var uservalues = $.getFormValues(uform);
					var passwordvalues = $.getFormValues(pform);
					$.call('sysOrganService.alterPassword', uservalues.id,
							passwordvalues.newpassword);
					passwordwindow.close();
				}
			}, {
				text : '清空',
				handler : function() {
					Ext.getCmp('pform').getForm().reset();
				}
			} ]
});
