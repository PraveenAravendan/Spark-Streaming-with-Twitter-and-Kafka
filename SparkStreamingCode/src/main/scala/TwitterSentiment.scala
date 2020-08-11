import java.util.Properties
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.storage._

object TwitterSentiment {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("kafka-assignment").setMaster("local[4]")
      .set("spark.driver.host", "localhost")

    if (args.length < 5) {
      println("5 Arguments are required in this format: Program_Name outputTopic TwitterconsumerKey consumerSecret accessToken accessTokenSecret")
      System.exit(1)
    }

    val inTopic = args(0)
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.WARN)

    val APIKey = args(1);val APISecret = args(2);val token = args(3);val tokenSecret = args(4)
    val filters = Seq("nfl", "nba", "ps5", "Pandemic", "Weather", "Music")
    // SetProperty so that Twitter4j library can be used by Twitter stream and to generate OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", APIKey)
    System.setProperty("twitter4j.oauth.consumerSecret", APISecret)
    System.setProperty("twitter4j.oauth.accessToken", token)
    System.setProperty("twitter4j.oauth.accessTokenSecret", tokenSecret)

    @transient val streamContext = new StreamingContext(sparkConf, Seconds(2))
    //val sqlContext = new SQLContext(streamContext.sparkContext)
    // Creating twitter Stream
    val tweets = TwitterUtils.createStream(streamContext, None, filters, StorageLevel.MEMORY_ONLY_SER_2)
    // Computing sentiment of tweet
    val englishTweets = tweets.filter(_.getLang() == "en")
    val tweetStatuses = englishTweets.map(status => (status.getText()))

    tweetStatuses.foreachRDD { (rdd, time) =>
      rdd.foreachPartition { partitionIter =>
        val props = new Properties()
        val localHostBootstrap = "localhost:9092" //-- your external ip of GCP VM, example: 10.0.0.1:9092
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("bootstrap.servers", localHostBootstrap)
        //Producer
        val kafProducer = new KafkaProducer[String, String](props)

        partitionIter.foreach { element =>
          val data = element.toString()
          val sentiment = SentimentChecker.mainSentiment(data).toString()
          val resultData = new ProducerRecord[String, String](inTopic, "sentiment", sentiment + "->" +  data)
          kafProducer.send(resultData)
        }
        kafProducer.flush()
        kafProducer.close()
      }
    }
    streamContext.start()
    streamContext.awaitTermination()
  }
}
