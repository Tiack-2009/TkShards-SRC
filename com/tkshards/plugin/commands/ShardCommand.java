package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.AmountParser;
import com.tkshards.plugin.util.NumberUtil;
import com.tkshards.plugin.util.TimeUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShardCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public ShardCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         return this.handleBalanceSelf(sender, label);
      } else if (args[0].equalsIgnoreCase("pay")) {
         this.handlePay(sender, args);
         return true;
      } else if (args[0].equalsIgnoreCase("time")) {
         this.handleTime(sender, args);
         return true;
      } else {
         return this.handleBalanceOther(sender, args[0]);
      }
   }

   private boolean handleBalanceSelf(CommandSender sender, String label) {
      if (sender instanceof Player player) {
         long balance = this.plugin.currency().getBalance(player.getUniqueId());
         sender.sendMessage(this.plugin.lang().get("balance.self", "balance", this.formatted(balance)));
         return true;
      } else {
         sender.sendMessage(this.plugin.lang().get("balance.console-specify", "label", label));
         return true;
      }
   }

   private boolean handleBalanceOther(CommandSender sender, String name) {
      if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("balance.no-permission-others"));
         return true;
      } else {
         OfflinePlayer target = Bukkit.getOfflinePlayer(name);
         long balance = this.plugin.currency().getBalance(target.getUniqueId());
         sender.sendMessage(this.plugin.lang().get("balance.other", "player", name, "balance", this.formatted(balance)));
         return true;
      }
   }

   private void handlePay(CommandSender sender, String[] args) {
      if (sender instanceof Player player) {
         if (args.length == 2 && args[1].equalsIgnoreCase("toggle")) {
            this.plugin.currency().togglePayments(player.getUniqueId());
            boolean accepting = this.plugin.currency().isAcceptingPayments(player.getUniqueId());
            player.sendMessage(this.plugin.lang().get(accepting ? "pay.toggle-on" : "pay.toggle-off"));
         } else if (args.length != 3) {
            player.sendMessage(this.plugin.lang().get("pay.usage", "label", "shard pay"));
         } else {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
               player.sendMessage(this.plugin.lang().get("common.player-not-online", "player", args[1]));
            } else if (target.equals(player)) {
               player.sendMessage(this.plugin.lang().get("pay.cant-pay-self"));
            } else {
               Long amount = AmountParser.parse(args[2]);
               if (amount == null) {
                  player.sendMessage(this.plugin.lang().get("common.invalid-amount", "amount", args[2]));
               } else if (amount <= 0L) {
                  player.sendMessage(this.plugin.lang().get("common.amount-positive"));
               } else if (!this.plugin.currency().isAcceptingPayments(target.getUniqueId())) {
                  player.sendMessage(this.plugin.lang().get("pay.not-accepting", "player", target.getName()));
               } else if (!this.plugin.currency().removeBalance(player.getUniqueId(), amount)) {
                  player.sendMessage(this.plugin.lang().get("pay.not-enough", "balance", String.valueOf(this.plugin.currency().getBalance(player.getUniqueId()))));
               } else {
                  this.plugin.currency().addBalance(target.getUniqueId(), amount);
                  player.sendMessage(this.plugin.lang().get("pay.sent", "amount", String.valueOf(amount), "player", target.getName(), "balance", String.valueOf(this.plugin.currency().getBalance(player.getUniqueId()))));
                  target.sendMessage(this.plugin.lang().get("pay.received", "amount", String.valueOf(amount), "player", player.getName(), "balance", String.valueOf(this.plugin.currency().getBalance(target.getUniqueId()))));
               }
            }
         }
      } else {
         sender.sendMessage(this.plugin.lang().get("common.players-only"));
      }
   }

   private void handleTime(CommandSender sender, String[] args) {
      if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("common.no-permission"));
      } else if (args.length != 3) {
         sender.sendMessage(this.plugin.lang().get("shardtime.usage"));
      } else {
         OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
         Long seconds = TimeUtil.parseDurationSeconds(args[2]);
         if (seconds == null) {
            sender.sendMessage(this.plugin.lang().get("shardtime.invalid-duration", "duration", args[2]));
         } else {
            this.plugin.currency().setCustomAfkInterval(target.getUniqueId(), seconds);
            String formatted = TimeUtil.formatDuration(seconds);
            sender.sendMessage(this.plugin.lang().get("shardtime.set", "player", String.valueOf(target.getName()), "duration", formatted));
            Player online = target.getPlayer();
            if (online != null) {
               online.sendMessage(this.plugin.lang().get("shardtime.notify", "duration", formatted));
            }

         }
      }
   }

   private String formatted(long balance) {
      boolean enabled = this.plugin.settings().getBoolean("number-format.enabled", true);
      int decimals = this.plugin.settings().getInt("number-format.decimals", 1);
      List<String> suffixes = this.plugin.settings().getStringList("number-format.suffixes");
      if (suffixes.isEmpty()) {
         suffixes = List.of("", "K", "M", "B", "T", "Q");
      }

      return NumberUtil.format(balance, enabled, decimals, suffixes);
   }
}
