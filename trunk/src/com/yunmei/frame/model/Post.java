package com.yunmei.frame.model;

import java.util.HashSet;
import java.util.Set;

/**
 * 岗位
 * @author ghost
 *
 */
public class Post {
	private Long id;
	private String name;
	private Long admin;
	private String remark;
	private Set<PostRole> postRoles=new HashSet<PostRole>(0);
	
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

	public Set<PostRole> getPostRoles() {
		return postRoles;
	}
	public void setPostRoles(Set<PostRole> postRoles) {
		this.postRoles = postRoles;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Long getAdmin() {
		return admin;
	}
	public void setAdmin(Long admin) {
		this.admin = admin;
	}
}
