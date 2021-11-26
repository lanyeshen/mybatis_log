package com.lfc.agent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Date;


public class MyBatisAgent implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        if (!"org/apache/ibatis/executor/BaseExecutor".equals(className)) {
            return null;
        }

        try {
            // tomcat 中可行，在Spring boot中不可行
            appendToLoader(loader);
        } catch (Exception e) {
            System.err.println("SQL打印插件装载失败！");
            e.printStackTrace();
            return null;
        }

        try {
            ClassPool pool = new ClassPool();
            pool.appendSystemPath();
            pool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = pool.get("org.apache.ibatis.executor.BaseExecutor");
            // 查询
            CtMethod ctMethod = ctClass.getDeclaredMethods("query")[1];
            ctMethod.addLocalVariable("info", pool.get(SqlInfo.class.getName()));
            ctMethod.insertBefore("info=com.lfc.agent.MyBatisAgent.begin($args);");
            ctMethod.insertAfter("com.lfc.agent.MyBatisAgent.end(info);");
            System.out.println("SQL打印插件装载完毕！");
            return ctClass.toBytecode();
        } catch (NotFoundException | CannotCompileException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void appendToLoader(ClassLoader loader) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, MalformedURLException {
        URLClassLoader urlClassLoader = (URLClassLoader) loader;
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        String path = MyBatisAgent.class.getResource("").getPath();
        path = path.substring(0, path.indexOf("!/"));
        addURL.invoke(urlClassLoader, new URL(path));
    }

    public static SqlInfo begin(Object[] params) {
        SqlInfo info = new SqlInfo();
        info.date = java.time.LocalDate.now();
        info.time = java.time.LocalTime.now();
        info.beginTime = System.currentTimeMillis();
        BoundSqlAdapter adapter = new BoundSqlAdapter(params[5]);
        info.sql = adapter.getSql();
        return info;
    }

    public static void end(SqlInfo info) {
        info.useTime = System.currentTimeMillis() - info.beginTime;
        System.out.println("\033[93m[\033[34m" + info.date.toString() + info.time.toString() + "\033[0m" + "\033[93m" + "---------------------------------------\033[0m");
        System.out.println("\033[32m" + info.sql + "\033[0m");
        System.out.println("\033[93m[\033[0m"+"\033[34m"+(info.useTime)+"ms\033[0m"+"\033[93m]----------------------------------------------------------\033[0m");
    }

    public static class SqlInfo {
        public long beginTime;
        public long useTime;
        public String sql;
        public java.time.LocalDate date;
        public java.time.LocalTime time;

        @Override
        public String toString() {
            return "SqlInfo{" +
                    "beginTime=" + new Date(beginTime) +
                    ", useTime=" + useTime +
                    ", sql='" + sql + '\'' +
                    '}';
        }
    }

    public static class BoundSqlAdapter {
        Object target;
        private static Method getSql;
        private static Class aClass;

        private synchronized static void init(Class cls) {
            try {
                aClass = cls;
                getSql = cls.getDeclaredMethod("getSql");
                getSql.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        }

        public BoundSqlAdapter(Object target) {
            this.target = target;
            if (aClass == null) {
                init(target.getClass());
            }
            this.target = target;
        }

        public String getSql() {
            try {
                return (String) getSql.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}