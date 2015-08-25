package com.yunmei.frame.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.yunmei.frame.utils.$;
import com.yunmei.frame.utils.Constraint;

public class User {
	private Long id;
	private String code;
	private String subject;
	private Date birthday;
	private String nationality;
	private String education;
	private String name;
	private String password;
	private String login;
	private String remark;
	private String tphone;
	private String email;
	private String sex;
	private String mphone;
	private String ip;
	private Long defaultPost;
	/**
	 * 上级信息
	 */
	private List<Post> posts = new ArrayList<Post>(2);
	/**
	 * 权限信息
	 */
	private List<Authority> auths = new ArrayList<Authority>();

	private Set<UserRole> userRoles = new HashSet<UserRole>(0);

	public User() {
	}

	public User(Long id) {
		this.id = id;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	public static User getUser() {
		if ($.getSession() == null)
			return null;
		return (User) $.getSession().getAttribute(Constraint.LOGIN);
	}

	public static void setUser(User user) {
		if (user == null)
			$.getSession().removeAttribute(Constraint.LOGIN);
		else
			$.getSession().setAttribute(Constraint.LOGIN, user);
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getTphone() {
		return tphone;
	}

	public void setTphone(String tphone) {
		this.tphone = tphone;
	}

	public String getMphone() {
		return mphone;
	}

	public void setMphone(String mphone) {
		this.mphone = mphone;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getDefaultPost() {
		return defaultPost;
	}

	public void setDefaultPost(Long defaultPost) {
		this.defaultPost = defaultPost;
	}

	public List<Authority> getAuths() {
		return auths;
	}

	public void setAuths(List<Authority> auths) {
		this.auths = auths;
	}
}
