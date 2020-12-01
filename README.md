# spring-boot-kafka

## Kafka
- 분산형 스트리밍 플랫폼
- 메시지 큐
- 대용량의 실시간 로그 처리에 특화되어 설계
- 메시지를 메모리에 저장하는 기존 시스템과 달리 카프카는 파일 시스템에 저장 -> 재시작으로 인한 유실 우려 감소
- 기존의 메시징 시스템은 broker가 consumer에게 메시지를 push해주는 방식 -> Kafka는 Consumer가 Broker로부터 직접 가지고 오는 pull 방식.

### 주요 개념
- Producer: 메시지 발행자
- Consumer: 메시지 소비자
- Broker: 카프카 서버
- Zookeeper: 카프카 서버(+클러스터 상태를 관리)
- Cluster: Broker 묶음
- Partitions: Topic이 나뉘는 단위
- Log: 1개의 메시지
- Offset: 파티션 내에서 각 메시지가 가지는 unique id

### ZooKeeper 컨테이너 설치
```yml
version: '3.1'

services:
  zoo1:
    image: zookeeper
    restart: always
    hostname: zoo1
    ports:
      - 2181:2181
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=0.0.0.0:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181

  zoo2:
    image: zookeeper
    restart: always
    hostname: zoo2
    ports:
      - 2182:2181
    environment:
      ZOO_MY_ID: 2
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=0.0.0.0:2888:3888;2181 server.3=zoo3:2888:3888;2181

  zoo3:
    image: zookeeper
    restart: always
    hostname: zoo3
    ports:
      - 2183:2181
    environment:
      ZOO_MY_ID: 3
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=0.0.0.0:2888:3888;2181
```

실행
```bash
 $ docker-compose -f zookeeper-docker-compose.yml up
```

### Kafka 컨테이너 설치 
```yml
version: '2'
services:
  kafka:
    image: wurstmeister/kafka
    restart: always
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: host.docker.internal
      # Topic 1 will have 1 partition and 1 replicas
      KAFKA_CREATE_TOPICS: "Topic1:1:1"
      KAFKA_ZOOKEEPER_CONNECT: host.docker.internal:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```
실행
```bash
$ docker-compose -f kafka-docker-compose.yml up
```

### 카프카에 필요한 zookeeper와 함께 구동되어 작동 함
```
$ docker network create --driver=bridge --subnet=172.19.0.0/16 my_devnet //네트워크 생성
$ docker network inspect my_devnet //네트워크 정보 확인
```

```yml
version: '3.5'
services:
  zookeeper:
    image: 'bitnami/zookeeper:latest'
    ports:
    - '2181:2181'
    networks:
      devnet:
        ipv4_address: 172.19.0.20
    environment:
    - ALLOW_ANONYMOUS_LOGIN=yes
  kafka:
    hostname: kafka
    image: 'bitnami/kafka:latest'
    ports:
    - '9092:9092'
    networks:
      devnet:
        ipv4_address: 172.19.0.21
    environment:
    - KAFKA_ADVERTISED_HOST_NAME=kafka
    - KAFKA_ZOOKEEPER_CONNECT=172.19.0.20:2181
    - ALLOW_PLAINTEXT_LISTENER=yes
 
networks:
  devnet:
    external:
      name: my_devnet
```

실행
```bash
$ docker-compose up
```

## 간단 테스트 해보기
토픽생성
```
$ kafka-topics.sh --create -bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic "test"
```
> - bootstrap-server : 연결할 카프카 서버, 이 옵션을 주면 직접 zookeeper에 연결하지 않아도 됨
> - replication-factor : partion 복제 수, 기본 값은 파티션 복제를 사용하지 않음
> - partitions : 토픽이 생성되거나 변경될 때의 파티션 수
> - topic : create, alter, describe, delete 옵션에 사용할 토픽 명
> > 파티션이 늘릴 수는 있는데 줄일 수는 없으니 늘릴 때 유의

토픽목록
```
$ kafka-topics.sh --list --bootstrap-server localhost:9092
```
토픽삭제
```
$ kafka-topics.sh --delete --topic "test" --bootstrap-server localhost:9092
```
> 토픽을 삭제하기 위해서는 server.properties 파일에서 delete.topic.enalbe=true설정을 추가해줘야 함(서버 재시작 필요)

producer
```
$ docker exec -it kafka_kafka_1 bash
@kafka:/$ kafka-console-producer.sh --broker-list localhost:9092 --topic my-topic
> test
```
consumer
```
$ docker exec -it kafka_kafka_1 bash
@kafka:/$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my-topic --from-beginning
test
```
> --from-beginning : Consumer에게 설정된 offset이 없으므로 가장 최신의 메시지 대신 가장 먼저 도착한 메시지부터 읽도록 하는 옵션
