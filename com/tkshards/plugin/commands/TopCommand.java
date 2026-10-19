package com.tkshards.plugin.commands;

import com.tkshards.plugin.TKShardsPlugin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCommand implements CommandExecutor {
   private final TKShardsPlugin plugin;

   public TopCommand(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length != 1) {
         sender.sendMessage(this.plugin.lang().get("top.usage"));
         return true;
      } else {
         String type = args[0].toLowerCase();
         List<UUID> players = new ArrayList(this.plugin.currency().getAllTrackedPlayers());
         switch (type) {
            case "shard" -> this.show(sender, "Shard", players, (uuid) -> (double)this.plugin.currency().getBalance(uuid), (value) -> String.valueOf(Math.round(value)));
            case "kill" -> this.show(sender, "Kill", players, (uuid) -> (double)this.plugin.currency().getKills(uuid), (value) -> String.valueOf(Math.round(value)));
            case "die" -> this.show(sender, "Death", players, (uuid) -> (double)this.plugin.currency().getDeaths(uuid), (value) -> String.valueOf(Math.round(value)));
            case "sell" -> this.show(sender, "Sell", players, (uuid) -> (double)this.plugin.currency().getSellTotal(uuid), (value) -> String.valueOf(Math.round(value)));
            case "money" -> this.showMoney(sender);
            default -> sender.sendMessage(this.plugin.lang().get("top.usage"));
         }

         return true;
      }
   }

   private void show(CommandSender sender, String label, List<UUID> players, Function<UUID, Double> valueGetter, Function<Double, String> formatter) {
      Stream var10000 = players.stream();
      Objects.requireNonNull(valueGetter);
      List<UUID> sorted = var10000.sorted(Comparator.comparingDouble(valueGetter::apply).reversed()).limit(10L).toList();
      sender.sendMessage(this.plugin.lang().get("top.header", "label", label));
      int rank = 1;

      for(UUID uuid : sorted) {
         double value = (Double)valueGetter.apply(uuid);
         sender.sendMessage(this.plugin.lang().get("top.entry", "rank", String.valueOf(rank), "player", this.plugin.currency().getName(uuid), "value", (String)formatter.apply(value)));
         ++rank;
      }

      if (sorted.isEmpty()) {
         sender.sendMessage(this.plugin.lang().get("top.empty"));
      }

   }

   private void showMoney(CommandSender sender) {
      if (!this.plugin.vault().isAvailable()) {
         sender.sendMessage(this.plugin.lang().get("top.vault-unavailable"));
      } else {
         List<UUID> players = new ArrayList(this.plugin.currency().getAllTrackedPlayers());

         for(Player online : Bukkit.getOnlinePlayers()) {
            if (!players.contains(online.getUniqueId())) {
               players.add(online.getUniqueId());
            }
         }

         List<Object[]> entries = new ArrayList();

         for(UUID uuid : players) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            entries.add(new Object[]{uuid, this.plugin.vault().getBalance(offline)});
         }

         entries.sort((a, b) -> Double.compare((Double)b[1], (Double)a[1]));
         sender.sendMessage(this.plugin.lang().get("top.header", "label", "Tiền $"));
         int rank = 1;

         for(Object[] entry : entries) {
            if (rank > 10) {
               break;
            }

            UUID uuid = (UUID)entry[0];
            double value = (Double)entry[1];
            String name = this.plugin.currency().getName(uuid);
            if ("?".equals(name)) {
               name = Bukkit.getOfflinePlayer(uuid).getName();
            }

            sender.sendMessage(this.plugin.lang().get("top.entry", "rank", String.valueOf(rank), "player", String.valueOf(name), "value", String.format("%.2f", value)));
            ++rank;
         }

         if (entries.isEmpty()) {
            sender.sendMessage(this.plugin.lang().get("top.empty"));
         }

      }
   }
}
