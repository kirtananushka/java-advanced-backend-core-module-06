package com.epam.ld.module2.testing.extension;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class TestExecutionLogger implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
   private static final String LOG_FILE = "test-execution.log";

   @Override
   public void beforeTestExecution(ExtensionContext context) throws Exception {
      logMessage("Starting test: " + context.getDisplayName() + " at " + LocalDateTime.now());
   }

   @Override
   public void afterTestExecution(ExtensionContext context) throws Exception {
      logMessage("Finished test: " + context.getDisplayName() +
            " Status: " + (context.getExecutionException().isPresent() ? "FAILED" : "PASSED") +
            " at " + LocalDateTime.now());
   }

   private void logMessage(String message) {
      try {
         Files.write(
               Paths.get(LOG_FILE),
               (message + "\n").getBytes(StandardCharsets.UTF_8),
               StandardOpenOption.CREATE,
               StandardOpenOption.APPEND
         );
      } catch (IOException e) {
         System.err.println("Failed to write to log file: " + e.getMessage());
      }
   }
}