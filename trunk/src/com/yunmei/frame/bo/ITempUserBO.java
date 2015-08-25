package com.yunmei.frame.bo;

import java.io.Serializable;

import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.User;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Forward;

public interface ITempUserBO {
	public Forward enter();

	/**
	 * 查询临时用户
	 */
	public Page find(User user, int start, int maxResults) throws BOException;

	/**
	 * 删除临时用户
	 */
	public void delete(Long[] userIds) throws BOException;

	/**
	 *添加临时用户
	 */
	public Serializable insert(User user) throws BOException;

	/**
	 *更新临时用户
	 */
	public void update(User user) throws BOException;
	/**
	 * 人员转正
	 */
	public void turnNormal(Long userId, Long treeId) throws BOException;

}
