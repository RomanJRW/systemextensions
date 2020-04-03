# System Extensions

These extensions were built as a JUnit5 equivalent for some of the functionality provided by the popular 
[System Rules](https://stefanbirkner.github.io/system-rules/) library. The port to JUnit5 extensions is WIP there, so 
this acts as a go between, borrowing a good chunk of the currently implemented logic, but omitting the fluent builder. 
There are no tests at the moment for any of the logic here. 

The extensions included here are:
 
- `SystemPropertiesExtension`: for modifying System Properties. It reverts all modifications after each test. 
- `EnvironmentVariablesExtension`: for modifying Environment Variables. It reverts all modifications after each test.
