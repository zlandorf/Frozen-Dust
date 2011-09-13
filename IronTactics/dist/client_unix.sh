#!/bin/bash

if [ $# = 1 ]
then
    java -jar -Djava.library.path=native/linux IronTactics.jar $1
else
    java -jar -Djava.library.path=native/linux IronTactics.jar    
fi


