# spring-boot-kafka
 
## Kafka
- 분산형 스트리밍 플랫폼
- 메시지 큐
- 대용량의 실시간 로그 처리에 특화되어 설계
- 메시지를 메모리에 저장하는 기존 시스템과 달리 카프카는 파일 시스템에 저장 -> 재시작으로 인한 유실 우려 감소
- 기존의 메시징 시스템은 broker가 consumer에게 메시지를 push해주는 방식 -> Kafka는 Consumer가 Broker로부터 직접 가지고 오는 pull 방식.

## Zookeeper
- 본래 Zookeeper의 용도는 클러스터 최신 설정정보 관리, 동기화, 리더 채택 등 클러스터의 서버들이 공유하는 데이터를 관리하기 위해 사용
- Broker에 분산 처리된 메시지 큐의 정보들을 관리
- 클러스터를 관리하는 Zookeeper없이는 Kafka 구동이 불가능함.

## Broker
- Kafka 서버
- 한 클러스터 내에 Kafka server 여러대 띄울 수 있음

## Partition
- Topic내에서 메시지가 분산되어 저장되는 단위
- 한 Topic에 Partition이 3개 있다면, 3개의 Partition에 대해서 메시지가 분산되어 저장 됨.
- Queue로 저장되므로 Partition의 마지막에 저장되어 Partition내에서는 순서가 보장되나, Partition끼리는 보장되지 않는다.

## Log
- Partition의 한칸에 해당하는 메시지
- key, value, timestamp로 구성

## Offset
- Partition내에서 각 메시지를 식별할 수 있는 unique id
- 메시지를 소비하는 Consumer가 읽을 차례를 의미하므로 Partition마다 별도로 관리 됨

## Producer
- 정해진 Topic으로 메시지를 기록
- Partition이 여러개 있을 경우, 기록 될 Partition의 선택은 기본적으로 Round-Robin 방식
- Partition내에서 가장 마지막 offset 뒤에 신규 메시지가 저장되므로, Partition내에서느 순서가 보장되지만 실제로는 Consumer의 동작 방식에 의해 **순서가 보장되지 않음**

## Consumer
- 구독한 topic에서 메시지를 소비
- 해당 topic내의 각 파티션에 존재하는 offset의 위치를 통해 이전에 소비했던 offset위치를 기억하고 관리
- Consumer가 죽었다가 다시 살아나도 전에 마지막으로 읽었던 위치에서부터 다시 읽어들일 수 있다.

## Consumer Group
- Consumer group은 한개의  Topic을 담당. (하나의 토픽은 여러개의 Consumer group이 접근할 수 있음)
- Topic내에 Partition에서 다음에 소비할 offset이 어디인지 공유하면서 메시지를 소비
- > Consumer group이 없을 경우, 하나의 partition에 2개 이상의 Consumer가 접근한다면 어떤 Consumer가 몇 번 offset을 소비할 지 모름

  > - Partition은 하나의 Consumer만 접근이 가능함.
  > - Consumer는 여러개의 Partition을 소비 가능

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
> - bootstrap-server : 브로커 리스트
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

### Kafka API Document
- https://kafka.apache.org/documentation/#gettingStarted

### 참고용
- https://www.popit.kr/kafka-%EC%9A%B4%EC%98%81%EC%9E%90%EA%B0%80-%EB%A7%90%ED%95%98%EB%8A%94-%EC%B2%98%EC%9D%8C-%EC%A0%91%ED%95%98%EB%8A%94-kafka/
- https://www.popit.kr/%ec%b9%b4%ed%94%84%ec%b9%b4-%ec%84%a4%ec%b9%98-%ec%8b%9c-%ea%b0%80%ec%9e%a5-%ec%a4%91%ec%9a%94%ed%95%9c-%ec%84%a4%ec%a0%95-4%ea%b0%80%ec%a7%80/
- https://www.popit.kr/kafka-consumer-group/
- https://12bme.tistory.com/529
