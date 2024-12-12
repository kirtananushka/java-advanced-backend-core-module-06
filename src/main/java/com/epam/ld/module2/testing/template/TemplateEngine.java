package com.epam.ld.module2.testing.template;

import com.epam.ld.module2.testing.Client;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Template engine.
 */
public class TemplateEngine {
   private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("#\\{([^}]+)}");

   public String generateMessage(Template template, Client client) {
      String result = template.getTemplateText();
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(result);

      while (matcher.find()) {
         String placeholder = matcher.group(1);
         if (!template.getVariables().containsKey(placeholder)) {
            throw new IllegalArgumentException("Missing value for placeholder: " + placeholder);
         }
      }

      for (Map.Entry<String, String> entry : template.getVariables().entrySet()) {
         String placeholder = "#{" + entry.getKey() + "}";
         result = result.replace(placeholder, entry.getValue());
      }

      return result;
   }
}
