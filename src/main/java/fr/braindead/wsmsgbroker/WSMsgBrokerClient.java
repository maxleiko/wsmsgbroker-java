package fr.braindead.wsmsgbroker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import fr.braindead.websocket.client.WebSocketClient;
import fr.braindead.wsmsgbroker.actions.client.*;
import fr.braindead.wsmsgbroker.callback.AnswerCallback;
import fr.braindead.wsmsgbroker.protocol.Register;
import fr.braindead.wsmsgbroker.protocol.Send;
import fr.braindead.wsmsgbroker.protocol.Unregister;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by leiko on 31/10/14.
 *
 */
public abstract class WSMsgBrokerClient implements IWSMsgBrokerClient, Runnable {

    private String id;
    private String host;
    private int port;
    private String path;
    private WebSocketClient client;
    // Event maps
    private Map<String, ClientAction> actions = new HashMap<>();
    private Map<String, AnswerCallback> ack2callback = new HashMap<>();
    private ScheduledExecutorService scheduledThreadPool;
    private int autoReconnectTiming = 3000;

    /**
     * Create a WSMsgBrokerClient with AutoReconnect false
     * @param id
     * @param host
     * @param port
     */
    public WSMsgBrokerClient(String id, String host, int port) {
        this(id, host, port, "", true);
    }

    /**
     * Create a WSMsgBrokerClient with empty path ("")
     * @param id
     * @param host
     * @param port
     * @param autoReconnect
     */
    public WSMsgBrokerClient(String id, String host, int port, boolean autoReconnect) {
        this(id, host, port, "", autoReconnect);
    }

    /**
     * Create a WSMsgBrokerClient
     * @param id
     * @param host
     * @param port
     * @param path
     * @param autoReconnect
     */
    public WSMsgBrokerClient(String id, String host, int port, String path, boolean autoReconnect) {
        this.id = id;
        this.host = host;
        this.port = port;
        if (!path.startsWith("/")) {
            this.path = "/" + path;
        }

        // register client actions
        this.actions.put(Action.ANSWER.toString(), new AnswerAction());
        this.actions.put(Action.MESSAGE.toString(), new MessageAction());
        this.actions.put(Action.REGISTERED.toString(), new RegisteredAction());
        this.actions.put(Action.UNREGISTERED.toString(), new UnregisteredAction());

        if (autoReconnect) {
            scheduledThreadPool = Executors.newScheduledThreadPool(1);
            scheduledThreadPool.scheduleAtFixedRate(this, 0, autoReconnectTiming, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void close() {
        scheduledThreadPool.shutdownNow();
        if (this.client != null) {
            try {
                this.client.close();
            } catch (IOException e) {
                onError(e);
            }
        }
    }

    @Override
    public void run() {
        try {
            if (client == null) {
                createClient();
            } else {
                if (!client.isOpen()) {
                    createClient();
                }
            }
        } catch (IOException e) {
            onError(e);
        }
    }

    private void createClient() throws IOException {
        // create WebSocket client
        URI uri = URI.create("ws://" + host + ":" + port + path);
        client = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Register r = new Register();
                r.setId(id);
                client.send(new Gson().toJson(r));
            }

            @Override
            public void onMessage(String msg) {
                JsonObject obj = (JsonObject) new JsonParser().parse(msg);
                JsonPrimitive rawAction = obj.getAsJsonPrimitive("action");
                if (rawAction != null) {
                    ClientAction action = actions.get(rawAction.getAsString());
                    if (action != null) {
                        action.execute(WSMsgBrokerClient.this, client, obj);
                    } else {
                        WSMsgBrokerClient.this.onError(new Exception("Unknown action '"+rawAction.getAsString()+"'"));
                    }
                } else {
                    WSMsgBrokerClient.this.onError(new Exception("Unable to parse received message from server"));
                }
            }

            @Override
            public void onClose(int i, String s) {
                WSMsgBrokerClient.this.onClose(i, s);
            }

            @Override
            public void onError(Exception e) {
                WSMsgBrokerClient.this.onError(e);
            }
        };
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void send(Object data, String dest) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }
        this.send(data, new String[]{dest});
    }

    @Override
    public void send(Object data, String[] dest) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }
        Send s = new Send();
        s.setMessage(data);
        s.setDest(dest);
        if (this.client != null && this.client.isOpen()) {
            this.client.send(new Gson().toJson(s));
        }
    }

    @Override
    public void send(Object data, String dest, AnswerCallback callback) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }
        this.send(data, new String[] {dest}, callback);
    }

    @Override
    public void send(Object data, String[] dest, AnswerCallback callback) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }
        Send s = new Send();
        s.setMessage(data);
        s.setDest(dest);
        s.setAck(generateAck(callback));
        if (this.client != null && this.client.isOpen()) {
            this.client.send(new Gson().toJson(s));
        }
    }

    @Override
    public void unregister() {
        Unregister u = new Unregister();
        u.setId(id);
        if (this.client != null && this.client.isOpen()) {
            this.client.send(new Gson().toJson(u));
        }
    }

    @Override
    public AnswerCallback getAnswerCallback(String ack) {
        return this.ack2callback.get(ack);
    }

    @Override
    public void setAutoReconnectTiming(int autoReconnectTiming) {
        this.autoReconnectTiming = autoReconnectTiming;
        scheduledThreadPool.shutdownNow();
        scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(this, 0, this.autoReconnectTiming, TimeUnit.MILLISECONDS);
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

    public abstract void onClose(int code, String reason);
}
