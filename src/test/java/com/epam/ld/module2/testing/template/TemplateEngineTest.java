package com.epam.ld.module2.testing.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

   @Test
   void shouldThrowExceptionWhenPlaceholderValueIsMissing() {
      // Given
      Template template = new Template("Hello, #{name}! Your order #{orderId} is ready.");
      template.addVariable("name", "John");
      TemplateEngine engine = new TemplateEngine();

      // When & Then
      IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> engine.generateMessage(template, null),
            "Should throw exception when placeholder value is missing"
      );

      assertTrue(exception.getMessage().contains("orderId"),
            "Exception message should mention the missing placeholder");
   }
}