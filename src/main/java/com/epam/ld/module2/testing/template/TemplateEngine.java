package com.epam.ld.module2.testing.template;

import com.epam.ld.module2.testing.Client;

import java.nio.charset.StandardCharsets;
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
      String templateText = new String(
            template.getTemplateText().getBytes(StandardCharsets.ISO_8859_1),
            StandardCharsets.ISO_8859_1
      );

      Set<String> placeholders = extractPlaceholders(templateText);
      validatePlaceholders(placeholders, template);

      Set<String> runtimeTags = new HashSet<>();
      for (String placeholder : placeholders) {
         String value = template.getVariables().get(placeholder);
         if (isRuntimeTag(value)) {
            runtimeTags.add(placeholder);
         }
      }

      String result = templateText;
      for (String placeholder : placeholders) {
         if (!runtimeTags.contains(placeholder)) {
            String value = ensureLatin1Encoding(template.getVariables().get(placeholder));
            result = result.replace("#{" + placeholder + "}", value);
         }
      }

      for (String placeholder : runtimeTags) {
         String value = template.getVariables().get(placeholder);
         result = result.replace("#{" + placeholder + "}", value);
      }

      return result;
   }

   private String ensureLatin1Encoding(String text) {
      return new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);
   }

   private Set<String> extractPlaceholders(String template) {
      Set<String> placeholders = new HashSet<>();
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
      while (matcher.find()) {
         placeholders.add(matcher.group(1));
      }
      return placeholders;
   }

   private void validatePlaceholders(Set<String> required, Template template) {
      Set<String> missing = new HashSet<>();
      for (String placeholder : required) {
         validatePlaceholderFormat(placeholder);
         if (!template.getVariables().containsKey(placeholder)) {
            missing.add(placeholder);
         } else if (template.getVariables().get(placeholder) == null) {
            throw new IllegalArgumentException("Null value not allowed for placeholder: " + placeholder);
         }
      }

      if (!missing.isEmpty()) {
         throw new IllegalArgumentException("Missing values for placeholders: " +
               String.join(", ", missing));
      }
   }

   private void validatePlaceholderFormat(String placeholder) {
      if (placeholder == null) {
         throw new IllegalArgumentException("Invalid placeholder: null name");
      }
      if (placeholder.isEmpty()) {
         throw new IllegalArgumentException("Invalid placeholder: empty name");
      }
      if (!placeholder.matches("[a-zA-Z][a-zA-Z0-9]*")) {
         throw new IllegalArgumentException("Invalid placeholder format: " + placeholder);
      }
   }

   private boolean isRuntimeTag(String value) {
      if (value == null) return false;
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
      return matcher.matches() || value.contains("#{") && value.contains("}");
   }
}