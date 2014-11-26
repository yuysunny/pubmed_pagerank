pubmed_pagerank
===============
This version is designed to run the pagerank calculation for a set of keywords
in a multi-threaded fashion.  The main routinre that is executed when the jar 
is run is the main routine in the ProcessPaperPageRank file.

That main has one optional parameter:
1) Path to the directory containing the pagerank.properties file.
   If a directory is provided, it will first be checked whether it
   is the full path to a directory.  If not, it will be checked whether
   it is a relative path within the user's working directory.  If no 
   directory is fount, or none was specified, the program then looks 
   for the pagerank.properties file in the user's working directory.

If the pagerank.properties file cannot be found (or cannot be opened as 
a properties file, the pagerank program will abort.

Within the pagerank.properties file you can set the following properties:
(all are described in the properties file).
1) process_dir - this is a full path to the directory used for all of the
                 other directories and files used or created.  If no 
                 property is provided for the process_dir, the program will
                 look for a directory named PageRank within the user's 
                 working directory.  If neither exists, the program 
                 will abort.
2) thread_cnt
3) node_file_name
4) edge_file_name 
5) node_score_dir
6) output_dir
7) output_prefix

Logging: The process_dir should contain the log4j.properties file that's 
         used to configure the logging. The default log4j.properties file 
         will write out the log as a file named pagerank.log.  The default 
         configuration for logging is to generate up to 10 rolling logs 
         (when the 11th log would be written, the oldest is deleted) and 
         each log file is up to 50MB.  In the log4j.properties file the 
         property named log4j.appender.fileAppender.File specifies the 
         name of the log file written and optionally the path.  The 
         default is a file named pagerank.log, but a different name 
         (or a full path) can be specified.  It is a rolling appender, 
         so if not deleted between runs, it will just append to the last 
         log from the prior run

         Although it does not log a lot of information per file at a 
         DEBUG threshold, For production, since there are 3,000 or  
         more keywords to process, we would recommend using the INFO 
         threshold.  If you only want error messages logged, set the 
         threshold to ERROR.

The ProcessPaperPageRank class handles a queue of pagerank jobs, where each 
prior score file in the node_score_dir directory is considered a job and 
the job consists of the file name, the keyword ID being processed, and the 
output file name (based on the keyword ID).  

That class also creates a set of workers, where each worker is an instance of
the MeshPaperPageRank class which implements the Runnable interface.  Each 
worker gets a job from the ProcessPaperPageRank, runs pagerank for the keyword
in that job, writes out the results, and then gets another job.  When there are
no more jobs, ProcessPaperPageRank will return null in response to a call to
getJob, and the worker will then clean up and shutdown.  When all of the
workers shutdown, the ProcessPaperPageRank will complete.

Each job generates a file in the PaperContributeToTopicoutput_dir 
subdirectory with the descriptor and qualifier for the keyword term.  
These files contain the pagerank output for each paper where the 
pagerank was not zero.

Each worker (MeshPaperPageRank) creates an instance of the PubMedGraph class
which extends SparseMultigraph<MyNode, MyLink>.  The graph class is passed the 
node and edge files and is responsible for creating its graph and maintaining
the hashmap of its nodes.

------------------------------------
Running the program:
------------------------------------
Assuming Java 7 is setup on the path, the jar file PubmedPagerank-0.0.2.jar
can be run as follows.  All of the jar files from the Jung2 2.0.1 installation 
should be copied into a directory along with the log4J jar (log4j-1.2.17.jar);
In these instructions that directory's path is referred to as pagerankjars.
Assuming all of the files are in a folder named pubmed in your home directory,
the command to run the program is:
java -cp /home/yourname/pubmed/PubmedPagerank-0.0.2.jar:/home/yourname/pubmed/pagerankjars/*: com.dlmu.pubmed.graph.ProcessPaperPageRank /home/yourname/pubmed/PageRank

In the above command, the pagerank.properties file is the following directory:
/home/yourname/pubmed/PageRank

Expected directory structure:
  process directory
     node score subdirectory
     output subdirectory
     node file
     edge file

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