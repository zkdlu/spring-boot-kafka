package com.zkdlu.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    Producer produce;

    @Autowired
    Consumer consumer;

    @GetMapping("/{message}")
    public String testKafka(@PathVariable String message) {
        produce.send(message);

        return consumer.getConsumedFromQueue();
    }
}
