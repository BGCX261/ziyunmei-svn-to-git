var userSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true
});
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
var userCM = new Ext.grid.ColumnModel( [ userSM, {
	header : "编码",
	dataIndex : "code",
	width : 60,
	sortable : true
}, {
	header : "用户名",
	dataIndex : "name",
	width : 120
}, {
	header : "登录名",
	dataIndex : "login",
	width : 120
}, {
	header : "性别",
	dataIndex : "sex",
	width : 60,
	renderer : function(value, cellmeta, record) {
		var index = sexGridDS.find('value', value);
		var record = sexGridDS.getAt(index);
		return record ? record.data.name : value;
	}
}, {
	header : "专业",
	dataIndex : "subject",
	width : 120

}, {
	header : "最高学历",
	dataIndex : "education",
	width : 120,
	renderer : function(value, cellmeta, record) {
		var index = educationUserDS.find('value', value);
		var record = educationUserDS.getAt(index);
		return record ? record.data.name : value;
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
	header : "邮箱",
	dataIndex : "email",
	width : 120
}, {
	header : "备注",
	dataIndex : "remark",
	width : 120
} ]);
var userDS = new Ext.data.JsonStore( {
	url : $.URL('sysTempUserService.find'),
	root : "results",
	totalProperty : "total",
	fields : [ 'id', 'name', 'code', 'login', 'subject', 'education',
			'birthday', 'sex', 'tphone', 'mphone', 'email', 'remark' ]
});
var sexGridDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'sex'),
	fields : [ 'name', 'value' ]
});
sexGridDS.load();
var sexWinUpdateDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'sex'),
	fields : [ 'name', 'value' ]
});
var educationUserDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'education'),
	fields : [ 'name', 'value' ]
});
educationUserDS.load();
// ---------------------------------用户面板-----------------------------------------------------
var userPanel = new Ext.FormPanel( {
	bodyStyle : 'padding:5px',
	frame : 'true',
	region : "center",
	tbar : [ {
		text : '查询',
		handler : doSelect
	}, {
		text : '重置',
		handler : doReset
	} ],
	items : [ {
		autoHeight : true,
		labelWidth : '40',
		layout : 'column',
		items : [ {
			columnWidth : .33,
			layout : 'form',
			items : [ {
				name : 'code',
				xtype : 'textfield',
				fieldLabel : '编码',
				anchor : '95%'
			} ]
		}, {
			columnWidth : .33,
			layout : 'form',
			items : [ {
				name : 'name',
				xtype : 'textfield',
				fieldLabel : '用户名',
				anchor : '95%'
			} ]
		}, {
			columnWidth : .33,
			layout : 'form',
			items : [ {
				name : 'login',
				xtype : 'textfield',
				fieldLabel : '登录名',
				anchor : '95%'
			} ]
		} ]

	}, {
		id : "userGrid",
		title : "人员信息",
		xtype : 'editorgrid',
		height : 400,
		tbar : [ {
			text : "增加",
			url : 'sysTempUserService.insert',
			handler : doInsert
		}, {
			text : '删除',
			url : 'sysTempUserService.insert',
			handler : doDelete
		}, {
			text : '修改',
			url : 'sysTempUserService.insert',
			handler : doUpdate
		}, {
			text : '修改密码',
			url : 'sysTempUserService.insert',
			handler : changePassword
		}, {
			text : '转正',
			url : 'sysTempUserService.turnNormal',
			handler : changePost
		} ],
		ds : userDS,
		cm : userCM,
		sm : userSM,
		listeners : {
			'render' : function() {
				var params = $.getFormValues(userPanel);
				userDS.load( {
					params : [ params, 0, 20 ]
				});
			}
		},
		bbar : new Ext.PagingToolbar( {
			store : userDS,
			displayInfo : true,
			pageSize : 20,
			prependButtons : true
		})
	} ]
});
function doSelect() {
	$.mask();
	var params = $.getFormValues(userPanel);
	userDS.load( {
		params : [ params, 0, 20 ]
	});
	$.unmask();
}
function doReset() {
	userPanel.getForm().reset();
}
function doInsert() {
	showUserWindow();
}
function doDelete() {
	$._delete('userGrid', 'sysTempUserService.delete');
}
function doUpdate() {
	var records = userSM.getSelections();
	if (records.length == 0) {
		$.msg('请选择要更新的记录');
		return;
	}
	showUserUpdateWindow(records[0].data);
}
var userWindow;
function showUserWindow(user) {
	userWindow = new Ext.Window( {
		layout : 'fit',
		width : 600,
		height : 300,
		modal : true,
		bodyStyle : 'padding:5px;',
		maximizable : false,
		closeAction : 'close',
		closable : true,
		plain : true,
		items : [ {
			title : '用户信息',
			id : 'userWinForm',
			bodyStyle : 'padding:5px',
			xtype : 'form',
			frame : true,
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
						name : 'login',
						xtype : 'textfield',
						fieldLabel : '登录名',
						vtype : 'alphanum',
						anchor : '95%',
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
						store : sexWinUpdateDS,
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
						vtype : 'mphone',
						anchor : '95%'
					} ]
				} ]
			} ],
			buttons : [ {
				text : '保存退出',
				handler : doSave
			}, {
				text : '保存继续',
				handler : doSaveAndContinue
			}, {
				text : '关闭',
				handler : function() {
					userWindow.close();
				}
			} ]
		} ]
	});
	userWindow.show();
}
function showUserUpdateWindow(user) {
	userWindow = new Ext.Window( {
		layout : 'fit',
		width : 600,
		height : 300,
		title : '用户信息',
		modal : true,
		bodyStyle : 'padding:5px;',
		maximizable : false,
		closeAction : 'close',
		closable : true,
		plain : true,
		items : [ {
			id : 'userWinUpdateForm',
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
						name : 'login',
						xtype : 'textfield',
						fieldLabel : '登录名',
						anchor : '95%',
						allowBlank : false
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
					}, {
						name : 'mphone',
						xtype : 'textfield',
						fieldLabel : '手机',
						vtype : 'mphone',
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
						store : sexWinUpdateDS,
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
					} ]
				} ]
			} ],
			buttons : [ {
				text : '保存退出',
				handler : doSaveUpdate
			}, {
				text : '关闭',
				handler : function() {
					userWindow.close();
				}
			} ]
		} ]
	});
	userWindow.show();
	sexWinUpdateDS.load( {
		callback : function() {
			Ext.getCmp('userWinUpdateForm').getForm().setValues(user);
		}
	});
}
function doSaveUpdate() {
	var userWinForm = Ext.getCmp('userWinUpdateForm');
	if (!userWinForm.getForm().isValid())
		return;
	var values = $.getFormValues(userWinForm);
	$.call('sysTempUserService.update', values, function() {
		$.mask();
		var params = $.getFormValues(userPanel);
		userDS.load( {
			params : [ params, 0, 20 ]
		});
		$.unmask();
		userWindow.close();
	});

}
function doSave() {
	var userWinForm = Ext.getCmp('userWinForm');
	if (!userWinForm.getForm().isValid())
		return;
	var values = $.getFormValues(userWinForm);
	$.call('sysTempUserService.insert', values, function(id) {
		$.mask();
		var params = $.getFormValues(userPanel);
		userDS.load( {
			params : [ params, 0, 20 ]
		});
		$.unmask();
		userWindow.close();
	});

}
function doSaveAndContinue(btn) {
	var userWinForm = Ext.getCmp('userWinForm');
	if (!userWinForm.getForm().isValid())
		return;
	var values = $.getFormValues(userWinForm);
	$.call('sysTempUserService.insert', values, function() {
		$.mask();
		var params = $.getFormValues(userPanel);
		userDS.load( {
			params : [ params, 0, 20 ]
		});
		$.unmask();
		userWinForm.getForm().reset();
	});
}

function changePassword() {
	var records = userSM.getSelections();
	if (records.length == 0) {
		$.msg('请选择要修改的记录');
		return;
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
				xtype : 'textfield',// 09-11-09 添加验证
				fieldLabel : '确认密码',
				msgTarget : 'side',
				id : 'conpassword',
				name : 'conpassword',
				inputType : 'password',
				vtype : 'password',
				confirmTo : 'newpassword', // 指定比较的对象
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
						var userId = records[0].id;
						var passwordvalues = $.getFormValues(pform);
						$.call('sysOrganService.alterPassword', userId,
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
	passwordwindow.show();
}
var organWindow;
function changePost() {
	var records = userSM.getSelections();
	if (records.length == 0) {
		$.msg('请选择要修改的记录');
		return;
	}
	organWindow = new Ext.Window( {
		title : '组织机构',
		id : 'postwin',
		width : 300,
		height : 400,
		modal : true,
		bodyStyle : 'padding:5px;',
		maximizable : false,
		closable : true,
		frame : true,
		items : [ {
			xtype : 'treepanel',
			useArrows : false,
			autoScroll : true,
			animate : true,
			baseCls : 'x-plain',
			containerScroll : true,
			rootVisible : false,
			root : {
				id : "-1"
			},
			listeners : {
				"click" : selectOrgan,
				"beforeload" : function(node) {
					this.loader.dataUrl = $.treeURL(
							'sysOrganService.findPostTree', node.id);
				}
			}
		} ]
	}).show();

}
Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : "border",
		items : [ userPanel ]
	});
});
function selectOrgan(node) {
	var record = userSM.getSelected();
	var userid = record.id;
	if (node.isLeaf()) {
		$.call("sysTempUserService.turnNormal", userid, node.id, function() {
			var rec = userSM.getSelected();
			userDS.remove(rec);
			$.msg();
		});
		organWindow.close();
	} else
		$.msg("请选择岗位节点");
}