package com.jmak.alfresco.activityMonitor.kafka;

import java.util.Objects;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import com.jmak.alfresco.activityMonitor.domain.ActivityMessage;
import com.jmak.alfresco.activityMonitor.service.MessageProducerService;
import com.jmak.alfresco.activityMonitor.websocket.common.JsonEncoder;

@Service
public class MessageProducerServiceImpl implements MessageProducerService {

	//TODO configure the final topic here 
	private static final String KAKFA_TOPIC = "site-activity-sample";

	private KafkaProducer<String, String> producer;


	@PostConstruct
	public void init() {
		producer = new KafkaProducer<>(createProducerConfig());
	}

	@Override
	public void publish(ActivityMessage activityMessage) {
		final String message = JsonEncoder.encode(activityMessage);

		if (Objects.nonNull(message)) {
			producer.send(new ProducerRecord<String, String>(KAKFA_TOPIC, message));			
		}
	}

	private Properties createProducerConfig() {
		final Properties props = new Properties();

		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		return props;
	}
}
