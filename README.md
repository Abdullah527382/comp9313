Install java 11 resources (for wsl) from local website download:

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
