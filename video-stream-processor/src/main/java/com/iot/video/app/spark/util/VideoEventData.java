package com.iot.video.app.spark.util;

import java.io.Serializable;
import java.sql.Timestamp;
/**
 * Java Bean to hold JSON message
 * 
 * @author abaghel
 *
 */
public class VideoEventData implements Serializable {

	private String cameraId;
	private Timestamp timestamp;
	private int rows;
	private int cols;
	private int type;
	private String data;
	
	public String getCameraId() {
		return cameraId;
	}
	public void setCameraId(String cameraId) {
		this.cameraId = cameraId;
	}	
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public int getCols() {
		return cols;
	}
	public void setCols(int cols) {
		this.cols = cols;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
}
