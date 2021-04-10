package at.tugraz.ist.qs2021;

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

class CostumMessage extends MessageStoreMessage {
    /**
     * The author of the message which should be looked up
     */

    public CostumMessage(long commId) {
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
    public void DispatcherstoppingWorker() throws UnknownClientException, InterruptedException {

        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        TestClient client2 = new TestClient();
        system.spawn(client2);

        //client.tell(new Stop());


        MessageStore msgStore =  new MessageStore();
        Worker worker = new Worker(dispatcher, (SimulatedActor) msgStore, system);

        MessageStore msgStore2 =  new MessageStore();
        Worker worker2 = new Worker(dispatcher, (SimulatedActor) msgStore2, system);


        dispatcher.receive(new Stop());
        worker.tell(new Stop());
        worker2.tell(new Stop());

        //dispatcher.wait(10L, 10);
        //dispatcher.receive(new Stop());


        Message Stop = new Stop();
        Assert.assertEquals(Stop.getDuration(), 2);
    }

    @Test(expected = Exception.class)
    public void ClientMessageException() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Stop());
        worker.tell(new TestClientMessage(12L));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test
    public void ProcessClientMessage() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");

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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Like("nila", 0, 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test(expected = Exception.class)
    public void ProcessDislike() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Dislike("nila", 0, 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }


    @Test(expected = Exception.class)
    public void ProcessPublishException() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;

        worker.tell(new Publish(new UserMessage("5ara", "test"), 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }

    @Test
    public void ProcessPublishUpdate() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);

        // send request and run system until a response is received
        // communication id is chosen by clients
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
        UserMessage usertext1 = new UserMessage("5ara", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Publish(new UserMessage("5ara", "t"), 10));
        worker.tell(new SearchMessages("nila", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
        Message msg = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg.getClass());
        OperationAck opAck_ = (OperationAck) msg;
        Assert.assertEquals(Long.valueOf(10), opAck_.communicationId);

        SimulatedActor worker2 = initAck2.worker;
        worker2.tell(new Publish(new UserMessage("5r", "r"), 11));
        worker2.tell(new Publish(new UserMessage("5r", "r"), 11));
        worker2.tell(new SearchMessages("dd", 11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg1 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

    }

    @Test
    public void ReportersCount() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);

        // send request and run system until a response is received
        // communication id is chosen by clients
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
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg1 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

    }
    @Test
    public void UserBannedPublish() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);

        // send request and run system until a response is received
        // communication id is chosen by clients
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
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");

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
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

    }

    @Test
    public void UserBannedLike() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);

        // send request and run system until a response is received
        // communication id is chosen by clients
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
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message msg1 = client2.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, msg1.getClass());
        OperationAck opAck1 = (OperationAck) msg1;
        Assert.assertEquals(Long.valueOf(11), opAck1.communicationId);

    }

    @Test
    public void UserBannedDislike() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);

        // send request and run system until a response is received
        // communication id is chosen by clients
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

    }

    @Test
    public void UserBannedReport() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        TestClient client2 = new TestClient();
        system.spawn(client);
        system.spawn(client2);

        // send request and run system until a response is received
        // communication id is chosen by clients
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

    }


    @Test(expected = Exception.class)
    public void ProcessReportException() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Report("nila", 0, "5araaa"));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test(expected = Exception.class)
    public void ProcessSearchMessageException() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new SearchMessages("nila", 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test(expected = Exception.class)
    public void processRetrieveMessages() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new RetrieveMessages("nila", 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


    }
    @Test(expected = Exception.class)
    public void ProcessFinishException() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new FinishCommunication(0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

    }

    @Test
    public void ProcessStop() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");
        UserMessage usertext3 = new UserMessage("zeft", "test");

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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Like ("Client1", 10,0));
       // worker.tell(new Like ("Client1", 10,0));
        worker.tell(new Like ("Client1", 10,1));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

    }

    @Test
    public void ProcessLikeWorkers2() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Like ("Client1", 10,0));
        worker.tell(new Like ("Client1", 10,0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

    }
    @Test
    public void testCommunication() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;

        // end the communication
        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;

        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());

        // TODO: run system until workers and dispatcher are stopped
    }

    // TODO: Implement test cases

    @Test
    public void testMoreClients() throws UnknownClientException {
        // testing only the acks
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

        // send request and run system until a response is received
        // communication id is chosen by clients
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

        // end the communication client 1
        worker.tell(new FinishCommunication(10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage.getClass());
        FinishAck finAck = (FinishAck) finAckMessage;

        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);

        // end the communication client 2
        worker2.tell(new FinishCommunication(11));
        while (client2.receivedMessages.size() == 0)
            system.runFor(1);

        Message finAckMessage2 = client2.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage2.getClass());
        FinishAck finAck2 = (FinishAck) finAckMessage2;

        Assert.assertEquals(Long.valueOf(11), finAck2.communicationId);

        // end the communication client 3
        worker3.tell(new FinishCommunication(12));
        while (client3.receivedMessages.size() == 0)
            system.runFor(1);
        Message finAckMessage3 = client3.receivedMessages.remove();
        Assert.assertEquals(FinishAck.class, finAckMessage3.getClass());
        FinishAck finAck3 = (FinishAck) finAckMessage3;

        Assert.assertEquals(Long.valueOf(12), finAck3.communicationId);

        // end the communication client 4
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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 0);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);
        TestClient client2 = new TestClient();
        system.spawn(client);
        TestClient client3 = new TestClient();
        system.spawn(client);

        //SimulatedActorSystem + Dispatcher + 3 clients
        Assert.assertEquals(system.getActors().size(), 5);
    }

    @Test
    public void IdCheck() throws UnknownClientException {
        // testing only the acks
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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
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

        // end the communication
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

        UserMessage user_message = new UserMessage("5araaa", "Halloooooooooooooooooo");
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

        UserMessage user_message = new UserMessage("5araaa", "Hallo");

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
        UserMessage usermsg = new UserMessage("zeft3", "hallo");

        worker.tell(new Publish(usermsg, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message message_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, message_que.getClass());
        OperationAck opAck = (OperationAck) message_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        worker.tell(new RetrieveMessages("zeft1", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        FoundMessages resMessage = (FoundMessages) message_que;
        Assert.assertEquals(Long.valueOf(10), resMessage.communicationId);

        Message retrieve = new RetrieveMessages("zeft", 10);
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

        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
        Report report = new Report("zeft", 10, "gela") ;
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
        UserMessage usermsg = new UserMessage("zeft", "hallo");

        worker.tell(new Publish(usermsg, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message message_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, message_que.getClass());
        OperationAck opAck = (OperationAck) message_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        worker.tell(new RetrieveMessages("zeft", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        FoundMessages resMessage = (FoundMessages) message_que;
        Assert.assertEquals(Long.valueOf(10), resMessage.communicationId);

        Message retrieve = new RetrieveMessages("zeft", 10);
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


        Like like = new Like("zeft", 10, 2);
        like.getDuration();
        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
        Report report = new Report("zeft", 10, "gela") ;
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
        UserMessage usermsg = new UserMessage("zeft", "hallo");

        worker.tell(new Publish(usermsg, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message message_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, message_que.getClass());
        OperationAck opAck = (OperationAck) message_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        worker.tell(new RetrieveMessages("zeft", 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        message_que = client.receivedMessages.remove();
        Assert.assertEquals(FoundMessages.class, message_que.getClass());
        FoundMessages resMessage = (FoundMessages) message_que;
        Assert.assertEquals(Long.valueOf(10), resMessage.communicationId);

        Message retrieve = new RetrieveMessages("zeft", 10);
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


        Like like = new Like("zeft", 10, 2);
        Assert.assertEquals(Long.valueOf(10), finAck.communicationId);
        dispatcher.tell(new Stop());
        Report report = new Report("zeft", 10, "gela") ;
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
        UserMessage m = new UserMessage("5ara", "test");

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
        //worker.tell(new Dislike("Client1", 10, 0));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        remove_que = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que.getClass());
        opAck = (OperationAck) remove_que;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);

        SimulatedActor worker2 = initAck.worker;
        UserMessage m2 = new UserMessage("5ara", "test");

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
       // worker2.tell(new Dislike("Client2", 10, 1));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        remove_que2 = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, remove_que2.getClass());
        opAck2 = (OperationAck) remove_que2;
        Assert.assertEquals(Long.valueOf(10), opAck2.communicationId);

        worker.tell(new Dislike("Client2", 10, 0));
        worker.tell(new Dislike("Client2", 10, 1));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message failed = client.receivedMessages.remove();
        Assert.assertEquals(OperationFailed.class, failed.getClass());

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
        UserMessage m = new UserMessage("5ara", "test");

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
        UserMessage m2 = new UserMessage("5ara", "test");

        Message mess2 = new Report("Client2", 10, "Client1");
        Message messClient = new OperationAck(10);
        worker2.tell(mess2);

        //client.tell(messClient);
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

        SearchInStore search = new SearchInStore("5ara", 10);
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
        // kann nicht von recive aufgerufen werden
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);


        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        client.getMessageLog();
        SimulatedActor worker = initAck.worker;
        UserMessage usermsg = new UserMessage("zeft", "test");
        Message publish = new Publish(usermsg, 10);
        worker.receive(publish);
        while (client.receivedMessages.size() == 0)
            system.runFor(1);
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

        // send request and run system until a response is received
        // communication id is chosen by clients
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

        SimulatedActor worker1 = initAck1.worker;
        SimulatedActor worker2 = initAck2.worker;
        UserMessage usermsg1 = new UserMessage("zeft", "test");
        UserMessage usermsg2 = new UserMessage("zeft", "test");
        Message publish1 = new Publish(usermsg1, 10);
        Message publish2 = new Publish(usermsg2, 11);


        dispatcher.tell(new Stop());
        Report report = new Report("zeft", 10, "gela") ;
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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");


        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Stop());
        worker.tell(new TestClientMessage(12L));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

    }


    @Test
    public void ProcessLikeWorkers3() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");

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
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        UserMessage usertext2 = new UserMessage("zeft", "test");

        worker.tell(new Publish(usertext1, 10));
        worker.tell(new Dislike ("Client1", 10,0));
        worker.tell(new Publish (usertext1, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message text = client.receivedMessages.remove();
        Assert.assertEquals(OperationAck.class, text.getClass());
        OperationAck opAck = (OperationAck) text;
        Assert.assertEquals(Long.valueOf(10), opAck.communicationId);
    }

    @Test
    public void ProcessIDWorkers() throws UnknownClientException {
        // testing only the acks
        SimulatedActorSystem system = new SimulatedActorSystem();
        Dispatcher dispatcher = new Dispatcher(system, 2);
        system.spawn(dispatcher);
        TestClient client = new TestClient();
        system.spawn(client);

        // send request and run system until a response is received
        // communication id is chosen by clients
        dispatcher.tell(new InitCommunication(client, 10));
        while (client.receivedMessages.size() == 0)
            system.runFor(1);

        Message initAckMessage = client.receivedMessages.remove();
        Assert.assertEquals(InitAck.class, initAckMessage.getClass());
        InitAck initAck = (InitAck) initAckMessage;
        Assert.assertEquals(Long.valueOf(10), initAck.communicationId);

        SimulatedActor worker = initAck.worker;
        UserMessage usertext1 = new UserMessage("5ara", "test");
        usertext1.setMessageId(1);
        UserMessage usertext2 = new UserMessage("zeft", "test");

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



