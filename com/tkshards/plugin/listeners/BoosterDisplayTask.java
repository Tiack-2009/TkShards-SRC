package com.tkshards.plugin.listeners;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.SchedulerUtil;
import com.tkshards.plugin.util.TimeUtil;
import org.bukkit.entity.Player;

public class BoosterDisplayTask {
   private final TKShardsPlugin plugin;
   private Object task;

   public BoosterDisplayTask(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public void start() {
      this.stop();
      this.task = SchedulerUtil.runTimer(this.plugin, this::tick, 20L, 20L);
   }

   public void stop() {
      SchedulerUtil.cancel(this.task);
      this.task = null;

      for(Player player : this.plugin.getServer().getOnlinePlayers()) {
         this.plugin.actionBar().setBooster(player.getUniqueId(), (String)null);
      }

   }

   private void tick() {
      String template = this.plugin.getConfig().getString("booster.countdown-actionbar", "&dBoost: &fx{multiplier} &dcòn &f{time}");

      for(Player player : this.plugin.getServer().getOnlinePlayers()) {
         SchedulerUtil.runForEntity(this.plugin, player, () -> {
            if (!this.plugin.booster().isActive(player.getUniqueId())) {
               this.plugin.actionBar().setBooster(player.getUniqueId(), (String)null);
            } else {
               long remainingSeconds = this.plugin.booster().getRemainingSeconds(player.getUniqueId());
               double multiplier = this.plugin.booster().getMultiplier(player.getUniqueId());
               String multiplierText = multiplier == Math.floor(multiplier) ? String.valueOf((long)multiplier) : String.valueOf(multiplier);
               String msg = template.replace("{time}", TimeUtil.formatDuration(remainingSeconds)).replace("{multiplier}", multiplierText);
               this.plugin.actionBar().setBooster(player.getUniqueId(), msg);
            }
         });
      }

   }
}
