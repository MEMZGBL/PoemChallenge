package com.manus.poemchallenge.manager;

import com.manus.poemchallenge.data.PoemData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 挑战管理器，负责生成填空题、验证答案和管理挑战状态。
 */
public class ChallengeManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    // 匹配中文标点符号，用于分割诗句
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[，。；！？]");
    private static final String BLANK = "______";

    private PoemData currentPoem;
    private String currentQuestion;
    private String currentAnswer;
    private final AtomicBoolean isChallengeActive = new AtomicBoolean(false);

    /**
     * 生成并广播一个新的古诗填空挑战。
     * @param poemData 古诗数据
     * @param challengeFormat 挑战消息格式 (包含 %question% 占位符)
     * @return 挑战是否成功生成
     */
    public boolean startNewChallenge(PoemData poemData, String challengeFormat) {
        if (poemData == null || isChallengeActive.get()) {
            return false;
        }

        this.currentPoem = poemData;
        
        // 1. 分割诗句，保留标点符号
        List<String> partsWithPunctuation = splitPoemWithPunctuation(poemData.getContent());
        
        if (partsWithPunctuation.isEmpty()) {
            Bukkit.getLogger().warning("Poem content cannot be split into parts: " + poemData.getContent());
            return false;
        }

        // 2. 随机选择一句作为答案
        Random random = new Random();
        int answerIndex = random.nextInt(partsWithPunctuation.size());
        
        StringBuilder finalQuestionBuilder = new StringBuilder();
        String answerPart = partsWithPunctuation.get(answerIndex);
        
        // 3. 生成问题和答案
        for (int i = 0; i < partsWithPunctuation.size(); i++) {
            String part = partsWithPunctuation.get(i);
            
            if (i == answerIndex) {
                // 答案部分，替换为填空
                finalQuestionBuilder.append(BLANK);
            } else {
                // 问题部分
                finalQuestionBuilder.append(part);
            }
            
            // 在句间添加空格
            if (i < partsWithPunctuation.size() - 1) {
                finalQuestionBuilder.append(" ");
            }
        }
        
        // 提取答案，去除标点符号
        String finalAnswer = answerPart.replaceAll("[，。；！？]", "").trim();
        
        this.currentQuestion = finalQuestionBuilder.toString().trim();
        this.currentAnswer = normalizeAnswer(finalAnswer);
        this.isChallengeActive.set(true);

        // 广播挑战消息
        String message = challengeFormat.replace("%question%", this.currentQuestion);
        Component component = MINI_MESSAGE.deserialize(message);
        Bukkit.getServer().sendMessage(component);

        Bukkit.getLogger().info("New Poem Challenge started: " + this.currentQuestion + " -> Answer: " + finalAnswer);
        return true;
    }
    
    /**
     * 分割诗句，保留标点符号
     */
    private List<String> splitPoemWithPunctuation(String content) {
        List<String> parts = new ArrayList<>();
        Matcher matcher = PUNCTUATION_PATTERN.matcher(content);
        int lastEnd = 0;
        while (matcher.find()) {
            String sentence = content.substring(lastEnd, matcher.end()).trim();
            if (!sentence.isEmpty()) {
                parts.add(sentence);
            }
            lastEnd = matcher.end();
        }
        String lastSentence = content.substring(lastEnd).trim();
        if (!lastSentence.isEmpty()) {
            parts.add(lastSentence);
        }
        return parts;
    }

    /**
     * 验证玩家的回答。
     * @param playerAnswer 玩家的回答
     * @return 是否正确
     */
    public boolean checkAnswer(String playerAnswer) {
        if (!isChallengeActive.get()) {
            return false;
        }
        String normalizedPlayerAnswer = normalizeAnswer(playerAnswer);
        return normalizedPlayerAnswer.equals(this.currentAnswer);
    }

    /**
     * 规范化答案：去除空格、标点符号，转换为小写。
     * @param answer 原始答案
     * @return 规范化后的答案
     */
    private String normalizeAnswer(String answer) {
        // 去除所有非中文字符，并转换为小写（虽然中文没有大小写，但为了统一处理）
        return answer.replaceAll("[^\\u4e00-\\u9fa5]", "").toLowerCase();
    }

    /**
     * 结束当前挑战。
     */
    public void endChallenge() {
        this.currentPoem = null;
        this.currentQuestion = null;
        this.currentAnswer = null;
        this.isChallengeActive.set(false);
    }

    public boolean isChallengeActive() {
        return isChallengeActive.get();
    }

    public PoemData getCurrentPoem() {
        return currentPoem;
    }
}
