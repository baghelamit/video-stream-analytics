package com.iot.video.app.kafka.collector;

import java.sql.Timestamp;
import java.util.Base64;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Class to convert Video Frame into byte array and generate JSON event using Kafka Producer.
 * 
 * @author abaghel
 *
 */
public class VideoEventGenerator implements Runnable {
	private static final Logger logger = Logger.getLogger(VideoEventGenerator.class);	
	private String cameraId;
	private String url;
	private Producer<String, String> producer;
	private String topic;
	
	public VideoEventGenerator(String cameraId, String url, Producer<String, String> producer, String topic) {
		this.cameraId = cameraId;
		this.url = url;
		this.producer = producer;
		this.topic = topic;
	}
	
	//load OpenCV native lib
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Override
	public void run() {
		logger.info("Processing cameraId "+cameraId+" with url "+url);
		try {
			generateEvent(cameraId,url,producer,topic);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	//generate JSON events for frame
	private void generateEvent(String cameraId,String url,Producer<String, String> producer, String topic) throws Exception{
		VideoCapture camera = null;
		if(StringUtils.isNumeric(url)){
			camera = new VideoCapture(Integer.parseInt(url));
		}else{
			camera = new VideoCapture(url);
		}
		//check camera working
		if (!camera.isOpened()) {
			Thread.sleep(5000);
			if (!camera.isOpened()) {
				throw new Exception("Error opening cameraId "+cameraId+" with url="+url+".Set correct file path or url in camera.url key of property file.");
			}
		}
		Mat mat = new Mat();
        Gson gson = new Gson();
		while (camera.read(mat)) {
			//resize image before sending
			Imgproc.resize(mat, mat, new Size(640, 480), 0, 0, Imgproc.INTER_CUBIC);
			int cols = mat.cols();
	        int rows = mat.rows();
	        int type = mat.type();
			byte[] data = new byte[(int) (mat.total() * mat.channels())];
			mat.get(0, 0, data);
	        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
			JsonObject obj = new JsonObject();
			obj.addProperty("cameraId",cameraId);
	        obj.addProperty("timestamp", timestamp);
	        obj.addProperty("rows", rows);
	        obj.addProperty("cols", cols);
	        obj.addProperty("type", type);
	        obj.addProperty("data", Base64.getEncoder().encodeToString(data));  
	        String json = gson.toJson(obj);
	        producer.send(new ProducerRecord<String, String>(topic,cameraId, json),new EventGeneratorCallback(cameraId));
	        logger.info("Generated events for cameraId="+cameraId+" timestamp="+timestamp);
		}      
	    camera.release();
		mat.release();
	}
	
	private class EventGeneratorCallback implements Callback {
		private String camId;

		public EventGeneratorCallback(String camId) {
			super();
			this.camId = camId;
		}

		@Override
		public void onCompletion(RecordMetadata rm, Exception e) {
			if (rm != null) {
				logger.info("cameraId="+ camId + " partition=" + rm.partition());
			}
			if (e != null) {
				e.printStackTrace();
			}
		}
	}

}
