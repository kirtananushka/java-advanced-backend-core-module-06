package com.epam.ld.module2.testing;

import com.epam.ld.module2.testing.template.Template;
import com.epam.ld.module2.testing.template.TemplateEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * The type Messenger.
 */
public class Messenger {
   private final MailServer mailServer;
   private final TemplateEngine templateEngine;
   private String inputFile;
   private String outputFile;

   /**
    * Constructor for Messenger
    *
    * @param mailServer     mail server instance
    * @param templateEngine template engine instance
    */
   public Messenger(MailServer mailServer, TemplateEngine templateEngine) {
      this.mailServer = mailServer;
      this.templateEngine = templateEngine;
   }

   /**
    * Sets the input and output files for file mode
    *
    * @param inputFile  path to input file
    * @param outputFile path to output file
    */
   public void setIOFiles(String inputFile, String outputFile) {
      this.inputFile = inputFile;
      this.outputFile = outputFile;
   }

   /**
    * Send message to specified client.
    *
    * @param client   client to receive message
    * @param template template to be processed
    */
   public void sendMessage(Client client, Template template) {
      try {
         String input;
         String messageContent;

         if (isFileMode()) {
            input = readFile(inputFile);
            template.addVariable("input", input);
            messageContent = templateEngine.generateMessage(template, client);
            writeFile(outputFile, messageContent);
         } else {
            try (BufferedReader reader = new BufferedReader(
                  new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
               input = reader.readLine();
               template.addVariable("input", input);
               messageContent = templateEngine.generateMessage(template, client);
               try (PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)) {
                  writer.println(messageContent);
               }
            }
         }

         mailServer.send(client.getAddresses(), messageContent);
      } catch (IOException e) {
         throw new RuntimeException("Error processing input/output", e);
      }
   }

   private boolean isFileMode() {
      return inputFile != null && outputFile != null;
   }

   String readFile(String path) throws IOException {
      StringBuilder content = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(Files.newInputStream(Paths.get(path)), StandardCharsets.UTF_8))) {
         String line;
         while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
         }
      }
      return content.toString().trim();
   }

   void writeFile(String path, String content) throws IOException {
      try (OutputStreamWriter writer = new OutputStreamWriter(
            Files.newOutputStream(Paths.get(path)), StandardCharsets.UTF_8)) {
         writer.write(content);
      }
   }
}
