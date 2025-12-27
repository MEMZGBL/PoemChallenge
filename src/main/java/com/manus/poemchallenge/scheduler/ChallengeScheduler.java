package com.manus.poemchallenge.scheduler;

import com.manus.poemchallenge.PoemChallengePlugin;
import com.manus.poemchallenge.api.PoemAPIClient;
import com.manus.poemchallenge.data.PoemData;
import com.manus.poemchallenge.manager.ChallengeManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

/**
 * 定时任务，用于每隔一段时间触发一次古诗挑战。
 */
public class ChallengeScheduler extends BukkitRunnable {

    private final PoemChallengePlugin plugin;
    private final PoemAPIClient apiClient;
    private final ChallengeManager challengeManager;
    private final String challengeFormat;

    public ChallengeScheduler(PoemChallengePlugin plugin, PoemAPIClient apiClient, ChallengeManager challengeManager, String challengeFormat) {
        this.plugin = plugin;
        this.apiClient = apiClient;
        this.challengeManager = challengeManager;
        this.challengeFormat = challengeFormat;
    }

    @Override
    public void run() {
        // 1. 结束上一个挑战（如果存在）
        if (challengeManager.isChallengeActive()) {
            PoemData poem = challengeManager.getCurrentPoem();
            if (poem != null) {
                // 广播正确答案
                String answerMessage = String.format("<yellow>挑战结束！</yellow> 正确答案是：<green>%s</green> - 《%s》 (<gray>%s</gray>)",
                        poem.getContent(), poem.getTitle(), poem.getAuthor());
                Bukkit.getServer().sendMessage(PoemChallengePlugin.MINI_MESSAGE.deserialize(answerMessage));
            }
            challengeManager.endChallenge();
        }

        // 2. 异步获取新的古诗
        apiClient.fetchRandomPoem().thenAccept(poemData -> {
            if (poemData != null) {
                // 3. 在主线程中启动新的挑战
                Bukkit.getScheduler().runTask(plugin, () -> {
                    boolean success = challengeManager.startNewChallenge(poemData, challengeFormat);
                    if (!success) {
                        Bukkit.getLogger().log(Level.WARNING, "Failed to start new poem challenge. Retrying...");
                        // 如果失败，尝试再次获取诗句（避免因为 API 返回不完整诗句而卡住）
                        // 注意：这里只是日志提示，实际重试逻辑应该更复杂，但为了简化，我们只在下次定时任务时重试
                    }
                });
            } else {
                Bukkit.getLogger().log(Level.WARNING, "Failed to fetch poem data from API. Skipping this challenge.");
            }
        });
    }
}
