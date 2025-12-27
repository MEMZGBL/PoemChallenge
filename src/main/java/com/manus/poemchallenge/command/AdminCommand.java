package com.manus.poemchallenge.command;

import com.manus.poemchallenge.PoemChallengePlugin;
import com.manus.poemchallenge.scheduler.ChallengeScheduler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * 处理管理员指令 /poem admin <subcommand>
 */
public class AdminCommand implements CommandExecutor {

    private final PoemChallengePlugin plugin;
    private final ChallengeScheduler scheduler;
    private final MiniMessage miniMessage = PoemChallengePlugin.MINI_MESSAGE;

    public AdminCommand(PoemChallengePlugin plugin, ChallengeScheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 权限检查
        if (!sender.hasPermission("poem.admin")) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.admin-no-permission", "<red>你没有权限执行此命令。</red>")));
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>用法: /poem admin <trigger></red>"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>用法: /poem admin <trigger></red>"));
            return true;
        }

        if (args[1].equalsIgnoreCase("trigger")) {
            // 手动触发挑战
            scheduler.run();
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("messages.admin-trigger-success", "<green>管理员已手动触发古诗挑战。</green>")));
            return true;
        }

        sender.sendMessage(miniMessage.deserialize("<red>未知子命令: " + args[1] + "</red>"));
        return true;
    }
}
