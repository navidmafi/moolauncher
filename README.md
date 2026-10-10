# MOOLauncher

Simple and effective cross-platform offline Minecraft launcher that doesn't spy on you. Powered by cows. Requires JRE 21.

![image](https://github.com/user-attachments/assets/3b1af5fb-e52a-4415-9e9a-db5e8a5275d1)


## Devlopment 
Use the gradle wrapper, no need for a system install anymore.

`./gradlew run` compiles and runs the launcher.

`./gradlew build` builds a jar file that can only be used on the compile host OS.

`./gradlew shadowJar` builds an uber-jar that can be run everywhere theoretically.

Several things done here deviate from standard Java and Mojang practices. For instance downloading fabric jars from maven could probably be done better but my knowledge of the Java ecosystem is limited. That said, I tried to make sure nothing unholy happens under the hood and tried my best to structure things in a sensible manner.
