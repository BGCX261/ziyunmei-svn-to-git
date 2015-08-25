package com.yunmei.frame.utils;

import java.io.Serializable;
import java.util.List;

import com.yunmei.frame.exception.DAOException;

public interface IBaseDAO<T> {

	public List<T> getList() throws DAOException;

	public List<T> getList(int firstResult, int maxResults) throws DAOException;

	public T getObject(Serializable id) throws DAOException;

	public T loadObject(Serializable id) throws DAOException;

	public void create(T paramT) throws DAOException;

	public void update(T paramT) throws DAOException;

	public void delete(Serializable id) throws DAOException;

	public List<T> findByExample(T example) throws DAOException;

	public List<T> findByLike(T example) throws DAOException;
}
