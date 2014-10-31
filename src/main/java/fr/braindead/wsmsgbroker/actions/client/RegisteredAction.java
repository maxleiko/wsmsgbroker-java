package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.braindead.wsmsgbroker.client.Client;
import fr.braindead.wsmsgbroker.protocol.Register;
import org.java_websocket.client.WebSocketClient;

/**
 * Created by leiko on 30/10/14.
 */
public class RegisteredAction implements ClientAction {

    @Override
    public void execute(Client client, WebSocketClient ws, JsonObject msg) {
        Register r = new Gson().fromJson(msg, Register.class);
        client.emitRegistered(r.getId());
    }
}
