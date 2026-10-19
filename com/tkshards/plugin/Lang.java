package com.tkshards.plugin;

import com.tkshards.plugin.util.MessageUtil;
import java.io.File;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
   private final TKShardsPlugin plugin;
   private YamlConfiguration messages;
   private String rawPrefix;
   private String currency;
   private String currencyCap;

   public Lang(TKShardsPlugin plugin) {
      this.plugin = plugin;
      this.reload();
   }

   public void reload() {
      File file = new File(this.plugin.getDataFolder(), "messages.yml");
      if (!file.exists()) {
         this.plugin.saveResource("messages.yml", false);
      }

      this.messages = YamlConfiguration.loadConfiguration(file);
      this.currency = this.plugin.getConfig().getString("currency-name", "TKShards");
      this.currencyCap = this.currency.isEmpty() ? this.currency : Character.toUpperCase(this.currency.charAt(0)) + this.currency.substring(1);
      this.rawPrefix = this.messages.getString("prefix", "");
   }

   public String currency() {
      return this.currency;
   }

   public String get(String path, String... replacements) {
      String raw = this.messages.getString(path, "");
      if (raw.isEmpty()) {
         return "";
      } else {
         String prefixResolved = raw.contains("{prefix}") ? this.rawPrefix.replace("{currency}", this.currency).replace("{Currency}", this.currencyCap) : "";
         raw = raw.replace("{prefix}", prefixResolved).replace("{currency}", this.currency).replace("{Currency}", this.currencyCap);

         for(int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", replacements[i + 1]);
         }

         return MessageUtil.color(raw);
      }
   }

   public List<String> getList(String path) {
      return this.messages.getStringList(path);
   }
}
