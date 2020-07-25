package org.dist.kvstore.gossip.failuredetector

import org.dist.kvstore.gossip.failuredetector.heartbeat.Peer
import org.dist.kvstore.network.{InetAddressAndPort, Networks}
import org.dist.kvstore.TestUtils
import org.scalatest.FunSuite

class FailureDetectorTests extends FunSuite {

  test("TimeoutBasedFailure Detector should detect liveness with heatbeat") {
    val localHost = new Networks().hostname()
    val senderIp = InetAddressAndPort.create(localHost, TestUtils.choosePort())
    val receiverIp = InetAddressAndPort.create(localHost, TestUtils.choosePort())

    val sender = new Sender(1, List(Peer(2, receiverIp)))
    val receiver = new Receiver(receiverIp, List(Peer(1, senderIp)), new MyTimeoutBasedFailureDetector[Int]())

    sender.start()
    receiver.start()

    TestUtils.waitUntilTrue(() ⇒ {
      receiver.failureDetector.isAlive(1)
    }, "Waiting for server 1 to be detected as alive")

    sender.stop()

    TestUtils.waitUntilTrue(() ⇒ {
      receiver.failureDetector.isAlive(1) == false
    }, "Waiting for server 1 to be detected as crashed")

    receiver.stop
  }


  test("Phi Accrual Failure Detector should detect liveness with heatbeat") {
    val localHost = new Networks().hostname()
    val senderIp = InetAddressAndPort.create(localHost, TestUtils.choosePort())
    val receiverIp = InetAddressAndPort.create(localHost, TestUtils.choosePort())

    val sender = new Sender(1, List(Peer(2, receiverIp)))
    val receiver = new Receiver(receiverIp, List(Peer(1, senderIp)), new PhiChiAccrualFailureDetector[Int]())

    sender.start()
    receiver.start()

    TestUtils.waitUntilTrue(() ⇒ {
      receiver.failureDetector.isAlive(1)
    }, "Waiting for server 1 to be detected as alive")

    Thread.sleep(1000) //take some time for multiple heartbeats to reach receiver to calculate phi values better
    sender.stop()

    TestUtils.waitUntilTrue(() ⇒ {
      receiver.failureDetector.isAlive(1) == false
    }, "Waiting for server 1 to be detected as crashed")

    receiver.stop
  }
}
