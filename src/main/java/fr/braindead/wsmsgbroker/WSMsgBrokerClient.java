package fr.braindead.wsmsgbroker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.braindead.wsmsgbroker.actions.client.*;
import fr.braindead.wsmsgbroker.callback.AnswerCallback;
import fr.braindead.wsmsgbroker.protocol.Register;
import fr.braindead.wsmsgbroker.protocol.Send;
import fr.braindead.wsmsgbroker.protocol.Unregister;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.*;
import org.xnio.*;

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
 */
public abstract class WSMsgBrokerClient implements IWSMsgBrokerClient, Runnable {

    private String id;
    private String host;
    private int port;
    private String path;
    private WebSocketChannel client;
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
        Xnio xnio = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
        XnioWorker worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, 2)
                .set(Options.CONNECTION_HIGH_WATER, 1000000)
                .set(Options.CONNECTION_LOW_WATER, 1000000)
                .set(Options.WORKER_TASK_CORE_THREADS, 30)
                .set(Options.WORKER_TASK_MAX_THREADS, 30)
                .set(Options.TCP_NODELAY, true)
                .set(Options.CORK, true)
                .getMap());
        ByteBufferSlicePool buffer = new ByteBufferSlicePool(BufferAllocator.BYTE_BUFFER_ALLOCATOR, 1024, 1024);
        URI uri = URI.create("ws://" + host + ":" + port + path);

        client = WebSocketClient.connect(worker, buffer, OptionMap.EMPTY, uri, WebSocketVersion.V13).get();
        client.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                String msg = message.getData();
                JsonObject obj = (JsonObject) new JsonParser().parse(msg);
                ClientAction action = actions.get(obj.getAsJsonPrimitive("action").getAsString());
                if (action != null) {
                    action.execute(WSMsgBrokerClient.this, client, obj);
                }
                // TODO handle errors
            }

            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                WSMsgBrokerClient.this.onError(new Exception(error));
                try {
                    client.close();
                } catch (IOException e) {
                    WSMsgBrokerClient.this.onError(e);
                }
            }
        });
        client.resumeReceives();

        Register r = new Register();
        r.setId(id);
        WebSockets.sendText(new Gson().toJson(r), client, null);
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
            WebSockets.sendText(new Gson().toJson(s), this.client, null);
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
            WebSockets.sendText(new Gson().toJson(s), this.client, null);
        }
    }

    @Override
    public void unregister() {
        Unregister u = new Unregister();
        u.setId(id);
        if (this.client != null && this.client.isOpen()) {
            WebSockets.sendText(new Gson().toJson(u), this.client, null);
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

    public abstract void onClose(int code, String reason, boolean remote);
}
