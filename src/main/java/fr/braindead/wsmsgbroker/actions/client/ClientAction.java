package fr.braindead.wsmsgbroker.actions.client;

import com.google.gson.JsonObject;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;
import io.undertow.websockets.core.WebSocketChannel;

/**
 * Created by leiko on 30/10/14.
 *
 */
public interface ClientAction {

    void execute(WSMsgBrokerClient client, WebSocketChannel ws, JsonObject msg);
}
