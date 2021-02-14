# NOTE
This has been deprecated in favor of https://github.com/ReflxctionDev/cub, a platform-independent, much more powerful annotation-based commands framework

[![](https://jitpack.io/v/ReflxctionDev/BukkitCommands.svg)](https://jitpack.io/#ReflxctionDev/BukkitCommands)
# BukkitCommands
**BukkitCommands** is a simple commands framework which simplifies the creation of sub-commands. No more boilerplate!

 - An annotation, **PluginSubcommand**, which contains all data about a specific command. No more redundant constructor creation
 - **Resolvers!** Instead of doing your standard ways to retrieve objects from arguments, checking their validity, doing blabla if not valid, then actually using the object, now you only access it directly!
 ```java
 Player player = context.resolve(context.getArgs()[0], Player.class);
 ```  
 No validation checks, as these are all done in the background! (Of course, behavior is customizable)
 * Tab completions through references! You can have static tab completions, or context-based tab completions
 ```java
 tabCompletions = "suggestion1|2ndsugg|3rd|4th @players",
 ```
 * Built-in Java primitives support
 * Ability to add custom actions when an invalid command is given, a command has an invalid usage, or an invalid argument is inputted.
 * Much much more!
