package com.yunmei.frame.utils;

import java.io.Serializable;
import java.util.List;

import com.yunmei.frame.exception.BOException;

public interface IBaseBO<T,PK extends Serializable> {

	public List<T> getList() throws BOException;

	public List<T> getList(int firstResult, int maxResults) throws BOException;

	public T getObject(PK id) throws BOException;

	public T loadObject(PK id) throws BOException;

	public void create(T instance) throws BOException;

	public void update(T instance) throws BOException;

	public void delete(PK id) throws BOException;

	public List<T> findByExample(T example) throws BOException;

	public List<T> findByLike(T example) throws BOException;
}
