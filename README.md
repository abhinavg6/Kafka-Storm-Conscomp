Local-Storm-Repository
======================

A simple storm topology to be run on local IDE (Eclipse/IntelliJ). It reads from a input file containing meetup events, maps those to a set of technology categories, and prints the counts per category to an output file every n ms.

Please download latest version of maven to run mvn commands from command-line, or import it as a maven project in your IDE (provided maven plug-in is present). Please run "mvn clean install" and "mvn eclipse:eclipse" if you're running from a command line, and then import the project in your IDE.

Once the project is setup in IDE, you may run the class MeetupStormTopology as a Java application, and storm should do its magic via local cluster.
