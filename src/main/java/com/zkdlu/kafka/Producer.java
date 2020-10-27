package com.zkdlu.kafka;

import org.apache.kafka.clients.producer.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Component
public class Producer {
    private KafkaProducer<String, String> producer = null;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;
    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;
    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    @PostConstruct
    public void init() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        producer = new KafkaProducer<>(properties);
    }

    public String send() {
        String result = "SEND FAIL";

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String message = "payLoad:" + dateFormat.format(now);

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, message);

        try {
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        System.out.println(exception.getMessage());
                    }
                }
            });

            result = "SEND SUCCESS";
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            producer.flush();
        }

        return result + " : " + message;
    }
}
