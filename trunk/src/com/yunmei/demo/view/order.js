var orderSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true,
	listeners : {
		"beforerowselect" : function(sm, index, keepExisting, rec) {
			rec.data.id = rec.id;
			orderPanel.getForm().setValues(rec.data);
			orderPanel.getForm().setValues( {
				date : new Date(rec.data.date).format('Y-m-d')
			});
		},
		"rowdeselect" : function() {
		}
	}
});

var orderCM = new Ext.grid.ColumnModel( [ orderSM, {
	header : "订单编码",
	dataIndex : "code",
	width : 80
}, {
	header : "订单名",
	dataIndex : "name",
	width : 120
}, {
	header : "订单金额",
	dataIndex : "monkey",
	width : 80,
	sortable : true

}, {
	header : "状态",
	dataIndex : "state",
	width : 80

}, {
	header : "需求日期",
	dataIndex : "date",
	width : 80,
	renderer : Ext.util.Format.dateRenderer('Y-m-d')
}, {
	header : "制定人",
	dataIndex : "user",
	width : 80
}, {
	header : "备注",
	dataIndex : "remark",
	width : 150
} ]);
var orderDS = new Ext.data.JsonStore( {
	url : $.URL('demoOrderService.find'),
	root : "results",
	totalProperty : "total",
	fields : [ 'code', 'name', 'monkey', 'date', 'user', 'state', 'remark' ]
});
var auditCM = new Ext.grid.ColumnModel( [ {
	header : "任务名",
	dataIndex : "taskName",
	width : 120
}, {
	header : "审批人",
	dataIndex : "userName",
	width : 100
}, {
	header : "审批结果",
	dataIndex : "result",
	width : 80
}, {
	header : "审批时间",
	dataIndex : "date",
	width : 100,
	renderer : Ext.util.Format.dateRenderer('Y-m-d')
}, {
	header : "审批意见",
	dataIndex : "opinion",
	width : 200
} ]);
var auditDS = new Ext.data.JsonStore( {
	url : $.URL('sysWorkflowService.findOpinions'),
	fields : [ 'id', 'taskName', 'userName', 'opinion', 'result', 'date' ]
});
// ---------------------------------用户面板-----------------------------------------------------
var orderPanel = new Ext.FormPanel( {
	bodyStyle : 'padding:5px',
	frame : 'true',
	region : "center",
	tbar : [ {
		text : '增加',
		url : 'demoOrderService.save',
		handler : doInsert
	}, {
		text : '提交',
		url : 'demoOrderService.submit',
		handler : doSubmit
	}, {
		text : '签字',
		url : 'demoOrderService.signed',
		handler : doSigned
	}, {
		text : '审批',
		url : 'demoOrderService.audit',
		handler : doAudit
	}, {
		text : '删除',
		url : 'demoOrderService.delete',
		handler : doDelete
	}, {
		text : '保存',
		url : 'demoOrderService.save',
		handler : doSave
	}, {
		text : '重置',
		url : 'demoOrderService.save',
		handler : doReset
	} ],
	items : [ {
		layout : 'column',
		bodyStyle : 'padding:5px',
		defaults : {
			columnWidth : .5,
			layout : 'form'
		},
		items : [ {
			items : [ {
				id : 'id',
				name : 'id',
				xtype : 'hidden'
			}, {
				name : 'code',
				xtype : 'textfield',
				fieldLabel : '编码',
				anchor : '85%'
			}, {
				name : 'monkey',
				xtype : 'textfield',
				fieldLabel : '金额',
				anchor : '85%'
			}, {
				name : 'date',
				xtype : 'datefield',
				// minValue : new Date(),
				fieldLabel : '需求日期',
				format : 'Y-m-d',
				anchor : '85%'
			} ]
		}, {
			items : [ {
				name : 'name',
				xtype : 'textfield',
				fieldLabel : '订单名',
				anchor : '85%'
			}, {
				name : 'state',
				xtype : 'textfield',
				fieldLabel : '状态',
				readOnly : true,
				value : '新建',
				anchor : '85%'
			}, {
				name : 'user',
				xtype : 'textfield',
				fieldLabel : '制定人',
				readOnly : true,
				value : window.top.user.name,
				anchor : '85%'
			} ]
		} ]
	}, {
		xtype : 'tabpanel',
		plain : true,
		height : 400,
		activeTab : 0,
		defaults : {
			bodyStyle : 'padding:10px'
		},
		items : [ {
			id : "auditGrid",
			xtype : 'grid',
			height : 400,
			title : '审批意见',
			ds : auditDS,
			cm : auditCM,
			listeners : {
				activate : tabChange
			}
		}, {
			id : 'queryPanel',
			title : '订单查询',
			height : 400,
			layout : 'border',
			frame : true,
			tbar : [ {
				text : '查询',
				handler : doSelect
			}, {
				text : '重置',
				handler : function() {
				}
			} ],
			items : [ {
				region : 'north',
				layout : 'column',
				height : 40,
				defaults : {
					columnWidth : .5,
					layout : 'form',
					bodyStyle : 'margin:10px'
				},
				items : [ {
					items : [ {
						name : 'queryName',
						xtype : 'textfield',
						fieldLabel : '订单名',
						anchor : '50%'
					} ]
				}, {
					items : [ {
						name : 'queryState',
						xtype : 'textfield',
						fieldLabel : '状态',
						anchor : '50%'
					} ]
				} ]
			}, {
				region : 'center',
				xtype : 'grid',
				height : 100,
				ds : orderDS,
				cm : orderCM,
				sm : orderSM,
				bbar : new Ext.PagingToolbar( {
					store : orderDS,
					pageSize : 20,
					displayInfo : true,
					prependButtons : true
				})
			} ]
		} ]
	} ]
});

function doReset() {
	orderPanel.getForm().reset();
}
function doSubmit(btn) {
	var order = $.getFormValues(orderPanel);
	if (order.state == '新建' || order.state == '驳回')
		$.call(btn.url, order, 'workflow', function(state) {
			orderPanel.getForm().setValues( {
				state : state
			});
			var rec = orderSM.getSelected();
			if (rec)
				rec.data.state = state;
			$.msg();
		});
	else {
		$.msg("您没有权进行此操作");
	}
}
function doSigned(btn) {
	var order = $.getFormValues(orderPanel);
	if (order.state == '财务签字')
		$.call(btn.url, order, function(state) {
			orderPanel.getForm().setValues( {
				state : state
			});
			var rec = orderSM.getSelected();
			if (rec)
				rec.data.state = state;
			$.msg();
		});
	else {
		$.msg("您没有权进行此操作");
	}
}
function doAudit(btn) {
	var order = $.getFormValues(orderPanel);
	if (order.state == '审批中')
		var roleWin = new Ext.Window( {
			layout : 'fit',
			width : 600,
			modal : true,
			height : 200,
			maximizable : false,
			closable : true,
			items : [ {
				id : 'auditPanel',
				xtype : 'form',
				frame : true,
				height : 200,
				bodyStyle : 'padding: 10px 10px 0 10px;',
				labelWidth : 100,
				defaults : {
					anchor : '95%',
					msgTarget : 'side'
				},
				items : [ {
					name : 'key',
					value : 'pass',
					xtype : 'hidden'
				}, {
					name : 'result',
					xtype : 'combo',
					store : new Ext.data.ArrayStore( {
						fields : [ 'value', 'name' ],
						data : [ [ '合格', '合格' ], [ '不合格', '不合格' ] ]
					}),
					displayField : 'name',
					typeAhead : true,
					fieldLabel : '通过',
					mode : 'local',
					value : '合格',
					valueField : 'value',
					forceSelection : true,
					triggerAction : 'all',
					selectOnFocus : true
				}, {
					xtype : 'textarea',
					fieldLabel : '审批意见',
					name : 'opinion'
				} ],
				buttons : [ {
					text : '确定',
					handler : function() {
						var opinion = $.getFormValues('auditPanel');
						opinion.businessId = Ext.getCmp('id').getValue();
						$.call(btn.url, opinion, function(state) {
							orderPanel.getForm().setValues( {
								state : state
							});
							var rec = orderSM.getSelected();
							if (rec)
								rec.data.state = state;
							roleWin.close();
							$.msg();
						});
					}
				} ]
			} ]
		}).show();
	else {
		$.msg("您没有权进行此操作");
	}
}
function doSelect() {
	var name = Ext.getCmp("queryPanel").find("name", "queryName")[0].getValue();
	var state = Ext.getCmp("queryPanel").find("name", "queryState")[0]
			.getValue();
	orderDS.load( {
		params : [ {
			name : "%" + name + "%",
			state : "%" + state + "%"
		}, 0, 20 ]
	});
}
function doInsert() {
	orderSM.clearSelections();
	orderPanel.getForm().reset();
}
function doDelete(btn) {
	Ext.Msg.confirm('信息', '确定要删除？', function(pass) {
		if (pass == 'yes') {
			var order = $.getFormValues(orderPanel);
			if (!order.id) {
				$.msg('还没保存呢');
				return;
			}
			$.call(btn.url, order.id, function() {
				var rec = orderDS.getById(order.id);
				if (rec) {
					orderDS.remove(rec);
				}
				orderPanel.getForm().reset();
				$.msg();
			});
		}
	});
}
function doSave(btn) {
	var order = $.getFormValues(orderPanel);
	$.call(btn.url, order, function(orderId) {
		orderPanel.getForm().setValues( {
			id : orderId
		});
		$.msg();
	});
}
function tabChange(tab) {
	var id = Ext.getCmp('id').getValue();
	auditDS.load( {
		params : [ id ? id : -1 ]
	});
}
Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : "border",
		items : [ orderPanel ]
	});
	if (id.length > 0) {
		$.call("demoOrderService.get", id, function(res) {
			orderPanel.getForm().setValues(res);
			orderPanel.getForm().setValues( {
				date : new Date(res.date).format('Y-m-d')
			});
			auditDS.load( {
				params : [ id ]
			});
		});
	}
});