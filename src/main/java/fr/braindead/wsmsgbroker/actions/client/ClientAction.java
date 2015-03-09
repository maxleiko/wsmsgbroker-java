package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.JsonObject;
import fr.braindead.websocket.client.WebSocketClient;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;

/**
 * Created by leiko on 30/10/14.
 *
 */
public interface ClientAction {

    void execute(WSMsgBrokerClient client, WebSocketClient ws, JsonObject msg);
}
