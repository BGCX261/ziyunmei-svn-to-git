package com.yunmei.frame.model;

import java.util.HashSet;
import java.util.Set;

public class Authority {
	private Long id;
	private Long parent;
	private String text;
	private Integer left;
	private Integer right;
	private Character type;
	private String url;
	private String remark;
	private Set<Authority> children = new HashSet<Authority>();
	private Set<RoleAuthority> authRoles = new HashSet<RoleAuthority>();

	public Set<Authority> getChildren() {
		return children;
	}

	public void setChildren(Set<Authority> children) {
		this.children = children;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}


	public Set<RoleAuthority> getAuthRoles() {
		return authRoles;
	}

	public void setAuthRoles(Set<RoleAuthority> authRoles) {
		this.authRoles = authRoles;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getLeft() {
		return left;
	}

	public void setLeft(Integer left) {
		this.left = left;
	}

	public Integer getRight() {
		return right;
	}

	public void setRight(Integer right) {
		this.right = right;
	}

	public Character getType() {
		return type;
	}

	public void setType(Character type) {
		this.type = type;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (id == null || !(obj instanceof Authority))
			return false;
		Authority a = (Authority) obj;
		return this.id.equals(a.getId());
	}

	public int hashCode() {
		return id == null ? System.identityHashCode(this) : id.hashCode();
	}
}
