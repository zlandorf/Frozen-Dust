@echo off

if (%1)==() java -jar -Djava.library.path=native/windows bin/IronTactics.jar

if not (%1)==() java -jar -Djava.library.path=native/windows bin/IronTactics.jar %1
