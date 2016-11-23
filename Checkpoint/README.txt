1.create directory under machine dc01:
~/CS6378/Checkpoint

2.upload all files. Please make sure config.txt is in correct format.
scp * zxx140430@dc01.utdallas.edu:~/CS6378/Checkpoint
scp Application/* zxx140430@dc01.utdallas.edu:~/CS6378/Checkpoint/Application
scp Tool/* zxx140430@dc01.utdallas.edu:~/CS6378/Checkpoint/Tool
scp Test/* zxx140430@dc01.utdallas.edu:~/CS6378/Checkpoint/Test

3.compile
cd ~/CS6378/Checkpoint
javac Application/Driver.java

4.run script
./launcher-server.sh

5.test correctness
javac Test/Testing.java
java Test.Testing