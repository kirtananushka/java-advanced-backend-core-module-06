package com.epam.ld.module2.testing.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

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

   @Test
   void shouldSupportRuntimeTagValues() {
      // Given
      Template template = new Template("Hello #{name}! Your tag is #{tag}, but not #{escaped}");
      template.addVariable("name", "John");
      template.addVariable("tag", "#{runtimeTag}");
      template.addVariable("escaped", "\\#{notATag}");  // Testing escaped sequence

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("Hello John! Your tag is #{runtimeTag}, but not \\#{notATag}", result,
            "Should preserve #{...} format in variable values and handle escaped sequences");
   }

   @Test
   void shouldSupportNestedRuntimeTagValues() {
      // Given
      Template template = new Template("Tags: #{tag1} -> #{tag2}");
      template.addVariable("tag1", "#{runtime1}");
      template.addVariable("tag2", "Value with #{nested}");

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("Tags: #{runtime1} -> Value with #{nested}", result,
            "Should handle #{...} values in different contexts");
   }

   @Test
   void shouldPreserveRuntimeTagsInVariableValues() {
      // Given
      Template template = new Template("#{prefix} #{content} #{suffix}");
      template.addVariable("prefix", "Start:");
      template.addVariable("content", "#{dynamicContent}");
      template.addVariable("suffix", "#{dynamicEnd}");

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("Start: #{dynamicContent} #{dynamicEnd}", result,
            "Should correctly handle mix of regular and runtime tag values");
   }

   @Test
   void shouldPreserveRuntimeTagAndReplaceNormal() {
      // Given
      Template template = new Template("Value is: #{value}");
      template.addVariable("value", "#{tag}");

      Template template2 = new Template("Prefix #{value} #{tag}");
      template2.addVariable("value", "Test");
      template2.addVariable("tag", "Success");

      // When
      String firstResult = engine.generateMessage(template, null);
      String secondResult = engine.generateMessage(template2, null);

      // Then
      assertEquals("Value is: #{tag}", firstResult,
            "First template should preserve the #{tag}");
      assertEquals("Prefix Test Success", secondResult,
            "Second template should replace both placeholders normally");
   }

   @Test
   void shouldChainRuntimeTagProcessing() {
      // Given
      Template template1 = new Template("Value is: #{value}");
      template1.addVariable("value", "#{nextTag}");

      Template template2 = new Template(engine.generateMessage(template1, null));
      template2.addVariable("nextTag", "final");

      // When
      String result = engine.generateMessage(template2, null);

      // Then
      assertEquals("Value is: final", result,
            "Should properly handle chained template processing");
   }

   @Test
   void shouldSupportLatin1CharactersInTemplate() {
      // Given
      Template template = new Template("¡Hola #{name}! Código: #{code}");
      template.addVariable("name", "José");
      template.addVariable("code", "123");

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("¡Hola José! Código: 123", result,
            "Should properly handle Latin-1 characters in template");
   }

   @Test
   void shouldSupportLatin1CharactersInVariables() {
      // Given
      Template template = new Template("Message: #{message}");
      template.addVariable("message", "Café avec crème et pâté");

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("Message: Café avec crème et pâté", result,
            "Should properly handle Latin-1 characters in variable values");
   }

   @Test
   void shouldSupportMixedLatin1AndRuntimeTags() {
      // Given
      Template template = new Template("#{greeting} señor #{name}! #{tagline}");
      template.addVariable("greeting", "¡Hola");
      template.addVariable("name", "González");
      template.addVariable("tagline", "#{custom_tag}");

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("¡Hola señor González! #{custom_tag}", result,
            "Should handle Latin-1 characters with runtime tags");
   }

   @Test
   void shouldSupportFullLatin1CharacterSet() {
      // Given
      String latin1Chars = "¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
      Template template = new Template("Special: #{chars}");
      template.addVariable("chars", latin1Chars);

      // When
      String result = engine.generateMessage(template, null);

      // Then
      assertEquals("Special: " + latin1Chars, result,
            "Should support all Latin-1 special characters");
   }

   static Stream<Arguments> provideTemplateTestCases() {
      return Stream.of(
            Arguments.of("Hello, #{name}!", "name", "John", "Hello, John!"),
            Arguments.of("No variables here", "any", "value", "No variables here"),
            Arguments.of("#{tag} is #{tag}", "tag", "repeated", "repeated is repeated")
      );
   }

   @ParameterizedTest(name = "Test #{index}: Template={0}, Expected={2}")
   @MethodSource("provideTemplateTestCases")
   void shouldProcessDifferentTemplates(String templateText, String varName,
                                        String varValue, String expected) {
      // Given
      Template template = new Template(templateText);
      template.addVariable(varName, varValue);
      TemplateEngine templateEngine = new TemplateEngine();

      // When
      String result = templateEngine.generateMessage(template, null);

      // Then
      assertEquals(expected, result);
   }

   @TestFactory
   @DisplayName("Dynamic tests for template processing")
   Stream<DynamicTest> dynamicTestsForTemplateProcessing() {
      TemplateEngine templateEngine = new TemplateEngine();

      class TestCase {
         private final String name;
         private final String template;
         private final String varName;
         private final String varValue;
         private final String expected;

         TestCase(String name, String template, String varName,
                  String varValue, String expected) {
            this.name = name;
            this.template = template;
            this.varName = varName;
            this.varValue = varValue;
            this.expected = expected;
         }
      }

      TestCase[] testCases = {
            new TestCase(
                  "Simple replacement",
                  "Hello, #{name}",
                  "name",
                  "World",
                  "Hello, World"
            ),
            new TestCase(
                  "Special characters",
                  "#{symbol}! #{symbol}?",
                  "symbol",
                  "@",
                  "@! @?"
            )
      };

      return Stream.of(testCases)
            .map(testCase -> DynamicTest.dynamicTest(
                  testCase.name,
                  () -> {
                     Template template = new Template(testCase.template);
                     template.addVariable(testCase.varName, testCase.varValue);
                     String result = templateEngine.generateMessage(template, null);
                     assertEquals(testCase.expected, result);
                  }
            ));
   }
}
