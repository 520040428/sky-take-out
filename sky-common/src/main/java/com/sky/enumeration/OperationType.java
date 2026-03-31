package com.sky.enumeration;

/**
 * 数据库操作类型
 * 为什么要使用数据库操作类型
 * 由于我们在进行update和insert操作时，我们对数据库的公共字段填充是不一样的，所以我们需要进行判断
 */
public enum OperationType {

    /**
     * 更新操作
     */
    UPDATE,

    /**
     * 插入操作
     */
    INSERT

}
