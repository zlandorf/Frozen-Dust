Copyright (c) 2011 
Paul Milian

contact : paul [.] milian (at) hotmail [.] fr


Install process :

- Just git clone the repo and try running it through eclipse.
- If the client can't connect to the server try changing the value of HOST constant in IronConst (or check the program's arguments)
- If the client wont launch with a lwjgl error, try changing the native value for lwjgl.jar and setting to it to the proper OS
- If the Ant file won't build you must go to Eclipse => window => preference => Ant => runtime => ant home entries => add jar and add JeraAntTasks.jar
(the jar should be in the IronTactics/jar folder)

todo: fog of war, campaign, ai

