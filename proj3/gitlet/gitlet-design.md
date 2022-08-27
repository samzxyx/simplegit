# Gitlet Design Document

**Name**: Xuanyi Zhang

## Classes and Data Structures
###Main

This class reacts to user inputs and calls specific methods in the command class. 

    HashMap forward: A map representing the permute mapping.
    HashMap backward: A map representing the inverse mapping.

###CommandClass

This class execute the actual commands.

    HashMap removall: Maps the name of the files in the remove stage to its blob id.
    HashMap backward: A map representing the inverse mapping.

###Commit

Defines the commit data structure. A commit should have a parent commit, 
a hashmap that all file and blob pair that are stored in this commit, and some metadata. 


    String message: the message that the user input when committing.
    String date: the timestamp of the commit.
    HashMap<String,String> filestracking: the files that is stored in this current commit,
                                        including the files tracked in its parent commit, and
                                        the ones in the staging addition area. 
    String parent_id: the commit previous to this one.

###GitletRepo

Consist of a staging area and a commits area. 

    Rotor[] allRotors: An array representing all rotors of this machine can use.


## Algorithms

Commit Class

    Commit(): The class constructor. Save the state of the Permutation and populate the forward and backward HashMaps to contain the permutations and inversions described by our cycles. First, separate the cycles string into individual cycles. For each character of each cycle, determine the forward mapping (look at the next character or wrap forward to the first character) and the backward mapping (look at the previous character or wrap backward to the last character).vAny characters without a mapping in forward or backward will map to itself.

Machine Class

    insertRotors(String[] rotors): Compare each name in rotors to the rotors in allRotors. If the names are equal, put that rotor in myRotors.
    setRotors(String setting): For each rotor besides the reflector, set rotor’s setting to the corresponding letter.


## Persistence
[Note: This section was not required for Project 1, but is necessary for Project 3. For the purposes of demonstrating how you might fill this section in your own design document, imagine that Project 1 also required keeping track of the state of the Machine after each call to enigma.Main. That is, imagine we execute:

java -ea enigma.Main [configuration file] [input] [output]

We would want to be able to encrypt another input file without providing a configuration file. That is, the settings we provided in the first run should persist across multiple executions of the program.]

In order to persist the settings of the machine, we will need to save the state of the rotors after each call to the enigma machine. To do this,

    Write the Permutation HashMaps to disk. We can serialize them into bytes that we can eventually write to a specially named file on disk. This can be done with writeObject method from the Utils class.
    Write all the Rotor objects to disk. We can serialize the Rotor objects and write them to files on disk (for example, “myRotor1” file, “myRotor2” file, etc.). This can be done with the writeObject method from the Utils class. We will make sure that our Rotor class implements the Serializable interface.

In order to retrieve our state, before executing any code, we need to search for the saved files in the working directory (folder in which our program exists) and load the objects that we saved in them. Since we set on a file naming convention (“myRotor1”, etc.) our program always knows which files it should look for. We can use the readObject method from the Utils class to read the data of files as and deserialize the objects we previously wrote to these files.
