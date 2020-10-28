package com.zkdlu.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {
//    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//
//    private final KafkaTemplate kafkaTemplate;
//
//    public ScheduledTasks(KafkaTemplate kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    public void send(String topic, String payload) {
//        kafkaTemplate.send(topic, payload);
//
//        System.out.println("Message: " + payload + " sent to topic: " + topic);
//    }
//
//    @Scheduled(fixedRate = 1000)
//    public void reportCurrentTime() {
//        send("test", "helloworld " + dateFormat.format(new Date()));
//    }
//
//    @KafkaListener(topics = "test")
//    public void receiveTopic1(ConsumerRecord consumerRecord) {
//        System.out.println("Receiver on topic1: "+consumerRecord.toString());
//    }
}

