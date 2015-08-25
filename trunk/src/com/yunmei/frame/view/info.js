var passwordwindow;
var sexUserDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'sex'),
	fields : [ 'name', 'value' ]
});
var educationUserDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'education'),
	fields : [ 'name', 'value' ]
});
var userRoleDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findRolesByUserId'),
	fields : [ 'name', 'remark', 'type' ]
});
educationUserDS.load();
sexUserDS.load();

// -------------两次密码输入一致验证--------------------------------
Ext.apply(Ext.form.VTypes, {
	password : function(val, field) {
		if (field.confirmTo) {
			var pwd = Ext.get(field.confirmTo);
			if (val.trim() == pwd.getValue().trim()) {
				return true;
			} else {
				return false;
			}
			return false;
		}
	}
});
var usertbar = new Ext.Toolbar( {
	style : 'margin: -5 0 30 -5px;',
	items : []
});
var userRoleCM = new Ext.grid.ColumnModel( [ new Ext.grid.RowNumberer( {
	header : '+'
}), {
	header : "角色名",
	dataIndex : "name",
	align : "center",
	width : 120,
	sortable : true
}, {
	header : "类型",
	align : "center",
	dataIndex : "type",
	width : 120,
	sortable : true
}, {
	header : "备注",
	align : "center",
	dataIndex : "remark",
	width : 315,
	sortable : true
} ]);

var formpanel = new Ext.FormPanel( {
	shadow : true,
	region : 'center',
	frame : true,
	autoScroll : true,
	layout : 'column',
	tbar : [ {
		text : '保存',
		handler : doSaveUser
	}, {
		text : '修改密码',
		handler : doAlterPassword
	} ],
	defaults : {
		labelWidth : 60,
		labelAlign : 'right',
		columnWidth : .5,
		layout : 'form',
		border : false
	},
	items : [ {
		defaults : {
			xtype : 'textfield',
			anchor : '80%'
		},
		items : [ {
			name : 'id',
			xtype : 'hidden'
		}, {
			name : 'name',
			fieldLabel : '姓名',
			regex : /^[\u0391-\uFFE5]+$/,
			regexText : '请输入规范的名称!',
			allowBlank : false,
			blankText : '输入项不能为空!'
		}, {
			name : 'login',
			fieldLabel : '登录名',
			allowBlank : false,
			blankText : '输入项不能为空!'
		}, {
			name : 'tphone',
			xtype : 'textfield',
			fieldLabel : '固定电话',
			regex : /^\d{7,11}$/,
			regexText : '固定电话格式不正确!',
			emptyText : '010-66666666',
			anchor : '80%'
		}, {
			name : 'mphone',
			fieldLabel : '手机',
			regex : /^(13[0-9]|15[0|3|6|8|9])\d{8}$/,
			regexText : '手机号码格式不正确!'
		}, {
			name : 'email',
			fieldLabel : '邮箱',
			vtype : 'email',
			vtypeText : '邮箱格式不正确!'
		} ]
	}, {
		defaults : {
			xtype : 'textfield',
			anchor : '80%'
		},
		items : [ {
			name : 'code',
			fieldLabel : '编码',
			allowBlank : false,
			blankText : '输入项不能为空!',
			invalidText : '请填写正确的编码格式',
			vtype : 'alphanum',
			vtypeText : '编码格式不正确!'
		}, {
			name : 'sex',
			fieldLabel : '性别',
			xtype : 'combo',
			store : sexUserDS,
			mode : 'local',
			valueField : 'value',
			triggerAction : 'all',
			readOnly : true,
			displayField : 'name',
			maxHeight : 230
		}, {
			name : 'education',
			labelAlign : 'right',
			fieldLabel : '最高学历',
			xtype : 'combo',
			store : educationUserDS,
			mode : 'local',
			valueField : 'value',
			triggerAction : 'all',
			readOnly : true,
			displayField : 'name',
			maxHeight : 230
		}, {
			name : 'subject',
			fieldLabel : '专业'
		} ]
	}, {
		columnWidth : 1,
		layout : 'form',
		style : 'margin:20 0 20 5px;',
		border : false,
		items : [ {
			xtype : 'tabpanel',
			plain : true,
			frame : true,
			defaults : {
				baseCls : 'x-plain'
			},
			border : false,
			height : 200,
			activeTab : 0,
			items : [ {
				title : "岗位信息",
				items : [ {
					xtype : 'panel',
					border : true,
					frame : true,
					height : 200,
					layout : 'form',
					labelWidth : 80,
					bodyStyle : 'padding: 20 0 0 5px',
					items : [ {
						name : 'id',
						xtype : 'hidden'
					}, {
						name : 'name',
						xtype : 'textfield',
						width : 300,
						readOnly : true,
						fieldLabel : '岗位名'
					}, {
						name : 'admin',
						xtype : 'textfield',
						readOnly : true,
						width : 300,
						fieldLabel : '局室信息'
					}, {
						name : 'remark',
						xtype : 'textfield',
						readOnly : true,
						width : 300,
						fieldLabel : '备注'
					} ]
				} ],
				listeners : {
					activate : tabChange
				}
			}, {
				title : "角色信息",
				items : [ {
					xtype : 'grid',
					height : 200,
					frame : true,
					stripeRows : true,
					ds : userRoleDS,
					cm : userRoleCM
				} ],
				listeners : {
					activate : tabChange
				}
			} ]
		} ]
	} ],
	listeners : {
		'render' : loadUserData
	}
});
Ext.onReady(function() {
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'under';// 使错误标记显示在输入框下侧
		var myview = new Ext.Viewport( {
			layout : "border",
			items : [ formpanel ]
		});
	});
var userId = window.top.user.id;
function loadUserData() {
	var userResult = $.call("sysOrganService.getUser", userId);
	formpanel.getForm().setValues(userResult.result);
}
function tabChange(tab) {
	if (tab.title == '岗位信息') {
		var u = window.top.user;
		if (u.posts.length != 0) {
			var id = this.find("name", "id")[0];
			var name = this.find("name", "name")[0];
			id.setValue(u.posts[0].id);
			name.setValue(u.posts[0].name);
		}
		userRoleDS.load( {
			params : u.id
		});
	} else if (tab.title == '角色信息') {

	}
}

function conAlter() {
	var pform = Ext.getCmp('pform');
	if (!pform.getForm().isValid())
		return;
	var passwordvalues = $.getFormValues(pform);
	$.call('sysOrganService.alterPassword', userId, passwordvalues.oldpassword,
			passwordvalues.newpassword, function(res) {
				if (res == 'success') {
					passwordwindow.close();
				}
				if (res == 'failure') {
					var oldpasswordvalue = pform.items.first();
					oldpasswordvalue.setValue('');
					oldpasswordvalue.blankText = "旧密码不正确!";
				}
			});
}
function doAlterPassword() {
	passwordwindow = new Ext.Window( {
		title : '修改密码',
		frame : true,
		resizable : false,
		width : 300,
		height : 168,
		plain : true,
		layout : 'form',
		labelAlign : "right",
		buttonAlign : 'center',
		closable : true,
		bodyStyle : 'padding: 10px',
		items : [ {
			xtype : 'form',
			id : 'pform',
			plain : true,
			baseCls : 'x-plain',
			defaults : {
				xtype : 'textfield',
				width : 160,
				msgTarget : 'side'
			},
			labelWidth : 65,
			items : [ {
				xtype : 'textfield',
				fieldLabel : '旧密码',
				id : 'oldpassword',
				name : 'oldpassword',
				minLength : 6,
				minLengthText : '密码长度最少6位！',
				maxLength : 20,
				maxLengthText : '密码长度最多20位！',
				allowBlank : false,
				blankText : '请输入旧密码'
			}, {
				xtype : 'textfield',
				fieldLabel : '设定密码',
				id : 'newpassword',
				name : 'newpassword',
				minLength : 6,
				minLengthText : '密码长度最少6位！',
				maxLength : 20,
				maxLengthText : '密码长度最多20位！',
				inputType : 'password',
				allowBlank : false,
				blankText : '请输入新密码'
			}, {
				xtype : 'textfield',
				fieldLabel : '确认密码',
				id : 'conpassword',
				name : 'conpassword',
				inputType : 'password',
				vtype : 'password',
				vtypeText : "两次输入的密码不一致！",
				confirmTo : 'newpassword',
				allowBlank : false,
				blankText : '请确认新密码'
			} ]
		} ],
		buttons : [ {
			text : '确定',
			url : 'sysOrganService.alterPassword',
			handler : conAlter
		}, {
			text : '清空',
			handler : function() {
				Ext.getCmp('pform').getForm().reset();
			}
		} ]
	});
	passwordwindow.show();
}
function doSaveUser() {
	if (!formpanel.getForm().isValid())
		return;
	var values = $.getFormValues(formpanel);
	$.call('sysTempUserService.update', values, function() {
		$.msg('保存成功');
	});
}
