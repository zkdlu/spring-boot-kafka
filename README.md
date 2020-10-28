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
