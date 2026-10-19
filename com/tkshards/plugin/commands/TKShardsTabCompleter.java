package com.tkshards.plugin.commands;

import com.tkshards.plugin.util.TabCompleteUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TKShardsTabCompleter implements TabCompleter {
   private static final List<String> DURATION_EXAMPLES = List.of("30s", "10m", "1h", "1d", "7d");
   private static final List<String> AMOUNT_EXAMPLES = List.of("10", "100", "1k", "10k", "1m");
   private static final List<String> ON_OFF = List.of("on", "off");
   private static final List<String> TOP_TYPES = List.of("shard", "money", "kill", "die", "sell");

   public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
      List var10000;
      switch (command.getName().toLowerCase()) {
         case "shard" -> var10000 = this.completeShard(args);
         case "tkshardspay" -> var10000 = this.completeShardsPay(args);
         case "tkshardsmanager" -> var10000 = this.completeManager(args);
         case "tkbooster" -> var10000 = this.completeBooster(args);
         case "tkpotion" -> var10000 = this.completePotion(args);
         case "tkautoshard" -> var10000 = this.completeAutoShard(args);
         case "tkzones" -> var10000 = this.completeZones(args);
         case "top" -> var10000 = this.completeTop(args);
         default -> var10000 = List.of();
      }

      return var10000;
   }

   private List<String> completeShard(String[] args) {
      if (args.length == 1) {
         List<String> options = new ArrayList(List.of("pay", "time"));
         options.addAll(this.onlineNamesPlaceholder());
         return TabCompleteUtil.filter(options, args[0]);
      } else {
         if (args.length >= 2 && args[0].equalsIgnoreCase("pay")) {
            if (args.length == 2) {
               List<String> options = new ArrayList(List.of("toggle"));
               options.addAll(this.onlineNamesPlaceholder());
               return TabCompleteUtil.filter(options, args[1]);
            }

            if (args.length == 3 && !args[1].equalsIgnoreCase("toggle")) {
               return TabCompleteUtil.filter(AMOUNT_EXAMPLES, args[2]);
            }
         }

         if (args.length >= 2 && args[0].equalsIgnoreCase("time")) {
            if (args.length == 2) {
               return TabCompleteUtil.onlinePlayerNames(args[1]);
            }

            if (args.length == 3) {
               return TabCompleteUtil.filter(DURATION_EXAMPLES, args[2]);
            }
         }

         return List.of();
      }
   }

   private List<String> completeShardsPay(String[] args) {
      if (args.length == 1) {
         List<String> options = new ArrayList(List.of("toggle"));
         options.addAll(this.onlineNamesPlaceholder());
         return TabCompleteUtil.filter(options, args[0]);
      } else {
         return args.length == 2 && !args[0].equalsIgnoreCase("toggle") ? TabCompleteUtil.filter(AMOUNT_EXAMPLES, args[1]) : List.of();
      }
   }

   private List<String> completeManager(String[] args) {
      if (args.length == 1) {
         return TabCompleteUtil.filter(List.of("add", "remove", "set"), args[0]);
      } else if (args.length == 2) {
         return TabCompleteUtil.onlinePlayerNames(args[1]);
      } else {
         return args.length == 3 ? TabCompleteUtil.filter(AMOUNT_EXAMPLES, args[2]) : List.of();
      }
   }

   private List<String> completeBooster(String[] args) {
      if (args.length == 1) {
         return TabCompleteUtil.onlinePlayerNames(args[0]);
      } else {
         return args.length == 2 ? TabCompleteUtil.filter(DURATION_EXAMPLES, args[1]) : List.of();
      }
   }

   private List<String> completePotion(String[] args) {
      return args.length == 1 ? TabCompleteUtil.onlinePlayerNames(args[0]) : List.of();
   }

   private List<String> completeAutoShard(String[] args) {
      if (args.length == 1) {
         return TabCompleteUtil.onlinePlayerNames(args[0]);
      } else {
         return args.length == 2 ? TabCompleteUtil.filter(ON_OFF, args[1]) : List.of();
      }
   }

   private List<String> completeZones(String[] args) {
      return args.length == 1 ? TabCompleteUtil.filter(List.of("wand", "create", "remove", "list", "check"), args[0]) : List.of();
   }

   private List<String> completeTop(String[] args) {
      return args.length == 1 ? TabCompleteUtil.filter(TOP_TYPES, args[0]) : List.of();
   }

   private List<String> onlineNamesPlaceholder() {
      return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
   }
}
