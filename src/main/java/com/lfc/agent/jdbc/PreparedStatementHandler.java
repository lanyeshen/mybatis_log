package com.lfc.agent.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;

/**
 * PreparedStatement 代理处理
 */
public class PreparedStatementHandler implements InvocationHandler {
    private final PreparedStatement statement;
    private final SqlInfo info;
    private final static String[] prepared_statement_methods = new String[]{"execute", "executeUpdate", "executeQuery"};

    public PreparedStatementHandler(PreparedStatement statement, SqlInfo info) {
        this.statement = statement;
        this.info = info;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean isTargetMethod = false;
        for (String agentm : prepared_statement_methods) {
            if (agentm.equals(method.getName())) {
                isTargetMethod = true;
                break;
            }
        }
        Object result = null;
        try {
            result = method.invoke(statement, args);
        } catch (Throwable e) {
            if (isTargetMethod) {
                JdbcCollects.error(info, e);
            }
            throw e;
        } finally {
            if (isTargetMethod) {
                info.sql = statement.toString();
                JdbcCollects.end(info);
            }
        }
        return result;
    }
}
