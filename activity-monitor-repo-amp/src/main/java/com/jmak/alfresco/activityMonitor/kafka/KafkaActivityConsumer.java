package com.jmak.alfresco.activityMonitor.kafka;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.jmak.alfresco.activityMonitor.domain.ActivityConsumer;
import com.jmak.alfresco.activityMonitor.domain.ActivityMessage;

public final class KafkaActivityConsumer implements ActivityConsumer, Runnable {

	private static final Log LOG = LogFactory.getLog(KafkaActivityConsumer.class);

	private KafkaConsumer<String, String> consumer;
	private Session session;
	private String topic;

	private boolean running;
	private Object driver = new Object();


	KafkaActivityConsumer(final String topic, final Session session) {
		this.topic = topic;
		this.session = session;
	}

	@Override
	public void run() {
		consumer = new KafkaConsumer<>(createConsumerConfig());
		consumer.subscribe(Arrays.asList(topic));

		running = true;

		try {
			session.getBasicRemote().sendObject(new ActivityMessage.StatusMessage().build(ActivityMessage.Status.connected));

			while (running) {
				final ConsumerRecords<String, String> records = consumer.poll(100);
				
				for (final ConsumerRecord<String, String> record : records) {
					session.getBasicRemote().sendText(record.value());
				}
			}
		}
		catch (IOException | EncodeException e) {
			LOG.error(e);
		}
		finally {
			consumer.close();
		}
	}

	public void start() {
		if (!running) {
			new Thread() {
				@Override
				public void run() {
					consumer = new KafkaConsumer<>(createConsumerConfig());
					consumer.subscribe(Arrays.asList(topic));

					running = true;

					try {
						session.getBasicRemote().sendObject(new ActivityMessage.StatusMessage().build(ActivityMessage.Status.connected));

						while (true) {
							synchronized (driver) {
								if (running) {
									final ConsumerRecords<String, String> records = consumer.poll(100);

									for (final ConsumerRecord<String, String> record : records) {
										session.getBasicRemote().sendText(record.value());
									}
								}
							}
						}
					}
					catch (IOException | EncodeException e) {
						LOG.error(e);
					}
					finally {
						consumer.close();
					}
				}
			}.start();
		}
	}

	public void finalize() {
		synchronized (driver) {
			running = false;
	    }
	}

	private Properties createConsumerConfig() {
		final Properties props = new Properties();

		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", topic + "-" + System.currentTimeMillis());
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
//		props.put("session.timeout.ms", "30000");
//		props.put("auto.offset.reset", "earliest");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		return props;
	}
}
