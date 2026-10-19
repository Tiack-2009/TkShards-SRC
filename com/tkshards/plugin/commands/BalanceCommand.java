package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.NumberUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public BalanceCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         if (sender instanceof Player) {
            Player player = (Player)sender;
            long balance = this.plugin.currency().getBalance(player.getUniqueId());
            sender.sendMessage(this.plugin.lang().get("balance.self", "balance", this.formatted(balance)));
            return true;
         } else {
            sender.sendMessage(this.plugin.lang().get("balance.console-specify", "label", label));
            return true;
         }
      } else if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("balance.no-permission-others"));
         return true;
      } else {
         OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
         long balance = this.plugin.currency().getBalance(target.getUniqueId());
         sender.sendMessage(this.plugin.lang().get("balance.other", "player", args[0], "balance", this.formatted(balance)));
         return true;
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
