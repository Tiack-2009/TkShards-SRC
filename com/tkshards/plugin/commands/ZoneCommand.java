package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.zones.Zone;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ZoneCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public ZoneCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("tkshards.admin")) {
         sender.sendMessage(this.plugin.lang().get("common.no-permission"));
         return true;
      } else if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.lang().get("common.players-only"));
         return true;
      } else {
         Player player = (Player)sender;
         if (args.length == 0) {
            player.sendMessage(this.plugin.lang().get("zones.usage"));
            return true;
         } else {
            switch (args[0].toLowerCase()) {
               case "wand":
                  player.getInventory().addItem(new ItemStack[]{this.plugin.zones().createWand()});
                  player.sendMessage(this.plugin.lang().get("zones.wand-given"));
                  break;
               case "create":
                  if (args.length != 2) {
                     player.sendMessage(this.plugin.lang().get("zones.usage"));
                     return true;
                  }

                  if (!this.plugin.zones().hasBothPoints(player.getUniqueId())) {
                     player.sendMessage(this.plugin.lang().get("zones.need-both-points"));
                     return true;
                  }

                  if (this.plugin.zones().zoneExists(args[1])) {
                     player.sendMessage(this.plugin.lang().get("zones.already-exists", "name", args[1]));
                     return true;
                  }

                  this.plugin.zones().createZone(player.getUniqueId(), args[1]);
                  player.sendMessage(this.plugin.lang().get("zones.created", "name", args[1]));
                  break;
               case "remove":
                  if (args.length != 2) {
                     player.sendMessage(this.plugin.lang().get("zones.usage"));
                     return true;
                  }

                  if (!this.plugin.zones().removeZone(args[1])) {
                     player.sendMessage(this.plugin.lang().get("zones.not-found", "name", args[1]));
                     return true;
                  }

                  player.sendMessage(this.plugin.lang().get("zones.removed", "name", args[1]));
                  break;
               case "list":
                  if (this.plugin.zones().getZones().isEmpty()) {
                     player.sendMessage(this.plugin.lang().get("zones.list-empty"));
                     return true;
                  }

                  player.sendMessage(this.plugin.lang().get("zones.list-header"));
                  this.plugin.zones().getZones().values().forEach((zone) -> player.sendMessage(this.plugin.lang().get("zones.list-entry", "name", zone.getName(), "world", zone.getWorld())));
                  break;
               case "check":
                  boolean everywhere = player.hasPermission("tkshards.everywhere");
                  boolean physicallyInZone = this.plugin.zones().getZones().values().stream().anyMatch((zone) -> zone.contains(player.getLocation()));
                  String zoneName = (String)this.plugin.zones().getZones().values().stream().filter((zone) -> zone.contains(player.getLocation())).map(Zone::getName).findFirst().orElse("(khong co)");
                  player.sendMessage(this.plugin.lang().get("zones.check-everywhere", "value", everywhere ? "&2CO" : "&cKHONG"));
                  player.sendMessage(this.plugin.lang().get("zones.check-physical", "value", physicallyInZone ? "&2CO" : "&cKHONG", "zone", zoneName));
                  player.sendMessage(this.plugin.lang().get("zones.check-result", "value", !everywhere && !physicallyInZone ? "&cKHONG DUOC TINH" : "&2DANG DUOC TINH"));
                  if (everywhere && !physicallyInZone) {
                     player.sendMessage(this.plugin.lang().get("zones.check-everywhere-hint"));
                  }
                  break;
               default:
                  player.sendMessage(this.plugin.lang().get("zones.usage"));
            }

            return true;
         }
      }
   }
}
