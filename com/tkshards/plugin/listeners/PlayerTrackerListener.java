package com.tkshards.plugin.listeners;

import com.tkshards.plugin.TKShardsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerTrackerListener implements Listener {
   private final TKShardsPlugin plugin;

   public PlayerTrackerListener(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      this.plugin.currency().setName(event.getPlayer().getUniqueId(), event.getPlayer().getName());
   }
}
