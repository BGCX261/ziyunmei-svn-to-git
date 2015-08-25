var tree;
var treeEditer;
Ext.onReady(function() {
	new Ext.Viewport( {
		enableTabScroll : true,
		layout : "border",
		items : [ {
			id : "menu-div",
			title : "菜单",
			region : "center",
			width : 200,
			tbar : [ {
				text : '增加',
				url : "sysAuthService.saveMenu",
				handler : saveNode
			}, {
				text : '删除',
				url : "sysAuthService.saveMenu",
				handler : deleteNode
			}, {
				text : '重载',
				url : "sysAuthService.saveMenu",
				handler : reloadOver
			} ],
			collapsible : true
		} ]
	});
	tree = new Ext.tree.TreePanel( {
		el : 'menu-div',
		useArrows : true,
		autoScroll : true,
		animate : true,
		enableDD : true,
		containerScroll : true,
		listeners : {
			"beforeload" : function(node) {
				this.loader.dataUrl = $.treeURL(
						'sysAuthService.findGobalMenus', node.id);
			},
			"beforemovenode" : moveNode,
			"nodedragover" : dragOver
		},
		rootVisible : false,
		root : {
			id : '-1'
		}
	});
	treeEditer = new Ext.tree.TreeEditor(tree, {
		allowBlank : false
	});
	treeEditer.on("complete", updateNode);
	tree.render();
});
function dragOver(e) {
	var n = e.target;
	if (n.leaf) {
		n.leaf = false;
	}
	return true;
}
function reloadOver() {
	Ext.Msg.confirm('信息', '此操作正在调试中,慎用？', function(btn) {
		if (btn == 'yes') {
			$.call("sysAuthService.reload", function() {
				$.msg('操作成功，请重新登录');
			});
		}
	});
}
function moveNode(tree, node, oldParent, newParent, index) {
	if (newParent.id == '-1' || newParent.type == 'M')
		return false;
	$.call("sysAuthService.moveMenu", node.id, newParent.id);
	return true;
}
function updateNode(treeEditer, newval, oldval) {
	if (newval == oldval)
		return true;
	$.call("sysAuthService.updateMenu", treeEditer.editNode.id, newval);
	return true;
}
function deleteNode() {
	var node = tree.getSelectionModel().getSelectedNode();
	if (node.hasChildNodes()) {
		$.msg('请先删除子节点');
	} else {
		Ext.Msg.confirm('信息', '确定要删除？', function(sure) {
			if (sure == 'yes') {
				$.call("sysAuthService.deleteMenu", node.id, function() {
					node.remove();
				});
			}
		});
	}
}
function saveNode() {
	var node = tree.getSelectionModel().getSelectedNode();
	if (!node)
		node = tree.root;
	else if (!node.isExpanded()) {
		node.ensureVisible(function() {
			node.expand();
		});
	}
	$.call("sysAuthService.saveMenu", {
		text : '默认名称',
		parent : node.id,
		type : 'R'
	}, function(resp) {
		var child = new Ext.tree.TreeNode( {
			id : resp,
			text : '默认名称',
			type : 'R'
		});
		node.appendChild(child);
	});
}