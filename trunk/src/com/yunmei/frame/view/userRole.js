var userSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true
});
var userCM = new Ext.grid.ColumnModel( [ userSM, {
	header : "用户名",
	dataIndex : "name",
	width : 120,
	sortable : true
}, {
	header : "登录名",
	dataIndex : "login",
	width : 120,
	sortable : true
} ]);
var userDS = new Ext.data.JsonStore( {
	url : $.URL('sysOrganService.findUsers'),
	root : "results",
	totalProperty : "total",
	fields : [ 'name', 'login' ]
});
var roleSM = new Ext.grid.CheckboxSelectionModel( {
	listeners : {
		"beforerowselect" : function(r1, r2, r3, r4) {
			if (r4.data.type == "岗位角色") {
				$.msg('不允许操作岗位角色');
				return false;
			}
			return true;
		}
	}
});
var roleCM = new Ext.grid.ColumnModel( [ roleSM, {
	header : "名称",
	dataIndex : "name",
	width : 120,
	sortable : true
}, {
	header : "角色类型",
	dataIndex : "type",
	width : 120,
	sortable : true
}, {
	header : "备注",
	dataIndex : "remark",
	width : 120,
	sortable : true
} ]);
var roleDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findRolesByUserId'),
	fields : [ 'id', 'name', 'type', 'remark' ]
});
var roleWinSM = new Ext.grid.CheckboxSelectionModel();
var roleWinCM = new Ext.grid.ColumnModel( [ roleWinSM, {
	header : "ID",
	dataIndex : "id",
	width : 120,
	sortable : true
}, {
	header : "名称",
	dataIndex : "name",
	width : 120,
	sortable : true
}, {
	header : "备注",
	dataIndex : "remark",
	width : 120,
	sortable : true
} ]);
var roleWinDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findDirectNotRoles'),
	root : "results",
	totalProperty : "total",
	fields : [ 'id', 'name', 'remark' ]
});

var roleWin;

function insertRoleWin() {
	var rec = userSM.getSelected();
	if (!rec) {
		Ext.Msg.alert('', '请先选择用户');
		return;
	}
	roleWin = new Ext.Window( {
		title : '添加用户角色',
		layout : 'fit',
		width : 500,
		modal : true,
		height : 390,
		plain : true,
		bodyStyle : 'padding:5px;',
		maximizable : false,
		closeAction : 'close',
		closable : true,
		items : [ {
			bodyStyle : 'padding:5px',
			xtype : 'form',
			tbar : [ {
				text : '查询',
				handler : function() {
					var rec = userSM.getSelected();
					roleWinDS.load( {
						params : [ rec.id, 0, 20 ]
					});
				}
			} ],
			frame : 'true',
			items : [ {
				collapsible : true,
				autoHeight : true,
				layout : 'table',
				layoutConfig : {
					column : 2
				},
				items : [ {
					layout : 'form',
					items : [ {
						name : 'name',
						xtype : 'textfield',
						fieldLabel : '角色名',
						width : '110'
					} ]
				}, {
					layout : 'form',
					items : [ {
						name : 'remark',
						xtype : 'textfield',
						fieldLabel : '备注',
						width : '110'
					} ]
				} ]
			}, {
				xtype : 'grid',
				height : 280,
				ds : roleWinDS,
				cm : roleWinCM,
				sm : roleWinSM,
				bbar : new Ext.PagingToolbar( {
					store : roleWinDS,
					displayMsg : '显示第{0}条到{1}条记录,一共{2}条',
					displayInfo : true,
					pageSize : 20,
					prependButtons : true
				}),
				tbar : [ {
					text : "选取",
					handler : insertRole2User
				} ]
			} ]
		} ]
	});
	roleWin.show();
}
function deleteDirectRoles() {
	var user = userSM.getSelected();
	var recs = roleSM.getSelections();
	if (recs.length == 0) {
		$.msg('请选择相应记录');
		return;
	}
	Ext.Msg.confirm('信息', '确定要删除？', function(btn) {
		if (btn == 'yes') {
			var roleIds = [];
			for ( var i = 0; i < recs.length; i++) {
				roleIds.push(recs[i].id);
			}
			$.call("sysAuthService.deleteDirectRoles", user.id, roleIds,
					function() {
						for ( var i = 0; i < recs.length; i++) {
							roleDS.remove(recs[i]);
						}
						$.msg('操作成功');
					});
		}
	});
}
function insertRole2User() {
	var recs = roleWinSM.getSelections();
	var roleIds = [];
	for ( var i = 0; i < recs.length; i++) {
		roleIds.push(recs[i].id);
	}
	var rec = userSM.getSelected();
	$.call("sysAuthService.insertRole2User", roleIds, rec.id, function() {
		for ( var i = 0; i < recs.length; i++) {
			recs[i].data.type = "用户角色";
			roleDS.insert(0, recs[i]);
		}
		roleWin.close();
	});
}
Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : "border",
		items : [ {
			id : 'userForm',
			title : "人员信息",
			region : 'west',
			xtype : 'form',
			frame : true,
			width : '400',
			tbar : [ {
				text : '查询',
				handler : function() {
					var values = $.getFormValues(this.ownerCt.ownerCt);
					userDS.load( {
						params : [ values, 0, 20 ]
					});
				}
			} ],
			items : [ {
				labelWidth : 40,
				autoHeight : true,
				layout : 'column',
				items : [ {
					columnWidth : .5,
					layout : 'form',
					labelAlign : "right",
					labelWidth : 70,
					items : [ {
						id : 'name',
						xtype : 'textfield',
						fieldLabel : '用户名',
						width : '140',
						anchor : '95%'
					} ]
				}, {
					columnWidth : .5,
					layout : 'form',
					labelAlign : "right",
					labelWidth : 70,
					items : [ {
						id : 'login',
						xtype : 'textfield',
						fieldLabel : '登录名',
						width : '140',
						anchor : '95%'
					} ]
				} ]
			}, {
				id : "userGrid",
				xtype : 'grid',
				height : 400,
				ds : userDS,
				cm : userCM,
				sm : userSM,
				listeners : {
					"rowclick" : function(grid, index) {
						var rec = userSM.getSelected();
						if (rec) 
							roleDS.load( {
								params : rec.id
							});
						 else 
							roleDS.removeAll();
					}
				},
				bbar : [ new Ext.PagingToolbar( {
					store : userDS,
					displayMsg : '显示第{0}条到{1}条记录,一共{2}条',
					displayInfo : true,
					pageSize : 20,
					prependButtons : true
				}) ]
			} ]
		}, {
			xtype : 'grid',
			title : '角色信息',
			region : 'center',
			tbar : [ {
				text : '增加',
				url : 'sysAuthService.insertRole2User',
				handler : insertRoleWin
			}, {
				text : '删除',
				url : 'sysAuthService.insertRole2User',
				handler : deleteDirectRoles
			} ],
			height : 200,
			ds : roleDS,
			cm : roleCM,
			sm : roleSM
		} ]
	});
});