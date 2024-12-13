package com.epam.ld.module2.testing.template;

import com.epam.ld.module2.testing.Client;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Template engine.
 * This class is responsible for generating messages by replacing placeholders in a given template
 * with corresponding values from the provided variables map in the template.
 */
public class TemplateEngine {
   private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("#\\{([^}]+)}");

   /**
    * Generates a message by replacing placeholders in the template with appropriate values.
    *
    * @param template the template containing text and placeholders
    * @param client   the client for additional context (not used in this implementation)
    * @return the generated message with placeholders replaced
    */
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

   /**
    * Ensures that the provided string is encoded using Latin-1 (ISO-8859-1).
    *
    * @param text the input text to encode
    * @return the encoded string
    */
   private String ensureLatin1Encoding(String text) {
      return new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);
   }

   /**
    * Extracts all placeholders within the given template string.
    * Placeholders are enclosed in #{..}.
    *
    * @param template the template string
    * @return a set of extracted placeholder names
    */
   private Set<String> extractPlaceholders(String template) {
      Set<String> placeholders = new HashSet<>();
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
      while (matcher.find()) {
         placeholders.add(matcher.group(1));
      }
      return placeholders;
   }

   /**
    * Validates whether all required placeholders have corresponding values in the template.
    * Also validates the format of the placeholders.
    *
    * @param required the set of required placeholders
    * @param template the template containing the variables map with values for placeholders
    */
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

   /**
    * Validates the format of a given placeholder.
    * Placeholders must start with a letter and can only contain alphanumeric characters.
    *
    * @param placeholder the placeholder name to validate
    * @throws IllegalArgumentException if the placeholder format is invalid
    */
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

   /**
    * Determines if the given value is a runtime tag, meaning it contains a placeholder format
    * or additional runtime-based expressions (e.g., surrounded by #{..}).
    *
    * @param value the string to check
    * @return {@code true} if the value is a runtime tag, otherwise {@code false}
    */
   private boolean isRuntimeTag(String value) {
      if (value == null) {
         return false;
      }
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
      return matcher.matches() || value.contains("#{") && value.contains("}");
   }
}