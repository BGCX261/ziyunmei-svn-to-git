package com.yunmei.frame.model;

import java.util.HashSet;
import java.util.Set;

public class Role {
	private Long id;
	private String name;
	private String remark;
	private Set<UserRole> userRoles=new HashSet<UserRole>(0);
	private Set<PostRole> postRoles=new HashSet<PostRole>(0);
	private Set<RoleAuthority> roleAuths=new HashSet<RoleAuthority>(0);
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Set<UserRole> getUserRoles() {
		return userRoles;
	}
	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}
	public Set<PostRole> getPostRoles() {
		return postRoles;
	}
	public void setPostRoles(Set<PostRole> postRoles) {
		this.postRoles = postRoles;
	}
	public Set<RoleAuthority> getRoleAuths() {
		return roleAuths;
	}
	public void setRoleAuths(Set<RoleAuthority> roleAuths) {
		this.roleAuths = roleAuths;
	}
}
