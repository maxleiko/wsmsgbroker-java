package fr.braindead.wsmsgbroker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.braindead.wsmsgbroker.actions.client.*;
import fr.braindead.wsmsgbroker.callback.*;
import fr.braindead.wsmsgbroker.protocol.Register;
import fr.braindead.wsmsgbroker.protocol.Send;
import fr.braindead.wsmsgbroker.protocol.Unregister;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by leiko on 31/10/14.
 */
public abstract class WSMsgBrokerClient implements IWSMsgBrokerClient {

    private String id;
    private WebSocketClient client;
    // Event maps
    private Map<String, ClientAction> actions = new HashMap<>();
    private Map<String, AnswerCallback> ack2callback = new HashMap<>();

    public WSMsgBrokerClient(String id, String host, int port) {
        this(id, host, port, "");
    }

    public WSMsgBrokerClient(String id, String host, int port, String path) {
        this.id = id;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // register client actions
        this.actions.put(Action.ANSWER.toString(), new AnswerAction());
        this.actions.put(Action.MESSAGE.toString(), new MessageAction());
        this.actions.put(Action.REGISTERED.toString(), new RegisteredAction());
        this.actions.put(Action.UNREGISTERED.toString(), new UnregisteredAction());

        // create WebSocket client
        this.client = new WebSocketClient(URI.create("ws://" + host + ":" + port + path)) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Register r = new Register();
                r.setId(WSMsgBrokerClient.this.id);
                this.send(new Gson().toJson(r));
            }

            @Override
            public void onMessage(String s) {
                JsonObject obj = (JsonObject) new JsonParser().parse(s);
                ClientAction action = actions.get(obj.getAsJsonPrimitive("action").getAsString());
                if (action != null) {
                    action.execute(WSMsgBrokerClient.this, this, obj);
                }
                // TODO handle errors
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                WSMsgBrokerClient.this.onClose(code, reason, remote);
            }

            @Override
            public void onError(Exception e) {
                WSMsgBrokerClient.this.onError(e);
            }
        };
    }

    @Override
    public void connect() {
        this.client.connect();
    }

    @Override
    public void connectBlocking() throws InterruptedException {
        this.client.connectBlocking();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Override
    public void closeBlocking() throws InterruptedException {
        if (this.client != null) {
            this.client.closeBlocking();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void send(Object data, String dest) {
        this.send(data, new String[]{dest});
    }

    @Override
    public void send(Object data, String[] dest) {
        Send s = new Send();
        s.setMessage(data);
        s.setDest(dest);
        this.client.send(new Gson().toJson(s));
    }

    @Override
    public void send(Object data, String dest, AnswerCallback callback) {
        this.send(data, new String[]{dest}, callback);
    }

    @Override
    public void send(Object data, String[] dest, AnswerCallback callback) {
        Send s = new Send();
        s.setMessage(data);
        s.setDest(dest);
        s.setAck(generateAck(callback));
        this.client.send(new Gson().toJson(s));
    }

    @Override
    public void unregister() {
        Unregister u = new Unregister();
        u.setId(id);
        this.client.send(new Gson().toJson(u));
    }

    @Override
    public AnswerCallback getAnswerCallback(String ack) {
        return this.ack2callback.get(ack);
    }

    private String generateAck(AnswerCallback callback) {
        UUID uuid = UUID.randomUUID();
        this.ack2callback.put(uuid.toString(), callback);
        return uuid.toString();
    }

    public abstract void onUnregistered(String id);

    public abstract void onRegistered(String id);

    public abstract void onMessage(Object msg, Response res);

    public abstract void onError(Exception e);

    public abstract void onClose(int code, String reason, boolean remote);
}
