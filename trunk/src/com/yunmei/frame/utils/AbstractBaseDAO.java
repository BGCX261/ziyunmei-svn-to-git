package com.yunmei.frame.utils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Example;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.yunmei.frame.servlet.Page;

public abstract class AbstractBaseDAO<T> extends HibernateDaoSupport {

	private static Logger log = Logger.getLogger(AbstractBaseDAO.class);
	private Class<T> modelClass;

	@SuppressWarnings("unchecked")
	public AbstractBaseDAO() {
		modelClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public List<T> findByLike(T instance) {
		Criteria crit = getSession().createCriteria(modelClass);
		Example example = Example.create(instance);
		example.enableLike();
		crit.add(example);
		return crit.list();
	}

	public Page findByLike(T instance, int start, int maxResults) {
		Page page = new Page();
		Criteria crit = getSession().createCriteria(modelClass);
		Example example = Example.create(instance);
		example.enableLike();
		crit.add(example);
		page.setCount(crit.list().size());
		crit.setFirstResult(start);
		crit.setMaxResults(maxResults);
		page.setList(crit.list());
		return page;
	}

	public void update(T instance) {
		getHibernateTemplate().merge(instance);
	}

	public Page findByExample(T instance, int start, int maxResults) {
		Page page = new Page();
		Criteria crit = getSession().createCriteria(modelClass);
		Example example = Example.create(instance);
		crit.add(example);
		page.setCount(crit.list().size());
		crit.setFirstResult(start);
		crit.setMaxResults(maxResults);
		page.setList(crit.list());
		return page;
	}

	public List<T> findByExample(T instance) {
		Criteria crit = getSession().createCriteria(modelClass);
		Example example = Example.create(instance);
		crit.add(example);
		return crit.list();
	}

	public Serializable insert(T instance) {
		return getHibernateTemplate().save(instance);
	}

	public void delete(Serializable id) {
		getHibernateTemplate().delete(
				getHibernateTemplate().get(modelClass, id));
	}

	public void delete(Serializable ids[]) {
		for (Serializable id : ids)
			this.delete(id);
	}

	public T get(Serializable id) {
		return (T) getHibernateTemplate().get(this.modelClass, id);
	}

	public T load(Serializable id) {
		return (T) getHibernateTemplate().load(this.modelClass, id);
	}
}
