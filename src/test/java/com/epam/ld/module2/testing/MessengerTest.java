package com.epam.ld.module2.testing;

import com.epam.ld.module2.testing.extension.TestExecutionLogger;
import com.epam.ld.module2.testing.template.Template;
import com.epam.ld.module2.testing.template.TemplateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(TestExecutionLogger.class)
class MessengerTest {
   private MailServer mailServer;
   private TemplateEngine templateEngine;
   private Messenger messenger;

   @BeforeEach
   void setUp() {
      mailServer = mock(MailServer.class);
      templateEngine = mock(TemplateEngine.class);
      messenger = new Messenger(mailServer, templateEngine);
   }

   @Test
   void shouldSendMessageInConsoleMode() {
      String input = "Test input";
      ByteArrayInputStream inStream = new ByteArrayInputStream(
            input.getBytes(StandardCharsets.UTF_8));
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      PrintStream originalOut = System.out;
      InputStream originalIn = System.in;

      try {
         System.setIn(inStream);
         // Fix for Java 8
         System.setOut(new PrintStream(outStream, true, StandardCharsets.UTF_8.name()));

         // Given
         Template template = new Template("Test template");
         Client client = new Client();
         when(templateEngine.generateMessage(template, client)).thenReturn("Generated message");

         // When
         messenger.sendMessage(client, template);

         // Then
         verify(mailServer).send(client.getAddresses(), "Generated message");
         assertTrue(outStream.toString(StandardCharsets.UTF_8.name()).trim()
               .contains("Generated message"));
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException("UTF-8 is not supported", e);
      } finally {
         System.setOut(originalOut);
         System.setIn(originalIn);
      }
   }

   @Test
   void shouldSendMessageInFileMode(@TempDir Path tempDir) throws IOException {
      File inputFile = tempDir.resolve("input.txt").toFile();
      File outputFile = tempDir.resolve("output.txt").toFile();

      // Fix: Use OutputStreamWriter with explicit encoding instead of FileWriter
      try (OutputStreamWriter writer = new OutputStreamWriter(
            Files.newOutputStream(inputFile.toPath()), StandardCharsets.UTF_8)) {
         writer.write("Test input");
      }

      // Given
      Template template = new Template("Test template");
      Client client = new Client();
      when(templateEngine.generateMessage(template, client)).thenReturn("Generated message");

      // Set file mode
      messenger.setIOFiles(inputFile.getPath(), outputFile.getPath());

      // When
      messenger.sendMessage(client, template);

      // Then
      verify(mailServer).send(client.getAddresses(), "Generated message");
      String outputContent = new String(Files.readAllBytes(outputFile.toPath()),
            StandardCharsets.UTF_8);
      assertEquals("Generated message", outputContent.trim());
   }

   @Test
   void shouldInterceptFileOperations() throws IOException {
      MailServer mockedMailServer = mock(MailServer.class);
      TemplateEngine mockedTemplateEngine = mock(TemplateEngine.class);
      Messenger messengerSpy = spy(new Messenger(mockedMailServer, mockedTemplateEngine));

      String generatedContent = "generated content";
      when(mockedTemplateEngine.generateMessage(any(Template.class), any(Client.class))).thenReturn(generatedContent);
      doReturn("test content").when(messengerSpy).readFile(anyString());
      doNothing().when(messengerSpy).writeFile(anyString(), anyString());

      Template template = new Template("test");
      Client client = new Client();
      messengerSpy.setIOFiles("input.txt", "output.txt");
      messengerSpy.sendMessage(client, template);

      verify(messengerSpy).readFile("input.txt");
      verify(messengerSpy).writeFile("output.txt", generatedContent);
      verify(mockedTemplateEngine).generateMessage(template, client);
   }

   @Test
   public void shouldTrackTemplateUsage() {
      MailServer mailServer = mock(MailServer.class);
      TemplateEngine templateEngine = mock(TemplateEngine.class);
      Messenger messenger = new Messenger(mailServer, templateEngine);
      Client client = new Client();
      Template template = spy(new Template("Template text"));

      messenger.sendMessage(client, template);

      // Only verify the interactions you care about
      verify(mailServer).send(any(), any());
      verify(templateEngine).generateMessage(eq(template), eq(client));
   }
}
