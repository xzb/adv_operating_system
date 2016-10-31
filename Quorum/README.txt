1.create directory under machine dc01:
~/CS6378/Quorum

2.upload all files. Please make sure config.txt is in correct format.
scp * zxx140430@dc01.utdallas.edu:~/CS6378/Quorum
scp Application/* zxx140430@dc01.utdallas.edu:~/CS6378/Quorum/Application
scp Tool/* zxx140430@dc01.utdallas.edu:~/CS6378/Quorum/Tool
scp Test/* zxx140430@dc01.utdallas.edu:~/CS6378/Quorum/Test

3.compile
cd ~/CS6378/Quorum
javac Application/App.java

4.run script
./launcher-server.sh

5.test correctness
javac Test/TestCorrectness.java
java Test.TestCorrectness