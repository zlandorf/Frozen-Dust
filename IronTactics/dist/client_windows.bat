@echo off

if (%1)==() "jvm\java" -jar -Djava.library.path=native/windows IronTactics.jar

if not (%1)==() "jvm\java" -jar -Djava.library.path=native/windows IronTactics.jar %1
