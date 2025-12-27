package com.manus.poemchallenge.command;

import com.manus.poemchallenge.PoemChallengePlugin;
import com.manus.poemchallenge.manager.ChallengeManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * 处理玩家回答指令 /pmans <answer>
 */
public class AnswerCommand implements CommandExecutor {

    private final PoemChallengePlugin plugin;
    private final ChallengeManager challengeManager;
    private final MiniMessage miniMessage = PoemChallengePlugin.MINI_MESSAGE;
    private final Random random = new Random();

    public AnswerCommand(PoemChallengePlugin plugin, ChallengeManager challengeManager) {
        this.plugin = plugin;
        this.challengeManager = challengeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { // 使用 Java 16+ 的模式匹配 for instanceof
            sender.sendMessage(miniMessage.deserialize("<red>只有玩家才能使用此命令。</red>"));
            return true;
        }

        // 权限检查
        if (!player.hasPermission("poem.ans")) {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.admin-no-permission", "<red>你没有权限执行此命令。</red>")));
            return true;
        }

        if (!challengeManager.isChallengeActive()) {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.no-active-challenge", "<red>当前没有正在进行的古诗挑战。</red>")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(miniMessage.deserialize("<red>用法: /pmans <答案></red>"));
            return true;
        }

        // 拼接玩家的回答
        String playerAnswer = String.join(" ", args);

        if (challengeManager.checkAnswer(playerAnswer)) {
            // 回答正确
            int min = plugin.getConfig().getInt("reward.min", 10);
            int max = plugin.getConfig().getInt("reward.max", 1000);
            int amount = random.nextInt(max - min + 1) + min;

            // 执行奖励命令
            String rewardCommand = plugin.getConfig().getString("reward.command", "money give %player% %amount%")
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount));

            // 确保在主线程执行命令
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCommand);
            });

            // 广播正确消息
            String correctMessage = plugin.getConfig().getString("messages.correct-answer", "<green>恭喜你，%player%！</green> 回答正确！奖励 <yellow>%amount%</yellow> 已发放。")
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount));
            Bukkit.getServer().sendMessage(miniMessage.deserialize(correctMessage));

            // 广播正确答案和诗句信息
            String poemContent = challengeManager.getCurrentPoem().getContent();
            String poemTitle = challengeManager.getCurrentPoem().getTitle();
            String poemAuthor = challengeManager.getCurrentPoem().getAuthor();
            String answerMessage = String.format("<yellow>挑战结束！</yellow> 正确答案是：<green>%s</green> - 《%s》 (<gray>%s</gray>)",
                    poemContent, poemTitle, poemAuthor);
            Bukkit.getServer().sendMessage(miniMessage.deserialize(answerMessage));

            // 结束挑战
            challengeManager.endChallenge();
        } else {
            // 回答错误
            String incorrectMessage = plugin.getConfig().getString("messages.incorrect-answer", "<red>很遗憾，%player%，</red> 回答错误。别灰心，下次一定！")
                    .replace("%player%", player.getName());
            player.sendMessage(miniMessage.deserialize(incorrectMessage));
        }

        return true;
    }
}
