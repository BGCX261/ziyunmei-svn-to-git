var dictDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDicts'),
	root : "results",
	totalProperty : "total",
	fields : [ 'id', 'name', 'desc' ]
});
var dictInfoDS = new Ext.data.JsonStore( {
	url : $.URL('sysDictService.findDictInfoByDictId'),
	root : "results",
	totalProperty : "total",
	fields : [ 'name', 'value', 'dictId' ]
});
var dictSM = new Ext.grid.CheckboxSelectionModel( {
	singleSelect : true,
	checkOnly : true,
	listeners : {
		"beforerowselect" : function(sm, index, keepExisting, rec) {
			dictInfoDS.load( {
				params : [ rec.data.id, 0, 12 ]
			});
		},
		"rowdeselect" : function() {
			dictInfoDS.removeAll();
		}
	}
});
var dictCM = new Ext.grid.ColumnModel( [ dictSM, {
	header : "编码",
	dataIndex : "id",
	width : 120,
	editor : new Ext.form.TextField( {
		allowBlank : false,
		vtype : 'alphanum'
	})
}, {
	header : "名称",
	dataIndex : "name",
	width : 120,
	editor : new Ext.form.TextField( {
		allowBlank : false
	})
}, {
	header : "描述",
	dataIndex : "desc",
	width : 120,
	editor : new Ext.form.TextField()
} ]);

var dictInfoCM = new Ext.grid.ColumnModel( [ {
	header : "名称",
	dataIndex : "name",
	width : 120,
	sortable : true,
	editor : new Ext.form.TextField( {
		allowBlank : false
	})
}, {
	header : "值",
	dataIndex : "value",
	width : 120,
	sortable : true,
	editor : new Ext.form.TextField( {
		allowBlank : false
	})
}, {
	dataIndex : "dictId",
	hidden : true
} ]);
// ---------------------------------用户面板-----------------------------------------------------
Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : "border",
		items : [ {
			id : 'dictForm',
			xtype : 'form',
			bodyStyle : 'padding:5px',
			region : "center",
			frame : 'true',
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
				items : [ {
					columnWidth : 0.33,
					layout : 'form',
					labelAlign : "right",
					labelWidth : 70,
					items : [ {
						name : 'id',
						xtype : 'textfield',
						fieldLabel : '编码',
						anchor : '95%'
					} ]
				}, {
					columnWidth : 0.33,
					layout : 'form',
					labelAlign : "right",
					labelWidth : 70,
					items : [ {
						name : 'name',
						xtype : 'textfield',
						fieldLabel : '名称',
						anchor : '95%'
					} ]
				}, {
					columnWidth : 0.33,
					layout : 'form',
					labelAlign : "right",
					labelWidth : 70,
					items : [ {
						name : 'desc',
						xtype : 'textfield',
						fieldLabel : '描述',
						anchor : '95%'
					} ]
				} ]
			}, {
				layout : "border",
				height : 310,
				items : [ {
					id : "dictGrid",
					title : "字典信息",
					xtype : 'editorgrid',
					region : "west",
					height : 300,
					width : 450,
					bodyStyle : 'padding:5px;margin:5px',
					tbar : [ {
						text : "增加",
						url : 'sysDictService.saveDict',
						handler : doInsert
					}, {
						text : '删除',
						url : 'sysDictService.saveDict',
						handler : doDelete
					}, {
						text : '保存',
						url : 'sysDictService.saveDict',
						handler : doSave
					} ],
					ds : dictDS,
					sm : dictSM,
					cm : dictCM,
					bbar : new Ext.PagingToolbar( {
						store : dictDS,
						displayInfo : true,
						pageSize : 12,
						prependButtons : true
					})
				}, {
					id : "dictInfoGrid",
					title : "字典明细信息",
					xtype : 'editorgrid',
					region : "center",
					bodyStyle : 'padding:5px;margin:5px',
					height : 300,
					tbar : [ {
						text : "增加",
						url : 'sysDictService.saveDictInfo',
						handler : doInsertDetail
					}, {
						text : '删除',
						url : 'sysDictService.saveDictInfo',
						handler : doDeleteDetail
					}, {
						text : '保存',
						url : 'sysDictService.saveDictInfo',
						handler : doSaveDetail
					} ],
					ds : dictInfoDS,
					cm : dictInfoCM,
					bbar : new Ext.PagingToolbar( {
						store : dictInfoDS,
						displayInfo : true,
						pageSize : 12,
						prependButtons : true
					})
				} ]
			} ]
		} ]
	});
});
function doSelect() {
	var params = $.getFormValues("dictForm", "name,desc");
	dictDS.load( {
		params : [ params, 0, 12 ]
	});
	dictInfoDS.removeAll();
}
function doInsert() {
	$.insert('dictGrid', {
		id : '',
		name : '',
		desc : ''
	});
	dictSM.clearSelections();
}
function doSave() {
	$.save('dictGrid', 'sysDictService.saveDict');
}
function doReset() {
	Ext.getCmp('dictForm').getForm().reset();
}

function doDelete() {
	Ext.Msg.confirm('信息', '确定要删除？', function(btn) {
		if (btn == 'yes') {
			var record = dictSM.getSelected();
			$.call("sysDictService.deleteDict", record.data.id, function() {
				dictDS.remove(record);
				dictInfoDS.removeAll();
			});
		}
	});
}
function doInsertDetail() {
	var record = dictSM.getSelected();
	if (!record) {
		$.msg("请先选择左边记录");
		return;
	}
	if (record.dirty) {
		$.msg("请先保存左边记录");
		return;
	}
	$.insert('dictInfoGrid', {
		name : '',
		value : '',
		dictId : record.get('id')
	});
}
function doSaveDetail(btn) {
	$.save('dictInfoGrid', btn.url);
}

function doDeleteDetail() {
	var sm = Ext.getCmp('dictInfoGrid').getSelectionModel();
	var cell = sm.getSelectedCell();
	if (cell) {
		Ext.Msg.confirm('信息', '确定要删除？', function(btn) {
			if (btn == 'yes') {
				var ds = Ext.getCmp('dictInfoGrid').store;
				var record = ds.getAt(cell[0]);
				$.call("sysDictService.deleteDictInfo", record.id, function() {
					ds.remove(record);
				});
			}
		});
	}else
		$.msg('选择相应的记录');
}
function doSaveDetail() {
	$.save('dictInfoGrid', "sysDictService.saveDictInfo");
}
