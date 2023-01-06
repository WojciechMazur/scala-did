package fmgp.webapp

import com.raquo.laminar.api.L._

import fmgp.did.Indentity
import fmgp.did.example.AgentProvider

object Global {
  // val keysVar: Var[Option[MyKeys]] = Var(initial = None)

  val dids = AgentProvider.allAgents.keys.toSeq.sorted :+ "<none>"
  def getAgentName(mAgent: Option[Indentity]): String =
    mAgent.flatMap(agent => AgentProvider.allAgents.find(_._2.id == agent.id)).map(_._1).getOrElse("<none>")

}
