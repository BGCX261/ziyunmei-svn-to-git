package com.yunmei.frame.model;
/**
 * 
 * @author ghost
 *
 */
public class PostRole {
	private Long id;
	private Role role;
	private Post post;
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public Post getPost() {
		return post;
	}
	public void setPost(Post post) {
		this.post = post;
	}

}
