# seereceive
Interface for the Seesaw

the master branch, and seesaw.jar in particular, should work for all of the operating systems: ubuntu/mac/windows

## Instructions

Create a file `port.properties` with the line

    port=<port of your device>

For instance on my ubuntu this is

    port=/dev/ttyACM0

Then run

    java -jar seesaw.jar

After the collection of 3000 data points, the software terminates.
Three files will be created in

    <home directory>/seesawState/<timestamp>/*

## Instructions (Quickstart)

Append the port name as a command line argument

    java -jar seesaw.jar <port of your device>

On my ubuntu this is

    java -jar seesaw.jar /dev/ttyACM0
