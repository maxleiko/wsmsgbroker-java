package fr.braindead.wsmsgbroker.client;

import fr.braindead.wsmsgbroker.callback.AnswerCallback;
import fr.braindead.wsmsgbroker.callback.ErrorCallback;
import fr.braindead.wsmsgbroker.callback.MessageCallback;
import fr.braindead.wsmsgbroker.callback.RegisteredCallback;

/**
 * Created by leiko on 30/10/14.
 */
public interface Client {

    String getId();

    void connect();

    void connectBlocking() throws InterruptedException;

    void send(Object data, String dest);

    void send(Object data, String[] dest);

    void send(Object data, String dest, AnswerCallback callback);

    void send(Object data, String[] dest, AnswerCallback callback);

    void onRegistered(RegisteredCallback callback);

    void onMessage(MessageCallback callback);

    void onError(ErrorCallback callback);

    void emitRegistered(String id);

    void emitMessage(Object msg, Response r);

    void emitError(Exception e);

    AnswerCallback getAnswerCallback(String ack);
}
