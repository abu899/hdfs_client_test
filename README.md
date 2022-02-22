# hdfs_client_test

## Build images

At hdfs_docker/base
- docker build -t hadoop-base:3.3.1 .

At hdfs_docker/namenode
- docker build -t hadoop-namenode:3.3.1 .

At hdfs_docker/datanode
- docker build -t hadoop-datanode:3.3.1 .

## Use docker only

---

### In Host

1. docker run -it -d -h namenode --name namenode -p 50070:50070 -p 8088:8088 -p 9000:9000 -p 9866:9866 hadoop-namenode:3.3.1
2. docker run -it -d -h datanode --name datanode --link namenode:namenode hadoop-datanode:3.3.1
---

### In Container (namenode, datanode)

3. docker inspect datanode | grep IPAddress (172.xxx.xxx.xxx)
4. docker exec -it namenode /bin/bash (namenode)
   1. nano /etc/hosts
   2. 3번에서 얻은 ip address 추가 (172.xx.xx.xxx    datanode)
5. service ssh start (both)
6. cd /etc/hadoop (namenode)
   1. nano hadoop-env.sh
   - export HDFS_NAMENODE_USER=root
   - export HDFS_DATANODE_USER=root
   - export HDFS_SECONDARYNAMENODE_USER=root
   - export JAVA_HOME="/usr/lib/jvm/zulu8-ca-amd64"
7. start-all.sh in namenode


## Use docker compose
1. docker-compose up
2. docker inspect datanode | grep IPAddress (172.xxx.xxx.xxx)
3. docker exec -it namenode /bin/bash (namenode)
   1. nano /etc/hosts
   2. 2번에서 얻은 ip address 추가
4. service ssh start (namenode, datanode)
5. start-all.sh