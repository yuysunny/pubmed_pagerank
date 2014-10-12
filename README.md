pubmed_pagerank
===============
This version is designed to run the pagerank calculation for a set of keywords
in a multi-threaded fashion.  The main routinre that is executed when the jar 
is run is the main routine in the ProcessPaperPageRank file.

That main has one required and one optional parameter:
1) Number of threads.  This determines the number of worker threads that
                       will be created.  Be sure to allow some threads for 
                       handling garbage collection in Java, or you will
                       throw an exception in the Jung library. this 
                       calculation is very CPU intensive.  The threads
                       running the calculations will be at 100% most of
                       the run.
2) Optional directory:  This is the path to the directory for the following:
    a) the csv input files for the nodes and edges of the grap
    b) the sub-directory contianing the prior score files for keywords
    c) the log4j.properties file for configuring the logging
    d) the output file directory will be written 
If the optional path is not provided, the current working directory will be 
assumed to be the directory where these files are found.

The default log4j.properties file will write out the log as a file named 
pagerank.log.  The default configuration for logging is to generate up to
10 rolling logs (when the 11th log would be written, the oldest is deleted) 
and each log file is up to 50MB.

Although it does not log a lot of information per file at a DEBUG threshold, 
For production, since there are 3,000 or more keywords to process, we would
recommend using the INFO threshold.  If you only want error messages logged,
set the threshold to ERROR.

The ProcessPaperPageRank class handles a queue of pagerank jobs, where each 
keyword prior score file in thePaperRelevanceInTopic directory is considered
a job and the job consists of the file name, the keyword ID being processed,
and the output file name (based on the keyword ID).  

That class also creates a set of workers, where each worker is an instance of
the MeshPaperPageRank class which implements the Runnable interface.  Each 
worker gets a job from the ProcessPaperPageRank, runs pagerank for the keyword
in that job, writes out the results, and then gets another job.  When there are
no more jobs, ProcessPaperPageRank will return null in response to a call to
getJob, and the worker will then clean up and shutdown.  When all of the
workers shutdown, the ProcessPaperPageRank will complete.

Each job generates a file in the PaperContributeToTopic subdirectory with 
the descriptor and qualifier for the keyword term.  These files contain the 
pagerank output for each paper where the pagerank was not zero.

Each worker (MeshPaperPageRank) creates an instance of the PubMedGraph class
which extends SparseMultigraph<MyNode, MyLink>.  The graph class is passed the 
node and edge files and is responsible for creating its graph and maintaining
the hashmap of its nodes.

------------------------------------
Running the program:
------------------------------------
Assuming Java 7 is setup on the path, the jar file PubmedPagerank-0.0.1.jar
can be run as follows.  All of the jar files from the Jung2 2.0.1 installation 
should be copied into a directory along with the log4J jar (log4j-1.2.17.jar);
In these instructions that directory's path is referred to as pagerankjars.
Assuming all of the files are in a folder named pubmed in my home directory,
the command to run the program is:
java -cp /home/scjensen/pubmed/PubmedPagerank-0.0.1.jar:/home/scjensen/pubmed/pagerankjars/*: com.dlmu.pubmed.graph.ProcessPaperPageRank 3 /home/scjensen/pubmed/pagerank

In the above run, we are asking to use 3 threads and the directory with the
datafiles for the graph, the log4j.properties file, and the subdirectories 
with the keyword prior score files and the output directory are in:
/home/scjensen/pubmed/pagerank


------------------------------------
Other Classes Used In This Project:
------------------------------------
Utility - this is used to setup the Log4j logging
MyNode - wrapper around a node in the graph
MyLink - an edge in the graph
PubmedPagerankException - extension of the Java Exception class used when 
throwing an exception in this program
------------------------------------
Currently Unused Code:
------------------------------------
There are also the following legacy classes within the project which are not
currently used in the multi-threaded version for the paper pagerank:
MyGraph
MyPaperGraph
PaperPageRank
VolumePageRank