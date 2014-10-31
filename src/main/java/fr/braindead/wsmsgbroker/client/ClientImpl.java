package fr.braindead.wsmsgbroker.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.braindead.wsmsgbroker.actions.client.*;
import fr.braindead.wsmsgbroker.callback.AnswerCallback;
import fr.braindead.wsmsgbroker.callback.ErrorCallback;
import fr.braindead.wsmsgbroker.callback.MessageCallback;
import fr.braindead.wsmsgbroker.callback.RegisteredCallback;
import fr.braindead.wsmsgbroker.protocol.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by leiko on 30/10/14.
 */
public class ClientImpl implements Client {

    private String id;
    private WebSocketClient client;
    // Event maps
    private Map<String, ClientAction> actions = new HashMap<>();
    private Map<String, AnswerCallback> ack2callback = new HashMap<>();
    // Event handlers
    private MessageCallback emitMessage;
    private ErrorCallback emitError;
    private RegisteredCallback emitRegistered;

    public ClientImpl(String id, String host, int port) throws InterruptedException {
        this(id, host, port, "");
    }

    public ClientImpl(String id, String host, int port, String path) throws InterruptedException {
        this.id = id;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // register client actions
        this.actions.put(Action.ANSWER.toString(), new AnswerAction());
        this.actions.put(Action.MESSAGE.toString(), new MessageAction());
        this.actions.put(Action.REGISTERED.toString(), new RegisteredAction());

        // create WebSocket client
        this.client = new WebSocketClient(URI.create("ws://"+host+":"+port+path)) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Register r = new Register();
                r.setId(ClientImpl.this.id);
                this.send(new Gson().toJson(r));
            }

            @Override
            public void onMessage(String s) {
                JsonObject obj = (JsonObject) new JsonParser().parse(s);
                ClientAction action = actions.get(obj.getAsJsonPrimitive("action").getAsString());
                if (action != null) {
                    action.execute(ClientImpl.this, this, obj);
                }
                // TODO handle errors
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Unregister u = new Unregister();
                u.setId(ClientImpl.this.id);
                this.send(new Gson().toJson(u));
            }

            @Override
            public void onError(Exception e) {}
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
    public void onMessage(MessageCallback callback) {
        this.emitMessage = callback;
    }

    @Override
    public void emitMessage(Object data, Response e) {
        this.emitMessage.execute(data, e);
    }

    @Override
    public AnswerCallback getAnswerCallback(String ack) {
        return this.ack2callback.get(ack);
    }

    @Override
    public void onError(ErrorCallback callback) {
        this.emitError = callback;
    }

    @Override
    public void emitError(Exception e) {
        this.emitError.execute(e);
    }

    @Override
    public void onRegistered(RegisteredCallback callback) {
        this.emitRegistered = callback;
    }

    @Override
    public void emitRegistered(String id) {
        this.emitRegistered.execute(id);
    }

    private String generateAck(AnswerCallback callback) {
        UUID uuid = UUID.randomUUID();
        this.ack2callback.put(uuid.toString(), callback);
        return uuid.toString();
    }
}
