package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public ReloadCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("common.no-permission"));
         return true;
      } else {
         sender.sendMessage(this.plugin.lang().get("reload.reloading"));

         try {
            this.plugin.reload();
            sender.sendMessage(this.plugin.lang().get("reload.success"));
         } catch (Exception e) {
            sender.sendMessage(this.plugin.lang().get("reload.error", "error", e.getMessage()));
         }

         return true;
      }
   }
}
