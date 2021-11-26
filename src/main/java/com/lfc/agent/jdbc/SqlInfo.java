package com.lfc.agent.jdbc;


/**
 * @author
 * @date 2021
 */
public class SqlInfo   {
    public Long begin;// 时间戳
    public Long useTime;
    // jdbc url
    public String jdbcUrl;
    // sql 语句
    public String sql;
    // 数据库名称
    public String databaseName;
    // 异常信息
    public String error;
    public java.time.LocalDate date;
    public java.time.LocalTime time;

    @Override
    public String toString() {
        return "SqlInfo{" +
                "begin=" + begin +
                ", useTime=" + useTime +
                ", jdbcUrl='" + jdbcUrl + '\'' +
                ", sql='" + sql + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
