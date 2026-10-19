package com.tkshards.plugin.zones;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.MessageUtil;
import com.tkshards.plugin.util.SchedulerUtil;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class AutoShardTask {
   private final TKShardsPlugin plugin;
   private Object task;
   private final Map<UUID, Integer> remainingSeconds = new ConcurrentHashMap();

   public AutoShardTask(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public void start() {
      this.stop();
      if (this.plugin.getConfig().getBoolean("auto-shard.enabled", true)) {
         long displayInterval = this.plugin.getConfig().getLong("auto-shard.display-interval-seconds", 5L);
         long ticks = Math.max(displayInterval, 1L) * 20L;
         this.remainingSeconds.clear();
         this.task = SchedulerUtil.runTimer(this.plugin, this::tick, ticks, ticks);
      }
   }

   public void stop() {
      SchedulerUtil.cancel(this.task);
      this.task = null;

      for(UUID uuid : this.remainingSeconds.keySet()) {
         this.plugin.actionBar().setAutoShard(uuid, (String)null);
      }

      this.remainingSeconds.clear();
   }

   private void tick() {
      String permission = this.plugin.getConfig().getString("auto-shard.permission", "tkautoshard");
      long amount = this.plugin.getConfig().getLong("auto-shard.amount", 1L);
      long intervalSeconds = this.plugin.getConfig().getLong("auto-shard.interval-seconds", 30L);
      long displayInterval = this.plugin.getConfig().getLong("auto-shard.display-interval-seconds", 5L);
      String messageTemplate = this.plugin.getConfig().getString("auto-shard.message", "");
      String actionBarTemplate = this.plugin.getConfig().getString("auto-shard.countdown-actionbar", "");

      for(Player player : this.plugin.getServer().getOnlinePlayers()) {
         SchedulerUtil.runForEntity(this.plugin, player, () -> this.processPlayer(player, permission, amount, intervalSeconds, displayInterval, messageTemplate, actionBarTemplate));
      }

   }

   private void processPlayer(Player player, String permission, long amount, long intervalSeconds, long displayInterval, String messageTemplate, String actionBarTemplate) {
      UUID uuid = player.getUniqueId();
      boolean eligible = player.hasPermission(permission) || this.plugin.currency().isAdminGranted(uuid);
      if (eligible && this.plugin.currency().isAutoShardEnabled(uuid)) {
         int left = (Integer)this.remainingSeconds.getOrDefault(uuid, (int)intervalSeconds) - (int)displayInterval;
         if (left <= 0) {
            double multiplier = this.plugin.booster().getMultiplier(uuid) * this.plugin.getRankMultiplier(player);
            long finalAmount = Math.round((double)amount * multiplier);
            this.plugin.currency().addBalance(uuid, finalAmount);
            if (!messageTemplate.isEmpty()) {
               String msg = messageTemplate.replace("{amount}", String.valueOf(finalAmount)).replace("{currency}", this.plugin.lang().currency());
               player.sendMessage(MessageUtil.color(msg));
            }

            left = (int)intervalSeconds;
         }

         this.remainingSeconds.put(uuid, left);
         if (!actionBarTemplate.isEmpty()) {
            String msg = actionBarTemplate.replace("{time}", String.valueOf(left));
            this.plugin.actionBar().setAutoShard(uuid, msg);
         }

      } else {
         this.remainingSeconds.remove(uuid);
         this.plugin.actionBar().setAutoShard(uuid, (String)null);
      }
   }
}
