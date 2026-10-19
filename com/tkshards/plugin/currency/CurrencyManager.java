package com.tkshards.plugin.currency;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.SchedulerUtil;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

public class CurrencyManager {
   private final TKShardsPlugin plugin;
   private final File dataFile;
   private final Map<UUID, PlayerRecord> cache = new ConcurrentHashMap();
   private volatile boolean dirty = false;

   public CurrencyManager(TKShardsPlugin plugin) {
      this.plugin = plugin;
      this.dataFile = new File(plugin.getDataFolder(), "data.yml");
      this.load();
      SchedulerUtil.runTimerAsync(plugin, () -> {
         if (this.dirty) {
            this.save();
         }

      }, 600L, 600L);
   }

   private synchronized void load() {
      if (this.dataFile.exists()) {
         YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.dataFile);
         if (yaml.isConfigurationSection("players")) {
            for(String uuidStr : yaml.getConfigurationSection("players").getKeys(false)) {
               try {
                  UUID uuid = UUID.fromString(uuidStr);
                  String path = "players." + uuidStr + ".";
                  PlayerRecord record = new PlayerRecord();
                  record.name = yaml.getString(path + "name", "?");
                  record.balance = yaml.getLong(path + "balance", 0L);
                  record.acceptingPayments = yaml.getBoolean(path + "accepting-payments", true);
                  record.autoShardEnabled = yaml.getBoolean(path + "autoshard-enabled", true);
                  record.adminGranted = yaml.getBoolean(path + "admin-granted", false);
                  record.customAfkIntervalSeconds = yaml.getLong(path + "custom-afk-interval-seconds", 0L);
                  record.kills = yaml.getLong(path + "kills", 0L);
                  record.deaths = yaml.getLong(path + "deaths", 0L);
                  record.sellTotal = yaml.getLong(path + "sell-total", 0L);
                  this.cache.put(uuid, record);
               } catch (IllegalArgumentException var7) {
                  this.plugin.getLogger().warning("Bỏ qua UUID không hợp lệ trong data.yml: " + uuidStr);
               }
            }

         }
      }
   }

   private synchronized void save() {
      try {
         YamlConfiguration yaml = new YamlConfiguration();

         for(Map.Entry<UUID, PlayerRecord> entry : this.cache.entrySet()) {
            String path = "players." + String.valueOf(entry.getKey()) + ".";
            PlayerRecord record = (PlayerRecord)entry.getValue();
            yaml.set(path + "name", record.name);
            yaml.set(path + "balance", record.balance);
            yaml.set(path + "accepting-payments", record.acceptingPayments);
            yaml.set(path + "autoshard-enabled", record.autoShardEnabled);
            yaml.set(path + "admin-granted", record.adminGranted);
            yaml.set(path + "custom-afk-interval-seconds", record.customAfkIntervalSeconds);
            yaml.set(path + "kills", record.kills);
            yaml.set(path + "deaths", record.deaths);
            yaml.set(path + "sell-total", record.sellTotal);
         }

         yaml.save(this.dataFile);
         this.dirty = false;
      } catch (Exception e) {
         this.plugin.getLogger().log(Level.SEVERE, "Khong the luu data.yml", e);
      }

   }

   private PlayerRecord record(UUID uuid) {
      return (PlayerRecord)this.cache.computeIfAbsent(uuid, (k) -> new PlayerRecord());
   }

   public void setName(UUID uuid, String name) {
      PlayerRecord r = this.record(uuid);
      if (!name.equals(r.name)) {
         r.name = name;
         this.dirty = true;
      }

   }

   public String getName(UUID uuid) {
      return this.record(uuid).name;
   }

   public long getBalance(UUID uuid) {
      return this.record(uuid).balance;
   }

   public void setBalance(UUID uuid, long amount) {
      this.record(uuid).balance = Math.max(amount, 0L);
      this.dirty = true;
   }

   public void addBalance(UUID uuid, long amount) {
      if (amount > 0L) {
         this.setBalance(uuid, this.getBalance(uuid) + amount);
      }
   }

   public boolean removeBalance(UUID uuid, long amount) {
      long current = this.getBalance(uuid);
      if (current < amount) {
         return false;
      } else {
         this.setBalance(uuid, current - amount);
         return true;
      }
   }

   public boolean isAcceptingPayments(UUID uuid) {
      return this.record(uuid).acceptingPayments;
   }

   public void togglePayments(UUID uuid) {
      PlayerRecord r = this.record(uuid);
      r.acceptingPayments = !r.acceptingPayments;
      this.dirty = true;
   }

   public boolean isAutoShardEnabled(UUID uuid) {
      return this.record(uuid).autoShardEnabled;
   }

   public boolean toggleAutoShard(UUID uuid) {
      PlayerRecord r = this.record(uuid);
      r.autoShardEnabled = !r.autoShardEnabled;
      this.dirty = true;
      return r.autoShardEnabled;
   }

   public boolean isAdminGranted(UUID uuid) {
      return this.record(uuid).adminGranted;
   }

   public void setAdminGranted(UUID uuid, boolean granted) {
      this.record(uuid).adminGranted = granted;
      this.dirty = true;
   }

   public long getCustomAfkInterval(UUID uuid) {
      return this.record(uuid).customAfkIntervalSeconds;
   }

   public void setCustomAfkInterval(UUID uuid, long seconds) {
      this.record(uuid).customAfkIntervalSeconds = Math.max(seconds, 0L);
      this.dirty = true;
   }

   public void incrementKills(UUID uuid) {
      ++this.record(uuid).kills;
      this.dirty = true;
   }

   public void incrementDeaths(UUID uuid) {
      ++this.record(uuid).deaths;
      this.dirty = true;
   }

   public long getKills(UUID uuid) {
      return this.record(uuid).kills;
   }

   public long getDeaths(UUID uuid) {
      return this.record(uuid).deaths;
   }

   public void addSellTotal(UUID uuid, long amount) {
      if (amount > 0L) {
         PlayerRecord var10000 = this.record(uuid);
         var10000.sellTotal += amount;
         this.dirty = true;
      }
   }

   public long getSellTotal(UUID uuid) {
      return this.record(uuid).sellTotal;
   }

   public List<UUID> getAllTrackedPlayers() {
      return List.copyOf(this.cache.keySet());
   }

   public void close() {
      if (this.dirty) {
         this.save();
      }

   }

   private static class PlayerRecord {
      String name = "?";
      long balance = 0L;
      boolean acceptingPayments = true;
      boolean autoShardEnabled = true;
      boolean adminGranted = false;
      long customAfkIntervalSeconds = 0L;
      long kills = 0L;
      long deaths = 0L;
      long sellTotal = 0L;
   }
}
