package com.tkshards.plugin.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class SchedulerUtil {
   private static Boolean folia;

   private SchedulerUtil() {
   }

   public static boolean isFolia() {
      if (folia == null) {
         try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            folia = true;
         } catch (ClassNotFoundException var1) {
            folia = false;
         }
      }

      return folia;
   }

   public static Object runTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
      return isFolia() ? Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (scheduledTask) -> task.run(), Math.max(delayTicks, 1L), Math.max(periodTicks, 1L)) : Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
   }

   public static Object runTimerAsync(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
      if (isFolia()) {
         long delayMs = delayTicks * 50L;
         long periodMs = Math.max(periodTicks, 1L) * 50L;
         return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (scheduledTask) -> task.run(), Math.max(delayMs, 1L), periodMs, TimeUnit.MILLISECONDS);
      } else {
         return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
      }
   }

   public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
      if (isFolia()) {
         entity.getScheduler().run(plugin, (scheduledTask) -> task.run(), (Runnable)null);
      } else {
         task.run();
      }

   }

   public static void cancel(Object handle) {
      if (handle != null) {
         if (handle instanceof BukkitTask) {
            BukkitTask bukkitTask = (BukkitTask)handle;
            bukkitTask.cancel();
         } else if (handle instanceof ScheduledTask) {
            ScheduledTask scheduledTask = (ScheduledTask)handle;
            scheduledTask.cancel();
         }

      }
   }
}
