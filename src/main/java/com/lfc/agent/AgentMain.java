package com.lfc.agent;

import com.lfc.agent.jdbc.MySqlDriverAgent;

import java.lang.instrument.*;


/**
 * @Classname AgentMain
 * @Description 打印sql
 * @Author yeshen.lan
 * @CreateTime 2020-11-04 09:52
 * @Version 1.0
 **/
public class AgentMain {

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new MySqlDriverAgent());
    }
}
