# Video Stream Collector
Video Stream Collector converts video file or webcam feed to JSON messages and sends to Kafka. This application requires following tools and technologies.

- JDK - 1.8
- Maven - 3.3.9
- ZooKeeper - 3.4.8
- Kafka - 2.11-0.10.2.0
- OpenCV - 3.2.0

Please check following configuration before running "Video Stream Collector" application. Run this application after "Video Stream Processor" application has started.
- This application uses OpenCV native libraries (.dll or .so).Set the directory path for these native libraries in system environment variable. For example, for 64-bit windows machine, path of native library file (opencv_java320.dll and opencv_ffmpeg320_64.dll) will be {OpenCV Installation Directory} \build\java\x64.
- Check "camera.url" and "camera.id" properties in "stream-collector.properties" file. 
- Set "stream-collector.log" file path in "log4j.properties" file. 
- Make sure Zookeepr and Kafka servers are up and running.
- Create "video-stream-event" topic using below command.
  ```sh
  kafka-topics.sh --create --zookeeper localhost:2181 --topic video-stream-event --replication-factor 1 --partitions 3
  ```
- Run "mvn clean" command to install opencv-320.jar in local maven repository. 
  ```sh
    mvn clean 
  ```
- Execute below command to start the "Video Stream Collector" application.
  ```sh
  mvn clean package exec:java -Dexec.mainClass="com.iot.video.app.kafka.collector.VideoStreamCollector" -Dexec.cleanupDaemonThreads=false
  ```
