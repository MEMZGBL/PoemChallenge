package com.manus.poemchallenge;

import com.manus.poemchallenge.api.PoemAPIClient;
import com.manus.poemchallenge.command.AdminCommand;
import com.manus.poemchallenge.command.AnswerCommand;
import com.manus.poemchallenge.manager.ChallengeManager;
import com.manus.poemchallenge.scheduler.ChallengeScheduler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class PoemChallengePlugin extends JavaPlugin {

    // MiniMessage 实例，用于全局消息格式化
    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private PoemAPIClient apiClient;
    private ChallengeManager challengeManager;
    private ChallengeScheduler challengeScheduler;

    @Override
    public void onEnable() {
        // 1. 保存默认配置
        saveDefaultConfig();

        // 2. 初始化组件
        String apiUrl = getConfig().getString("api-url", "https://v2.jinrishici.com/one.json");
        this.apiClient = new PoemAPIClient(apiUrl);
        this.challengeManager = new ChallengeManager();

        // 3. 注册指令
        // 确保指令在 plugin.yml 中已定义
        Objects.requireNonNull(getCommand("pmans")).setExecutor(new AnswerCommand(this, challengeManager));
        Objects.requireNonNull(getCommand("poem")).setExecutor(new AdminCommand(this, getChallengeScheduler()));

        // 4. 启动定时任务
        long intervalSeconds = getConfig().getLong("challenge-interval", 120);
        long intervalTicks = intervalSeconds * 20L; // 秒转 Tick (20 ticks = 1 second)
        
        // 延迟 10 秒后开始第一次任务，然后每隔 intervalTicks 执行一次
        getChallengeScheduler().runTaskTimerAsynchronously(this, 200L, intervalTicks);

        getLogger().info("PoemChallenge 插件已启用！定时任务已启动，每 " + intervalSeconds + " 秒触发一次挑战。");
    }

    @Override
    public void onDisable() {
        // 停止定时任务
        if (challengeScheduler != null) {
            challengeScheduler.cancel();
        }
        // 关闭 HttpClient
        if (apiClient != null) {
            apiClient.close();
        }
        getLogger().info("PoemChallenge 插件已禁用！");
    }

    /**
     * 延迟初始化 ChallengeScheduler，避免循环依赖
     */
    private ChallengeScheduler getChallengeScheduler() {
        if (challengeScheduler == null) {
            String prefix = getConfig().getString("messages.prefix", "<gold>【古诗挑战】</gold> ");
            String format = getConfig().getString("messages.challenge-format", "<white>%question%</white> <gray>请使用 /pmans <答案> 回答</gray>");
            String challengeFormat = prefix + format;
            this.challengeScheduler = new ChallengeScheduler(this, apiClient, challengeManager, challengeFormat);
        }
        return challengeScheduler;
    }
}
