package com.epam.ld.module2.testing.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateEngineTest {

   @Test
   void shouldReplaceSinglePlaceholder() {
      // Given
      Template template = new Template("Hello, #{name}!");
      template.addVariable("name", "John");
      TemplateEngine engine = new TemplateEngine();

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("Hello, John!", result, "Should replace #{name} with 'John'");
   }
   
}
