package com.yunmei.frame.bo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Authority;
import com.yunmei.frame.model.DataFilter;
import com.yunmei.frame.model.Role;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Forward;

public interface IAuthorityBO {
	/**
	 * 进入权限配置菜单
	 */
	public Forward enterAuth();

	public List<Long> findUsersByRoleId(Long roleId);

	public List<Long> findUsersByPostId(Long postId);

	/**
	 * 进入菜单树菜单
	 */
	public Forward enterMenu();

	/**
	 *进入数据过滤菜单
	 */
	public Forward enterDataAuth();

	/**
	 *个人信息菜单
	 */
	public Forward enterInfo();

	/**
	 *用户角色配置
	 */
	public Forward enterUserRole();

	/**
	 *为角色添加过滤条件
	 */
	public Serializable insertFilter2Role(DataFilter df) throws BOException;

	/**
	 * 查找过滤信息
	 */
	public List findFilters() throws BOException;

	/**
	 * 查找发布的方法
	 */
	public Page findInvokeMethod(Map map1, Integer start, Integer end)
			throws BOException;

	/**
	 * 重新加载权限配置信息
	 */
	public void reload() throws BOException;

	/**
	 * 加载用户菜单
	 */
	public String findMenus(Long id) throws BOException;

	public List<Map> findDataFields(Long id) throws BOException;

	/**
	 * 加载全局菜单
	 */
	public String findGobalMenus(Long parent) throws BOException;

	public String findDataMenus(Long parent) throws BOException;

	/**
	 * 加载所有可配置权限(菜单，数据，还有操作权限)
	 */
	public String findAuths(Long parent) throws BOException;

	/**
	 * 用户登录
	 */
	public Forward login(String login, String password) throws BOException;

	/**
	 * 增加权限到角色
	 */

	public Forward changePost(Long userId, Long postId) throws BOException;

	public void insertAuthToRole(Long roleId, Long arrray[]) throws BOException;

	/**
	 * 获得角色拥有的权限
	 */
	public List<Long> getAuthsByRoleId(Long roleId) throws BOException;

	/**
	 * 用户注销
	 */
	public Forward logout() throws BOException;

	/**
	 * 查找未拥有的直接角色
	 */
	public Page findDirectNotRoles(Long userId, int start, int maxResult)
			throws BOException;

	/**
	 *角色查询
	 */
	public Page findRoles(Role role, int start, int maxResult)
			throws BOException;

	/**
	 * 更新菜单名
	 */
	public void updateMenu(Long menuId, String text) throws BOException;

	/**
	 *删除该菜单
	 */
	public void deleteMenu(Long menuId) throws BOException;

	/**
	 * 给角色赋予菜单
	 */
	public void insertRole2User(Long[] roleIds, Long userId) throws BOException;

	/**
	 * 保存菜单
	 */
	public Long saveMenu(Authority auth) throws BOException;

	public List<DataFilter> findDataAuthsByRoleId(Long roleId)
			throws BOException;

	/**
	 *菜单移动
	 */
	public void moveMenu(Long sonId, Long parentId) throws BOException;

	/**
	 * 删除用户的直接角色
	 */
	public void deleteDirectRoles(Long userId, Long[] roleId)
			throws BOException;

	public void deleteRole(Long roleId) throws BOException;

	/**
	 *根据岗位ID得到岗位角色
	 */
	public List findRolesByPostId(Long id) throws BOException;

	/**
	 * 角色更新
	 */
	public List<Map> saveRoles(List<Role> insert, List<Role> update)
			throws BOException;

	public void deleteDataFromRole(Long id) throws BOException;

	/**
	 *根据岗位ID得到未拥有的角色
	 */
	public List findNotRolesByPostId(Long id) throws BOException;

	/**
	 * 根据用户ID得到所有角色(岗位角色,用户角色)
	 */
	public List findRolesByUserId(Long id) throws BOException;

}
