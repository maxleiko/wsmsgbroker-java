package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.braindead.websocket.client.WebSocketClient;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;
import fr.braindead.wsmsgbroker.protocol.Unregister;

/**
 * Created by leiko on 30/10/14.
 */
public class UnregisteredAction implements ClientAction {

    @Override
    public void execute(WSMsgBrokerClient client, WebSocketClient ws, JsonObject msg) {
        Unregister u = new Gson().fromJson(msg, Unregister.class);
        client.onUnregistered(u.getId());
    }
}
