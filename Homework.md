# Testing Module Practical Task

## Environment

- **Build Tool:** Gradle/Maven
- **Version Control:** Git
- **Testing Framework:** JUnit 5

## Task description

Messenger.

Using scratch project (/source/messenger.zip), it is necessary to create messenger that will pretend to send emails
using custom template generator.

The implementation of the template generator should be your own. You are not allowed to use any third-parties libraries.
Use TDD approach for template generator. Each step should be a separate commit.

Set of requirements for the mail template generator, you should create list of TDD tests for these requirements:

    The system replaces variable placeholders like #{subject} from a template with values provided at runtime. 

    If at least for one placeholder value is not provided at runtime – template generator should throw an exception 

    Template generator ignores values for variables provided at runtime that aren’t found from the template. 

    System should support values passed in runtime with #{…}. E.g. template is  “Some text: #{value}” and  at runtime #{value} passed as  #{tag}. Output should be “Some text: #{tag}”. 

    The system supports the full Latin-1 character set in templates and in variables. 

Messenger should work in two modes: console and file mode.

- In console mode the application takes expression from console and prints result to console. No application parameters
  should be specified to use this mode.

- In file mode application takes expression from file and output results to file. To use this mode user should specify
  input and output file names as application parameters.

## Tasks:

You should create tests for Messenger with the following condition (In scope of this task you can create more than one
test to cover the same functionality):

    1. TDD approach – (40 points)  

    2. @Parameterized runner  and Dynamic tests (6 points)  

    3. Implement meta annotations and filtering (6 points) 

    4. TemporaryFolder rule (6 points)  

    5. Mock reading from file/console (6 points)  

    6. Use partial mock (6 points)  

    7. Use spy (6 points)  

    8. Create custom extension (jUnit5) to output test execution information to file (6 points)  

    9. Using ExpectedException rule to check exceptions + Assertion mechanism (6 points)  

    10. Implement Disabling test on condition (2 points) 

    11. Test quality and adequate coverage will be assessed as (10 points) 

Don't forget about good tests, checkstyle and other staff that already included into your build phase

### Extra mile:

Write BDD tests for messenger. You may use any BDD framework (Spock, JBehave, Cucumber,…). Negotiate framework with your
mentor

 
