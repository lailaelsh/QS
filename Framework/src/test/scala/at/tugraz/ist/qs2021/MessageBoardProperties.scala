package at.tugraz.ist.qs2021

import at.tugraz.ist.qs2021.actorsystem.{Message, SimulatedActor}
import at.tugraz.ist.qs2021.messageboard.MessageStore.USER_BLOCKED_AT_COUNT
import at.tugraz.ist.qs2021.messageboard.UserMessage
import at.tugraz.ist.qs2021.messageboard.Worker.MAX_MESSAGE_LENGTH
import at.tugraz.ist.qs2021.messageboard.clientmessages._
import at.tugraz.ist.qs2021.messageboard.messagestoremessages.AddLike
import org.junit.runner.RunWith
import org.scalacheck.Prop.{classify, forAll}
import org.scalacheck.{Gen, Properties}

import scala.jdk.CollectionConverters._
import scala.util.control.Breaks.break

@RunWith(classOf[ScalaCheckJUnitPropertiesRunner])
class MessageBoardProperties extends Properties("MessageBoardProperties") {

  val validMessageGen: Gen[String] = Gen.asciiPrintableStr.map(s =>
    if (s.length <= MAX_MESSAGE_LENGTH) s else s.substring(0, MAX_MESSAGE_LENGTH)
  )

  def getSimilarity(l1: List[UserMessage], l2: List[UserMessage]) =
    l1.zip(l2).count({case (x,y) => x.toString != y.toString})


  property("message length: Publish + Ack [R1]") = forAll { (author: String, message: String) =>
    // arrange-  initialize the message board
    val sut = new SUTMessageBoard
    sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
    while (sut.getClient.receivedMessages.isEmpty)
      sut.getSystem.runFor(1)
    val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
    val worker: SimulatedActor = initAck.worker

    // act - send and receive the messages
    worker.tell(new Publish(new UserMessage(author, message), sut.getCommId))
    while (sut.getClient.receivedMessages.isEmpty)
      sut.getSystem.runFor(1)
    val reply = sut.getClient.receivedMessages.remove()

    worker.tell(new FinishCommunication(sut.getCommId))
    while (sut.getClient.receivedMessages.isEmpty)
      sut.getSystem.runFor(1)
    sut.getClient.receivedMessages.remove.asInstanceOf[FinishAck]

    // assert - define your property and check against it
    // The following classify is optional, it prints stats on the generated values.
    // But the check inside is required.
    classify(message.length <= MAX_MESSAGE_LENGTH, "valid message length", "invalid message length") {
      // if operationAck is received, the message length should be smaller or equal to 10
      reply.isInstanceOf[OperationAck] == message.length <= MAX_MESSAGE_LENGTH
    }
  }
  // todo: add another property for R1
//  R1 A message may only be stored if its text contains less than or exactly MAX MESSAGE LENGTH
//    (= 10) characters. This check is performed in the Worker class.
  property("another property for R1") =
    forAll("www", Gen.nonEmptyListOf(validMessageGen)) { (author: String, messages: List[String]) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop

      // act - send and receive the messages
      worker.tell(new Publish(new UserMessage(author, messages.last), sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply = sut.getClient.receivedMessages.remove()

      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()


      // assert - define your property and check against it
      // The following classify is optional, it prints stats on the generated values.
      // But the check inside is required.
      classify(messages.last.length <= MAX_MESSAGE_LENGTH, "valid message length", "invalid message length") {
        // if operationAck is received, the message length should be smaller or equal to 10
        reply.isInstanceOf[OperationAck] == messages.last.length <= MAX_MESSAGE_LENGTH
      }
    }

//  R2 A message may only be saved if no identical message has been saved yet. Two messages are
//  identical if both author and text of both messages are the same.

  property("property for R2") =
    forAll{ (author: String, message: String) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop

      val msg1 = new UserMessage(author, message)
      val msg2 = new UserMessage(author, message)

      // act - send and receive the messages
      worker.tell(new Publish(msg1, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Publish(msg2, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply2 = sut.getClient.receivedMessages.remove()



      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()


      reply2.isInstanceOf[OperationFailed] == (msg1.getMessage == msg2.getMessage) && (msg1.getAuthor == msg2.getAuthor)
    }
//  R3 A message may only be liked/disliked if it exists.
  property("property for R3") =
    forAll{ (author: String, message: String) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop

      val msg1 = new UserMessage(author, message)
      val msg2 = new UserMessage(author, message)

      // act - send and receive the messages
      worker.tell(new Publish(msg1, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Like("client ",sut.getCommId, msg1.getMessageId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply1 = sut.getClient.receivedMessages.remove()

      worker.tell(new Like("client ",sut.getCommId, msg2.getMessageId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply2 = sut.getClient.receivedMessages.remove()



      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()


      reply1.isInstanceOf[OperationAck] == true
      reply2.isInstanceOf[OperationAck] == false

    }

//  R4 A message may only be liked/disliked by users who have not yet liked/disliked the corresponding message.

  property("property for R4") =
    forAll{ (author: String, message: String) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop

      val msg1 = new UserMessage(author, message)
      val msg2 = new UserMessage(author, message)

      // act - send and receive the messages
      worker.tell(new Publish(msg1, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Dislike("client ",sut.getCommId, msg1.getMessageId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply1 = sut.getClient.receivedMessages.remove()

      worker.tell(new Dislike("client ",sut.getCommId, msg1.getMessageId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply2 = sut.getClient.receivedMessages.remove()



      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()


      reply1.isInstanceOf[OperationAck]
      !reply2.isInstanceOf[OperationAck]
    }

  //    R5 It should be possible to retrieve a list of all existing messages of an author.
  property("property for R5") =
    forAll(Gen.alphaStr, Gen.nonEmptyListOf(validMessageGen)) { (author: String, messages: List[String]) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop
      var msgs = List[UserMessage]()
      for (message <- messages) {
        msgs ::= new UserMessage(author, message)
      }

      for (msg <- msgs) {
        // act - send and receive the messages
        worker.tell(new Publish(msg, sut.getCommId))
        while (sut.getClient.receivedMessages.isEmpty)
          sut.getSystem.runFor(1)
        sut.getClient.receivedMessages.remove()
      }

      worker.tell(new RetrieveMessages(author, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply = sut.getClient.receivedMessages.remove()

      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()
      reply.isInstanceOf[FoundMessages]
//      var a = getSimilarity(msgs, reply.asInstanceOf[FoundMessages].messages.asInstanceOf[List[UserMessage]])
//      var x = true



//      for (m <- msgs; f <- reply.asInstanceOf[FoundMessages].messages.asInstanceOf[List[UserMessage]];
//           x = m.toString == f.toString
//           if(!x)
//           ) yield x

//      x
//      reply.isInstanceOf[FoundMessages]
    }

//  R6 It should be possible to search for messages containing a given text and get back list of those
//  messages.

  property("property for R6") =
    forAll(Gen.alphaStr, Gen.nonEmptyListOf(validMessageGen)) { (author: String, messages: List[String]) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop
      var msgs = List[UserMessage]()
      for (message <- messages) {
        msgs ::= new UserMessage(author, message)
      }

      for (msg <- msgs) {
        // act - send and receive the messages
        worker.tell(new Publish(msg, sut.getCommId))
        while (sut.getClient.receivedMessages.isEmpty)
          sut.getSystem.runFor(1)
        sut.getClient.receivedMessages.remove()
      }

      worker.tell(new SearchMessages(msgs(0).getMessage, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply = sut.getClient.receivedMessages.remove()

      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()


      reply.isInstanceOf[FoundMessages]
      msgs.size != 0 == reply.asInstanceOf[FoundMessages].messages.size() != 0
    }
//    R7 A user may report another user only if he has not previously reported the user in question.

  property("property for R7") =
    forAll(Gen.alphaStr, Gen.nonEmptyListOf(validMessageGen)) { (author: String, messages: List[String]) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop
      var msgs = List[UserMessage]()
      for (message <- messages) {
        msgs ::= new UserMessage(author, message)
      }

      for (msg <- msgs) {
        // act - send and receive the messages
        worker.tell(new Publish(msg, sut.getCommId))
        while (sut.getClient.receivedMessages.isEmpty)
          sut.getSystem.runFor(1)
        sut.getClient.receivedMessages.remove()
      }

      worker.tell(new Report("c1", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply = sut.getClient.receivedMessages.remove()

      worker.tell(new Report("c1", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply2 = sut.getClient.receivedMessages.remove()

      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()


      reply.isInstanceOf[OperationAck]
      reply2.isInstanceOf[OperationFailed]
    }

//    R8 If a user has been reported at least USER BLOCKED AT COUNT (= 6) times, he/she cannot send
//  any further Publish, Like, Dislike or Report messages.
  property("property for R8") =
    forAll{ (author: String, message: String) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop
      var msg = new UserMessage(author, message)
        // act - send and receive the messages
      worker.tell(new Publish(msg, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()


      worker.tell(new Report("c1", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Report("c2", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Report("c3", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Report("c4", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Report("c5", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Report("c6", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      worker.tell(new Like(author, sut.getCommId, msg.getMessageId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply = sut.getClient.receivedMessages.remove()

      worker.tell(new Dislike(author, sut.getCommId, msg.getMessageId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply1 = sut.getClient.receivedMessages.remove()


      worker.tell(new Publish(new UserMessage(author,message), sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply2 = sut.getClient.receivedMessages.remove()

      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()


      reply.isInstanceOf[UserBanned]
      reply2.isInstanceOf[UserBanned]
      reply1.isInstanceOf[UserBanned]
    }

//  R9 Successful requests should be confirmed by sending OperationAck. Requests are considered
//  successful when a message has been saved, a Like or Dislike has been added to a message, or
//  a report for an author has been added.

  property("property for R9") =
    forAll{ (author: String, message: String) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop
      var msg = new UserMessage(author, message)
//      var msg2 = new UserMessage(author, message)
      // act - send and receive the messages
      worker.tell(new Publish(msg, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      var reply = sut.getClient.receivedMessages.remove()


      worker.tell(new Report("c1", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      var reply2 = sut.getClient.receivedMessages.remove()


      worker.tell(new Like(author, sut.getCommId, msg.getMessageId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply3 = sut.getClient.receivedMessages.remove()





      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      reply.isInstanceOf[OperationAck] ==  message.length <= MAX_MESSAGE_LENGTH
      reply2.isInstanceOf[OperationAck] ==  message.length <= MAX_MESSAGE_LENGTH
      reply3.isInstanceOf[OperationAck] ==  message.length <= MAX_MESSAGE_LENGTH

    }

//    R10 Requests that are not successful should be confirmed by sending OperationFailed or
//  UserBanned.

  property("property for R10") =
    forAll{ (author: String, message: String) =>
      val sut = new SUTMessageBoard
      sut.getDispatcher.tell(new InitCommunication(sut.getClient, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val initAck = sut.getClient.receivedMessages.remove.asInstanceOf[InitAck]
      val worker: SimulatedActor = initAck.worker

      // here would be a worker.tell, e.g. in a loop
      var msg = new UserMessage(author, message)

      //      var msg2 = new UserMessage(author, message)
      // act - send and receive the messages
      worker.tell(new Publish(msg, sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      var reply = sut.getClient.receivedMessages.remove()


      worker.tell(new Report("c1", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      var reply2 = sut.getClient.receivedMessages.remove()

      worker.tell(new Report("c1", sut.getCommId, author))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      var reply_failed = sut.getClient.receivedMessages.remove()


      worker.tell(new Dislike(author, sut.getCommId, 12))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      val reply3 = sut.getClient.receivedMessages.remove()


      worker.tell(new FinishCommunication(sut.getCommId))
      while (sut.getClient.receivedMessages.isEmpty)
        sut.getSystem.runFor(1)
      sut.getClient.receivedMessages.remove()

      reply.isInstanceOf[OperationFailed] ==  message.length > MAX_MESSAGE_LENGTH
      reply2.isInstanceOf[OperationAck] ==  message.length <= MAX_MESSAGE_LENGTH
      reply_failed.isInstanceOf[OperationFailed]
      reply3.isInstanceOf[OperationFailed] ==  msg.getMessageId != 12

    }
}





