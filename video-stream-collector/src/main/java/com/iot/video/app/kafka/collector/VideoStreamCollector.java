package com.iot.video.app.kafka.collector;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.log4j.Logger;

import com.iot.video.app.kafka.util.PropertyFileReader;

/**
 *  Class to configure Kafka Producer and connect to Video camera url.
 *  
 * @author abaghel
 *
 */
public class VideoStreamCollector {

	private static final Logger logger = Logger.getLogger(VideoStreamCollector.class);

	public static void main(String[] args) throws Exception {
		
		// set producer properties
		Properties prop = PropertyFileReader.readPropertyFile();	
		Properties properties = new Properties();
		properties.put("bootstrap.servers", prop.getProperty("kafka.bootstrap.servers"));
		properties.put("acks", prop.getProperty("kafka.acks"));
		properties.put("retries",prop.getProperty("kafka.retries"));
		properties.put("batch.size", prop.getProperty("kafka.batch.size"));
		properties.put("linger.ms", prop.getProperty("kafka.linger.ms"));
		properties.put("max.request.size", prop.getProperty("kafka.max.request.size"));
		properties.put("compression.type", prop.getProperty("kafka.compression.type"));
		properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		// generate event
		Producer<String, String> producer = new KafkaProducer<String, String>(properties);
		generateIoTEvent(producer,prop.getProperty("kafka.topic"),prop.getProperty("camera.id"),prop.getProperty("camera.url"));
	}

	private static void generateIoTEvent(Producer<String, String> producer, String topic, String camId, String videoUrl) throws Exception {
		String[] urls = videoUrl.split(",");
		String[] ids = camId.split(",");
		if(urls.length != ids.length){
			throw new Exception("There should be same number of camera Id and url");
		}
		logger.info("Total urls to process "+urls.length);
		for(int i=0;i<urls.length;i++){
			Thread t = new Thread(new VideoEventGenerator(ids[i].trim(),urls[i].trim(),producer,topic));
			t.start();
		}
	}
}
