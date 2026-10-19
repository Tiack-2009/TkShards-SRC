package com.tkshards.plugin.util;

import com.tkshards.plugin.TKShardsPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ActionBarCombiner {
   private final TKShardsPlugin plugin;
   private Object task;
   private final Map<UUID, String> zoneText = new ConcurrentHashMap();
   private final Map<UUID, String> autoShardText = new ConcurrentHashMap();
   private final Map<UUID, String> boosterText = new ConcurrentHashMap();

   public ActionBarCombiner(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public void start() {
      this.stop();
      this.task = SchedulerUtil.runTimer(this.plugin, this::tick, 20L, 20L);
   }

   public void stop() {
      SchedulerUtil.cancel(this.task);
      this.task = null;
      this.zoneText.clear();
      this.autoShardText.clear();
      this.boosterText.clear();
   }

   public void setZone(UUID uuid, String coloredText) {
      if (coloredText == null) {
         this.zoneText.remove(uuid);
      } else {
         this.zoneText.put(uuid, coloredText);
      }

   }

   public void setAutoShard(UUID uuid, String coloredText) {
      if (coloredText == null) {
         this.autoShardText.remove(uuid);
      } else {
         this.autoShardText.put(uuid, coloredText);
      }

   }

   public void setBooster(UUID uuid, String coloredText) {
      if (coloredText == null) {
         this.boosterText.remove(uuid);
      } else {
         this.boosterText.put(uuid, coloredText);
      }

   }

   private void tick() {
      for(Player player : this.plugin.getServer().getOnlinePlayers()) {
         UUID uuid = player.getUniqueId();
         String zone = (String)this.zoneText.get(uuid);
         String auto = (String)this.autoShardText.get(uuid);
         String booster = (String)this.boosterText.get(uuid);
         if (zone != null || auto != null || booster != null) {
            List<String> parts = new ArrayList();
            if (zone != null) {
               parts.add(zone);
            }

            if (auto != null) {
               parts.add(auto);
            }

            if (booster != null) {
               parts.add(booster);
            }

            String combined = String.join("  &8|&r  ", parts);
            SchedulerUtil.runForEntity(this.plugin, player, () -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageUtil.color(combined))));
         }
      }

   }
}
