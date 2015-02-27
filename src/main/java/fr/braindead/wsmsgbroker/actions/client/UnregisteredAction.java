package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;
import fr.braindead.wsmsgbroker.protocol.Unregister;
import io.undertow.websockets.core.WebSocketChannel;

/**
 * Created by leiko on 30/10/14.
 */
public class UnregisteredAction implements ClientAction {

    @Override
    public void execute(WSMsgBrokerClient client, WebSocketChannel ws, JsonObject msg) {
        Unregister u = new Gson().fromJson(msg, Unregister.class);
        client.onUnregistered(u.getId());
    }
}
