package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.braindead.wsmsgbroker.callback.AnswerCallback;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;
import fr.braindead.wsmsgbroker.protocol.Answer;
import org.java_websocket.client.WebSocketClient;

/**
 * Created by leiko on 30/10/14.
 */
public class AnswerAction implements ClientAction {

    @Override
    public void execute(WSMsgBrokerClient client, WebSocketClient ws, JsonObject msg) {
        Answer a = new Gson().fromJson(msg, Answer.class);
        AnswerCallback callback = client.getAnswerCallback(a.getAck());
        if (callback != null) {
            if (a.getFrom() != null && a.getMessage() != null) {
                callback.execute(a.getFrom(), a.getMessage());
            } else {
                client.onError(new Exception("Unable to parse 'answer' message (msg.from or msg.message are undefined)"));
            }
        } else {
            client.onError(new Exception("Unable to find answer callback for " + client.getId() + " and ack = " + a.getAck()));
        }
    }
}
