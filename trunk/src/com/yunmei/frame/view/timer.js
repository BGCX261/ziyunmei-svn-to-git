var roleWin;
var taskStateDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'task_state'),
	fields : [ 'name', 'value' ]
});
var taskFixedMonthDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'task_fixed_month'),
	fields : [ 'name', 'value' ]
});
var taskFixedWeekDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'task_fixed_week'),
	fields : [ 'name', 'value' ]
});
var taskTypeDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'task_type'),
	fields : [ 'name', 'value' ]
});
var executeTypeDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'task_execute_type'),
	fields : [ 'name', 'value' ]
});
taskStateDS.load();
taskFixedMonthDS.load();
taskFixedWeekDS.load();
taskTypeDS.load();
executeTypeDS.load();
// ///////////////////////////////////////////////////////////
var timerDS = new Ext.data.JsonStore( {
	url : $.URL('sysTimerService.find'),
	root : "results",
	totalProperty : "total",
	fields : [ 'id', 'name', 'state', 'type', 'executeType', 'executeDate',
			'executeTime', , 'executeTimeEnd', 'repeatInterval', 'startDate',
			'endDate', 'fixed', 'invoke', 'params'/* 'executeDateTime', */]
});
var timerSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true
});
var timerCM = new Ext.grid.ColumnModel( [
		timerSM,
		{
			header : "名称",
			dataIndex : "name",
			width : 80
		},
		{
			header : "状态",
			dataIndex : "state",
			width : 70,
			renderer : function(value) {
				var rec = taskStateDS.getAt(taskStateDS.find("value", value));
				return rec ? rec.data.name : '';
			}
		},
		{
			header : "类型",
			dataIndex : "type",
			width : 70,
			renderer : function(value) {
				var rec = taskTypeDS.getAt(taskTypeDS.find("value", value));
				return rec ? rec.data.name : '';
			}
		},
		{
			header : "执行方式",
			dataIndex : "executeType",
			width : 80,
			renderer : function(value) {
				var rec = executeTypeDS.getAt(executeTypeDS
						.find("value", value));
				return rec ? rec.data.name : '';
			}
		},
		{
			header : "执行日期",
			dataIndex : "executeDate",
			width : 70,
			renderer : Ext.util.Format.dateRenderer('Y-m-d')
		},
		{
			header : "执行时间",
			dataIndex : "executeTime",
			width : 70
		},
		{
			header : "执行时间至",
			dataIndex : "executeTimeEnd",
			width : 70
		},
		{
			header : "间隔时间",
			dataIndex : "repeatInterval",
			width : 70
		},
		{
			header : "生效日期",
			dataIndex : "startDate",
			width : 70,
			renderer : Ext.util.Format.dateRenderer('Y-m-d')
		},
		{
			header : "生效日期至",
			dataIndex : "endDate",
			width : 80,
			renderer : Ext.util.Format.dateRenderer('Y-m-d')
		},
		{
			header : "修正值",
			dataIndex : "fixed",
			width : 70,
			renderer : function(value, meta, record) {
				if (value) {
					var rec;
					if (record.data.type == 2) {
						rec = taskFixedWeekDS.getAt(taskFixedWeekDS.find(
								"value", value));
						return rec.data.name;
					} else if (record.data.type == 3) {
						rec = taskFixedMonthDS.getAt(taskFixedMonthDS.find(
								"value", value));
						return rec.data.name;
					} else
						return value;
				} else
					return "";
			}
		}, {
			header : "调用逻辑",
			dataIndex : "invoke",
			width : 100
		}, {
			header : "参数",
			dataIndex : "params",
			width : 200
		} ]);
function changePanel(obj) {
	if (obj.type) {
		if (obj.type == 0) {
			Ext.getCmp('startDatePannel').hide();
			Ext.getCmp('endDatePannel').hide();
			Ext.getCmp('fixedMonthPanel').hide();
			Ext.getCmp('fixedWeekPanel').hide();
			Ext.getCmp('executeDate').show();
		} else {
			if (obj.type == 1) {
				Ext.getCmp('fixedMonthPanel').hide();
				Ext.getCmp('fixedWeekPanel').hide();
			} else if (obj.type == 2) {
				Ext.getCmp('fixedMonthPanel').hide();
				Ext.getCmp('fixedWeekPanel').show();
			} else {
				Ext.getCmp('fixedWeekPanel').hide();
				Ext.getCmp('fixedMonthPanel').show();
			}
			Ext.getCmp('startDatePannel').show();
			Ext.getCmp('endDatePannel').show();
			Ext.getCmp('executeDate').hide();
		}
	}
	if (obj.executeType) {
		if (obj.executeType == 0) {
			Ext.getCmp('executeTimeEndPanel').hide();
			Ext.getCmp('repeatInterval').hide();
		} else {
			Ext.getCmp('executeTimeEndPanel').show();
			Ext.getCmp('repeatInterval').show();
		}
	}
}
function taskTypeSelect(combo, record, index) {
	changePanel( {
		type : record.data["value"]
	});
}
function taskExecuteType(combo, record, index) {
	changePanel( {
		executeType : record.data["value"]
	});
}

function doSelect() {
	var value = $.getFormValues('queryForm');
	timerDS.load( {
		params : [ values, 0, 20 ]
	});
}
function doDelete(btn) {
	var id = Ext.getCmp('id').getValue();
	$.call("sysTimerService.delete", id, function() {
		var value = $.getFormValues('queryForm');
		timerDS.load( {
			params : [ values, 0, 20 ]
		});
		btn.ownerCt.ownerCt.getForm().reset();
		$.msg();
	});
}
function doReset() {
	changePanel( {
		type : 0,
		executeType : 0
	});
	timerSM.clearSelections();
	Ext.getCmp("queryForm").getForm().reset();
}
function doInvoke() {
	var value = $.getFormValues(this.ownerCt.ownerCt);
	if (value.executeTimeEnd) {
		if (!value.repeatInterval)
			value.repeatInterval = 1;
	}
	if (value.type == 2) {
		value.fixed = value.fixedWeek;
	} else if (value.type == 3) {
		value.fixed = value.fixedMonth;
	}
	$.call("sysTimerService.invokeNow", value, function() {
		$.msg();
	});
}
function doSave() {
	var formPanel = this.ownerCt.ownerCt;
	var value = $.getFormValues(formPanel);
	if (!formPanel.getForm().isValid()) {
		if (value.type > 0) {
			if (!value.startDate)
				Ext.getCmp('startDate').markInvalid('不能为空');
			if (!value.endDate)
				Ext.getCmp('endDate').markInvalid('不能为空');
		} else if (!value.executeDate)
			Ext.getCmp('executeDateF').markInvalid('不能为空');
		return;
	}
	if (value.type > 0) {
		if (!value.startDate) {
			Ext.getCmp('startDate').markInvalid('不能为空');
			return;
		}
		if (!value.endDate) {
			Ext.getCmp('endDate').markInvalid('不能为空');
			return;
		}
	} else if (!value.executeDate) {
		Ext.getCmp('executeDateF').markInvalid('不能为空');
		return;
	}

	if (value.executeTimeEnd) {
		if (!value.repeatInterval)
			value.repeatInterval = 1;
	}
	if (value.type == 2) {
		value.fixed = value.fixedWeek;
	} else if (value.type == 3) {
		value.fixed = value.fixedMonth;
	}
	$.call("sysTimerService.insertTask", value, function(id) {
		$.msg();
		Ext.getCmp('timerTaskForm').getForm().setValues( {
			id : id
		});
		timerDS.reload();
	});
}
function gridRowClick(grid, index) {
	var rec = timerSM.getSelected();
	if (!rec) {
		timerSM.deselectRow(index);
		return;
	}
	changePanel( {
		type : rec.data.type,
		executeType : rec.data.executeType
	});
	var val = {};
	Ext.apply(val, rec.data);
	if (rec.data.startDate) {
		val.startDate = new Date(rec.data.startDate).format('Y-m-d');
	}
	if (rec.data.endDate) {
		val.endDate = new Date(rec.data.endDate).format('Y-m-d');
	}
	if (rec.data.executeDate) {
		val.executeDate = new Date(rec.data.executeDate).format('Y-m-d');
	}
	if (rec.data.type == 2) {
		val.fixedWeek = rec.data.fixed;
	} else if (rec.data.type == 3) {
		val.fixedMonth = rec.data.fixed;
	}
	Ext.getCmp('timerTaskForm').getForm().setValues(val);
}
var invokeSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true,
	listeners : {
		"rowselect" : function(sm, index, rec) {
			Ext.getCmp('timerTaskForm').getForm().setValues( {
				invoke : rec.id
			});
			roleWin.close();
		}
	}
});
var invokeCM = new Ext.grid.ColumnModel( [ invokeSM, {
	header : "调用方法",
	dataIndex : "id",
	width : 250,
	sortable : true
}, {
	header : "所属构件",
	dataIndex : "class",
	width : 120,
	sortable : true
}, {
	header : "参数1类型",
	dataIndex : "param0",
	width : 120,
	sortable : true
}, {
	header : "参数2类型",
	dataIndex : "param2",
	width : 120,
	sortable : true
} ]);
var invokeDS = new Ext.data.JsonStore( {
	url : $.URL('sysAuthService.findInvokeMethod'),
	root : "results",
	totalProperty : "total",
	fields : [ 'id', 'class', 'param0', 'param1' ]
});

function showInvokeMethod() {
	roleWin = new Ext.Window( {
		title : '查找调用方法',
		layout : 'fit',
		width : 650,
		modal : true,
		height : 400,
		plain : true,
		bodyStyle : 'padding:5px;',
		maximizable : false,
		closeAction : 'close',
		closable : true,
		items : [ {
			id : 'invokeForm',
			bodyStyle : 'padding:5px',
			xtype : 'form',
			tbar : [ {
				text : '查询',
				handler : function() {
					var val = $.getFormValues('invokeForm');
					invokeDS.load( {
						params : [ val, 0, 20 ]
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
						fieldLabel : '方法名'
					} ]
				}, {
					layout : 'form',
					items : [ {
						name : 'class',
						xtype : 'textfield',
						fieldLabel : '所在构件'
					} ]
				} ]
			}, {
				xtype : 'grid',
				height : 280,
				ds : invokeDS,
				cm : invokeCM,
				sm : invokeSM,
				bbar : new Ext.PagingToolbar( {
					store : invokeDS,
					displayInfo : true,
					pageSize : 20,
					prependButtons : true
				})
			} ]
		} ]
	});
	roleWin.show();
}

Ext.onReady(function() {
	Ext.form.Field.prototype.msgTarget = 'under';
	new Ext.Viewport( {
		enableTabScroll : true,
		items : [ {
			id : 'queryForm',
			xtype : 'form',
			frame : true,
			collapsible : true,
			tbar : [ {
				text : '查询',
				handler : doSelect
			}, {
				text : '重置',
				handler : doReset
			} ],
			items : [ {
				layout : 'column',
				border : false,
				defaults : {
					layout : 'form',
					border : false,
					columnWidth : .33
				},
				items : [ {
					items : [ {
						xtype : 'textfield',
						fieldLabel : '任务名',
						name : 'name',
						anchor : '90%',
						allowBlank : false,
						vtype : 'name'
					} ]
				}, {
					items : [ {
						name : 'state',
						anchor : '90%',
						fieldLabel : '是否启动',
						xtype : 'combo',
						store : taskStateDS,
						mode : 'local',
						valueField : 'value',
						triggerAction : 'all',
						displayField : 'name',
						maxHeight : 230
					} ]
				}, {
					items : [ {
						name : 'type',
						anchor : '90%',
						fieldLabel : '任务类型',
						xtype : 'combo',
						mode : 'local',
						store : taskTypeDS,
						valueField : 'value',
						triggerAction : 'all',
						displayField : 'name',
						maxHeight : 230
					} ]
				}, {
					items : [ {
						xtype : 'datefield',
						fieldLabel : '执行日期',
						name : 'executeDate',
						format : 'Y-m-d',
						anchor : '90%'
					} ]
				}, {
					items : [ {
						xtype : 'textfield',
						fieldLabel : '业务方法',
						name : 'invoke',
						anchor : '90%'
					} ]
				} ]
			}, {
				id : 'timerGrid',
				xtype : 'grid',
				height : 200,
				ds : timerDS,
				cm : timerCM,
				sm : timerSM,
				listeners : {
					"rowclick" : gridRowClick
				},
				bbar : new Ext.PagingToolbar( {
					store : timerDS,
					displayInfo : true,
					pageSize : 20,
					prependButtons : true
				})
			} ]
		}, {
			labelWidth : 65,
			id : 'timerTaskForm',
			xtype : 'form',
			bodyStyle : 'padding:15px',
			labelAlign : 'left',
			frame : true,
			monitorValid : true,
			buttonAlign : 'center',
			tbar : [ {
				text : '增加',
				handler : function() {
					changePanel( {
						type : '0',
						executeType : '0'
					});
					timerSM.clearSelections();
					this.ownerCt.ownerCt.getForm().reset();
				}
			}, {
				text : '立即执行',
				handler : doInvoke
			}, {
				text : '删除',
				handler : doDelete
			}, {
				text : '保存',
				handler : doSave
			} ],
			items : [ {
				layout : 'column',
				border : false,
				defaults : {
					layout : 'form',
					border : false,
					columnWidth : .33
				},
				items : [ {
					hidden : true,
					items : [ {
						xtype : 'textfield',
						fieldLabel : 'ID',
						id : 'id',
						name : 'id',
						anchor : '90%'
					} ]
				}, {
					items : [ {
						xtype : 'textfield',
						fieldLabel : '任务名',
						name : 'name',
						anchor : '90%',
						allowBlank : false
					} ]
				}, {
					items : [ {
						name : 'state',
						anchor : '90%',
						fieldLabel : '是否启动',
						value : '0',
						valueNotFoundText : '不启动',
						xtype : 'combo',
						mode : 'local',
						store : taskStateDS,
						valueField : 'value',
						triggerAction : 'all',
						displayField : 'name',
						maxHeight : 230
					} ]
				}, {
					items : [ {
						name : 'type',
						anchor : '90%',
						fieldLabel : '任务类型',
						mode : 'local',
						xtype : 'combo',
						value : '0',
						valueNotFoundText : '一次性任务',
						store : taskTypeDS,
						valueField : 'value',
						triggerAction : 'all',
						displayField : 'name',
						listeners : {
							"select" : taskTypeSelect
						},
						maxHeight : 230
					} ]
				}, {
					items : [ {
						fieldLabel : '执行方式',
						name : 'executeType',
						anchor : '90%',
						value : '0',
						valueNotFoundText : '固定时间',
						mode : 'local',
						listeners : {
							"select" : taskExecuteType
						},
						xtype : 'combo',
						store : executeTypeDS,
						valueField : 'value',
						triggerAction : 'all',
						displayField : 'name',
						maxHeight : 230
					} ]
				}, {
					id : 'executeDate',
					items : [ {
						id : 'executeDateF',
						xtype : 'datefield',
						fieldLabel : '执行日期',
						name : 'executeDate',
						format : 'Y-m-d',
						anchor : '90%'
					} ]
				}, {
					id : 'executeTimePanel',
					items : [ {
						xtype : 'textfield',
						fieldLabel : '执行时间',
						name : 'executeTime',
						vtype : 'time',
						allowBlank : false,
						anchor : '90%'
					} ]
				}, {
					id : 'repeatInterval',
					hidden : true,
					items : [ {
						xtype : 'textfield',
						fieldLabel : '间隔时间',
						name : 'repeatInterval',
						regex : /^\d+$/,
						regexText : '必须为数字',
						anchor : '90%'
					} ]
				}, {
					id : 'executeTimeEndPanel',
					hidden : true,
					items : [ {
						xtype : 'textfield',
						fieldLabel : '执行时间至',
						name : 'executeTimeEnd',
						vtype : 'time',
						anchor : '90%'
					} ]
				}, {
					id : 'startDatePannel',
					hidden : true,
					items : [ {
						xtype : 'datefield',
						fieldLabel : '生效日期',
						name : 'startDate',
						format : 'Y-m-d',
						id : 'startDate',
						vtype : 'daterange',
						endDateField : 'endDate',
						anchor : '90%'
					} ]
				}, {
					id : 'endDatePannel',
					hidden : true,
					items : [ {
						xtype : 'datefield',
						fieldLabel : '失效日期',
						name : 'endDate',
						id : 'endDate',
						format : 'Y-m-d',
						vtype : 'daterange',
						startDateField : 'startDate',
						anchor : '90%'
					} ]
				}, {
					id : 'fixedMonthPanel',
					hidden : true,
					items : [ {
						fieldLabel : '修正值',
						name : 'fixedMonth',
						anchor : '90%',
						xtype : 'combo',
						store : taskFixedMonthDS,
						value : '1',
						mode : 'local',
						valueField : 'value',
						triggerAction : 'all',
						displayField : 'name',
						maxHeight : 230
					} ]
				}, {
					id : 'fixedWeekPanel',
					hidden : true,
					items : [ {
						fieldLabel : '修正值',
						name : 'fixedWeek',
						anchor : '90%',
						xtype : 'combo',
						mode : 'local',
						value : '1',
						store : taskFixedWeekDS,
						valueField : 'value',
						triggerAction : 'all',
						displayField : 'name',
						maxHeight : 230
					} ]
				}, {
					items : [ {
						xtype : 'queryfield',
						fieldLabel : '业务方法',
						name : 'invoke',
						click : showInvokeMethod,
						anchor : '90%',
						allowBlank : false
					} ]
				}, {
					columnWidth : 1,
					items : [ {
						xtype : 'textarea',
						fieldLabel : '输入数据',
						name : 'params',
						anchor : '90%'
					} ]
				} ]
			} ]
		} ]
	});
});
