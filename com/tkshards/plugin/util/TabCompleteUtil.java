package com.tkshards.plugin.util;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

public final class TabCompleteUtil {
   private TabCompleteUtil() {
   }

   public static List<String> filter(List<String> options, String partial) {
      String lower = partial.toLowerCase();
      return (List)options.stream().filter((o) -> o.toLowerCase().startsWith(lower)).collect(Collectors.toList());
   }

   public static List<String> onlinePlayerNames(String partial) {
      List<String> names = (List)Bukkit.getOnlinePlayers().stream().map((p) -> p.getName()).collect(Collectors.toList());
      return filter(names, partial);
   }
}
