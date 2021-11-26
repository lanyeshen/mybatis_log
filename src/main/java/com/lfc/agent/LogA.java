package com.lfc.agent;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yeshen.lan
 * @date 2021/11/18 16:07
 * <p>
 */
public class LogA implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        try {
            // 改造的类
            CtClass ctClass = null;
            try {
                ctClass = pool.get("com.mysql.cj.jdbc.ClientPreparedStatement");
            } catch (NotFoundException e) {
                try {
                    ctClass = pool.get("com.mysql.cj.jdbc.PreparedStatement");
                } catch (NotFoundException notFoundException) {
                    ctClass = pool.get("com.mysql.jdbc.PreparedStatement");
                }
            }
            // 需要改造的方法
            Map<String,String> methods = new HashMap<>(3);
            methods.put("executeQuery", "java.sql.ResultSet");
            methods.put("execute", "boolean");
            methods.put("executeUpdate", "int");

            for (String methodName : methods.keySet()) {
                String s = methods.get(methodName);
                CtMethod executeQuery = ctClass.getDeclaredMethod(methodName);
                CtMethod executeQueryNew = CtNewMethod.copy(executeQuery, ctClass, null);
                String newMethodName = executeQuery.getName()+"$agent";
                executeQuery.setName(newMethodName);
                executeQueryNew.setBody("{            " +
                        "            long begin = System.currentTimeMillis();\n" +
                        "            java.time.LocalDate date = java.time.LocalDate.now();\n"+
                        "            java.time.LocalTime time = java.time.LocalTime.now();\n"+
                        "            System.out.println(\"\033[93m[\033[34m\"+date.toString() +\" \"+ time.toString()+\"\033[0m\033[93m]---------------------------------------\033[0m\");\n"+
                        "            System.out.println(\"\033[32m\"+asSql()+\"\033[0m\");\n" +
                        "            "+s+" execResults = "+newMethodName+"();\n" +
                        "            System.out.println(\"\033[93m[\033[0m\"+\"\033[34m\"+(System.currentTimeMillis()-begin)+\"ms\033[0m\"+\"\033[93m]----------------------------------------------------------\033[0m\");"+
                        "            return ($r)execResults;" +
                        "}");
                ctClass.addMethod(executeQueryNew);
            }

            ctClass.toClass();
            System.out.println(">>>>>>MybatisLog初始化成功<<<<<<<");
        } catch (NotFoundException e) {
            System.err.println("<<<<<<MybatisLog初始化失败>>>>>>>");
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }


        return new byte[0];
    }
}
