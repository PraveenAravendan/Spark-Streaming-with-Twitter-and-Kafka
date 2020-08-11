object TweetSentiment extends Enumeration {
  type Sentiment = Value
  val POSITIVE, NEGATIVE, NEUTRAL = Value

  def toSentiment(sentiment: Int): Sentiment = sentiment match {
    case x if x == 0 || x == 1 => TweetSentiment.NEGATIVE
    case 2 => TweetSentiment.NEUTRAL
    case x if x == 3 || x == 4 => TweetSentiment.POSITIVE
  }
}
