package com.lfc.agent.jdbc;

import com.lfc.agent.MyBatisAgent;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

public class MySqlDriverAgent implements ClassFileTransformer {

    private static final String TARGET_CLASS = "com.mysql.cj.jdbc.NonRegisteringDriver";
    private static final String TARGET_METHOD = "connect";
    private static final String TARGET_METHOD_DESC = "(Ljava/lang/String;Ljava/util/Properties;)Ljava/sql/Connection;";


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!TARGET_CLASS.replaceAll("\\.", "/").equals(className)) {
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
            return buildClass(loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] buildClass(ClassLoader loader) throws Exception {
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.get(TARGET_CLASS);
        CtMethod oldMethod = ctClass.getMethod(TARGET_METHOD, TARGET_METHOD_DESC);
        CtMethod newMethod = CtNewMethod.copy(oldMethod, ctClass, null);
        oldMethod.setName(oldMethod.getName() + "$agent");
        String endSrc = "result=com.lfc.agent.jdbc.JdbcCollects.proxyConnection((java.sql.Connection)result);";
        newMethod.setBody(String.format(source, "", TARGET_METHOD, "", endSrc));
        ctClass.addMethod(newMethod);
        System.out.println("SQL打印插件装载完毕！");
        return ctClass.toBytecode();
    }

    private void appendToLoader(ClassLoader loader) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, MalformedURLException {
        URLClassLoader urlClassLoader = (URLClassLoader) loader;
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        String path = MyBatisAgent.class.getResource("").getPath();
        path = path.substring(0, path.indexOf("!/"));
        addURL.invoke(urlClassLoader, new URL(path));
    }
    final static String source = "{\n"
            + "%s"
            + "        Object result=null;\n"
            + "       try {\n"
            + "            result=($w)%s$agent($$);\n"
            + "        } catch (Throwable e) {\n"
            + "%s"
            + "            throw e;\n"
            + "        }finally{\n"
            + "%s"
            + "        }\n"
            + "        return ($r) result;\n"
            + "}\n";
}