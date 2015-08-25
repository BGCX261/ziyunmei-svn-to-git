package com.yunmei.frame.model;

public class DictInfo implements java.io.Serializable {

	private Long id;
	
	private String dictId;
	/**
	 * 字典信息名称
	 */
	private String name;
	/**
	 * 字典信息值
	 */
	private String value;
	/**
	 * 字典信息排序
	 */
	private Long order;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getDictId() {
		return dictId;
	}

	public void setDictId(String dictId) {
		this.dictId = dictId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

}
