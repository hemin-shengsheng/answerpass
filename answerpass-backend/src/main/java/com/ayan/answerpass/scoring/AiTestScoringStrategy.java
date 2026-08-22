package com.ayan.answerpass.scoring;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ayan.answerpass.common.ErrorCode;
import com.ayan.answerpass.exception.BusinessException;
import com.ayan.answerpass.manager.AiManager;
import com.ayan.answerpass.model.dto.question.QuestionAnswerDTO;
import com.ayan.answerpass.model.dto.question.QuestionContentDTO;
import com.ayan.answerpass.model.entity.App;
import com.ayan.answerpass.model.entity.Question;
import com.ayan.answerpass.model.entity.UserAnswer;
import com.ayan.answerpass.model.vo.QuestionVO;
import com.ayan.answerpass.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 测评类应用评分策略
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
@Slf4j
public class AiTestScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedissonClient redissonClient;

    private static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";

    private final Cache<String, String> answerCacheMap =
            Caffeine.newBuilder().initialCapacity(1024)
                    .expireAfterAccess(5L, TimeUnit.MINUTES)
                    .build();

    private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
            "1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）\n" +
            "2. 严格按照下面的 json 格式输出评价名称和评价描述，注意描述内容中的换行请使用\\n表示，引号请使用\\\"转义\n" +
            "```\n" +
            "{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
            "```\n" +
            "3. 返回格式必须为 JSON 对象，不要包含任何其他内容";

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        String jsonStr = JSONUtil.toJsonStr(choices);
        String cacheKey = buildCacheKey(appId, jsonStr);
        String answerJson = answerCacheMap.getIfPresent(cacheKey);

        // 如果有缓存，直接返回
        if (StrUtil.isNotBlank(answerJson)) {
            UserAnswer userAnswer = JSONUtil.toBean(answerJson, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;
        }

        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + cacheKey);
        try {
            boolean res = lock.tryLock(3, 15, TimeUnit.SECONDS);
            if (!res) {
                return null;
            }

            // 1. 查询题目
            Question question = questionService.getOne(
                    Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
            );
            QuestionVO questionVO = QuestionVO.objToVo(question);
            List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

            // 2. 调用 AI 获取结果
            String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);
            String result = aiManager.doSyncStableRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage);

            // 打印原始返回，方便调试
            log.info("AI原始返回: {}", result);

            // 3. 提取并清理 JSON
            String json = extractAndCleanJson(result);
            log.info("清理后的JSON: {}", json);

            // 4. 验证 JSON 格式
            try {
                JSONUtil.parseObj(json);
            } catch (Exception e) {
                log.error("JSON解析失败，尝试转义特殊字符");
                // 最后的兜底：手动转义换行符
                json = json.replace("\n", "\\n").replace("\r", "\\r");
                json = json.replace("\t", "\\t");
                try {
                    JSONUtil.parseObj(json);
                } catch (Exception e2) {
                    log.error("JSON仍无法解析，内容: {}", json);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成结果格式错误，请重试");
                }
            }

            // 5. 缓存结果
            answerCacheMap.put(cacheKey, json);

            // 6. 构造返回值
            UserAnswer userAnswer = JSONUtil.toBean(json, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;

        } finally {
            if (lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 从AI返回内容中提取并清理JSON字符串
     */
    private String extractAndCleanJson(String result) {
        if (StrUtil.isBlank(result)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI返回内容为空");
        }

        // 去掉markdown代码块标记
        result = result.replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        // 找到JSON对象的起止位置
        int start = result.indexOf("{");
        int end = result.lastIndexOf("}");

        if (start == -1 || end == -1 || start >= end) {
            log.error("未找到有效JSON，原始内容: {}", result);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成结果格式错误");
        }

        return result.substring(start, end + 1).trim();
    }

    /**
     * AI 评分用户消息封装
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }

    /**
     * 构建缓存 key
     */
    private String buildCacheKey(Long appId, String choices) {
        return DigestUtil.md5Hex(appId + ":" + choices);
    }
}