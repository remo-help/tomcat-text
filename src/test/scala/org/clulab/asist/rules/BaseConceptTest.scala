package org.clulab.asist.rules

import org.clulab.asist.BaseTest

class BaseConceptTest extends BaseTest {

  passingTest should "Recognize role entities" in {
    val doc = extractor.annotate("searcher search specialist. I'm a medic. He's an engineer.")
    val mentions = extractor.extractFrom(doc)

    val searcher1_mention = DesiredMention("Searcher", "searcher")
    val searcher2_mention = DesiredMention("Searcher", "search specialist")
    val medic_mention = DesiredMention("Medic", "medic")
    val engineer_mention = DesiredMention("Engineer", "engineer")

    testMention(mentions, searcher1_mention)
    testMention(mentions, searcher2_mention)
    testMention(mentions, medic_mention)
    testMention(mentions, engineer_mention)
  }

  passingTest should "Recognize call-sign entities" in {
    val doc = extractor.annotate("Hello green. Yes Blue. I'm Red.")
    val mentions = extractor.extractFrom(doc)

    val alpha_mention = DesiredMention("Red", "Red")
    val delta_mention = DesiredMention("Green", "green")
    val bravo_mention = DesiredMention("Blue", "Blue")

    testMention(mentions, alpha_mention)
    testMention(mentions, delta_mention)
    testMention(mentions, bravo_mention)
  }


}
