package com.ayan.answerpass.manager;

import com.ayan.answerpass.common.ErrorCode;
import com.ayan.answerpass.exception.BusinessException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public String doStreamRequest(String systemMessage, String userMessage, Float temperature) {
        return doRequest(systemMessage, userMessage, temperature);
    }

    private String doRequest(String systemMessage, String userMessage, Float temperature) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("deepseek-chat")
                .temperature(temperature.doubleValue())
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
}