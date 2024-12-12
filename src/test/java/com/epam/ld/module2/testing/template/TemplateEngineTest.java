package com.epam.ld.module2.testing.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateEngineTest {
   private TemplateEngine engine;

   @BeforeEach
   void setUp() {
      engine = new TemplateEngine();
   }

   @Test
   void shouldReplaceSinglePlaceholder() {
      // Given
      Template template = new Template("Hello, #{name}!");
      template.addVariable("name", "John");

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

      // When & Then
      IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> engine.generateMessage(template, null),
            "Should throw exception when placeholder value is missing"
      );

      assertTrue(exception.getMessage().contains("orderId"),
            "Exception message should mention the missing placeholder");
   }

   @Test
   void shouldIgnoreExtraVariables() {
      // Given
      Template template = new Template("Hello, #{name}!");
      template.addVariable("name", "John");
      template.addVariable("unused1", "Extra Value 1");
      template.addVariable("unused2", "Extra Value 2");

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("Hello, John!", result,
            "Should only replace #{name} and ignore extra variables");

      // Verify that adding extra variables doesn't affect the template
      template.addVariable("anotherUnused", "Extra Value 3");
      String secondResult = engine.generateMessage(template, null);
      assertEquals(result, secondResult,
            "Adding more extra variables should not affect the result");
   }
}