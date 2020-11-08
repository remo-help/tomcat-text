package org.clulab.asist

import java.io.{File, FileNotFoundException, IOException, PrintWriter}
import java.util.Properties

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.clulab.odin.Mention
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.collection.immutable
import scala.io.Source
import scala.util.parsing.json.JSON

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

  val taxonomy_json = JsonParser(
    Source.fromResource("taxonomy_map.json").mkString
  )
  val tax_map = taxonomy_json
    .convertTo[immutable.Map[String, Array[immutable.Map[String, String]]]]

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

  //https://alvinalexander.com/scala/how-to-list-subdirectories-under-directory-in-scala/
  def getFilesInDir(dir: File): Array[String] = {
    dir.listFiles
      .filter(_.isFile)
      .map(_.getAbsolutePath)
      .filter(_.endsWith(".vtt"))
  }

  println("Starting in: " + input_dir_name)
  var output_file_name = ""
  for (input_file_name <- getFilesInDir(new File(input_dir_name))) {
    println("Extracting from: " + input_file_name + " . . .")
    output_file_name = input_file_name.substring(0, input_file_name.size-4) + "_extractions.txt"
    val output_file = new PrintWriter(new File(output_file_name))
    val extracted_mention_json = extractor.extractMentions(input_file_name, experiment_id)
    try {
      for (event_json <- extracted_mention_json) {
        output_file.write(event_json + "\n")
      }
    } catch {
      case e: FileNotFoundException => println("Failed to create new file for extracted events")
      case e: IOException => println("IOException occurred when creating file: "+output_file_name)
    } finally {
      output_file.close
    }
  }
}
