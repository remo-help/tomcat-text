package org.clulab.asist

import java.util.Date
import org.clulab.asist.AsistEngine
import org.clulab.odin.Mention
import org.clulab.processors.Processor
import org.clulab.processors.fastnlp.FastNLPProcessor
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

class TestParse extends FlatSpec with Matchers {
  val extractor = new AsistEngine()

  def getMapOfResults(mentions: Vector[Mention]): Map[String, Int] = {
    var mention_map = Map[String, Int]()
    for (mention <- mentions){
      if (mention_map contains mention.label){
        mention_map += (mention.label -> (mention_map(mention.label) + 1))
      } else {
        mention_map += (mention.label -> 1)
      }
    }
    mention_map
  }

  "AsistEngine" should "Parse close events properly" in {
    val doc = extractor.annotate("I will close the door")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(3)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Infrastructure")
    mention_map.keySet should contain("Switch")
    mention_map.keySet should contain("Close")
    mention_map("Infrastructure") should be(1)
    mention_map("Switch") should be(1)
    mention_map("Close") should be(1)
  }

  it should "Parse craft events properly" in {
    val doc = extractor.annotate("After crafting this sword, I push the button")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(1)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Craft")
    mention_map("Craft") should be(1)
  }

  it should "Parse sight events properly" in {
    val doc = extractor.annotate("I think I see a victim over there")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(3)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Deictic")
    mention_map.keySet should contain("Sight")
    mention_map.keySet should contain("Victim")
    mention_map("Deictic") should be(1)
    mention_map("Sight") should be(1)
    mention_map("Victim") should be(1)
  }

  ignore should "Parse extinguish events properly" in {
    val doc = extractor.annotate("I'm going to put out the fire before leaving")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(2)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Fire")
    mention_map.keySet should contain("Extinguish")
    mention_map("Fire") should be(1)
    mention_map("Extinguish") should be(1)
  }

  it should "Parse toggle events properly" in {
    val doc =
      extractor.annotate("He opened the door and walked into the sunset.")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(4)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Toggle")
    mention_map.keySet should contain("Infrastructure")
    mention_map.keySet should contain("Switch")
    mention_map.keySet should contain("Deictic")
    mention_map("Toggle") should be(1)
    mention_map("Infrastructure") should be(1)
    mention_map("Switch") should be(1)
    mention_map("Deictic") should be(1)
  }

  it should "Parse save events properly" in {
    val doc = extractor.annotate("I'm going to save the villager over there")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(3)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Save")
    mention_map.keySet should contain("Victim")
    mention_map.keySet should contain("Deictic")
    mention_map("Save") should be(1)
    mention_map("Victim") should be(1)
    mention_map("Deictic") should be(1)
  }

  it should "Parse defeat events properly" in {
    val doc = extractor.annotate("To progress I will kill the zombies")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(2)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Foe")
    mention_map.keySet should contain("Defeat")
    mention_map("Foe") should be(1)
    mention_map("Defeat") should be(1)
  }

  it should "Parse search events properly" in {
    val doc =
      extractor.annotate("I will search for the villagers inside the building")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(4)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Search")
    mention_map.keySet should contain("Victim")
    mention_map.keySet should contain("Infrastructure")
    mention_map.keySet should contain("Deictic")
    mention_map("Search") should be(1)
    mention_map("Victim") should be(1)
    mention_map("Infrastructure") should be(1)
    mention_map("Deictic") should be(1)
  }

  ignore should "Recognize fire entities" in {
    val doc =
      extractor.annotate("Inside, a flame was spreading through the kitchen")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(1)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Fire")
    mention_map("Fire") should be(1)
  }

  it should "Recognize infrastructure entities" in {
    val doc = extractor.annotate(
      "The room was up the stairs, behind the first door on the left"
    )
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(3)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Infrastructure")
    mention_map.keySet should contain("Switch")
    mention_map("Infrastructure") should be(2)
    mention_map("Switch") should be(1)
  }

  it should "Recognize switch entities" in {
    val doc = extractor.annotate("The lever is behind the door")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(3)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Switch")
    mention_map.keySet should contain("Infrastructure")
    mention_map("Switch") should be(2)
    mention_map("Infrastructure") should be(1)
  }

  it should "Recognize foe entities" in {
    val doc = extractor.annotate(
      "Down the road there is a mob who looks like a zombie."
    )
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(3)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Foe")
    mention_map.keySet should contain("Deictic")
    mention_map("Foe") should be(2)
    mention_map("Deictic") should be(1)
  }

  it should "Recognize person entities" in {
    val doc =
      extractor.annotate("There's a guy over there, next to the other person")
    val mentions = extractor.extractFrom(doc)
    mentions.size should be(4)
    val mention_map = getMapOfResults(mentions)
    mention_map.keySet should contain("Deictic")
    mention_map.keySet should contain("Victim")
    mention_map("Deictic") should be(2)
    mention_map("Victim") should be(2)
  }
}
