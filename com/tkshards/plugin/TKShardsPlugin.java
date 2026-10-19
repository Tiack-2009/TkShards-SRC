package com.tkshards.plugin;

import com.tkshards.plugin.commands.AutoShardCommand;
import com.tkshards.plugin.commands.BalanceCommand;
import com.tkshards.plugin.commands.BoosterCommand;
import com.tkshards.plugin.commands.ManagerCommand;
import com.tkshards.plugin.commands.PayCommand;
import com.tkshards.plugin.commands.PotionCommand;
import com.tkshards.plugin.commands.ReloadCommand;
import com.tkshards.plugin.commands.ShardCommand;
import com.tkshards.plugin.commands.TKShardsTabCompleter;
import com.tkshards.plugin.commands.TopCommand;
import com.tkshards.plugin.commands.ZoneCommand;
import com.tkshards.plugin.currency.CurrencyManager;
import com.tkshards.plugin.economy.VaultBridge;
import com.tkshards.plugin.listeners.BoosterDisplayTask;
import com.tkshards.plugin.listeners.BoosterListener;
import com.tkshards.plugin.listeners.KillListener;
import com.tkshards.plugin.listeners.PlayerTrackerListener;
import com.tkshards.plugin.listeners.PotionListener;
import com.tkshards.plugin.placeholder.TKShardsExpansion;
import com.tkshards.plugin.util.ActionBarCombiner;
import com.tkshards.plugin.zones.AFKTask;
import com.tkshards.plugin.zones.AutoShardTask;
import com.tkshards.plugin.zones.ZoneManager;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TKShardsPlugin extends JavaPlugin {
   private static TKShardsPlugin instance;
   private Lang lang;
   private CurrencyManager currencyManager;
   private ZoneManager zoneManager;
   private BoosterListener boosterListener;
   private PotionListener potionListener;
   private AFKTask afkTask;
   private AutoShardTask autoShardTask;
   private ActionBarCombiner actionBarCombiner;
   private BoosterDisplayTask boosterDisplayTask;
   private VaultBridge vaultBridge;
   private YamlConfiguration settings;

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.saveResourceIfMissing("messages.yml");
      this.saveResourceIfMissing("settings.yml");
      this.saveResourceIfMissing("zones/afk-zones.yml");
      this.saveResourceIfMissing("zones/zones.yml");
      this.settings = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "settings.yml"));
      this.lang = new Lang(this);
      this.currencyManager = new CurrencyManager(this);
      this.zoneManager = new ZoneManager(this);
      this.boosterListener = new BoosterListener(this);
      this.potionListener = new PotionListener(this);
      this.actionBarCombiner = new ActionBarCombiner(this);
      this.actionBarCombiner.start();
      this.boosterDisplayTask = new BoosterDisplayTask(this);
      this.boosterDisplayTask.start();
      this.vaultBridge = new VaultBridge();
      this.getCommand("tkshards").setExecutor(new BalanceCommand(this));
      this.getCommand("tkshardspay").setExecutor(new PayCommand(this));
      this.getCommand("tkshardsmanager").setExecutor(new ManagerCommand(this));
      this.getCommand("tkshardsreload").setExecutor(new ReloadCommand(this));
      this.getCommand("tkbooster").setExecutor(new BoosterCommand(this));
      this.getCommand("tkzones").setExecutor(new ZoneCommand(this));
      this.getCommand("tkautoshard").setExecutor(new AutoShardCommand(this));
      this.getCommand("tkpotion").setExecutor(new PotionCommand(this));
      this.getCommand("shard").setExecutor(new ShardCommand(this));
      this.getCommand("top").setExecutor(new TopCommand(this));
      TKShardsTabCompleter tabCompleter = new TKShardsTabCompleter();

      for(String cmdName : new String[]{"shard", "tkshardspay", "tkshardsmanager", "tkbooster", "tkpotion", "tkautoshard", "tkzones", "top"}) {
         this.getCommand(cmdName).setTabCompleter(tabCompleter);
      }

      Bukkit.getPluginManager().registerEvents(new KillListener(this), this);
      Bukkit.getPluginManager().registerEvents(new PlayerTrackerListener(this), this);
      Bukkit.getPluginManager().registerEvents(this.boosterListener, this);
      Bukkit.getPluginManager().registerEvents(this.potionListener, this);
      this.potionListener.startExpiryCleanup();
      Bukkit.getPluginManager().registerEvents(this.zoneManager, this);
      this.afkTask = new AFKTask(this);
      this.afkTask.start();
      this.autoShardTask = new AutoShardTask(this);
      this.autoShardTask.start();
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         (new TKShardsExpansion(this)).register();
         this.getLogger().info("Đã đăng ký placeholder %tuankashard_...% với PlaceholderAPI.");
      } else {
         this.getLogger().warning("Không tìm thấy PlaceholderAPI - placeholder %tuankashard_...% sẽ không hoạt động.");
      }

      this.getLogger().info("TKShards đã khởi động xong, tên tiền tệ: " + this.lang.currency());
   }

   public void onDisable() {
      if (this.afkTask != null) {
         this.afkTask.stop();
      }

      if (this.autoShardTask != null) {
         this.autoShardTask.stop();
      }

      if (this.actionBarCombiner != null) {
         this.actionBarCombiner.stop();
      }

      if (this.boosterDisplayTask != null) {
         this.boosterDisplayTask.stop();
      }

      if (this.potionListener != null) {
         this.potionListener.stopExpiryCleanup();
      }

      if (this.currencyManager != null) {
         this.currencyManager.close();
      }

   }

   private void saveResourceIfMissing(String path) {
      File file = new File(this.getDataFolder(), path);
      if (!file.exists()) {
         this.saveResource(path, false);
      }

   }

   public void reload() {
      this.reloadConfig();
      this.settings = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "settings.yml"));
      this.lang.reload();
      this.zoneManager.reload();
      if (this.autoShardTask != null) {
         this.autoShardTask.start();
      }

   }

   public static TKShardsPlugin get() {
      return instance;
   }

   public YamlConfiguration settings() {
      return this.settings;
   }

   public Lang lang() {
      return this.lang;
   }

   public CurrencyManager currency() {
      return this.currencyManager;
   }

   public ZoneManager zones() {
      return this.zoneManager;
   }

   public BoosterListener booster() {
      return this.boosterListener;
   }

   public PotionListener potion() {
      return this.potionListener;
   }

   public AutoShardTask autoShard() {
      return this.autoShardTask;
   }

   public VaultBridge vault() {
      return this.vaultBridge;
   }

   public double getRankMultiplier(Player player) {
      if (!this.getConfig().getBoolean("rank-multiplier.enabled", true)) {
         return (double)1.0F;
      } else {
         ConfigurationSection section = this.getConfig().getConfigurationSection("rank-multiplier.permissions");
         if (section == null) {
            return (double)1.0F;
         } else {
            double best = (double)1.0F;

            for(String permission : section.getKeys(false)) {
               if (player.hasPermission(permission)) {
                  best = Math.max(best, section.getDouble(permission, (double)1.0F));
               }
            }

            return best;
         }
      }
   }

   public ActionBarCombiner actionBar() {
      return this.actionBarCombiner;
   }
}
