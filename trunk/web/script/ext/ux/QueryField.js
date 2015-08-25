Ext.ns('Ext.ux.form');
Ext.ux.form.QueryField = Ext.extend(Ext.form.TextField, {
	buttonOffset : 3,
	autoSize : Ext.emptyFn,
	initComponent : function() {
		Ext.ux.form.QueryField.superclass.initComponent.call(this);
	},
	onRender : function(ct, position) {
		Ext.ux.form.QueryField.superclass.onRender.call(this, ct, position);
		this.wrap = this.el.wrap( {
			cls : 'x-form-field-wrap x-form-file-wrap'
		});
		this.el.addClass('x-form-file-text');
		this.fileInput = this.wrap.createChild( {
			cls : 'x-form-query',
			tag : 'input',
			type : 'text',
			size : 1
		});
		this.button = new Ext.Button( {
			renderTo : this.wrap,
			cls : 'x-form-file-btn',
			iconCls : 'x-form-query-icon'
		});
		this.fileInput.on( {
			scope : this,
			click : this.click
		});
		this.resizeEl = this.positionEl = this.wrap;
	},

	reset : function() {
		this.fileInput.remove();
		this.fileInput = this.wrap.createChild( {
			cls : 'x-form-query',
			tag : 'input',
			type : 'text',
			size : 1
		});
		this.fileInput.on( {
			scope : this,
			click : this.click
		});
		Ext.ux.form.QueryField.superclass.reset.call(this);
	},

	// private
	onResize : function(w, h) {
		Ext.ux.form.QueryField.superclass.onResize.call(this, w, h);

		this.wrap.setWidth(w);

		if (!this.buttonOnly) {
			var w = this.wrap.getWidth() - this.button.getEl().getWidth()
					- this.buttonOffset;
			this.el.setWidth(w);
		}
	},

	// private
	onDestroy : function() {
		Ext.ux.form.QueryField.superclass.onDestroy.call(this);
		Ext.destroy(this.fileInput, this.button, this.wrap);
	},

	onDisable : function() {
		Ext.ux.form.QueryField.superclass.onDisable.call(this);
		this.doDisable(true);
	},

	onEnable : function() {
		Ext.ux.form.QueryField.superclass.onEnable.call(this);
		this.doDisable(false);

	},

	// private
	doDisable : function(disabled) {
		this.fileInput.dom.disabled = disabled;
		this.button.setDisabled(disabled);
	},

	preFocus : Ext.emptyFn,

	alignErrorIcon : function() {
		this.errorIcon.alignTo(this.wrap, 'tl-tr', [ 2, 0 ]);
	}

});
Ext.reg('queryfield', Ext.ux.form.QueryField);
Ext.form.QueryField = Ext.ux.form.QueryField;
