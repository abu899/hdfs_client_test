#!/bin/bash

NAME_DIR=$1
echo $NAME_DIR

if [ "$(ls -A $NAME_DIR)" ]; then
  echo "NameNode is already formatted."
else
  echo "Format NameNode."
  $HADOOP_HOME/bin/hdfs --config $HADOOP_CONF_DIR namenode -format
fi

$HADOOP_HOME/bin/hdfs --config $HADOOP_CONF_DIR namenode