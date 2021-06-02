package com.lfc.agent;

import java.lang.instrument.*;
import java.util.HashMap;
import java.util.Map;

import javassist.*;

/**
 * @Classname AgentMain
 * @Description 打印sql
 * @Author yeshen.lan
 * @CreateTime 2020-11-04 09:52
 * @Version 1.0
 **/
public class AgentMain {

    public static void premain(String args, Instrumentation instrumentation) {

        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        try {
            // 改造的类
            CtClass ctClass = null;
            try {
                ctClass = pool.get("com.mysql.cj.jdbc.ClientPreparedStatement");
            } catch (NotFoundException e) {
                ctClass = pool.get("com.mysql.cj.jdbc.PreparedStatement");
            }
            //CtClass ctClass = pool.get("com.mysql.cj.jdbc.PreparedStatement");
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
                        "             System.out.println(\"\033[93m[\033[34m"+java.time.LocalDate.now()+" "+java.time.LocalTime.now()+"\033[0m\033[93m]---------------------------------------\033[0m\");\n"+
                        "            System.out.println(\"\033[35m\"+asSql()+\"\033[0m\");\n" +
                        "            "+s+" execResults = "+newMethodName+"();\n" +
                        "            System.out.println(\"\033[93m[\033[0m\"+\"\033[34m\"+(System.currentTimeMillis()-begin)+\"ms\033[0m\"+\"\033[93m]----------------------------------------------------------\033[0m\");"+
                        "            return ($r)execResults;" +
                        "}");
                ctClass.addMethod(executeQueryNew);
            }

            ctClass.toClass();
        } catch (NotFoundException e) {
            System.err.println("没有找到这个类");
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }

    }

    void dataSourceInfo(){

    }
}
