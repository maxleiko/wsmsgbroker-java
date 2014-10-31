package fr.braindead.wsmsgbroker.callback;

import fr.braindead.wsmsgbroker.Response;

/**
 * Created by leiko on 30/10/14.
 */
public interface MessageCallback {
    void execute(Object message, Response response);
}
