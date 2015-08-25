package com.yunmei.frame.bo;

import java.util.List;
import java.util.Map;

import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Dict;
import com.yunmei.frame.model.DictInfo;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Forward;

public interface IDictBO {
	/**
	 * 批量插入更新
	 */
	public List<Map> saveDict(List<Dict> insert, List<Dict> update)
			throws BOException;

	/**
	 * 批量插入更新
	 */
	public List<Map> saveDictInfo(List<DictInfo> insert, List<DictInfo> update)
			throws BOException;

	/**
	 * 字典定义查询
	 */
	public Page findDicts(Dict dict, Integer start, Integer max)
			throws BOException;
	/**
	 * 字典明细查询
	 */
	public Page findDictInfoByDictId(String dictId, Integer start, Integer max)
			throws BOException;

	public List findDictInfoByDictId(String dictId) throws BOException;

	public void deleteDict(String id) throws BOException;

	public void deleteDictInfo(Long dictInfoId) throws BOException;

	public Forward enterDict() throws BOException;

}
