package com.zkdlu.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Component
public class Consumer {
    private Queue<String> result = new LinkedList<>();

    @KafkaListener(topics = "${kafka.topic}")
    public void receiveTopic(ConsumerRecord consumerRecord) {
        String consumed = consumerRecord.toString();

        result.add(consumed);

        System.out.println(consumed);
    }

    public String getConsumedFromQueue() {
        while (result.isEmpty());

        return result.poll();
    }
}
