package com.yunmei.frame.model;

import java.util.HashSet;
import java.util.Set;

public class OrganTree  {
	private Long id;
	private Long parent;
	private String text;
	private Integer left;
	private Integer right;
	private Character type;
	private User user;
	private Organ organ;
	private Post post;
	private Set<OrganTree> children=new HashSet<OrganTree>(0);
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public Set<OrganTree> getChildren() {
		return children;
	}

	public void setChildren(Set<OrganTree> children) {
		this.children = children;
	}

	public Organ getOrgan() {
		return organ;
	}

	public void setOrgan(Organ organ) {
		this.organ = organ;
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
}
