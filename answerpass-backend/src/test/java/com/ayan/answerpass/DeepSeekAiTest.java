package com.ayan.answerpass;

import com.ayan.answerpass.manager.AiManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class DeepSeekAiTest {

    @Resource
    private AiManager aiManager;

    @Test
    public void testDeepSeek() {
        String systemMessage = "你是一个有用的助手";
        String userMessage = "请用一句话介绍你自己";

        String result = aiManager.doSyncRequest(systemMessage, userMessage, 0.7f);

        System.out.println("DeepSeek 返回结果：");
        System.out.println(result);
    }
}