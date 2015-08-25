package com.yunmei.frame.model;

import java.util.HashSet;
import java.util.Set;
public class Dict implements java.io.Serializable {
	private String id;
	private String name;
	private Long order;
	private String desc;

	private Set<DictInfo> dictInfos = new HashSet<DictInfo>();

	public Set<DictInfo> getDictInfos() {
		return dictInfos;
	}

	public void setDictInfos(Set<DictInfo> dictInfos) {
		this.dictInfos = dictInfos;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
