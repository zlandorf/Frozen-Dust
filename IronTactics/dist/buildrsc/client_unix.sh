#!/bin/bash

if [ $# = 1 ]
then
    java -jar -Djava.library.path=native/linux bin/IronTactics.jar $1
else
    java -jar -Djava.library.path=native/linux bin/IronTactics.jar    
fi


