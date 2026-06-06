package com.ayan.answerpass.manager;

import com.ayan.answerpass.common.ErrorCode;
import com.ayan.answerpass.exception.BusinessException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.openai.core.http.StreamResponse;
import com.openai.models.chat.completions.ChatCompletionChunk;

@Component
public class AiManager {

    private static final float STABLE_TEMPERATURE = 0.05f;
    private static final float UNSTABLE_TEMPERATURE = 0.99f;

    private final OpenAIClient openAIClient;

    public AiManager(@Value("${deepseek.api.key}") String apiKey) {
        this.openAIClient = OpenAIOkHttpClient.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(apiKey)
                .build();
    }

    public String doSyncUnstableRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, UNSTABLE_TEMPERATURE);
    }

    public String doSyncStableRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, STABLE_TEMPERATURE);
    }

    public String doSyncRequest(String systemMessage, String userMessage, Float temperature) {
        return doRequest(systemMessage, userMessage, temperature);
    }

    /**
     * 同步调用，用create一次返回完整结果
     * @param systemMessage
     * @param userMessage
     * @param temperature
     * @return
     */
    private String doRequest(String systemMessage, String userMessage, Float temperature) {
        double temp=(temperature!=null)?temperature.doubleValue():STABLE_TEMPERATURE;
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("deepseek-chat")
                .temperature(temp)
                .addSystemMessage(systemMessage)
                .addUserMessage(userMessage)
                .build();

        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content().orElse("");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
    /**
     * 流式请求 DeepSeek API
     *用 createStreaming()，返回一个流，数据逐块到达
     * 返回值 StreamResponse<ChatCompletionChunk>：
     * - 可以遍历，每次迭代拿到一个 chunk（AI 刚生成的一小段文本）
     * - Controller 拿到这个流后，自己决定怎么处理（比如拼成题目、推 SSE）
     * @param systemMessage 系统提示词
     * @param userMessage   用户消息
     * @param temperature   温度参数（null 则用默认值 0.05）
     * @return 流式响应对象
     */
    public StreamResponse<ChatCompletionChunk> doStreamRequest(
            String systemMessage,
            String userMessage,
            Float temperature) {

        // 处理温度默认值，和 doRequest 逻辑一样
        double temp = (temperature != null) ? temperature.doubleValue() : STABLE_TEMPERATURE;

        // 构建请求参数，和 doRequest 完全一样
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("deepseek-chat")
                .temperature(temp)
                .addSystemMessage(systemMessage)
                .addUserMessage(userMessage)
                .build();

        try {
            // 关键区别：这里调 createStreaming() 而不是 create()
            // createStreaming() 返回 StreamResponse，数据是按 chunk 流式过来的
            return openAIClient.chat().completions().createStreaming(params);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}