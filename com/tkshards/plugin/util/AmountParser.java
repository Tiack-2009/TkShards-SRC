package com.tkshards.plugin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AmountParser {
   private static final Pattern PATTERN = Pattern.compile("^(\\d+(?:[.,]\\d+)?)([kmb]?)$", 2);

   private AmountParser() {
   }

   public static Long parse(String input) {
      if (input != null && !input.isEmpty()) {
         Matcher matcher = PATTERN.matcher(input.trim());
         if (!matcher.matches()) {
            return null;
         } else {
            try {
               double base = Double.parseDouble(matcher.group(1).replace(",", "."));
               double var10000;
               switch (matcher.group(2).toLowerCase()) {
                  case "k" -> var10000 = (double)1000.0F;
                  case "m" -> var10000 = (double)1000000.0F;
                  case "b" -> var10000 = (double)1.0E9F;
                  default -> var10000 = (double)1.0F;
               }

               double multiplier = var10000;
               return Math.round(base * multiplier);
            } catch (NumberFormatException var9) {
               return null;
            }
         }
      } else {
         return null;
      }
   }
}
