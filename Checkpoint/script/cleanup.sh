#!/bin/bash


# Change this to your netid
netid=zxx140430

#
# Root directory of your project
PROJDIR=$HOME/CS6378/Checkpoint

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$PROJDIR/config.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR

#
# Your main project class
#
PROG=Application.Driver

n=1

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | grep "\s*[0-9]\+\s\+\w\+\..*" |
(
    #read i
    #echo $i
    while read line
    do
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        ssh $netid@$host killall -u $netid &
        sleep 1

        n=$(( n + 1 ))
    done

)


echo "Cleanup complete"

