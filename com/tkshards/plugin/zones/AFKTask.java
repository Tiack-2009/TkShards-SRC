package com.tkshards.plugin.zones;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.SchedulerUtil;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class AFKTask {
   private final TKShardsPlugin plugin;
   private Object task;
   private final Map<UUID, Integer> remainingSeconds = new ConcurrentHashMap();

   public AFKTask(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public void start() {
      this.stop();
      YamlConfiguration cfg = this.loadAfkConfig();
      long displayInterval = cfg.getLong("AFK-ZONES.display-interval-seconds", 5L);
      long ticks = Math.max(displayInterval, 1L) * 20L;
      this.remainingSeconds.clear();
      this.task = SchedulerUtil.runTimer(this.plugin, this::tick, ticks, ticks);
   }

   public void stop() {
      SchedulerUtil.cancel(this.task);
      this.task = null;

      for(UUID uuid : this.remainingSeconds.keySet()) {
         this.plugin.actionBar().setZone(uuid, (String)null);
      }

      this.remainingSeconds.clear();
   }

   private YamlConfiguration loadAfkConfig() {
      File file = new File(this.plugin.getDataFolder(), "zones/afk-zones.yml");
      return YamlConfiguration.loadConfiguration(file);
   }

   private void tick() {
      YamlConfiguration cfg = this.loadAfkConfig();
      long base = cfg.getLong("AFK-ZONES.shards-per-interval", 2L);
      String actionBarTemplate = cfg.getString("AFK-ZONES.action-bar", "");
      long intervalSeconds = cfg.getLong("AFK-ZONES.interval-seconds", 60L);
      long displayInterval = cfg.getLong("AFK-ZONES.display-interval-seconds", 5L);

      for(Player player : this.plugin.getServer().getOnlinePlayers()) {
         SchedulerUtil.runForEntity(this.plugin, player, () -> this.processPlayer(player, cfg, base, actionBarTemplate, intervalSeconds, displayInterval));
      }

   }

   private void processPlayer(Player player, YamlConfiguration cfg, long base, String actionBarTemplate, long intervalSeconds, long displayInterval) {
      UUID uuid = player.getUniqueId();
      boolean everywhere = player.hasPermission("tkshards.everywhere");
      boolean inZone = everywhere || this.plugin.zones().getZones().values().stream().anyMatch((zone) -> zone.contains(player.getLocation()));
      if (!inZone) {
         this.remainingSeconds.remove(uuid);
         this.plugin.actionBar().setZone(uuid, (String)null);
      } else {
         long customInterval = this.plugin.currency().getCustomAfkInterval(uuid);
         long effectiveInterval = customInterval > 0L ? customInterval : intervalSeconds;
         int left = (Integer)this.remainingSeconds.getOrDefault(uuid, (int)effectiveInterval) - (int)displayInterval;
         if (left <= 0) {
            long amount = base;
            int bestBoost = 0;
            if (cfg.isConfigurationSection("AFK-ZONES.permission-boosts")) {
               for(String perm : cfg.getConfigurationSection("AFK-ZONES.permission-boosts").getKeys(false)) {
                  if (player.hasPermission(perm)) {
                     bestBoost = Math.max(bestBoost, cfg.getInt("AFK-ZONES.permission-boosts." + perm, 0));
                  }
               }
            }

            if (bestBoost > 0) {
               amount = (long)bestBoost;
            }

            double multiplier = this.plugin.booster().getMultiplier(uuid) * this.plugin.getRankMultiplier(player);
            amount = Math.round((double)amount * multiplier);
            this.plugin.currency().addBalance(uuid, amount);
            left = (int)effectiveInterval;
         }

         this.remainingSeconds.put(uuid, left);
         if (!actionBarTemplate.isEmpty()) {
            String msg = actionBarTemplate.replace("{time}", String.valueOf(left));
            this.plugin.actionBar().setZone(uuid, msg);
         }

      }
   }
}
