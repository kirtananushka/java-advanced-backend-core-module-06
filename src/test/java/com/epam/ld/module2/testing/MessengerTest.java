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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
      ByteArrayInputStream inStream = new ByteArrayInputStream(input.getBytes());
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      PrintStream originalOut = System.out;
      InputStream originalIn = System.in;

      try {
         System.setIn(inStream);
         System.setOut(new PrintStream(outStream));

         // Given
         Template template = new Template("Test template");
         Client client = new Client();
         when(templateEngine.generateMessage(template, client)).thenReturn("Generated message");

         // When
         messenger.sendMessage(client, template);

         // Then
         verify(mailServer).send(client.getAddresses(), "Generated message");
         assertTrue(outStream.toString().trim().contains("Generated message"));
      } finally {
         System.setOut(originalOut);
         System.setIn(originalIn);
      }
   }

   @Test
   void shouldSendMessageInFileMode(@TempDir Path tempDir) throws IOException {
      File inputFile = tempDir.resolve("input.txt").toFile();
      File outputFile = tempDir.resolve("output.txt").toFile();

      try (FileWriter writer = new FileWriter(inputFile)) {
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
      String outputContent = new String(java.nio.file.Files.readAllBytes(outputFile.toPath()));
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
   void shouldTrackTemplateUsage() {
      // Given
      Template templateSpy = spy(new Template("#{value}"));
      TemplateEngine engine = new TemplateEngine();

      // When
      templateSpy.addVariable("value", "test");
      engine.generateMessage(templateSpy, new Client());

      verify(templateSpy, times(1)).getTemplateText();
      verify(templateSpy, times(4)).getVariables();
   }
}
