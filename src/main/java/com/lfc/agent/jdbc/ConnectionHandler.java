package com.lfc.agent.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

/**
 * connection 代理处理
 */
public class ConnectionHandler implements InvocationHandler {
    private final Connection connection;
    private final static String[] connection_agent_methods = new String[]{"prepareStatement"};

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean isTargetMethod = Arrays.stream(connection_agent_methods).anyMatch(n -> n.equals(method.getName()));


        Object result = null;
        SqlInfo info = null;
        try {
            if (isTargetMethod) { // 获取PreparedStatement 开始统计
                info = JdbcCollects.begin(connection, (String) args[0]);
            }
            result = method.invoke(connection, args);
            // 代理 PreparedStatement
            if (isTargetMethod && result instanceof PreparedStatement) {
                PreparedStatement ps = (PreparedStatement) result;
                result = JdbcCollects.proxyPreparedStatement(ps, info);
            }
        } catch (Throwable e) {
            JdbcCollects.error(info, e);
            JdbcCollects.end(info);
            throw e;
        }
        return result;
    }
}