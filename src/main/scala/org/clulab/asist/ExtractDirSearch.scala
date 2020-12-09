package org.clulab.asist

import java.io.{File, FileNotFoundException, IOException, PrintWriter}
import java.util.Properties
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.clulab.odin.Mention

import scala.collection.immutable
import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._

object ExtractDirSearch extends App {

  // SCRIPT START

  println("[CoreNLP] Initializing the CoreNLP pipeline ...")
  val corenlp_properties = new Properties()
  corenlp_properties.setProperty(
    "annotators",
    "tokenize, ssplit, pos, lemma, ner, parse, dcoref"
  )
  val pipeline = new StanfordCoreNLP(corenlp_properties)
  println("[CoreNLP] Completed Initialization")

  val taxonomy_json = parse(
    Source.fromResource("taxonomy_map.json").mkString
  )
  implicit val formats = DefaultFormats
  case class Pair(term: String, score: String)
  case class TermMap(term: String, pairs: List[Pair])
  case class TaxonomyMap(terms: List[TermMap])
  val tax_map = taxonomy_json.extract[TaxonomyMap]

  println("[AsistEngine] Initializing the AsistEngine ...")
  val ieSystem = new AsistEngine()
  var proc = ieSystem.proc
  println("[AsistEngine] Completed Initialization ...")

  val extractor = new Extractor(pipeline, ieSystem, tax_map)

  val input_dir_name = if (args.length > 0) {
    args(0)
  } else {
    throw new Exception(
      "ERROR: Must include transcript directory path as argument"
    )
  }

  val experiment_id = if (args.length > 1) {
    args(1)
  } else {
    "NULL"
  }

  var output_filename = if (args.length > 2) {
    args(2)
  } else {
    "output_events.txt"
  }

  var output_file = new PrintWriter(new File(output_filename))

  //https://alvinalexander.com/scala/how-to-list-subdirectories-under-directory-in-scala/
  def getFilesInDir(dir: File): Array[String] = {
    dir.listFiles
      .filter(_.isFile)
      .map(_.getAbsolutePath)
      .filter(_.endsWith(".vtt"))
  }

  try {
    println("Starting in: " + input_dir_name)
    for (input_file_name <- getFilesInDir(new File(input_dir_name))) {
      val extension_index = input_file_name.lastIndexOf(".")
      output_file = new PrintWriter(new File(input_file_name.substring(0, extension_index) + ".out"))
      println("Extracting from: " + input_file_name + " . . .")
      val extracted_mention_json =
        extractor.extractMentions(input_file_name, experiment_id, raw_file=input_file_name.endsWith(".txt"))
      for (event_json <- extracted_mention_json) {
        output_file.write(event_json + "\n")
      }
      output_file.close
    }
  }
}
