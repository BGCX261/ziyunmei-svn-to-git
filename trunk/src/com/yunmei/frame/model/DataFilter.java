package com.yunmei.frame.model;

public class DataFilter {

	private Long id;
	private Long roleId;
	private Long authId;
	private String filterSQL;
	private Integer order;
	private String remark;
	private String authName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Long getAuthId() {
		return authId;
	}

	public void setAuthId(Long authId) {
		this.authId = authId;
	}

	public String getFilterSQL() {
		return filterSQL;
	}

	public void setFilterSQL(String filterSQL) {
		this.filterSQL = filterSQL;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAuthName() {
		return authName;
	}

	public void setAuthName(String authName) {
		this.authName = authName;
	}
}
