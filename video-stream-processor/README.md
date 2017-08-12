# Video Stream Processor
Video Stream Processor processes kafka JSON messages using spark structured streaming. It uses OpenCV library for video stream analytics. This project requires following tools and technologies.

- JDK - 1.8
- Maven - 3.3.9
- ZooKeeper - 3.4.8
- Kafka - 2.11-0.10.2.0
- OpenCV - 3.2.0
- Spark - 2.2.0

Please check following configuration before running "Video Stream Processor" application. 
- This application uses OpenCV native libraries (.dll or .so).Set the directory path for these native libraries in system environment variable. For example, for 64-bit windows machine, path of native library file (opencv_java320.dll and opencv_ffmpeg320_64.dll) will be {OpenCV Installation Directory} \build\java\x64.
- Check "processed.output.dir" property in "stream-processor.properties" file. Create and set directory path for this property.
- Set "stream-processor.log" file path in "log4j.properties" file.
- Make sure Zookeepr and Kafka servers are up and running.
- Run "mvn clean" command to install opencv-320.jar in local maven repository. 
  ```sh
  mvn clean 
  ```
- Execute below command to start the Video Stream Processor" application. 
  ```sh
  mvn clean package exec:java -Dexec.mainClass="com.iot.video.app.spark.processor.VideoStreamProcessor"
  ```
