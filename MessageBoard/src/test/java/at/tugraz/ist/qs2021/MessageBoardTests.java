package at.tugraz.ist.qs2021;

import at.tugraz.ist.qs2021.actorsystem.ISimulatedActor;
import at.tugraz.ist.qs2021.actorsystem.Message;
import at.tugraz.ist.qs2021.actorsystem.SimulatedActor;
import at.tugraz.ist.qs2021.actorsystem.SimulatedActorSystem;
import at.tugraz.ist.qs2021.messageboard.*;
import at.tugraz.ist.qs2021.messageboard.clientmessages.*;
import at.tugraz.ist.qs2021.messageboard.dispatchermessages.Stop;
import at.tugraz.ist.qs2021.messageboard.dispatchermessages.StopAck;
import at.tugraz.ist.qs2021.messageboard.messagestoremessages.*;
import org.graalvm.compiler.nodes.calc.IntegerDivRemNode;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Simple actor, which can be used in tests, e.g. to check if the correct messages are sent by workers.
 * This actor can be sent to workers as client.
 */
class TestClient extends SimulatedActor {

    /**
     * Messages received by this actor.
     */
    final Queue<Message> receivedMessages;

    TestClient() {
        receivedMessages = new LinkedList<>();
    }

    /**
     * does not implement any logic, only saves the received messages
     *
     * @param message Non-null message received
     */
    @Override
    public void receive(Message message) {
        receivedMessages.add(message);
    }
}

class CustomMessage extends MessageStoreMessage {
    /**
     * The author of the message which should be looked up
     */

    public CustomMessage(long commId) {
        this.communicationId = commId;
    }
}

class TestClientMessage extends ClientMessage {
    public TestClientMessage(long communicationId) {
        super(communicationId);
    }

    @Override
    public int getDuration() {
        return 0;
    }
}



public class MessageBoardTests {

    /**
     * Simple first test initiating a communication and closing it afterwards.
     */
    @Test
    public void MessageStoreElse() throws UnknownClientException, InterruptedException {

        MessageStore msg = new MessageStore();
        CustomMessage new_msg = new CustomMessage(10);
        new_msg.storeClient = new SimulatedActor() {
            @Override
            public void receive(Message message){

            }
        };
        msg.receive(new_msg);
    }

    @Test(expected = Exception.class)
    public void SimulatedTickTimeSinceSystemStart() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        int time = client.getTimeSinceSystemStart();
        Assert.assertNotEquals(0, time);
        client.tick();
        Assert.assertFalse(client.getTimeSinceSystemStart() < time);
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Dislike("text3", 0, 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test
    public void DispatcherstoppingWorker() throws UnknownClientException, InterruptedException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        TestClient client2 = new TestClient();
        system.spawn(client2);


        MessageStore msgStore =  new MessageStore();
        Worker worker = new Worker(dispatcher, (SimulatedActor) msgStore, system);

        MessageStore msgStore2 =  new MessageStore();
        Worker worker2 = new Worker(dispatcher, (SimulatedActor) msgStore2, system);


        dispatcher.receive(new Stop());
        worker.tell(new Stop());
        worker2.tell(new Stop());

        Message Stop = new Stop();
        Assert.assertEquals(Stop.getDuration(), 2);
    }

    @Test(expected = Exception.class)
    public void ClientMessageException() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Stop());
        worker.tell(new TestClientMessage(12L));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }

    @Test
    public void ClientMessageDoubleStop() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Stop());
        worker.tell(new Stop());
        system.runFor(30);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
    }
    @Test
    public void ProcessClientMessage() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Stop());
        worker.tell(new TestClientMessage(10L));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();

        client.tell(new OperationFailed (10));
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);


    }
    @Test(expected = Exception.class)
    public void ProcessLikeException() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");

        Like likemessage = new Like("text3", 0, 0);
        worker.tell(new Publish(usertext1, 10));
        worker.tell(likemessage);
        Assert.assertEquals(likemessage.getDuration(), 1);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

    }
    @Test(expected = Exception.class)
    public void ProcessDislike() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Dislike("text3", 0, 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }


    @Test(expected = Exception.class)
    public void ProcessPublishException() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;

        worker.tell(new Publish(new UserMessage("usertext", "test"), 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }

    @Test
    public void ProcessPublishUpdate() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client2, 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        Message initAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage2.getClass());
        InitAck initAck2 = (InitAck) initAckMessage2;
        Assert.assertEquals(Long.valueOf(11), initAck2.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Publish(new UserMessage("usertext", "t"), 10));
        worker.tell(new SearchMessages("test", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message msg = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg.getClass());
        OperationAck opAck_ = (OperationAck) msg;
        Assert.assertEquals(Long.valueOf(10), opAck_.communicationId);

        SimulatedActor worker2 = initAck2.worker;
        worker2.tell(new Publish(new UserMessage("5r", "r"), 11));
        worker2.tell(new Publish(new UserMessage("5r", "r"), 11));
        worker2.tell(new SearchMessages("r", 11));
        system.runFor(30);

        Message msg1 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

        Message msg2 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, msg2.getClass());
        OperationFailed failed = (OperationFailed) msg2;
        Assert.assertEquals(Long.valueOf(11), failed.communicationId);

        Message msg3 = client2.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, msg3.getClass());
        FoundMessages found = (FoundMessages) msg3;
        Assert.assertFalse(found.messages.isEmpty());
    }

    @Test
    public void ReportersCount() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client2, 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        Message initAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage2.getClass());
        InitAck initAck2 = (InitAck) initAckMessage2;
        Assert.assertEquals(Long.valueOf(11), initAck2.communicationId);


        SimulatedActor worker = initAck.worker;

        worker.tell(new Report("c", 10, "c1"));
        worker.tell(new Report("c22", 10, "c1"));
        worker.tell(new Report("c23", 10, "c1"));
        worker.tell(new Report("c24", 10, "c1"));
        worker.tell(new Report("c25", 10, "c1"));
        worker.tell(new Publish(new UserMessage("c1", "t"), 10));
        worker.tell(new Report("c26", 10, "c1"));

        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message msg = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg.getClass());
        OperationAck opAck_ = (OperationAck) msg;
        Assert.assertEquals(Long.valueOf(10), opAck_.communicationId);

        SimulatedActor worker2 = initAck2.worker;
        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new SearchMessages("dd", 11));
            system.runFor(30);

        Message msg1 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

        Message msg2 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, msg2.getClass());
        OperationFailed opAck2 = (OperationFailed) msg2;
        Assert.assertEquals(Long.valueOf(11), opAck2.communicationId);

    }
    @Test
    public void UserBannedPublish() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client2, 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        Message initAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage2.getClass());
        InitAck initAck2 = (InitAck) initAckMessage2;
        Assert.assertEquals(Long.valueOf(11), initAck2.communicationId);


        SimulatedActor worker = initAck.worker;

        worker.tell(new Report("c", 10, "c1"));
        worker.tell(new Report("c22", 10, "c1"));
        worker.tell(new Report("c23", 10, "c1"));
        worker.tell(new Report("c24", 10, "c1"));
        worker.tell(new Report("c25", 10, "c1"));
        worker.tell(new Report("c26", 10, "c1"));

        worker.tell(new Publish(new UserMessage("c1", "t"), 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message msg = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg.getClass());
        OperationAck opAck_ = (OperationAck) msg;
        Assert.assertEquals(Long.valueOf(10), opAck_.communicationId);

        SimulatedActor worker2 = initAck2.worker;
        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new SearchMessages("dd", 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg1 = client2.receivedMessages.remove();
        client.receivedMessages.size();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new SearchMessages("dd", 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg2 = client2.receivedMessages.remove();
        Assert.assertEquals(UserBanned.class, msg2.getClass());
        UserBanned opAck2 = (UserBanned) msg2;
        Assert.assertEquals(Long.valueOf(11), opAck2.communicationId);
    }

    @Test
    public void UserBannedLike() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client2, 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        Message initAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage2.getClass());
        InitAck initAck2 = (InitAck) initAckMessage2;
        Assert.assertEquals(Long.valueOf(11), initAck2.communicationId);


        SimulatedActor worker = initAck.worker;

        worker.tell(new Report("c", 10, "c1"));
        worker.tell(new Report("c22", 10, "c1"));
        worker.tell(new Report("c23", 10, "c1"));
        worker.tell(new Report("c24", 10, "c1"));
        worker.tell(new Report("c25", 10, "c1"));
        worker.tell(new Report("c26", 10, "c1"));

        worker.tell(new Publish(new UserMessage("c1", "t"), 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message msg = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg.getClass());
        OperationAck opAck_ = (OperationAck) msg;
        Assert.assertEquals(Long.valueOf(10), opAck_.communicationId);

        SimulatedActor worker2 = initAck2.worker;
        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Like("c1", 11, 0));
        worker2.tell(new SearchMessages("dd", 11));

        system.runFor(30);

        Message msg1 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

        Message msg2 = client2.receivedMessages.remove();
        Assert.assertEquals(UserBanned.class, msg2.getClass());
        UserBanned opAck2 = (UserBanned) msg2;
        Assert.assertEquals(Long.valueOf(11), opAck2.communicationId);

        Message msg3 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, msg3.getClass());
        OperationFailed opAck3 = (OperationFailed) msg3;
        Assert.assertEquals(Long.valueOf(11), opAck3.communicationId);

        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Like("c1", 11, 0));
        while (client2.receivedMessages.size() < 2)
        system.runFor(30);

    }

    @Test
    public void UserBannedDislike() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client2, 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        Message initAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage2.getClass());
        InitAck initAck2 = (InitAck) initAckMessage2;
        Assert.assertEquals(Long.valueOf(11), initAck2.communicationId);


        SimulatedActor worker = initAck.worker;

        worker.tell(new Report("c", 10, "c1"));
        worker.tell(new Report("c22", 10, "c1"));
        worker.tell(new Report("c23", 10, "c1"));
        worker.tell(new Report("c24", 10, "c1"));
        worker.tell(new Report("c25", 10, "c1"));
        worker.tell(new Report("c26", 10, "c1"));

        worker.tell(new Publish(new UserMessage("c1", "t"), 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message msg = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg.getClass());
        OperationAck opAck_ = (OperationAck) msg;
        Assert.assertEquals(Long.valueOf(10), opAck_.communicationId);

        SimulatedActor worker2 = initAck2.worker;
        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Dislike("c1", 11, 0));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg1 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Dislike("c1", 11, 0));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg2 = client2.receivedMessages.remove();
        Assert.assertEquals(UserBanned.class, msg2.getClass());
        UserBanned opAck2 = (UserBanned) msg2;
        Assert.assertEquals(Long.valueOf(11), opAck2.communicationId);

    }

    @Test
    public void UserBannedReport() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client2, 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        Message initAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage2.getClass());
        InitAck initAck2 = (InitAck) initAckMessage2;
        Assert.assertEquals(Long.valueOf(11), initAck2.communicationId);


        SimulatedActor worker = initAck.worker;

        worker.tell(new Report("c", 10, "c1"));
        worker.tell(new Report("c22", 10, "c1"));
        worker.tell(new Report("c23", 10, "c1"));
        worker.tell(new Report("c24", 10, "c1"));
        worker.tell(new Report("c25", 10, "c1"));
        worker.tell(new Report("c26", 10, "c1"));

        worker.tell(new Publish(new UserMessage("c1", "t"), 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message msg = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg.getClass());
        OperationAck opAck_ = (OperationAck) msg;
        Assert.assertEquals(Long.valueOf(10), opAck_.communicationId);

        SimulatedActor worker2 = initAck2.worker;
        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Report("c1", 11, "c26"));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg1 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);


        worker2.tell(new Publish(new UserMessage("c1", "r"), 11));
        worker2.tell(new Report("c1", 11, "c26"));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg2 = client2.receivedMessages.remove();
        Assert.assertEquals(UserBanned.class, msg2.getClass());
        UserBanned opAck2 = (UserBanned) msg2;
        Assert.assertEquals(Long.valueOf(11), opAck2.communicationId);

    }


    @Test(expected = Exception.class)
    public void ProcessReportException() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Report("text3", 0, "usertextaa"));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test(expected = Exception.class)
    public void ProcessSearchMessageException() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new SearchMessages("text3", 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test(expected = Exception.class)
    public void processRetrieveMessages() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new RetrieveMessages("text3", 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test(expected = Exception.class)
    public void ProcessFinishException() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        InitCommunication initmessage = new InitCommunication(client, 10);
        dispatcher.tell(initmessage);
        Assert.assertEquals(initmessage.getDuration(), 2);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        Assert.assertEquals(initAckMessage.getDuration(), 1);
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");

        FinishCommunication finishmessage = new FinishCommunication(0);
        worker.tell(new Publish(usertext1, 10));
        worker.tell(finishmessage);
        Assert.assertEquals(finishmessage.getDuration(), 3);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

    }

    @Test
    public void ProcessStop() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");
        UserMessage usertext2 = new UserMessage("text2", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Publish(usertext2, 10));
        worker.tell(new Stop ());
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();

        client.tell(new OperationFailed (10));
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);


    }
    @Test
    public void ProcessSearchMessage() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("author", "client1");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new SearchMessages ("Client1", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);




    }

    @Test
    public void ProcessSearchMessage2() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;

        UserMessage usertext2 = new UserMessage("author", "test");
        worker.tell(new Publish(usertext2, 10));

        worker.tell(new SearchMessages ("Author", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

    }

    @Test
    public void ProcessSearchMessage3() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;

        UserMessage usertext2 = new UserMessage("test", "test");
        worker.tell(new Publish(usertext2, 10));

        worker.tell(new SearchMessages ("Author", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

    }

    @Test
    public void ProcessLikeWorkers() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Like ("Client1", 10,0));
        worker.tell(new Like ("Client1", 10,1));

        system.runFor(30);

        Assert.assertFalse(client.receivedMessages.size() < 3);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        Message text2 = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text2.getClass());
        OperationAck opAck2 = (OperationAck) text2;
        Assert.assertEquals(Long.valueOf(10), opAck2.communicationId);

        Message text3 = client.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, text3.getClass());
        OperationFailed opfailed = (OperationFailed) text3;
        Assert.assertEquals(Long.valueOf(10), opfailed.communicationId);
    }

    @Test
    public void ProcessLikeWorkers2() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Like ("Client1", 10,0));
        worker.tell(new Like ("Client1", 10,0));
        system.runFor(30);

        Assert.assertFalse(client.receivedMessages.size() < 3);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        Message text2 = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text2.getClass());
        OperationAck opAck2 = (OperationAck) text2;
        Assert.assertEquals(Long.valueOf(10), opAck2.communicationId);

        Message text3 = client.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, text3.getClass());
        OperationFailed failed = (OperationFailed) text3;
        Assert.assertEquals(Long.valueOf(10), failed.communicationId);

    }
    @Test
    public void testCommunication() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;
        Assert.assertEquals(finAck.getDuration(),1);

        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
    }



    @Test
    public void testMoreClients() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 5);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        TestClient client3 = new TestClient();
        TestClient client4 = new TestClient();
        system.spawn(client);
        system.spawn(client2);
        system.spawn(client3);
        system.spawn(client4);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client2, 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client3, 12));
        while (client3.receivedMessages.size() == 0)
            system.runFor(1);
        dispatcher.tell(new InitCommunication(client4, 13));
        while (client4.receivedMessages.size() == 0)
            system.runFor(1);


        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        Message initAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage2.getClass());
        InitAck initAck2 = (InitAck) initAckMessage2;
        Assert.assertEquals(Long.valueOf(11), initAck2.communicationId);

        Message initAckMessage3 = client3.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage3.getClass());
        InitAck initAck3 = (InitAck) initAckMessage3;
        Assert.assertEquals(Long.valueOf(12), initAck3.communicationId);

        Message initAckMessage4 = client4.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage4.getClass());
        InitAck initAck4 = (InitAck) initAckMessage4;
        Assert.assertEquals(Long.valueOf(13), initAck4.communicationId);


        SimulatedActor worker = initAck.worker;
        SimulatedActor worker2 = initAck2.worker;
        SimulatedActor worker3 = initAck3.worker;
        SimulatedActor worker4 = initAck4.worker;
        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;

        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        worker2.tell(new FinishCommunication(11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage2.getClass());
        FinishAck finAck2 = (FinishAck) finAckMessage2;

        Assert.assertEquals(Long.valueOf(11), finAck2.communicationId);
        worker3.tell(new FinishCommunication(12));
        while (client3.receivedMessages.size() == 0)
            system.runFor(1);
        Message finAckMessage3 = client3.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage3.getClass());
        FinishAck finAck3 = (FinishAck) finAckMessage3;

        Assert.assertEquals(Long.valueOf(12), finAck3.communicationId);
        worker4.tell(new FinishCommunication(13));
        while (client4.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage4 = client4.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage4.getClass());
        FinishAck finAck4 = (FinishAck) finAckMessage4;

        Assert.assertEquals(Long.valueOf(13), finAck4.communicationId);
        dispatcher.tell(new Stop());
    }

    @Test
    public void ActorCheck() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        TestClient client2 = new TestClient();
        system.spawn(client);
        TestClient client3 = new TestClient();
        system.spawn(client);
        Assert.assertEquals(system.getActors().size(), 5);
    }

    @Test
    public void IdCheck() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        TestClient client1 = new TestClient();
        system.spawn(client1);


        Assert.assertEquals(dispatcher.getId(), 0);
        Assert.assertEquals(client.getId(), 2);
        Assert.assertEquals(client1.getId(), 3);
    }

    @Test
    public void CurrentTimeCheck() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        system.runUntil(100);

        SimulatedActorSystem runUntil = new SimulatedActorSystem();
        Assert.assertEquals(SimulatedActorSystem.class, runUntil.getClass());
        while (client.receivedMessages.size() == 0)
            system.runUntil(system.getCurrentTime());

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(system.getCurrentTime());

        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;

        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
        client.getTimeSinceSystemStart();
        system.stop(client);
    }
    @Test
    public void DispatcherstoppingInitCommunication() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        TestClient client2 = new TestClient();
        system.spawn(client2);

        dispatcher.receive(new Stop());
        dispatcher.receive(new InitCommunication(client, 10));

        Message Stop = new Stop();
        Assert.assertEquals(Stop.getDuration(), 2);
    }
    @Test
    public void DispatcherstoppingStopAttack() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        TestClient client2 = new TestClient();
        system.spawn(client2);

        dispatcher.receive(new Stop());
        dispatcher.receive(new StopAck(client));


        Message Stop = new Stop();
        Assert.assertEquals(Stop.getDuration(), 2);
    }


    @Test
    public void DispatchergetDuration() throws UnknownClientException {

            SimulatedActorSystem system = new SimulatedActorSystem();
            Dispatcher dispatcher = new Dispatcher(system, 0);
            system.spawn(dispatcher);
            TestClient client = new TestClient();
            system.spawn(client);

            TestClient client2 = new TestClient();
            system.spawn(client2);

            Message Stop = new Stop();
            Assert.assertEquals(Stop.getDuration(), 2);
    }

    @Test
    public void StopAckDuration() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        Message StopAck = new StopAck(client);
        Assert.assertEquals(StopAck.getDuration(), 2);
    }
    @Test
    public void PublishEdgeCase() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


        Message initAckMessage = null;
        initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);


        SimulatedActor worker = initAck.worker;

        UserMessage user_message = new UserMessage("usertextaa", "Halloooooooooooooooooo");
        Message publish = new Publish(user_message, 10);
        client.tell(new OperationFailed(10));
        worker.receive(publish);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        worker.tell(new OperationFailed(10));
        dispatcher.tell(new Stop());

    }
    @Test
    public void Publish() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


        Message initAckMessage = null;
        initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);


        SimulatedActor worker = initAck.worker;

        UserMessage user_message = new UserMessage("usertextaa", "Hallo");

        Message publish = new Publish(user_message, 10);
        client.tell(new OperationFailed(10));
        worker.receive(publish);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        worker.tell(new OperationFailed(10));
        dispatcher.tell(new Stop());

    }

    @Test
    public void RetrievedMessage() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usermsg = new UserMessage("text23", "hallo");

        worker.tell(new Publish(usermsg, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message message_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, message_que.getClass());
        OperationAck opAck = (OperationAck) message_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        worker.tell(new RetrieveMessages("text21", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        FoundMessages resMessage = (FoundMessages) message_que;
        Assert.assertEquals(resMessage.getDuration(), 1);
        Assert.assertEquals(Long.valueOf(10), resMessage.communicationId);

        Message retrieve = new RetrieveMessages("text23", 10);
        worker.receive(retrieve);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        resMessage = (FoundMessages) message_que;
        Assert.assertFalse(resMessage.messages.isEmpty());

        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;

        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
        Report report = new Report("text2", 10, "text4") ;
        SimulatedActor client4;
        client4 = initAck.worker;
        MessageStoreMessage reportedMessage = new AddReport(report.clientName, report.communicationId, report.reportedClientName);
        WorkerHelper helper = new WorkerHelper(client4, client, reportedMessage, system);
        system.spawn(helper);


    }

    @Test
    public void ProcessLikeGetDuration() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usermsg = new UserMessage("text2", "hallo");

        worker.tell(new Publish(usermsg, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message message_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, message_que.getClass());
        OperationAck opAck = (OperationAck) message_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        RetrieveMessages retrievemessage  = new RetrieveMessages("text2", 10);
        worker.tell(retrievemessage);
        Assert.assertEquals(retrievemessage.getDuration(),3);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        FoundMessages resMessage = (FoundMessages) message_que;
        Assert.assertEquals(Long.valueOf(10), resMessage.communicationId);

        Message retrieve = new RetrieveMessages("text2", 10);
        worker.receive(retrieve);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        resMessage = (FoundMessages) message_que;
        Assert.assertEquals(Long.valueOf(10), resMessage.communicationId);

        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;


        Like like = new Like("text2", 10, 2);
        like.getDuration();
        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
        Report report = new Report("text2", 10, "text4") ;
        SimulatedActor client4;
        client4 = initAck.worker;
        MessageStoreMessage reportedMessage = new AddReport(report.clientName, report.communicationId, report.reportedClientName);
        WorkerHelper helper = new WorkerHelper(client4, client, reportedMessage, system);
        system.spawn(helper);

        SimulatedActor client5 = initAck.worker;
        MessageStoreMessage retrievedMessages = new AddLike(like.clientName, like.messageId, like.communicationId);
        WorkerHelper helper2 = new WorkerHelper(client5, client, retrievedMessages, system);
        system.spawn(helper2);
    }


    @Test
    public void ProcessLike2() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usermsg = new UserMessage("text2", "hallo");

        worker.tell(new Publish(usermsg, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message message_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, message_que.getClass());
        OperationAck opAck = (OperationAck) message_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);
        Assert.assertEquals(opAck.getDuration(), 1);

        worker.tell(new RetrieveMessages("text2", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        FoundMessages resMessage = (FoundMessages) message_que;
        Assert.assertEquals(Long.valueOf(10), resMessage.communicationId);

        Message retrieve = new RetrieveMessages("text2", 10);
        worker.receive(retrieve);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        resMessage = (FoundMessages) message_que;
        Assert.assertEquals(Long.valueOf(10), resMessage.communicationId);

        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;


        Like like = new Like("text2", 10, 2);
        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
        Report report = new Report("text2", 10, "text4") ;
        SimulatedActor client4;
        client4 = initAck.worker;
        MessageStoreMessage reportedMessage = new AddReport(report.clientName, report.communicationId, report.reportedClientName);
        WorkerHelper helper = new WorkerHelper(client4, client, reportedMessage, system);
        system.spawn(helper);
        MessageStoreMessage retrievedMessages = new AddLike(like.clientName, like.messageId, like.communicationId);
        WorkerHelper helper2 = new WorkerHelper(client, client, retrievedMessages, system);
        MessageStore msgStore =  new MessageStore();
        Worker w = new Worker(dispatcher, (SimulatedActor) msgStore, system);

        SimulatedActor worker1 = new Worker(dispatcher, msgStore  ,system);

        worker1.receive(message_que);
        WorkerHelper helper3 = new WorkerHelper(msgStore, client, retrievedMessages, system);
        system.spawn(helper3);



    }

    @Test
    public void ProcessDisklike() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage m = new UserMessage("usertext", "test");

        Message mess = new Publish(m, 10);

        worker.tell(mess);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message remove_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que.getClass());
        OperationAck opAck = (OperationAck) remove_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);
        Message msg = new Dislike("Client1", 10, 0);
        worker.tell(msg);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        remove_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que.getClass());
        opAck = (OperationAck) remove_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        SimulatedActor worker2 = initAck.worker;
        UserMessage m2 = new UserMessage("usertext", "test");

        Message mess2 = new Report("Client2", 10, "Client1");

        worker2.tell(mess2);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message remove_que2 = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que2.getClass());
        OperationAck opAck2 = (OperationAck) remove_que2;
        Assert.assertEquals(Long.valueOf(10), opAck2.communicationId);
        Message msg2 = new Dislike("Client2", 10, 0);
        worker2.tell(msg2);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        remove_que2 = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que2.getClass());
        opAck2 = (OperationAck) remove_que2;
        Assert.assertEquals(Long.valueOf(10), opAck2.communicationId);

        worker.tell(new Dislike("Client2", 10, 0));
        worker.tell(new Dislike("Client2", 10, 1));

        system.runFor(30);

        Assert.assertFalse(client.receivedMessages.size() < 2);

        Message failed = client.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, failed.getClass());

        Message text3 = client.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, text3.getClass());
        dispatcher.tell(new Stop());

    }


    @Test
    public void Likes() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage m = new UserMessage("usertext", "test");

        Message mess = new Publish(m, 10);

        worker.tell(mess);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message remove_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que.getClass());
        OperationAck opAck = (OperationAck) remove_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);
        Message msg = new Dislike("Client1", 10, 0);
        worker.tell(msg);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        remove_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que.getClass());
        opAck = (OperationAck) remove_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);




        SimulatedActor worker2 = initAck.worker;

        Message mess2 = new Report("Client2", 10, "Client1");
        worker2.tell(mess2);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message remove_que2 = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que2.getClass());
        OperationAck opAck2 = (OperationAck) remove_que2;
        Assert.assertEquals(Long.valueOf(10), opAck2.communicationId);
        Message msg2 = new Dislike("Client2", 10, 0);
        worker2.tell(msg2);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        remove_que2 = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que2.getClass());
        opAck2 = (OperationAck) remove_que2;
        Assert.assertEquals(Long.valueOf(10), opAck2.communicationId);

        worker.tell(new Report("Client2", 10, "Client1"));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message failed = client.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, failed.getClass());



        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;
        client.tell(finAckMessage);
        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());

        SearchInStore search = new SearchInStore("usertext", 10);
        search.getDuration();

    }

    @Test
    public void Userband() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        Message StopAck = new StopAck(client);
        Assert.assertEquals(StopAck.getDuration(), 2);

        UserBanned user = new UserBanned(10);
        user.getDuration();
    }

    @Test
    public void SearchMessages() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        Message StopAck = new StopAck(client);
        Assert.assertEquals(StopAck.getDuration(), 2);

        UserBanned user = new UserBanned(10);
        user.getDuration();

        SearchMessages search = new SearchMessages("search", 10);
        search.getDuration();
    }
    @Test
    public void UnkownClientExecption() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        Message StopAck = new StopAck(client);
        Assert.assertEquals(StopAck.getDuration(), 2);

        UserBanned user = new UserBanned(10);
        user.getDuration();

        SearchMessages search = new SearchMessages("search", 10);
        search.getDuration();

        UnknownClientException exception2 = new UnknownClientException("test");
        exception2.getMessage();
    }

    @Test
    public void ToString() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        UserMessage usermsg = new UserMessage("test", "test");
        usermsg.toString();

        UserBanned user = new UserBanned(10);
        user.getDuration();

        SearchMessages search = new SearchMessages("search", 10);
        search.getDuration();

        UnknownClientException exception2 = new UnknownClientException("test");
        exception2.getMessage();

    }

    @Test
    public void StoppingDispatcher() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        Message forStop = new Stop();
        Assert.assertEquals(forStop.getDuration(), 2);
        dispatcher.receive(forStop);
        dispatcher.tell(new Stop());
    }

    @Test
    public void ProcessStopWorker() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        client.getMessageLog();
        Assert.assertNotEquals(0,client.getMessageLog().size());
        SimulatedActor worker = initAck.worker;
        UserMessage usermsg = new UserMessage("text2", "test");
        Message publish = new Publish(usermsg, 10);
        worker.receive(publish);
        system.runFor(30);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);
        worker.tell(new OperationFailed(10));


        dispatcher.tell(new StopAck(worker));
    }

    @Test
    public void TickCheck() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client1 = new TestClient();
        TestClient client2 = new TestClient();

        system.spawn(client1);
        system.spawn(client2);
        dispatcher.tell(new InitCommunication(client1, 10));
        dispatcher.tell(new InitCommunication(client2, 11));
        while (client1.receivedMessages.size() == 0)
            system.runFor(1);
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);


        Message initAckMessage1 = client1.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage1.getClass());
        InitAck initAck1 = (InitAck) initAckMessage1;
        Assert.assertEquals(Long.valueOf(10), initAck1.communicationId);

        Message initAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage2.getClass());
        InitAck initAck2 = (InitAck) initAckMessage2;
        Assert.assertEquals(Long.valueOf(11), initAck2.communicationId);
        UserMessage usermsg1 = new UserMessage("text2", "test");
        UserMessage usermsg2 = new UserMessage("text2", "test");


        dispatcher.tell(new Stop());
        Report report = new Report("text2", 10, "text4") ;
        SimulatedActor simulated_client;
        simulated_client = initAck1.worker;
        MessageStoreMessage reportedMessage = new AddReport(report.clientName, report.communicationId, report.reportedClientName);
        WorkerHelper helper = new WorkerHelper(simulated_client, client1, reportedMessage, system);
        system.spawn(helper);

        for (int i = 0; i < 12; i++)
            helper.tick();
    }

    @Test(expected = Exception.class)
    public void ClientMessageException2() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Stop());
        worker.tell(new TestClientMessage(12L));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

    }


    @Test
    public void ProcessLikeWorkers3() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Like ("Client1", 10,0));
        worker.tell(new Publish (usertext1, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);
    }


    @Test
    public void ProcessDislikeWorkers() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");
        Dislike dmessage = new Dislike ("Client1", 10,0);

        worker.tell(new Publish(usertext1, 10));
        worker.tell(dmessage);
        Assert.assertEquals(dmessage.getDuration(), 1);
        worker.tell(new Publish (usertext1, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);
    }

    @Test
    public void ProcessDislikeWorkers2() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");
        UserMessage usertext2 = new UserMessage("text2", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new CustomMessage(10));
        worker.tell(new Publish(usertext2, 10));

        system.runFor(30);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);
    }

    @Test
    public void ProcessIDWorkers() throws UnknownClientException {
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("usertext", "test");
        usertext1.setMessageId(1);

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Dislike ("Client1", 10,0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
    }

    @Test
    public void DispatcherstoppingStopAttack2() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 4);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        TestClient client2 = new TestClient();
        system.spawn(client2);

        dispatcher.receive(new Stop());
        dispatcher.receive(new StopAck(client));


        Message Stop = new Stop();
        Assert.assertEquals(Stop.getDuration(), 2);
    }

    @Test
    public void DispatcherstoppingStopAttack3() throws UnknownClientException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 4);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        TestClient client2 = new TestClient();
        system.spawn(client2);

        dispatcher.receive(new TestClientMessage(10));
        dispatcher.receive(new StopAck(client));


        Message Stop = new Stop();
        Assert.assertEquals(Stop.getDuration(), 2);
    }






}



