package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PotionCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public PotionCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("common.no-permission"));
         return true;
      } else if (args.length != 1) {
         sender.sendMessage(this.plugin.lang().get("potion.usage"));
         return true;
      } else {
         Player target = Bukkit.getPlayerExact(args[0]);
         if (target == null) {
            sender.sendMessage(this.plugin.lang().get("common.player-not-online", "player", args[0]));
            return true;
         } else {
            this.plugin.potion().give(target);
            sender.sendMessage(this.plugin.lang().get("potion.given", "player", target.getName()));
            target.sendMessage(this.plugin.lang().get("potion.notify"));
            return true;
         }
      }
   }
}
