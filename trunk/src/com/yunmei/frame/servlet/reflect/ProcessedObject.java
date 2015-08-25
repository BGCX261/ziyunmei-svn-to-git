package com.yunmei.frame.servlet.reflect;

import java.util.LinkedList;
import java.util.List;

public class ProcessedObject {
	/**
	 * The parent object of this object. It will be null if this is the root
	 * object being processed.
	 */
	private ProcessedObject parent;

	/**
	 * The processed incoming object. When marshalling, this is the java object
	 * that is being marshalled to json, when unmarshalling, this is the json
	 * object being marshalled to java.
	 */
	private Object object;

	private Object serialized;

	/**
	 * The "json" reference key such that [the json representation of]
	 * parent[ref] = object. this will either be a String for an object
	 * reference or an Integer for an array reference.
	 */
	private Object ref;

	public ProcessedObject getParent() {
		return parent;
	}

	public void setParent(ProcessedObject parent) {
		this.parent = parent;
	}

	/**
	 * Get the actual Object that this ProcessedObject wraps.
	 * 
	 * @return the actual Object that this ProcessedObject wraps.
	 */
	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public void setSerialized(Object serialized) {
		this.serialized = serialized;
	}

	public Object getSerialized() {
		return serialized;
	}

	public Object getRef() {
		return ref;
	}

	public void setRef(Object ref) {
		this.ref = ref;
	}

	public List getLocation() {
		ProcessedObject link = this;
		List path = new LinkedList();
		while (link != null) {
			path.add(0, link.ref);
			link = link.getParent();
		}
		return path;
	}
}
