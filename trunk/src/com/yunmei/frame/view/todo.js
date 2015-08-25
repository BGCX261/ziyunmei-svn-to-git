var todoSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true
});
var todoDS = new Ext.data.JsonStore( {
	url : $.URL('sysWorkflowService.findTodos', window.top.user.id),
	fields : [ 'name', 'createTime', 'id', 'description', 'progress',
			'executionId', 'activityName', 'assignee' ]
});
var todoCM = new Ext.grid.ColumnModel( [ todoSM, {
	header : "ID",
	dataIndex : "id",
	width : 80
}, {
	header : "任务名",
	dataIndex : "name",
	width : 150
}, {
	header : "创建时间",
	dataIndex : "createTime",
	width : 150,
	renderer : Ext.util.Format.dateRenderer('Y-m-d H:i:s')
}, {
	header : "活动名",
	dataIndex : "activityName",
	width : 150
}, {
	header : "描述",
	dataIndex : "description",
	width : 200
} ]);
todoDS.load();
Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		items : [ {
			xtype : 'grid',
			height : 400,
			frame : true,
			ds : todoDS,
			cm : todoCM,
			sm : todoSM,
			listeners : {
				"celldblclick" : function(grid, rowIndex, columnIndex, e) {
					var rec = grid.store.getAt(rowIndex);
					$.forward('demoOrderService.selectOrder', rec.id);
				}
			},
			tbar : [ {
				text : '查看',
				handler : viewWorkflow
			}, {
				text : '执行',
				handler : executeWorkflow
			} ]
		} ]
	});
});

function viewWorkflow() {
	var rec = todoSM.getSelected();
	if (rec) {
		var roleWin = new Ext.Window( {
			layout : 'fit',
			width : 600,
			modal : true,
			height : 400,
			plain : true,
			html : "<img border=0 src='jbpm?executionId="
					+ rec.data.executionId + "&activityName="
					+ rec.data.activityName + ">",
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
function executeWorkflow() {
	var rec = todoSM.getSelected();
	if (!rec) {
		$.msg("选择相关记录");
		return;
	}
	$.forward('demoOrderService.selectOrder', rec.id);

}
