package com.epam.ld.module2.testing.template;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Template.
 */
public class Template {
   private final String templateText;
   private final Map<String, String> variables;

   public Template(String templateText) {
      this.templateText = templateText;
      this.variables = new HashMap<>();
   }

   public void addVariable(String key, String value) {
      variables.put(key, value);
   }

   public String getTemplateText() {
      return templateText;
   }

   public Map<String, String> getVariables() {
      return new HashMap<>(variables);
   }
}