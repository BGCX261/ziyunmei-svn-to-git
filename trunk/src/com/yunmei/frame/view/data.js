var roleDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findRoles'),
	fields : [ 'id', 'name', 'remark' ],
	root : "results",
	totalProperty : "total"
});
var roleCM = new Ext.grid.ColumnModel( [ {
	header : "ID",
	dataIndex : "id",
	width : 70
}, {
	header : "角色名",
	dataIndex : "name",
	width : 100
}, {
	header : "备注",
	dataIndex : "remark",
	width : 210
} ]);

var showDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findDataFields'),
	fields : [ 'field', 'value', 'order' ]
});
var showCM = new Ext.grid.ColumnModel( [ {
	header : "数据库字段",
	dataIndex : "field",
	width : 80
}, {
	header : "显示名",
	dataIndex : "value",
	width : 120
} ]);

var dataDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findDataAuthsByRoleId'),
	fields : [ 'filterSQL', 'authName', 'remark' ]
});
var dataCM = new Ext.grid.ColumnModel( [ {
	header : "权限名",
	dataIndex : "authName",
	width : 120
}, {
	header : "表达式",
	dataIndex : "filterSQL",
	width : 200
}, {
	header : "备注",
	dataIndex : "remark",
	width : 200
} ]);

Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : "border",
		items : [ {
			id : 'roleForm',
			title : "角色列表",
			xtype : 'form',
			frame : 'true',
			region : "west",
			tbar : [ {
				text : '查询',
				handler : doSelect
			}, {
				text : '重置',
				handler : doReset
			} ],
			width : 500,
			items : [ {
				layout : 'column',
				items : [ {
					columnWidth : .5,
					layout : 'form',
					labelAlign : "right",
					labelWidth : 70,
					items : [ {
						name : 'name',
						xtype : 'textfield',
						fieldLabel : '角色名',
						width : '110'
					} ]
				}, {
					columnWidth : .5,
					layout : 'form',
					labelAlign : "right",
					labelWidth : 70,
					items : [ {
						name : 'remark',
						xtype : 'textfield',
						fieldLabel : '备注',
						width : '110'
					} ]
				} ]

			}, {
				id : 'roleGrid',
				height : 300,
				xtype : 'grid',
				listeners : {
					"rowclick" : roleGridRowClick
				},
				ds : roleDS,
				cm : roleCM,
				autoExpandColumn : 2,
				bbar : new Ext.PagingToolbar( {
					store : roleDS,
					displayMsg : '显示第{0}条到{1}条记录,一共{2}条',
					displayInfo : true,
					pageSize : 20,
					prependButtons : true
				})
			} ]
		}, {
			id : 'dataGrid',
			height : 300,
			region : 'center',
			xtype : 'grid',
			tbar : [ {
				text : "增加",
				url : "sysAuthService.deleteDataFromRole",
				handler : insertDataWin
			}, {
				text : '删除',
				url : "sysAuthService.deleteDataFromRole",
				handler : deleteData
			} ],
			store : dataDS,
			cm : dataCM
		} ]
	});
});
function roleGridRowClick(grid, rowIndex) {
	var rec = grid.selModel.getSelected();
	if (!rec) {
		dataDS.removeAll();
	} else {
		dataDS.load( {
			params : [ rec.id ]
		});
	}
}
function deleteData() {
	var rec = Ext.getCmp('dataGrid').selModel.getSelected();
	if (!rec) {
		$.msg('请先选择要删除的表达式');
		return;
	}
	Ext.Msg.confirm('信息', '确定要删除？', function(btn) {
		if (btn == 'yes') {
			$.call("sysAuthService.deleteDataFromRole", rec.id, function() {
				dataDS.remove(rec);
			});
		}
	});
}
function insertDataWin() {
	var rec = Ext.getCmp('roleGrid').selModel.getSelected();
	if (!rec) {
		$.msg('请先选择角色');
		return;
	}
	showDS.removeAll();
	roleWin = new Ext.Window( {
		id : 'roleWin',
		title : '数据过滤定义',
		width : 500,
		modal : true,
		height : 300,
		layout : 'column',
		bodyStyle : 'padding:5px;',
		maximizable : false,
		closeAction : 'close',
		closable : true,
		tbar : [ {
			text : '保存',
			handler : saveFilter
		}, {
			text : '关闭',
			handler : function() {
				Ext.getCmp("roleWin").close();
			}
		} ],
		items : [ {
			id : 'showForm',
			xtype : 'form',
			layout : 'column',
			frame : true,
			defaults : {
				layout : 'form',
				border : false,
				labelWidth : 60,
				columnWidth : .5
			},
			items : [ {
				items : [ {
					id : 'authId',
					fieldLabel : '过滤点',
					anchor : '95%',
					xtype : 'combo',
					mode : 'local',
					triggerAction : 'all',
					maxHeight : 230,
					tree : {
						url : 'sysAuthService.findDataMenus',
						id : '-1',
						leaf : true
					},
					listeners : {
						"select" : comboChange
					},
					allowBlank : false,
					onSelect : Ext.emptyFn
				} ]
			}, {
				items : [ {
					fieldLabel : '备注',
					name : 'remark',
					xtype : 'textfield',
					anchor : '100%'
				} ]
			}, {
				columnWidth : 1,
				items : [ {
					fieldLabel : '过滤语句',
					name : 'filterSQL',
					xtype : 'textarea',
					anchor : '100%',
					allowBlank : false
				} ]
			}, {
				columnWidth : 1,
				items : [ {
					id : 'showGrid',
					height : 150,
					region : 'west',
					xtype : 'grid',
					ds : showDS,
					cm : showCM,
					anchor : '100%'
				} ]
			} ]
		} ]
	});
	roleWin.show();
}

function doSelect() {
	// var params = Ext.getCmp('roleForm').getForm().getValues(false);
	// $.deleteNullAndLike(params);
	var params = $.getFormValues('roleForm');
	roleDS.load( {
		params : [ params, 0, 20 ]
	});
}
function comboChange(field, newVal, oldVal) {
	showDS.load( {
		params : field.getValue()
	});
}
function saveFilter() {
	var rec = Ext.getCmp('roleGrid').selModel.getSelected();
	var form = Ext.getCmp('showForm').getForm();
	if (!form.isValid()) {
		return;
	}
	var params = form.getValues(false);
	params.roleId = rec.id;
	params.authId = Ext.getCmp('authId').getValue();
	params.authName = Ext.getCmp('authId').lastSelectionText;
	params.order = showDS.getAt(0).get('order');
	$.call('sysAuthService.insertFilter2Role', params, function(result) {
		var p = new Ext.data.Record(params, result);
		dataDS.removeAll();
		dataDS.insert(0, p);
		Ext.getCmp("roleWin").close();
	});
}

function doReset() {
	Ext.getCmp('roleForm').getForm().reset();
}
