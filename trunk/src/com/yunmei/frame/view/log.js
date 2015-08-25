var successDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId', 'success'),
	fields : [ 'name', 'value' ]
});
var logSM = new Ext.grid.CheckboxSelectionModel();

var logDS = new Ext.data.JsonStore( {
	url : $.URL('sysLogService.findByExample'),
	root : "results",
	totalProperty : "total",
	fields : ['reqMethod', 'logTime', 'isSuccess',
			'userId', 'userName', 'userIp' ]
});

var logCM = new Ext.grid.ColumnModel( [ logSM, {
	header : "操作人姓名",
	dataIndex : "userName",
	width : 100
}, {
	header : "请求方法",
	dataIndex : "reqMethod",
	width : 120
}, {
	header : "操作时间",
	dataIndex : "logTime",
	width : 160,
	sortable : true,
	renderer : Ext.util.Format.dateRenderer('Y-m-d H:i:s')
}, {
	header : "结果",
	dataIndex : "isSuccess",
	width : 80,
	renderer : function(value) {
		if (value == 'S')
			return "成功";
		else
			return "<span style='color:red;font-width:bold;'>失败</span>";
	},
	sortable : true
}, {
	header : "操作人IP",
	dataIndex : "userIp",
	width : 100
} ]);
// ---------------------------------用户面板-----------------------------------------------------
var logPanel = new Ext.FormPanel( {
	bodyStyle : 'padding:5px',
	frame : true,
	region : "center",
	tbar : [ {
		text : '查询',
		handler : doSelect
	}, {
		text : '重置',
		handler : doReset
	} ],
	items : [ {
		collapsible : true,
		autoHeight : true,
		layout : 'column',
		defaults : {
			layout : 'form',
			columnWidth : .25
		},
		items : [ {
			items : [ {
				name : 'userName',
				xtype : 'textfield',
				fieldLabel : '操作人',
				width : 120,
				anchor : '95%'
			} ]
		}, {
			items : [ {
				name : 'reqMethod',
				xtype : 'textfield',
				fieldLabel : '操作方法',
				width : 120,
				anchor : '95%'
			} ]
		}, {
			items : [ {
				fieldLabel : '开始时间',
				name : 'fromLogTime',
				format : 'Y-m-d',
				width : 120,
				xtype : 'datefield'
			} ]
		}, {
			items : [ {
				fieldLabel : '结束时间',
				name : 'toLogTime',
				format : 'Y-m-d',
				width : 120,
				xtype : 'datefield'
			} ]
		} ]
	}, {
		id : 'logGrid',
		title : "详细信息",
		xtype : 'grid',
		height : 400,
		frame : true,
		ds : logDS,
		cm : logCM,
		sm : logSM,
		bbar : new Ext.PagingToolbar( {
			store : logDS,
			displayInfo : true,
			pageSize : 20,
			prependButtons : true
		})
	} ]
});
Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : "border",
		items : [ logPanel ]
	});
});
function doSelect() {
	var params = $.getFormValues(logPanel, "userName", "reqMethod");
	logDS.load( {
		params : [ params, 0, 20 ]
	});
}
function doReset() {
	logPanel.getForm().reset();
}
