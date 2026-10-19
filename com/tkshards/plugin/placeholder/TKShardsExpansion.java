package com.tkshards.plugin.placeholder;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.NumberUtil;
import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TKShardsExpansion extends PlaceholderExpansion {
   private final TKShardsPlugin plugin;

   public TKShardsExpansion(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public @NotNull String getIdentifier() {
      return "tuankashard";
   }

   public @NotNull String getAuthor() {
      return "TKShards";
   }

   public @NotNull String getVersion() {
      return "1.0.0";
   }

   public boolean persist() {
      return true;
   }

   public String onRequest(OfflinePlayer player, @NotNull String params) {
      if (player == null) {
         return "";
      } else {
         long balance = this.plugin.currency().getBalance(player.getUniqueId());
         String var10000;
         switch (params.toLowerCase()) {
            case "balance":
               var10000 = String.valueOf(balance);
               break;
            case "balance_formatted":
               boolean enabled = this.plugin.settings().getBoolean("number-format.enabled", true);
               int decimals = this.plugin.settings().getInt("number-format.decimals", 1);
               List<String> suffixes = this.plugin.settings().getStringList("number-format.suffixes");
               if (suffixes.isEmpty()) {
                  suffixes = List.of("", "K", "M", "B", "T", "Q");
               }

               var10000 = NumberUtil.format(balance, enabled, decimals, suffixes);
               break;
            case "currency":
               var10000 = this.plugin.lang().currency();
               break;
            default:
               var10000 = null;
         }

         return var10000;
      }
   }
}
