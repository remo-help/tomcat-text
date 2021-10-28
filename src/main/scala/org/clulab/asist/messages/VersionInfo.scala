package org.clulab.asist.messages

import org.clulab.asist.agents.DialogAgent
import buildinfo.BuildInfo

/**
 *  Authors:  Joseph Astier, Adarsh Pyarelal, Rebecca Sharp
 *
 *  Updated:  2021 August
 *
 *  Testbed version info, based on:
 *
 *  https://gitlab.asist.aptima.com/asist/testbed/-/blob/develop/MessageSpecs/Agent/versioninfo/agent_versioninfo.md
 *
 *  This message is generated by the Dialog Agent whenever there is a trial 
 *  start message published on the message bus
 */

/** Configuration settings */
case class VersionInfoDataConfig(
  name: String = null,
  value: String = null,
)

/** channel on the Message Bus */
case class VersionInfoDataMessageChannel(
  topic: String = null,
  message_type: String = null,
  sub_type: String = null,
)

/** Part of the Info class */
case class VersionInfoData(
  agent_name: String = null,
  owner: String = null,
  version: String = null,
  source: Seq[String] = List(),
  dependencies: Seq[String] = List(),
  config: Seq[VersionInfoDataConfig] = List(),
  publishes: Seq[VersionInfoDataMessageChannel] = List(),
  subscribes: Seq[VersionInfoDataMessageChannel] = List()
)

/** Contains the full data of the Version Info message */
case class VersionInfo (
  header: CommonHeader,
  msg: CommonMsg,
  data: VersionInfoData
) 

/** Same as VersionInfo but with Message Bus topic */
case class VersionInfoMetadata(
  topic: String,
  header: CommonHeader,
  msg: CommonMsg,
  data: VersionInfoData
) 

object VersionInfoMetadata {
  def apply(
    agent: DialogAgent, 
    trialMessage: TrialMessage,
    timestamp: String
  ): VersionInfoMetadata = {
    val versionInfo = VersionInfo(agent, trialMessage, timestamp)
    VersionInfoMetadata(
      topic = agent.topicPubVersionInfo,
      header = versionInfo.header,
      msg = versionInfo.msg,
      data = versionInfo.data
    )
  }
}

// Return a VersionInfo populated with the current DialogAgent 
// testbed configuration
object VersionInfo 
{
  val dataSource = 
    s"https://gitlab.asist.aptima.com:5050/asist/testbed/uaz_dialog_agent:${BuildInfo.version}"

  // create a VersionInfo by copying some fields from the input CommonMsg
  def apply(
    agent: DialogAgent,
    trialMessage: TrialMessage,
    timestamp: String): VersionInfo = VersionInfo(
      agent.commonHeader(timestamp),
      trialMessage.msg.copy(
        timestamp = timestamp,
        source = agent.dialogAgentSource,
        sub_type = "versioninfo",
        version = agent.dialogAgentVersion,
      ),
      data(agent)
    )

  def data(agent: DialogAgent): VersionInfoData = VersionInfoData(
    agent_name = "tomcat_textAnalyzer",
    owner = "University of Arizona",
    version = agent.dialogAgentVersion,
    source = List(dataSource),
    dependencies = List(),
    config = List(),
    publishes = List(
      VersionInfoDataMessageChannel(
        topic = agent.topicPubDialogAgent,
        message_type = agent.dialogAgentMessageType,
        sub_type = agent.dialogAgentSubType
      ),
      VersionInfoDataMessageChannel(
        topic = agent.topicPubVersionInfo,
        message_type = "agent/versioninfo",
        sub_type = "versioninfo"
      ),
      VersionInfoDataMessageChannel(
        topic = agent.topicPubHeartbeat,
        message_type = "status",
        sub_type = "heartbeat"
      )
    ),
    subscribes = List(
      VersionInfoDataMessageChannel(
        topic = agent.topicSubTrial,
        message_type = "trial",
        sub_type = "versioninfo"
      ),
      VersionInfoDataMessageChannel(
        topic = agent.topicSubChat,
        message_type = "chat",
        sub_type = "Event:Chat"
      ),
      VersionInfoDataMessageChannel(
        topic = agent.topicSubUazAsr,
        message_type = "observation",
        sub_type = "asr"
      ),
      VersionInfoDataMessageChannel(
        topic = agent.topicSubAptimaAsr,
        message_type = "observation",
        sub_type = "asr"
      )
    )
  )
}
