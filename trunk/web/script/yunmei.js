/**
 * Ext.data.JsonStore重载load 实现统一前台请求写法
 */
var newDiv;
Ext.override(
		Ext.data.JsonStore,
		{
			load : function(options) {
				this.removeAll();
				options = options || {};
				// TODO 这个地方有问题,需要处理
				if (this.hasListener('exception')) {
					this.on('exception', function() {
						var result = Ext.decode(arguments[4].responseText);
						if (!!result && !!result["error"])
							Ext.Msg.alert('错误提示', result["error"]);
					});
				}
				if ("params" in options) {
					var temp = options["params"];
					delete options.params;
					options["params"] = {};
					if (temp instanceof Array) {
						options["params"]["!jsonparams"] = Ext.encode(temp);
						if (temp.length > 2) {
							options["params"]["start"] = temp[temp.length - 2];
							options["params"]["limit"] = temp[temp.length - 1];
						} else {
							options["params"]["start"] = temp[0];
							options["params"]["limit"] = temp[1];
						}
						this.setBaseParam("!jsonparams", temp);
					} else {
						if (typeof temp == "object" && "start" in temp
								&& "limit" in temp
								&& "!jsonparams" in this.baseParams) {
							var oldParams = this.baseParams["!jsonparams"];
							oldParams[oldParams.length - 2] = temp["start"];
							options["params"]["!jsonparams"] = Ext
									.encode(oldParams);
							options["params"]["start"] = temp["start"];
							options["params"]["limit"] = temp["limit"];
						} else {
							var array = [];
							array.push(temp);
							options["params"]["!jsonparams"] = Ext
									.encode(array);
						}
					}
				}
				this.storeOptions(options);
				if (this.sortInfo && this.remoteSort) {
					var pn = this.paramNames;
					options.params = options.params || {};
					options.params[pn.sort] = this.sortInfo.field;
					options.params[pn.dir] = this.sortInfo.direction;
				}
				try {
					return this.execute('read', null, options);
				} catch (e) {
					this.handleException(e);
					return false;
				}
			}
		});
/**
 * 
 */
Ext.override(Ext.Button, {
	initComponent : function() {
		var user = window.top.user || undefined;
		var _self = this;
		if ("url" in _self && !!user) {
			var url = _self["url"];
			_self["hidden"] = true;
			if (user["opers"]) {
				for ( var i = 0; i < user.opers.length; i++) {
					if (url == user.opers[i].url) {
						_self["hidden"] = false;
					}
				}
			}
		}
		if (/删除.*/.test(_self.text))
			this.icon = "images/toolbar/delete.gif";
		if (/查询.*/.test(_self.text))
			this.icon = "images/toolbar/query.gif";
		if (/增加.*/.test(_self.text))
			this.icon = "images/toolbar/add.gif";
		if (/保存.*/.test(_self.text))
			this.icon = "images/toolbar/save.gif";
		if (/重置.*/.test(_self.text))
			this.icon = "images/toolbar/reset.png";
		if (/修改.*/.test(_self.text))
			this.icon = "images/toolbar/edit.gif";
		if (/关闭.*/.test(_self.text))
			this.icon = "images/toolbar/close.gif";
		Ext.Button.superclass.initComponent.call(this);
		this.addEvents('click', 'toggle', 'mouseover', 'mouseout', 'menushow',
				'menuhide', 'menutriggerover', 'menutriggerout');
		if (this.menu) {
			this.menu = Ext.menu.MenuMgr.get(this.menu);
		}
		if (Ext.isString(this.toggleGroup)) {
			this.enableToggle = true;
		}
	}
});
Ext.override(Ext.form.ComboBox, {
	initComponent : function() {
		if (!this.store) {
			this.store = new Ext.data.SimpleStore( {
				fields : [ 'value', 'text' ],
				data : [ [] ]
			});
			this.valueField = 'value';
			this.displayField = 'text';
		}
		Ext.form.ComboBox.superclass.initComponent.call(this);
		this.addEvents('expand', 'collapse', 'beforeselect', 'select',
				'beforequery');
		if (this.store) {
			this.store = Ext.StoreMgr.lookup(this.store);
			if (this.store.autoCreated) {
				this.displayField = this.valueField = 'field1';
				if (!this.store.expandData) {
					this.displayField = 'field2';
				}
				this.mode = 'local';
			}
		}
		this.selectedIndex = -1;
		if (this.mode == 'local') {
			if (!Ext.isDefined(this.initialConfig.queryDelay)) {
				this.queryDelay = 10;
			}
			if (!Ext.isDefined(this.initialConfig.minChars)) {
				this.minChars = 0;
			}
		}
		if (this.tree) {
			this.tree.realId = Ext.id();
			var _self = this;
			if (!this.tree.leaf == undefined) {
				this.tree.leaf = true;
			}
			_self.tpl = "<tpl for='.'><div style='height:230px'><div id='"
					+ _self.tree.realId + "_combo'></div></div></tpl>";
			_self.treeObject = new Ext.tree.TreePanel( {
				useArrows : true,
				autoScroll : true,
				animate : true,
				containerScroll : true,
				rootVisible : false,
				listeners : {
					"beforeload" : function(node) {
						var id = _self.tree.realId == node.id ? _self.tree.id
								: node.id;
						_self.treeObject.loader.dataUrl = $.treeURL(
								_self.tree.url, id);
					},
					"click" : function(node, e) {
						if (_self.tree.leaf && !node.isLeaf()) {
							_self.collapse();
							Ext.Msg.alert('', '请您选择叶子节点');
							return;
						}
						var value;
						if (_self.tree.value) {
							value = node.attributes[_self.tree.value];
						} else
							value = node.id;
						var p = new Ext.data.Record( {
							value : value,
							text : node.text
						});
						_self.valueNotFoundText = node.text;
						_self.setValue(value);
						_self.collapse();
						_self.fireEvent('select', _self, p);
					}
				},
				root : {
					id : _self.tree.realId
				}
			});
			_self.on('expand', function() {
				_self.treeObject.render(_self.tree.realId + '_combo');
				_self.treeObject.root.expand();
			});
		}
	},
	setValue : function(v) {
		if (Ext.isDefined(this.valueNotFoundText)
				&& v == this.valueNotFoundText && this.valueNotFoundText != '')
			return this;
		var text = v;
		if (this.valueField) {
			var r = this.findRecord(this.valueField, v);
			if (r) {
				text = r.data[this.displayField];
			} else if (Ext.isDefined(this.valueNotFoundText)) {
				text = this.valueNotFoundText;
			}
		}
		this.lastSelectionText = text;
		if (this.hiddenField) {
			this.hiddenField.value = v;
		}
		Ext.form.ComboBox.superclass.setValue.call(this, text);
		this.value = v;
		return this;
	}
});
Ext
		.apply(
				Ext.form.VTypes,
				{
					phone : function(val, field) {
						return /^((\(\d{3}\))|(\d{3}\-))?(\(0\d{2,3}\)|0\d{2,3}-)?[1-9]\d{6,7}$/
								.test(val);
					},
					phoneText : '固定电话有错误',
					phoneMask : /[\d-]/i
				});
Ext.apply(Ext.form.VTypes, {
	mphone : function(val, field) {
		return /^(13[0-9]|15[0|3|6|8|9])\d{8}$/.test(val);
	},
	mphoneText : '移动电话有错误',
	mphoneMask : /[\d-+]/i
});
Ext
		.apply(
				Ext.form.VTypes,
				{
					name : function(val) {
						return /^[\u0391-\uFFE5A-z_]([\u0391-\uFFE5A-z _]*[\u0391-\uFFE5A-z_])?$/
								.test(val);
					},
					nameText : '请输入规范的名称',
					nameMask : /[\u0391-\uFFE5A-z _]/i
				});
Ext.apply(Ext.form.VTypes, {
	daterange : function(val, field) {
		var date = field.parseDate(val);
		if (!date) {
			return;
		}
		if (field.startDateField
				&& (!this.dateRangeMax || (date.getTime() != this.dateRangeMax
						.getTime()))) {
			var start = Ext.getCmp(field.startDateField);
			start.setMaxValue(date);
			start.validate();
			this.dateRangeMax = date;
		} else if (field.endDateField
				&& (!this.dateRangeMin || (date.getTime() != this.dateRangeMin
						.getTime()))) {
			var end = Ext.getCmp(field.endDateField);
			end.setMinValue(date);
			end.validate();
			this.dateRangeMin = date;
		}
		return true;
	}
});
Ext.apply(Ext.form.VTypes, {
	time : function(val, field) {
		return /^(0\d{1}|1\d{1}|2[0-3]):[0-5]\d{1}:([0-5]\d{1})$/.test(val);
	},
	timeText : '时间格式不正确(hh24:mm:ss)',
	timeMask : /[0-9:]/i
});
$ = function() {
	var randomId = -1;
	var http_spare = [];
	var http_max_spare = 8;
	var http_active_num = 0;
	var requestId = 0;
	var msxmlNames = [ "MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.3.0",
			"MSXML2.XMLHTTP", "MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0",
			"Microsoft.XMLHTTP" ];
	return {
		poolGetHTTPRequest : function() {
			var http = http_spare.pop();
			if (http) {
				return http;
			}
			return $.getHTTPRequest();
		},
		poolReturnHTTPRequest : function(http) {
			if (http_spare.length >= http_max_spare) {
				delete http;
			} else {
				http_spare.push(http);
			}
		},
		getHTTPRequest : function() {
			try {
				httpObjectName = "XMLHttpRequest";
				return new XMLHttpRequest();
			} catch (e) {
			}
			for ( var i = 0; i < msxmlNames.length; i++) {
				try {
					httpObjectName = msxmlNames[i];
					return new ActiveXObject(msxmlNames[i]);
				} catch (e) {
				}
			}
		},
		sendRequest : function(req) {
			var http = $.poolGetHTTPRequest();
			var retval = {};
			http.open("POST", $.serverURL, !!req.cb, null, null);
			http.setRequestHeader("Content-type", "text/plain");
			http.onreadystatechange = function() {
				if (http.readyState == 4) {
					var response = null;
					try {
						response = Ext.decode(http.responseText, true);
					} catch (e) {
						response = http.responseText;
					}
					if (response && !!response['error']) {
						$.unmask();
						Ext.Msg.alert('错误提示', response['error']);
					} else {
						req.cb(response, http.status, http.statusText);
					}
				}
			};
			try {
				http.send(req.data);
				if (!req.cb) {
					response = Ext.decode(http.responseText, false);
					if (response && !!response['error']) {
						$.unmask();
						Ext.Msg.alert('错误提示', response['error']);
						retval.status = http.status;
						retval.statusText = http.statusText;
					}
					retval.result = response || {};
					retval.status = http.status;
					retval.statusText = http.statusText;
				}
			} catch (e) {
				alert(e);
			}
			return retval;
		},
		makeRequest : function(methodName, args, cb) {
			var req = {};
			req.requestId = requestId++;
			var obj = "{id:" + req.requestId + ",method:\"" + methodName + "\"";
			if (cb) {
				req.cb = cb;
			}
			obj += ",params:" + Ext.encode(args) + "}";
			req.data = obj;
			return req;
		},
		call : function() {
			var argumentsLength = arguments.length;
			var arg_shift = 0, callBack, method;
			if (typeof arguments[argumentsLength - 1] == "function") {
				argumentsLength--;
				callBack = arguments[argumentsLength];
			}
			method = arguments[arg_shift];
			arg_shift++;
			var args = [];
			for (; arg_shift < argumentsLength; arg_shift++) {
				args.push(arguments[arg_shift]);
			}
			var req = $.makeRequest(method, args, callBack);
			return $.sendRequest(req);
		},
		makeURL : function(array, type_) {
			var arg_shift = 0, method;
			method = array[arg_shift];
			arg_shift++;
			var args = [];
			for ( var i = arg_shift; i < array.length; i++) {
				args.push(array[i]);
			}
			var req = $.makeRequest(method, args);
			return $.serverURL + "?!params=" + req.data + "&!type=" + type_;
		},
		URL : function() {
			return $.makeURL(arguments, 'N');
		},
		treeURL : function() {
			return $.makeURL(arguments, 'T');
		},
		linkURL : function() {
			var arg_shift = 0, method;
			method = arguments[arg_shift];
			arg_shift++;
			var args = [];
			for ( var i = arg_shift; i < arguments.length; i++) {
				args.push(arguments[i]);
			}
			var req = $.makeRequest(method, args);
			return $.serverURL + "?!params=" + escape(req.data) + "&!type=F";
		},
		forward : function() {
			var arg_shift = 0, method;
			method = arguments[arg_shift];
			arg_shift++;
			var args = [];
			for ( var i = arg_shift; i < arguments.length; i++) {
				args.push(arguments[i]);
			}
			var req = $.makeRequest(method, args);
			var randId = "MR_" + (new Date()).valueOf();
			var form = document.createElement("form");
			form.action = $.serverURL;
			form.method = "post";
			var t = document.createElement("input");
			t.type = "hidden";
			form.appendChild(t);
			t.name = '!params';
			t.value = req.data;
			var tt = document.createElement("input");
			tt.type = "hidden";
			form.appendChild(tt);
			tt.name = '!type';
			tt.value = 'F';
			document.body.appendChild(form);
			form.submit();
			return null;
		},
		randomId : function(begin) {
			if (!!begin) {
				return begin + randomId--;
			}
			return randomId--;
		},
		deleteNull : function(obj) {
			for ( var p in obj) {
				if (/ext-comp.*/.test(p) || obj[p] == '')
					delete obj[p];
			}
			return obj;
		},
		getFormValues : function(obj, likestr) {
			var formPanel;
			if (typeof obj == 'string') {
				formPanel = Ext.getCmp(obj);
			} else
				formPanel = obj;
			values = formPanel.getForm().getValues(false);
			combos = formPanel.findByType('combo');
			for ( var i = 0; i < combos.length; i++) {
				values[combos[i].getName()] = combos[i].getValue();
			}
			dates = formPanel.findByType('datefield');
			for ( var i = 0; i < dates.length; i++) {
				var dateVal = values[dates[i].getName()];
				if (dateVal == '')
					continue;
				values[dates[i].getName()] = Date.parseDate(dateVal,
						dates[i].format).getTime();
			}
			return likestr ? this.deleteNullAndLike(values, likestr) : this
					.deleteNull(values);
		},
		_insert : function(grid, initVal, config) {
			insert(grid, initVal, config);
		},
		insert : function(grid, initVal, config) {
			config = config || {};
			var p = new Ext.data.Record(initVal, config.id ? config.id : $
					.randomId());
			if (typeof grid == 'string') {
				grid = Ext.getCmp(grid);
			}
			grid.stopEditing();
			p.dirty = true;
			p.modified = initVal;
			grid.store.insert(config.index ? config.index : 0, p);
			grid.startEditing(config.index ? config.index : 0, 0);
			return p;
		},
		_save : function(grid, url, msg) {
			this.save(grid, url, msg);
		},
		save : function(grid, url, msg) {
			if (typeof grid == 'string') {
				grid = Ext.getCmp(grid);
			}
			var array = grid.store.getModifiedRecords();
			var inserts = [];
			var updates = [];
			for ( var i = 0; i < array.length; i++) {
				var sendData = {};
				Ext.apply(sendData, array[i].data);
				if (!sendData["id"]) {
					sendData.id = array[i].id;
				}
				if (array[i].id < 0)
					inserts.push(sendData);
				else
					updates.push(sendData);
			}
			$.call(url, inserts, updates, function(result) {
				for ( var i = 0; i < result.length; i++) {
					for ( var p in result[i]) {
						if (p != result[i][p]) {
							var index = grid.store.data.indexOfKey(p - 0);
							var rec = grid.store.getById(p);
							grid.store.data.keys[index] = result[i][p];
							grid.store.data.items[index] = rec;
							rec.id = result[i][p];
						}
					}
				}
				grid.store.commitChanges();
				$.msg(msg ? msg : '保存成功');
			});
		},
		_delete : function(grid, url) {
			var _grid = Ext.getCmp(grid);
			var sm = _grid.getSelectionModel();
			var records = sm.getSelections();
			var ids = [];
			for ( var i = 0; i < records.length; i++) {
				ids.push(records[i].id);
			}
			if (ids.length == 0) {
				$.msg('请选择相关记录');
				return;
			}
			Ext.Msg.confirm('信息', '确定要删除？', function(btn) {
				if (btn == 'yes') {
					$.call(url, ids, function() {
						for ( var i = 0; i < records.length; i++) {
							_grid.store.remove(records[i]);
						}
					});
				}
			});
		},
		deleteNullAndLike : function(obj, likes) {
			if (likes) {
				liks = likes.split(",");
				for ( var p in obj) {
					if (/ext-comp.*/.test(p) || obj[p] == '')
						delete obj[p];
					else {
						for ( var i = 0; i < liks.length; i++) {
							if (liks[i] == p)
								obj[p] = "%" + obj[p] + "%";
						}
					}
				}
			} else {
				for ( var p in obj) {
					if (obj[p] == '')
						delete obj[p];
					else
						obj[p] = "%" + obj[p] + "%";
				}
			}
			return obj;
		},
		mask : function(msg) {
			if (!msg) {
				msg = "正在处理,请稍后";
			}
			window.top.Ext.getCmp('tabpanel').getEl().mask(msg);
		},
		unmask : function() {
			window.top.Ext.getCmp('tabpanel').getEl().unmask();
		},
		msg : function(msg, time_) {
			if (!msg) {
				msg = "操作成功";
			}
			var newDiv = document.createElement("div");// 创建div
		newDiv.style.position = "absolute";// relative
		newDiv.style.backgroundColor = "Honeydew";
		newDiv.style.height = "25px";
		newDiv.style.width = "180px";
		newDiv.style.border = "solid LightSalmon 1px";
		newDiv.style.top = "200px";
		newDiv.style.left = "280px";
		newDiv.style.fontSize = "12px";
		newDiv.style.zIndex='10000';
		newDiv.style.visibility = "visible";// hidden
		newDiv.innerHTML = "<div style='TEXT-ALIGN: middle; padding: 5 0 0 0px; color: midnightblue' align='center'> <b>"
				+ msg + "</b><div>";
		document.body.appendChild(newDiv);// 
		setTimeout(function() {
			newDiv.style.visibility = 'hidden';
		}, time_ ? time_ : 2000);
	}
	}
}();
Sinosoft = $;
