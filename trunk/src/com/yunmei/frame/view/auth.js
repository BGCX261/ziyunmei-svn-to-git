//  
var roleSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true,
	checkOnly : true,
	listeners : {
		"beforerowselect" : function(sm, index, keepExisting, rec) {
			if (rec.id < 0) {
				$.msg('请先保存该角色');
				return false;
			}
			var tree = Ext.getCmp('tree');
			var selIds = tree.getChecked('id');
			$.call("sysAuthService.getAuthsByRoleId", rec.id, function(result) {
				tree.un("checkchange", treeCheckChange);
				while (id = selIds.shift()) {
					node = tree.getNodeById(id);
					if (node) {
						node.checked = false;
						node.ui.toggleCheck(false);
					}
				}
				while (id = result.shift()) {
					node = tree.getNodeById(id);
					if (node) {
						if (!node.isExpanded()) {
							node.expand();
						}
						node.checked = true;
						node.ui.toggleCheck(true);
					}
				}
				tree.on("checkchange", treeCheckChange);
			});
		},
		"rowdeselect" : function() {
			var tree = Ext.getCmp('tree');
			var selIds = tree.getChecked('id');
			tree.un("checkchange", treeCheckChange);
			while (id = selIds.shift()) {
				node = tree.getNodeById(id);
				if (!node)
					continue;
				node.checked = false;
				node.ui.toggleCheck(false);
			}
			tree.on("checkchange", treeCheckChange);
		}
	}
});
var comboDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findFilters'),
	fields : [ 'text', 'id' ]
});
var roleDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findRoles'),
	fields : [ 'id', 'name', 'remark' ],
	root : "results",
	totalProperty : "total"
});
var roleCM = new Ext.grid.ColumnModel( [ roleSM, {
	header : "角色名",
	dataIndex : "name",
	width : 120,
	editor : new Ext.form.TextField( {
		allowBlank : false
	})
}, {
	header : "备注",
	dataIndex : "remark",
	width : 210,
	editor : new Ext.form.TextField()
} ]);

function comboSelect(combo, rec) {
	var user_tree = Ext.getCmp('tree');
	user_tree.getLoader().dataUrl = $.treeURL('sysAuthService.findAuths', rec
			.get('id'));
	user_tree.root.reload();
	roleSM.clearSelections();
	return true;
}
function treeCollapse(btn) {
	btn.ownerCt.ownerCt.root.eachChild(function(node) {
		node.collapse();
	});
}
function treeCheckChange(node, checked) {
	node.attributes.checked = checked;
	if (checked == true && node.parentNode
			&& node.parentNode.attributes.checked == false) {
		Ext.getCmp('tree').un("checkchange", treeCheckChange);
		checkParent(node.parentNode, checked);
		Ext.getCmp('tree').on("checkchange", treeCheckChange);
	}
	node.expand(false, true, function() {
		node.eachChild(function(child) {
			child.ui.toggleCheck(checked);
			child.attributes.checked = checked;
		});
	});
}
function checkParent(node, checked) {
	if (node) {
		node.ui.toggleCheck(checked);
		node.attributes.checked = checked;
		checkParent(node.parentNode, checked);
	}
}
function doSelect() {
	var params = $.getFormValues('roleForm');
	roleDS.load( {
		params : [ params, 0, 20 ]
	});
}
function doSaveAuth(btn) {
	var record = roleSM.getSelected();
	if (!record) {
		$.msg('请先选择左边的角色');
		return;
	}
	var selIds = Ext.getCmp('tree').getChecked('id');
	for ( var i = 0; i < selIds.length; i++) {
		if (selIds[i] == -1) {
			selIds.splice(i, 1);
		}
	}
	$.call(btn.url, record.id, selIds, function() {
		$.msg();
	});
}
function doReset() {
	Ext.getCmp('roleForm').getForm().reset();
}
function doInsert() {
	$.insert('roleGrid', {
		name : '',
		remark : ''
	});
}
function doDelete() {
	var rec = roleSM.getSelected();
	if (rec) {
		if (rec.id < 0)
			roleDS.remove(rec);
		else
			Ext.Msg.confirm('信息', '确定要删除？', function(btn) {
				if (btn == 'yes') {
					$.call("sysAuthService.deleteRole", rec.id, function() {
						roleDS.remove(rec);
						$.msg();
					});
				}
			});

	} else
		$.msg('请先选择左边的角色');
}
function doSave(btn) {
	$.save('roleGrid', btn.url);
}

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
				xtype : 'editorgrid',
				tbar : [ {
					text : "增加",
					url : 'sysAuthService.saveRoles',
					handler : doInsert
				}, {
					text : '删除',
					url : 'sysAuthService.saveRoles',
					handler : doDelete
				}, {
					text : '保存',
					url : 'sysAuthService.saveRoles',
					handler : doSave
				} ],
				ds : roleDS,
				cm : roleCM,
				sm : roleSM,
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
			id : 'tree',
			title : '权限树',
			xtype : 'treepanel',
			useArrows : true,
			autoScroll : true,
			rootVisible : false,
			region : "center",
			dataUrl : $.treeURL('sysAuthService.findAuths', -1),
			listeners : {
				"checkchange" : treeCheckChange
			},
			root : {
				id : '-1'
			},
			tbar : [ {
				name : 'filterValue',
				anchor : '95%',
				xtype : 'combo',
				triggerAction : 'all',
				store : comboDS,
				enableKeyEvents : true,
				valueField : 'id',
				displayField : 'text',
				maxHeight : 230,
				listeners : {
					"select" : comboSelect
				}
			}, {
				text : "收起",
				handler : treeCollapse
			}, {
				text : "保存",
				url : 'sysAuthService.insertAuthToRole',
				handler : doSaveAuth
			} ]
		} ]
	});
});// end ready
