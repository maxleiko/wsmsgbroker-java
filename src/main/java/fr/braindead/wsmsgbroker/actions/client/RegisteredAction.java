package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.braindead.websocket.client.WebSocketClient;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;
import fr.braindead.wsmsgbroker.protocol.Register;

/**
 * Created by leiko on 30/10/14.
 *
 */
public class RegisteredAction implements ClientAction {

    @Override
    public void execute(WSMsgBrokerClient client, WebSocketClient ws, JsonObject msg) {
        Register r = new Gson().fromJson(msg, Register.class);
        client.onRegistered(r.getId());
    }
}
