# Install java 11 resources (for wsl) from local website download:

Solved my last problem:

```
E.g.:
sudo mkdir -p /var/cache/oracle-jdk11-installer-local
sudo cp jdk-11.0.4_linux-x64_bin.tar.gz /var/cache/oracle-jdk11-installer-local/
sha256sum mismatch jdk-11.0.13_linux-x64_bin.tar.gz
Oracle JDK 11 is NOT installed.
dpkg: error processing package oracle-java11-installer-local (--configure):
 installed oracle-java11-installer-local package post-installation script subprocess returned error exit status 1
Errors were encountered while processing:
 oracle-java11-installer-local
E: Sub-process /usr/bin/dpkg returned an error code (1)
```

https://www.linuxquestions.org/questions/linux-software-2/sha256sum-mismatch-jdk-11-0-5_linux-x64_bin-tar-gz-4175664982/

## Here are the steps:

Download the JDK:
`sudo apt install default-jdk`
`sudo update`

Download the JRE
`sudo apt install default-jre`
`sudo update`

Download a ppa:
[Note: I've also tried ppa:webupd8team but it failed so I had to run
`sudo add-apt-repository -r ppa:webupd8team/java`]

`sudo apt install software-properties-common`
`sudo add-apt-repository ppa:linuxuprising/java`

Check if this installs, if not proceed:
`sudo apt install oracle-java11-installer`

Make dir (for linux command to find the tar.gz file)
sudo mkdir /var/cache/oracle-jdk11-installer-local/

Download: (jdk-11.0.15_linux-aarch64_bin.tar.gz) here --> https://www.oracle.com/au/java/technologies/javase/jdk11-archive-downloads.html

cd into your local Downloads dir and cp the tar.gz file
`sudo cp jdk-11.0.15_linux-x64_bin.tar.gz /var/cache/oracle-jdk11-installer-local/`

`sudo rm /var/lib/dpkg/info/oracle-java11-installer-local.postinst -f`

`sudo apt-get install oracle-java11-installer-local`

`java --version`

Sources:
Main: https://phoenixnap.com/kb/how-to-install-java-ubuntu
https://www.linuxuprising.com/2019/06/new-oracle-java-11-installer-for-ubuntu.html

# Get HADOOP working:

Follow the guide: https://webcms3.cse.unsw.edu.au/static/uploads/course/COMP9313/22T2/52663a121dc657d694c185569ddb6982f6d6a7394f3e5abcf151581f6617830b/Lab_1.pdf

Make sure you add the code snippets between the <configuration><configuration/> tags

# !!! IMPORTANT !!!

Make sure the ssh server is running AND check that it is installed with:
`sudo apt list --installed | grep openssh-server`
Check if running:
`sudo service ssh status`
If you get an error or some response like:
` * sshd is not running`
Run the service:
` sudo service ssh start`

If you get the error like here: https://stackoverflow.com/questions/68077905/getting-error-permission-denied-publickey-password-after-start-dfs-sh:
Please run the commands:

```
sudo apt install ssh
# Only run below command if you are fine with overriding/don't have a RSA key
# ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 0600 ~/.ssh/authorized_keys
```

## SET THE ENV VARS (add in ~/.bashrc file):

```
export HDFS_NAMENODE_USER="root"
export HDFS_DATANODE_USER="root"
export HDFS_SECONDARYNAMENODE_USER="root"
export YARN_RESOURCEMANAGER_USER="root"
export YARN_NODEMANAGER_USER="root"
```

`start-dfs.sh and stop-dfs.sh` will run and stop hdfs

Run `jps` to make sure Hadoop has started correctly

### Check the HADOOP server health, visit link:

`http://localhost:9870/dfshealth.html#tab-overview`

### MapReduce:

Check out some commands by running:
`hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.2.jar`

#### Examples:

1. An example command which runs estimation of Pi with 16 maps and 10,000 samples:
   `hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.2.jar pi 16 10000`

2. Search for all the strings starting with ‘dfs’ in the xml files
   `hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.2.jar grep /user/comp9313/input /user/comp9313/output 'dfs[a-z.]+'`

   - Copy output files from DFS to local filesystem with command:
     ` hdfs dfs -get /user/comp9313/output output`
   - Check files
     `cat output/*`
     ...

3. `hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.2.jar wordcount <input_file> <output_file>`
   - Will need to delete our output file first, run ` hdfs dfs -rm output/*` and then `hdfs dfs -rmdir output` OR `hdfs dfs -rm -r output`

### YARN:

## Using MRJob =:

Commands which are helpful.
Create a file e.g. called mydata.txt with a bunch of lines which contains words

```
# Create a directory to contain input data
hdfs dfs -mkdir hdfs://localhost:9000/user/comp9313/input
# Move the local data file to the dfs
hdfs dfs -put mydata.txt hdfs://localhost:9000/user/comp9313/input/
# Run the wordcount project on that input data with -r hadoop specified
python3 wordcount.py -r hadoop hdfs://localhost:9000/user/comp9313/input/mydata.txt
```

## Submitting stuff:

cp z5207998_proj1.zip ~/../mnt/c/Users/admin/Desktop/comp9313/

## Setup SPARK / SCALA

Link: https://downloads.apache.org/spark/spark-3.3.0/
(We will need to wget https://downloads.apache.org/spark/spark-3.3.0/spark-3.3.0-bin-hadoop3.tgz)
Follow the guide here: https://kontext.tech/article/560/apache-spark-301-installation-on-linux-guide

After the installation (and outside the scala shell); run example `run-example SparkPi 10` to see spark in action

## Get SBT (bild and package scala app)

```
sudo apt-get update
sudo apt-get install apt-transport-https curl gnupg -yqq
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo -H gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import
sudo chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg
sudo apt-get update
sudo apt-get install sbt
```

Source:
https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html

## Run the spark app:

NOTE: Remember to first put your files in the HDFS file system in 'project2' dir
e.g. `hdfs dfs -put tiny-doc.txt project2`
Note: Followed this guide --> https://spark.apache.org/docs/latest/quick-start.html

- First build the app with `sbt package`, then run:

```
$SPARK_HOME/bin/spark-submit --class "SimpleApp" --master local[4] target/scala-2.12/simple-project_2.12-1.0.jar
```

For our assignment, problem1:

```
spark-submit --class "Problem1" --master local[4] target/scala-2.12/problem-1_2.12-1.0.jar
```
