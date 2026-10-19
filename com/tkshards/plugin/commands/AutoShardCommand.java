package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoShardCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public AutoShardCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length != 2 || !args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off")) {
         if (sender instanceof Player) {
            Player player = (Player)sender;
            boolean enabled = this.plugin.currency().toggleAutoShard(player.getUniqueId());
            player.sendMessage(this.plugin.lang().get(enabled ? "autoshard.toggle-on" : "autoshard.toggle-off"));
            return true;
         } else {
            sender.sendMessage(this.plugin.lang().get("common.players-only"));
            return true;
         }
      } else if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("common.no-permission"));
         return true;
      } else {
         OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
         boolean grant = args[1].equalsIgnoreCase("on");
         this.plugin.currency().setAdminGranted(target.getUniqueId(), grant);
         sender.sendMessage(this.plugin.lang().get(grant ? "autoshard.given-on" : "autoshard.given-off", "player", String.valueOf(target.getName())));
         if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(this.plugin.lang().get(grant ? "autoshard.received-on" : "autoshard.received-off"));
         }

         return true;
      }
   }
}
