package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public PayCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player player) {
         if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            this.plugin.currency().togglePayments(player.getUniqueId());
            boolean accepting = this.plugin.currency().isAcceptingPayments(player.getUniqueId());
            player.sendMessage(this.plugin.lang().get(accepting ? "pay.toggle-on" : "pay.toggle-off"));
            return true;
         } else if (args.length != 2) {
            player.sendMessage(this.plugin.lang().get("pay.usage", "label", label));
            return true;
         } else {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
               player.sendMessage(this.plugin.lang().get("common.player-not-online", "player", args[0]));
               return true;
            } else if (target.equals(player)) {
               player.sendMessage(this.plugin.lang().get("pay.cant-pay-self"));
               return true;
            } else {
               long amount;
               try {
                  amount = Long.parseLong(args[1]);
               } catch (NumberFormatException var10) {
                  player.sendMessage(this.plugin.lang().get("common.invalid-amount", "amount", args[1]));
                  return true;
               }

               if (amount <= 0L) {
                  player.sendMessage(this.plugin.lang().get("common.amount-positive"));
                  return true;
               } else if (!this.plugin.currency().isAcceptingPayments(target.getUniqueId())) {
                  player.sendMessage(this.plugin.lang().get("pay.not-accepting", "player", target.getName()));
                  return true;
               } else if (!this.plugin.currency().removeBalance(player.getUniqueId(), amount)) {
                  player.sendMessage(this.plugin.lang().get("pay.not-enough", "balance", String.valueOf(this.plugin.currency().getBalance(player.getUniqueId()))));
                  return true;
               } else {
                  this.plugin.currency().addBalance(target.getUniqueId(), amount);
                  player.sendMessage(this.plugin.lang().get("pay.sent", "amount", String.valueOf(amount), "player", target.getName(), "balance", String.valueOf(this.plugin.currency().getBalance(player.getUniqueId()))));
                  target.sendMessage(this.plugin.lang().get("pay.received", "amount", String.valueOf(amount), "player", player.getName(), "balance", String.valueOf(this.plugin.currency().getBalance(target.getUniqueId()))));
                  return true;
               }
            }
         }
      } else {
         sender.sendMessage(this.plugin.lang().get("common.players-only"));
         return true;
      }
   }
}
