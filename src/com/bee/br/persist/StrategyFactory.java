package com.bee.br.persist;

/**
 * <title>
 *  输出策略
 * </title> 
 * @author XiangYuan 
 * @version 0.1 
 * @time 2011-10-21
 * @mailto liyj2@wondershare.cn
 */
public class StrategyFactory {

	/**
	 * get the input type
	 * @param type
	 * @return
	 */
	public static IStrategy getStrategy(String type) {
		if ("json".equals(type)) {
			return new JsonIO();
		}
		if ("xml".equals(type)) {
			
		}
		return null;
	}
}
