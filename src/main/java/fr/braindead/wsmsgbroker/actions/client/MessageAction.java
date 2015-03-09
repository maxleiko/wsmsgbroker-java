package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.braindead.websocket.client.WebSocketClient;
import fr.braindead.wsmsgbroker.Response;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;
import fr.braindead.wsmsgbroker.protocol.Answer;
import fr.braindead.wsmsgbroker.protocol.Message;
import io.undertow.websockets.core.WebSockets;

/**
 * Created by leiko on 30/10/14.
 *
 */
public class MessageAction implements ClientAction {

    @Override
    public void execute(WSMsgBrokerClient client, WebSocketClient ws, JsonObject msg) {
        Message m = new Gson().fromJson(msg, Message.class);
        Response r = null;
        if (m.getAck() != null) {
            r = response -> {
                Answer a = new Answer();
                a.setAck(m.getAck());
                a.setFrom(client.getId());
                a.setMessage(response);
                if (ws != null && ws.isOpen()) {
                    ws.send(new Gson().toJson(a));
                }
            };
        }
        client.onMessage(m.getMessage(), r);
    }
}
