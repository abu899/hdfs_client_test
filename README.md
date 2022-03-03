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

## Error 정리

### Java ReadUTF()

`HdfsClientConnectorTest.java` 의 `readSimpleTextTest`와 `readWriteWithJavaTest` method 참조

- `readSimpleTextTest`의 read 대상이 되는 파일은 linux 상에서 직접 hdfs를 통해 put 해준 데이터이다.
- 이 파일을 `readUTF()`를 통해 읽게 되면 `EOFException`이 발생하게 된다
- Linux에는 String 타입이 존재하지 않기 때문에 `readUTF()`로 읽게되면 String 데이터가 존재하지 않기 때문에 Exception이 나온다.
- 이런 경우 byte 형태로 한 글자씩 읽은 후 char로 변환하는 방법과 `ByteBufferReader`를 통해 읽을 수 있다
- 반면 `readWriteWithJavaTest`에서는 자바로 String 형태로 데이터를 입력해주기 때문에 `readUTF()`로 데이터를 읽어도
오류가 발생하지 않는다!!

### Hadoop - Docker datanode access timeout or fail

- Client에서 hadoop을 접근할 때, timeout이나 fail이 되는 경우가 존재하는데 에러를 보면 접근하는 ip가 docker 내부 ip인 경우가 존재한다
```text
WARN impl.BlockReaderFactory: I/O error constructing remote block reader.
org.apache.hadoop.net.ConnectTimeoutException: 60000 millis timeout while waiting for channel to be ready for connect. 
ch : java.nio.channels.SocketChannel[connection-pending remote=/172.17.0.2:9866]
at org.apache.hadoop.net.NetUtils.connect(NetUtils.java:589)
```
- `connection-pending remote=/172.17.0.2:9866`와 같이 도커 내부에서 설정한 ip가 나오게되는데, 원래 같은 경우
접속을 원하는 datanode로 접근해야 데이터를 읽어올 수 있게 된다
- 이런 경우 `dfs.client.use.datanode.hostname` 를 `true`로 설정해줘서 client가 datanode를 hostname으로 접근하게 바꿔준다.
- 이때 `/etc/hosts`에 datanode의 주소가 mapping되어 있지 않으면 `UnKnownHostException`이 발생할 수 있으니 참고해야 한다

### UnKnownHostException

- 앞서 설명한 timeout 및 fail 이외에 namenode 접근시에도 발생할 수 있는 오류로 로그상 접근하려는 주소가 hostname으로 되어있지만,
정작 hostname의 주소가 mapping되어 있지 않은 경우 발생했다
- `/etc/hosts`에 namenode의 hostname과 ip주소를 넣어주자!

### NoRouteToHostException

구글에서 찾아본 결과 여러가지 상황에서 발생했지만, 개발 중에는 두 가지를 시도하였고 해결되었다.

1. firewall
   - 방화벽에서 특정 port로 들어오는 패킷자체를 막아버리는 경우
   - 이 경우 namenode나 datanode이 들어오는 port를 확인하고 port를 오픈해주는 방법이 존재한다
   - 아니면 그냥 방화벽을 내려버리면 되지만.. 이 방법은 크게 추천하지 않고 테스트할 때 사용하는게 좋을 것 같다
   - `iptables` 또는 `firewall-cmd`를 통해 해결해보자

2. Docker 및 route 확인
   - 동료 개발자가 이 문제로 `NoRouteToHostException` 문제가 생겼다
   - Route는 루트가 아닌 라우터로, path 문제가 아니라는 점을 인지해야했다.
   - 문제 발생 상황 당시, 개발자의 pc에 docker container가 올라가 있는 상태로서 docker의 ip로 라우팅 되고 있었다
   - 즉, docker로 route된 패킷이 필터링되어 버리면서 문제가 생긴 것으로 유추된다.
     - docker contatiner를 모두 내리고 `ifconfig docker0 down`을 실행하니 정상 동작되었다.
   - 비슷한 라우팅 문제로 에러가 발생한 글이 있어 참조 남긴다
     - [RoutingError Post](https://dd00oo.tistory.com/entry/java-NoRouteToHost-%ED%98%B8%EC%8A%A4%ED%8A%B8%EB%A1%9C-%EA%B0%88-%EB%A3%A8%ED%8A%B8%EA%B0%80-%EC%97%86%EC%9D%8C-%EC%97%90%EB%9F%AC)

### BlockMissingError

개발 중에 서버를 올리고 내리면서 가장 빈번하게 발생했던 error. 여러가지 원인이 존재하는 것 같지만 겪어본 내용들만 정리해보았다.

1. File not found
   - 단순하게 파일의 경로를 잘못 입력했을 때.
   - block missing error는 아니지만 block의 위치를 아예 찾지 못하는 것이므로 같이 정리해보았다.
   - 단순 로그 확인만으로도 진단 가능하고, 경로 재조정만 해주면 된다

2. Namenode가 block의 위치를 정상적으로 가져오지만, 실제 데이터는 읽을 수 없을 때.
   - Missing Block
      - namenode와 datanode를 올리고 내리다 보면 발생했던 error.
      - namenode에 기록되어있는 block의 위치와 실제 datanode의 위치가 다를 때 발생
        - `hdfs dfsadmin -report`를 통해 확인할 수 있다
      - namenode를 foramt하고 다시 파일을 넣어 block 위치를 맞춰줘야한다
      - `hdfs namnode -format`

  - datanode 접근 error
    - Missing block이 없고 block의 위치와 datanode 내 기록된 block 정보도 올바른데 못 읽어오는 경우
    - 정확히 어떤 문제인지 진단하기쉽지 않았던 error
    - 시도했던 방법
      - `UnKnownHostException`와 마찬가지로 namenode에 datanode의 주소를 mapping 해준다
      - `service ssh start` ssh가 실행이 중지된 경우에도 발생
      - docker로 hdfs를 구현할 시, 간혹 datanode 접근 시 conatiner id로 접근하려고 하는 경우,
      datanode의 hostname을 datanode의 이름으로 변경 필요
      - datanode의 ipc port가 제대로 열려있는지 확인
      - hdfs-site.xml에서 configuration 변경
        - `dfs.datanode.use.datanode.hostname`, `dfs.client.use.datanode.hostname`, 기타 등등..
        
### Safe mode

간혹 코드로 접근 시, safe mode에 따라 write 작업이 동작하지 않는 경우 발생한다. 인터넷에 따르면 세가지 이유가 있는데 다음과 같다.
1. Namenode 시작 시
   - 블록 복제수가 일정 수준을 만족할 때 safe mode가 해제 되는데, 만약 블록 리포트를 받는데 시간이 걸리면 safe mode에 머문다고 한다
2. Namenode 디스크 공간 부족
3. 관리자가 safe mode를 on 시킬 때

ps. Namenode를 동작시키고 정상적으로 write, read를 한 후, 일정 시간이 지난 후에 write가 안되는 문제가 발생했다.
1,2,3에 해당하는 문제를 살펴보았으나 2번의 문제로 유추되지만 해제 후 write를 해보면 정상적으로 write이 되는걸로 봐선, 추가적인 원인이 있을 수 있다.

- 확인 방법
  - `hdfs dfsadmin -safemode get`
- 해제 방법
  - `hdfs dfsadmin -safemode leave`