# Spark Streaming with Twitter and Kafka

## Steps to run project
- Create an app on twitter and get access tokens and API tokens.
- Build the project and create a Fat Jar file. (On IntelliJ use Assembly to build rather than package)
- Start Zookeeper: - bin\windows\zookeeper-server-start.bat config\zookeeper.properties
- Start Apache Kafka: - bin\windows\kafka-server-start.bat config\server.properties
- Make the Kafka producer with the topic name "topicA": - bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic topicA
- Start the Kafka Consumer and check the connection/logs: - bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic topicA --from-beginning
- Now start the following services in the following order:
        a) Elasticsearch: - bin\elasticsearch.bat
	b) Kibana: - bin\kibana.bat
	c) Logstash (Create logstash-simple.conf)
- Add this configuration to logstash-simple.conf

`input {
	kafka {
		bootstrap_servers => "localhost:9092"
		topics => ["topicA"]
	}
}
output {
	elasticsearch {
		hosts => ["localhost:9200"]
		index => "mytopicindex"
	}
}`

- Run Logstash server: - bin\logstash.bat -f PathToFile\logstash-simple.conf
- Now run the application using the following command from the root folder of your project folder:-
        spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.6 --class "TwitterSentiment" PathtoJarFile\kafka-assembly-0.1.jar topicA "twitterConsumerKey(or)APIKey" "twitterConsumerSecret(or)APISecretKey" "accessToken" "accessTokenSecret"
Replace the fields between " " with the respective values, top
- Kafka consumer will start showing the tweets with sentiment appended at the beginning.
    After setup is complete, you should be able to go to http://localhost:5601 and use Kibana to visualize your data in real-time.
- Setup Index on kibana.
- Visualization can be done using tools available on kibana.
