package com.yunmei.frame.bo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Organ;
import com.yunmei.frame.model.OrganTree;
import com.yunmei.frame.model.Post;
import com.yunmei.frame.model.PostRole;
import com.yunmei.frame.model.User;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Forward;

public interface IOrganBO {
	/**
	 * 进入组织结构菜单
	 */
	public Forward enterOrgan();

	/**
	 * 得到节点信息(可能为局室，岗位，用户)
	 */
	public Object getNode(Long nodeId) throws BOException;

	/**
	 *得到用户信息 
	 */
	public User getUser(Long userId) throws BOException;
	/**
	 * 得到上级节点
	 */
	public List<OrganTree> findSuperNodes(Long userId, Character type)
			throws BOException;
	/**
	 * 得到子节点信息，类型为type.
	 */
	public List findSubNodes(Long userId, String type) throws BOException;
	/**
	 * 根据节点来获得相应的树,参数为树ID
	 */
	public String findOrganTree(Long id) throws BOException;
	/**
	 *查找用户树 
	 */
	public String findUserTree(Long id) throws BOException;
	/**
	 *查找岗位树 
	 */
	public String findPostTree(Long id) throws BOException;



	public void deleteNode(Long nodeId) throws BOException;
	/**
	 *正式用户查找
	 */
	// TODO 用户查找
	public Page findUsers(User user, int start, int maxResults)
			throws BOException;
	/**
	 * 添加局室节点
	 */
	public Map insertOrgan(Organ organ, Long parent) throws BOException;
	/**
	 * 添加岗位节点
	 */
	public Map insertPost(Post post, Long parent) throws BOException;
	/**
	 *添加用户节点
	 */
	public Map insertUser(User user, Long parent) throws BOException;
	/**
	 * 更新局室节点
	 */
	public Serializable updateOrgan(Organ organ) throws BOException;
	/**
	 *更新岗位 
	 */
	public Serializable updatePost(Post post) throws BOException;
	/**
	 *正式用户 
	 */
	public List<Serializable> updateUser(User user) throws BOException;
	/**
	 * 保存岗位角色
	 */
	public void savePostRole(PostRole pr) throws BOException;
	/**
	 * 删除岗位角色
	 */
	public void deletePostRole(PostRole pr) throws BOException;
	/**
	 * 修改用户密码 sun 09-11-10
	 */
	public String alterPassword(Long userid, String password) throws BOException;
	/**
	 * 重载修改用户密码 sun 09-11-17
	 */
	public String alterPassword(Long userid, String oldpassword, String password) throws BOException;
/**
	 * 人员掉岗
	 */
	public void changePost(Long userTreeId,Long postTreeId)throws BOException;
	
	public Serializable insertUser2Post(Long userTreeId,Long postTreeId)throws BOException;
}
