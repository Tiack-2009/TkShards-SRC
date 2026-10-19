package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ManagerCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public ManagerCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("common.no-permission"));
         return true;
      } else if (args.length != 3) {
         sender.sendMessage(this.plugin.lang().get("manager.help-title"));
         sender.sendMessage(this.plugin.lang().get("manager.help-add", "label", label));
         sender.sendMessage(this.plugin.lang().get("manager.help-remove", "label", label));
         sender.sendMessage(this.plugin.lang().get("manager.help-set", "label", label));
         return true;
      } else {
         String action = args[0].toLowerCase();
         OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

         long amount;
         try {
            amount = Long.parseLong(args[2]);
         } catch (NumberFormatException var11) {
            sender.sendMessage(this.plugin.lang().get("common.invalid-amount", "amount", args[2]));
            return true;
         }

         if (amount < 0L) {
            sender.sendMessage(this.plugin.lang().get("common.amount-positive"));
            return true;
         } else {
            switch (action) {
               case "add":
                  this.plugin.currency().addBalance(target.getUniqueId(), amount);
                  sender.sendMessage(this.plugin.lang().get("manager.added", "amount", String.valueOf(amount), "player", String.valueOf(target.getName()), "total", String.valueOf(this.plugin.currency().getBalance(target.getUniqueId()))));
                  break;
               case "remove":
                  this.plugin.currency().removeBalance(target.getUniqueId(), amount);
                  sender.sendMessage(this.plugin.lang().get("manager.removed", "amount", String.valueOf(amount), "player", String.valueOf(target.getName()), "total", String.valueOf(this.plugin.currency().getBalance(target.getUniqueId()))));
                  break;
               case "set":
                  this.plugin.currency().setBalance(target.getUniqueId(), amount);
                  sender.sendMessage(this.plugin.lang().get("manager.set", "player", String.valueOf(target.getName()), "amount", String.valueOf(amount)));
                  break;
               default:
                  sender.sendMessage(this.plugin.lang().get("manager.help-title"));
            }

            return true;
         }
      }
   }
}
