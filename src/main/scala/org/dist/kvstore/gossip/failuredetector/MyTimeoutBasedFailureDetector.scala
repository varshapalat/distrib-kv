package org.dist.kvstore.gossip.failuredetector

import java.util

import scala.jdk.CollectionConverters._

class MyTimeoutBasedFailureDetector[T] extends FailureDetector[T] {
  val serverHeartBeatReceived = new util.HashMap[T, Long]
  val timeOutNanos = 100

  def heartBeatCheck(): Unit = {
    val currentTime: Long = System.nanoTime()
    val keys = serverHeartBeatReceived.keySet()
    for (key ← keys.asScala) {
      val lastReceivedNanos = serverHeartBeatReceived.get(key)
      val timeSinceLastHeartBeat = currentTime - lastReceivedNanos
      info(s"Time since last heartbeat from ${key} is ${timeSinceLastHeartBeat}")
      if (timeSinceLastHeartBeat >= timeOutNanos) {
        serverStates.put(key, ServerState.DOWN)
      }
    }
  }

  def heartBeatReceived(serverId: T): Unit = {
    val currentTime: Long = System.nanoTime()
    info(s"Heartbeat received from ${serverId} at ${currentTime}")
    serverHeartBeatReceived.put(serverId, currentTime)
    serverStates.put(serverId, ServerState.UP)
  }
}
