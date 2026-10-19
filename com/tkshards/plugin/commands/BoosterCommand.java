package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoosterCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public BoosterCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("common.no-permission"));
         return true;
      } else if (args.length != 2) {
         sender.sendMessage(this.plugin.lang().get("booster.usage"));
         sender.sendMessage(this.plugin.lang().get("booster.usage-durations"));
         return true;
      } else {
         Player target = Bukkit.getPlayerExact(args[0]);
         if (target == null) {
            sender.sendMessage(this.plugin.lang().get("common.player-not-online", "player", args[0]));
            return true;
         } else {
            Long seconds = TimeUtil.parseDurationSeconds(args[1]);
            if (seconds == null) {
               sender.sendMessage(this.plugin.lang().get("booster.invalid-duration", "duration", args[1]));
               return true;
            } else {
               this.plugin.booster().activate(target.getUniqueId(), seconds);
               String formatted = TimeUtil.formatDuration(seconds);
               sender.sendMessage(this.plugin.lang().get("booster.given", "duration", formatted, "player", target.getName()));
               target.sendMessage(this.plugin.lang().get("booster.received", "duration", formatted));
               return true;
            }
         }
      }
   }
}
