package com.lfc.agent.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * jdbc 数据采集
 */

public class JdbcCollects {

    public static SqlInfo begin(Connection connection, String sql) {
        SqlInfo info = new SqlInfo();
        info.sql = sql;
        info.date = java.time.LocalDate.now();
        info.time = java.time.LocalTime.now();
        info.begin = System.currentTimeMillis();
        try {
            info.jdbcUrl = connection.getMetaData().getURL();
            info.databaseName = getDbName(info.jdbcUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }


    public static  void end(SqlInfo info) {
        String sql = info.sql;
        String[] split = sql.split(": ");
        info.sql = split[split.length-1];
        info.useTime = System.currentTimeMillis() - info.begin;
        System.out.println("\033[93m[\033[34m" + info.date.toString() + " " + info.time.toString() + "\033[0m" + "\033[93m" + "---------------------------------------\033[0m");
        System.out.println("\033[32m" + info.sql + "\033[0m");
        System.out.println("\033[93m[\033[0m"+"\033[34m"+(info.useTime)+"ms\033[0m"+"\033[93m]----------------------------------------------------------\033[0m");
    }

    public static void error(SqlInfo stat, Throwable throwable) {
        if (stat != null) {
            if (throwable instanceof InvocationTargetException) {
                stat.error = ((InvocationTargetException) throwable).getTargetException().getMessage();
            } else {
                stat.error = throwable.getMessage();
            }
        }
    }


    public static Connection proxyConnection(final Connection connection) {
        Object c = Proxy.newProxyInstance(JdbcCollects.class.getClassLoader()
                , new Class[]{Connection.class}, new ConnectionHandler(connection));
        return (Connection) c;
    }


    public static PreparedStatement proxyPreparedStatement(final PreparedStatement statement, SqlInfo jdbcStat) {
        Object c = Proxy.newProxyInstance(JdbcCollects.class.getClassLoader()
                , new Class[]{PreparedStatement.class}, new PreparedStatementHandler(statement, jdbcStat));
        return (PreparedStatement) c;
    }

    private static String getDbName(String url) {
        int index = url.indexOf("?"); //$NON-NLS-1$
        if (index != -1) {
            String paramString = url.substring(index + 1, url.length());
            url = url.substring(0, index);
        }
        String dbName = url.substring(url.lastIndexOf("/") + 1);
        return dbName;
    }
}
