# COMP9313 22T2 Project 2:

### Problem 1:

In this problem, we are still going to use the dataset of Australian news from
ABC.

Your task is to find out the top-k most frequent co-occurring term pairs.

- The co-occurrence of (w, u) is defined as: u and w appear in the same article
  headline (i.e., (w, u) and (u, w) are treated equally).

#### Output:

We have to generate k-key value pairs in descending order (on descending order based on freq)

- Keys are the pair of terms (co-occurent terms), ordered alphabetically
- Values are the co-occurring frequencies

The keys and values are seperated by \t
If 2 pairs have the same frequencies, sort them alphabetically then output

E.g. Given k=3 and the sample dataset, the output would be

```
coronavirus,economy\t2
council,welcomes\t2
cowboys,eels\t2
```

#### Format:

Package should be named `comp9311.proj2` and the file as `Problem1.scala` and the object
as Problem1. Store output as a txt file on disk.

- Your code should take 4 arg params; k, stop words file, input text file and output folder

#### Run the program with:

```
spark-submit --class "Problem1" --master local[4] target/scala-2.12/problem-1_2.12-1.0.jar 3 "project2/stopwords.txt" "project2/tiny-doc.txt" "project2/output"
```

Check results here:
hdfs dfs -ls ./project2
hdfs dfs -cat project2/output/part-00000

Delete the generated output folder:
`hdfs dfs -rmr -r project2/output`

### Problem 2:

Given a directed graph, for each vertex v, compute the number of vertices that
are reachable from v in the graph (including v itself if there is a path starting
from v and ending at v).

For example, for node 0, the number of vertices that
are reachable from 0 is 6, since there exists a path from node 0 to each node in
the graph

#### Input and output:

_Inputs_

```
0 0 1
1 0 3
2 1 2
3 1 4
4 1 5
5 2 1
6 2 3
7 3 0
8 3 5
9 4 0
10 4 5
11 5 2
```

_Output_

```
0:6
1:6
2:6
3:6
4:6
5:6
```

#### Format:

Package should be named `comp9311.proj2` and the file as `Problem1.scala` and the object
as Problem1. Store output as a txt file on disk.

- Your code should take 2 arg params; Input graph file, output folder
