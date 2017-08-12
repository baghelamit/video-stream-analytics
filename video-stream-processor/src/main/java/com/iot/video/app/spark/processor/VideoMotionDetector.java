package com.iot.video.app.spark.processor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.iot.video.app.spark.util.VideoEventData;


/**
 * Class to detect motion from video frames using OpenCV library.
 * 
 * @author abaghel
 *
 */
public class VideoMotionDetector implements Serializable {	
	private static final Logger logger = Logger.getLogger(VideoMotionDetector.class);	
	
	//load native lib
	static {
		 System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	/**
	 * Method to detect motion from batch of frames
	 * 
	 * @param camId camera Id
	 * @param frames list of VideoEventData
	 * @param outputDir directory to save image files
	 * @return last processed VideoEventData 
	 * @throws Exception
	 */
	public static VideoEventData detectMotion(String camId, Iterator<VideoEventData> frames, String outputDir, VideoEventData previousProcessedEventData) throws Exception {
		VideoEventData currentProcessedEventData = new VideoEventData();
		Mat frame = null;
		Mat copyFrame = null;
		Mat grayFrame = null;
		Mat firstFrame = null;
		Mat deltaFrame = new Mat();
		Mat thresholdFrame = new Mat();	
		ArrayList<Rect> rectArray = new ArrayList<Rect>();
		
		//previous processed frame 
		if (previousProcessedEventData != null) {
			logger.warn("cameraId=" + camId + " previous processed timestamp=" + previousProcessedEventData.getTimestamp());
			Mat preFrame = getMat(previousProcessedEventData);
			Mat preGrayFrame = new Mat(preFrame.size(), CvType.CV_8UC1);
			Imgproc.cvtColor(preFrame, preGrayFrame, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(preGrayFrame, preGrayFrame, new Size(3, 3), 0);
			firstFrame = preGrayFrame;
		}
		
		//sort by timestamp
		ArrayList<VideoEventData> sortedList = new ArrayList<VideoEventData>();
		while(frames.hasNext()){
			sortedList.add(frames.next());
		}
		sortedList.sort(Comparator.comparing(VideoEventData::getTimestamp));
		logger.warn("cameraId="+camId+" total frames="+sortedList.size());
		
		//iterate and detect motion
		for (VideoEventData eventData : sortedList) {
			frame = getMat(eventData);
			copyFrame = frame.clone();
			grayFrame = new Mat(frame.size(), CvType.CV_8UC1);
			Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(grayFrame, grayFrame, new Size(3, 3), 0);
			logger.warn("cameraId=" + camId + " timestamp=" + eventData.getTimestamp());
			//first
			if (firstFrame != null) {
				Core.absdiff(firstFrame, grayFrame, deltaFrame);
				Imgproc.threshold(deltaFrame, thresholdFrame, 20, 255, Imgproc.THRESH_BINARY);
				rectArray = getContourArea(thresholdFrame);
				if (rectArray.size() > 0) {
					Iterator<Rect> it2 = rectArray.iterator();
					while (it2.hasNext()) {
						Rect obj = it2.next();
						Imgproc.rectangle(copyFrame, obj.br(), obj.tl(), new Scalar(0, 255, 0), 2);
					}
					logger.warn("Motion detected for cameraId=" + eventData.getCameraId() + ", timestamp="+ eventData.getTimestamp());
					//save image file
					saveImage(copyFrame, eventData, outputDir);
				}
			  }
				firstFrame = grayFrame;
				currentProcessedEventData = eventData;
		   }
			return currentProcessedEventData;
		}
	
	//Get Mat from byte[]
	private static Mat getMat(VideoEventData ed) throws Exception{
		 Mat mat = new Mat(ed.getRows(), ed.getCols(), ed.getType());
		 mat.put(0, 0, Base64.getDecoder().decode(ed.getData()));                                      
		 return mat;
	}
	
	//Detect contours
	private static ArrayList<Rect> getContourArea(Mat mat) {
		Mat hierarchy = new Mat();
		Mat image = mat.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		Rect rect = null;
		double maxArea = 300;
		ArrayList<Rect> arr = new ArrayList<Rect>();
		for (int i = 0; i < contours.size(); i++) {
			Mat contour = contours.get(i);
			double contourArea = Imgproc.contourArea(contour);
			if (contourArea > maxArea) {
				rect = Imgproc.boundingRect(contours.get(i));
				arr.add(rect);
			}
		}
		return arr;
	}
	
	//Save image file
	private static void saveImage(Mat mat,VideoEventData ed,String outputDir){
		String imagePath = outputDir+ed.getCameraId()+"-T-"+ed.getTimestamp().getTime()+".png";
		logger.warn("Saving images to "+imagePath);
		boolean result = Imgcodecs.imwrite(imagePath, mat);
		if(!result){
			logger.error("Couldn't save images to path "+outputDir+".Please check if this path exists. This is configured in processed.output.dir key of property file.");
		}
	}
}
