# Conway's Game of Life

Navigate to the scripts folder to find two ```.cmd``` files. The batch file called ```run_program.cmd```
will run the application on a Windows system. The other will generate Javadocs
for the application code. If you are on a different system and choose to run the program, you must compile 
all code and run the command ```java GUI``` while in the ```src/code/driver``` directory.

## Note
The game is not playable until you first load in a text file to build the grid. If you navigate to
the ```samples``` folder you can test with sample input files. Settings such as output file
name pattern, output file location, color, row count, column count, and the starting grid
are saved to a ```_CONFIG_.txt``` file. These settings will persist between application sessions.

* See the help menu in the application for additional instructions

## Example Input
```
5, 5
1, 0, 1, 0, 0
1, 1, 1, 0, 1
0, 1, 1, 0, 1
0, 0, 1, 1, 0
1, 0, 1, 0, 0
```
