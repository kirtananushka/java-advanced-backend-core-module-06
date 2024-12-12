package com.epam.ld.module2.testing.template;

import com.epam.ld.module2.testing.Client;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Template engine.
 */
public class TemplateEngine {
   private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("#\\{([^}]+)}");

   public String generateMessage(Template template, Client client) {
      Set<String> requiredPlaceholders = extractPlaceholders(template.getTemplateText());

      for (String placeholder : requiredPlaceholders) {
         if (!template.getVariables().containsKey(placeholder)) {
            throw new IllegalArgumentException("Missing value for placeholder: " + placeholder);
         }
      }

      String result = template.getTemplateText();
      for (String placeholder : requiredPlaceholders) {
         String value = template.getVariables().get(placeholder);
         result = result.replace("#{" + placeholder + "}", value);
      }

      return result;
   }

   private Set<String> extractPlaceholders(String template) {
      Set<String> placeholders = new HashSet<>();
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
      while (matcher.find()) {
         placeholders.add(matcher.group(1));
      }
      return placeholders;
   }
}
