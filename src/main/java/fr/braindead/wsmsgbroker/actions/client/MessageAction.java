package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.braindead.wsmsgbroker.client.Client;
import fr.braindead.wsmsgbroker.client.Response;
import fr.braindead.wsmsgbroker.protocol.Answer;
import fr.braindead.wsmsgbroker.protocol.Message;
import org.java_websocket.client.WebSocketClient;

/**
 * Created by leiko on 30/10/14.
 */
public class MessageAction implements ClientAction {

    @Override
    public void execute(Client client, WebSocketClient ws, JsonObject msg) {
        Message m = new Gson().fromJson(msg, Message.class);
        Response r = null;
        if (m.getAck() != null) {
            r = response -> {
                Answer a = new Answer();
                a.setAck(m.getAck());
                a.setFrom(client.getId());
                a.setMessage(response);
                ws.send(new Gson().toJson(a));
            };
        }
        client.emitMessage(m.getMessage(), r);
    }
}
