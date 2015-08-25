var workflowSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true,
	listeners : {
		"beforerowselect" : function(sm, index, keepExisting, rec) {
			taskDS.load( {
				params : [ rec.id ]
			});
			processInstanceDS.load( {
				params : [ rec.id ]
			});

		},
		"rowdeselect" : function() {
			taskDS.removeAll();
			processInstanceDS.removeAll();
		}
	}
});
var workflowDS = new Ext.data.JsonStore( {
	url : $.URL('sysWorkflowService.findProcessDefinitions'),
	fields : [ 'name', 'key', 'id', 'description', 'deploymentId', 'version',
			'suspended' ]
});
var workflowCM = new Ext.grid.ColumnModel( [ workflowSM, {
	header : "ID",
	dataIndex : "id",
	width : 80
}, {
	header : "KEY",
	dataIndex : "key",
	width : 80
}, {
	header : "流程名",
	dataIndex : "name",
	width : 120
}, {
	header : "部署ID",
	dataIndex : "deploymentId",
	width : 80
}, {
	header : "版本",
	dataIndex : "version",
	width : 80
}, {
	header : "挂起",
	dataIndex : "suspended",
	width : 80
}, {
	header : "描述",
	dataIndex : "description",
	width : 200
} ]);

var taskSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true
});
var taskDS = new Ext.data.JsonStore( {
	url : $.URL('sysWorkflowService.findTaskNodes'),
	fields : [ 'name', 'type', 'text', 'value' ]
});
var taskCM = new Ext.grid.ColumnModel( [ taskSM, {
	header : "任务名",
	dataIndex : "name",
	width : 80
}, {
	header : "类型",
	dataIndex : "type",
	width : 120,
	renderer : function(value) {
		var typeDS = Ext.getCmp('type').store;
		var rec = typeDS.getAt(typeDS.find("value", value));
		return rec ? rec.data.name : '';
	}
}, {
	header : "名称",
	dataIndex : "text",
	width : 80
} ]);

var processInstanceSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true
});
var processInstanceDS = new Ext.data.JsonStore( {
	url : $.URL('sysWorkflowService.findProcessInstances'),
	fields : [ 'id', 'key', 'name', 'active', 'activityName', 'state',
			'suspended', 'ended' ]
});
var processInstanceCM = new Ext.grid.ColumnModel( [ taskSM, {
	header : "ID",
	dataIndex : "id",
	width : 80
}, {
	header : "业务key",
	dataIndex : "key",
	width : 80
}, {
	header : "名称",
	dataIndex : "name",
	width : 80
//
		}, {
			header : "当前任务",
			dataIndex : "activityName",
			width : 80
		}, {
			header : "活动",
			dataIndex : "active",
			width : 80
		}, {
			header : "状态",
			dataIndex : "state",
			width : 80
		}, {
			header : "挂起",
			dataIndex : "suspended",
			width : 80
		}, {
			header : "是否结束",
			dataIndex : "ended",
			width : 120
		} ]);

workflowDS.load();
Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : 'border',
		items : [ {
			region : 'center',
			xtype : 'grid',
			ds : workflowDS,
			cm : workflowCM,
			sm : workflowSM,
			tbar : [ {
				text : '查看',
				handler : viewWorkflow
			}, {
				text : '删除',
				handler : deleteWorkflow
			}, {
				text : '部署',
				handler : deployWorkflow
			} ]
		}, {
			id : 'organTab',
			xtype : 'tabpanel',
			plain : true,
			height : 200,
			region : 'south',
			defaultType : 'grid',
			activeTab : 0,
			defaults : {
				bodyStyle : 'padding:10px'
			},
			items : [ {
				xtype : 'grid',
				title : '分配参与者',
				height : 200,
				frame : true,
				ds : taskDS,
				cm : taskCM,
				sm : taskSM,
				tbar : [ {
					id : 'type',
					xtype : 'combo',
					store : new Ext.data.ArrayStore( {
						fields : [ 'value', 'name' ],
						data : [ [ 'P', '岗位' ], [ 'R', '角色' ], [ 'U', '用户' ] ]
					}),
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					value : 'P',
					valueField : 'value',
					forceSelection : true,
					triggerAction : 'all',
					selectOnFocus : true
				}, {
					text : '分配参与者',
					handler : showAssign
				} ]
			}, {
				xtype : 'grid',
				title : '流程实例监控',
				height : 200,
				frame : true,
				ds : processInstanceDS,
				cm : processInstanceCM,
				sm : processInstanceSM,
				tbar : [ {
					text : '查看',
					handler : viewProcess
				}, {
					text : '删除',
					handler : deleteProcessInstance
				} ]
			} ]
		} ]
	});
});
function deleteProcessInstance() {
	var rec = processInstanceSM.getSelected();
	if (rec) {
		Ext.Msg.confirm('信息', '确定要删除？', function(btn) {
			if (btn == 'yes') {
				$.call("sysWorkflowService.deleteProcessInstance", rec.id,
						function() {
							processInstanceDS.remove(rec);
							$.msg();
						});
			}
		});
	} else {
		$.msg("请选择相应的记录");
	}
}
function deleteWorkflow() {
	var rec = workflowSM.getSelected();
	if (rec) {
		Ext.Msg.confirm('信息', '确定要删除？', function(btn) {
			if (btn == 'yes') {
				$.call("sysWorkflowService.delete", rec.data.deploymentId,
						function() {
							workflowDS.remove(rec);
							taskDS.removeAll();
							$.msg();
						});
			}
		});
	} else {
		$.msg("请选择相应的记录");
	}
}

function showAssign() {
	if (!taskSM.getSelected()) {
		$.msg("请选择相应的任务");
		return;
	}
	var type = Ext.getCmp('type').getValue();
	var organWindow = null;
	if (type == 'P' || type == 'U') {
		organWindow = new Ext.Window(
				{
					title : '组织机构',
					width : 300,
					height : 400,
					modal : true,
					bodyStyle : 'padding:5px;',
					maximizable : false,
					closable : true,
					frame : true,
					items : [ {
						xtype : 'treepanel',
						autoScroll : true,
						animate : true,
						baseCls : 'x-plain',
						containerScroll : true,
						rootVisible : false,
						root : {
							id : "-1"
						},
						listeners : {
							"click" : function(node) {
								var taskUsers = {};
								taskUsers.id = workflowSM.getSelected().id
										+ "_" + taskSM.getSelected().data.name;
								taskUsers.type = type;
								taskUsers.entityId = node.attributes[type == 'P' ? "post"
										: "user"];
								taskUsers.entityName = node.text;
								if (node.isLeaf()) {
									$.call("sysWorkflowService.assigned",
											taskUsers, function() {
												organWindow.close();
												taskDS.load( {
													params : [ workflowSM
															.getSelected().id ]
												});
											});
								} else
									$.msg("请选择岗位节点");
							},
							"beforeload" : function(node) {
								this.loader.dataUrl = $
										.treeURL(
												type == 'U' ? 'sysOrganService.findUserTree'
														: 'sysOrganService.findPostTree',
												node.id);
							}
						}
					} ]
				}).show();
	} else {
		var roleSM = new Ext.grid.CheckboxSelectionModel( {
			singleSelect : true,
			checkOnly : true,
			listeners : {
				"rowselect" : function(sm, index, rec) {
					var taskUsers = {};
					taskUsers.id = workflowSM.getSelected().id + "_"
							+ taskSM.getSelected().data.name;
					taskUsers.type = type;
					taskUsers.entityId = rec.id;
					taskUsers.entityName = rec.data.name;
					$.call("sysWorkflowService.assigned", taskUsers,
							function() {
								organWindow.close();
								taskDS.load( {
									params : [ workflowSM.getSelected().id ]
								});
							});
				}
			}
		});
		var roleCM = new Ext.grid.ColumnModel( [ roleSM, {
			header : "角色名",
			dataIndex : "name",
			width : 120
		}, {
			header : "备注",
			dataIndex : "remark",
			width : 210
		} ]);
		var roleDS = new Ext.data.JsonStore( {
			url : $.URL('sysAuthService.findRoles'),
			fields : [ 'id', 'name', 'remark' ],
			root : "results",
			totalProperty : "total"
		});
		function doSelect() {
			var params = $.getFormValues('roleForm');
			roleDS.load( {
				params : [ params, 0, 20 ]
			});
		}
		organWindow = new Ext.Window( {
			layout : 'fit',
			title : "角色列表",
			width : 600,
			modal : true,
			height : 400,
			plain : true,
			items : [ {
				id : 'roleForm',
				xtype : 'form',
				frame : 'true',
				tbar : [ {
					text : '查询',
					handler : doSelect
				} ],
				width : 500,
				items : [ {
					layout : 'column',
					defaults : {
						columnWidth : .5,
						layout : 'form'
					},
					items : [ {
						items : [ {
							name : 'name',
							xtype : 'textfield',
							fieldLabel : '角色名',
							width : '110'
						} ]
					}, {
						items : [ {
							name : 'remark',
							xtype : 'textfield',
							fieldLabel : '备注',
							width : '110'
						} ]
					} ]
				}, {
					height : 300,
					xtype : 'grid',
					sm : roleSM,
					ds : roleDS,
					cm : roleCM,
					bbar : new Ext.PagingToolbar( {
						store : roleDS,
						displayInfo : true,
						pageSize : 20,
						prependButtons : true
					})
				} ]
			} ],
			bodyStyle : 'padding:5px;',
			maximizable : false,
			closeAction : 'close',
			closable : true
		}).show();
	}
}
function viewWorkflow() {
	var rec = workflowSM.getSelected();
	if (rec) {
		new Ext.Window( {
			layout : 'fit',
			width : 600,
			modal : true,
			height : 400,
			plain : true,
			html : "<img border=0 src='jbpm?processDefinitionId=" + rec.id
					+ "'>",
			bodyStyle : 'padding:5px;',
			maximizable : false,
			closeAction : 'close',
			closable : true
		}).show();
	} else {
		$.msg("请选择相应的记录");
	}
}
function viewProcess() {
	var rec = processInstanceSM.getSelected();
	if (rec) {
		var roleWin = new Ext.Window( {
			layout : 'fit',
			width : 700,
			modal : true,
			height : 500,
			plain : true,
			html : "<img border=0 src='jbpm?executionId=" + rec.id
					+ "&activityName=" + rec.data.activityName + ">",
			bodyStyle : 'padding:5px;',
			maximizable : false,
			closeAction : 'close',
			closable : true
		});
		roleWin.show();
	} else {
		$.msg("请选择相应的记录");
	}
}
function deployWorkflow() {
	var roleWin = new Ext.Window( {
		layout : 'fit',
		width : 600,
		modal : true,
		height : 150,
		maximizable : false,
		closeAction : 'close',
		closable : true,
		items : [ {
			fileUpload : true,
			xtype : 'form',
			frame : true,
			autoHeight : true,
			bodyStyle : 'padding: 10px 10px 0 10px;',
			labelWidth : 100,
			defaults : {
				anchor : '95%',
				msgTarget : 'side'
			},
			items : [ {
				xtype : 'fileuploadfield',
				fieldLabel : '流程文件(zip)',
				buttonText : '',
				name : 'workflow',
				buttonCfg : {
					iconCls : 'x-form-query-icon'
				}
			}, {
				xtype : 'textfield',
				fieldLabel : '备注',
				name : 'name'
			} ],
			buttons : [ {
				text : '上传',
				handler : function() {
					var form = this.ownerCt.ownerCt.getForm();
					if (form.isValid()) {
						form.submit( {
							url : 'jbpm',
							waitMsg : '正在上传...',
							success : function() {
								workflowDS.load();
								$.msg();
							}
						});
					}
				}
			} ]
		} ]
	});
	roleWin.show();
}
