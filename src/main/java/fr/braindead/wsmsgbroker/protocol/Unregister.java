package fr.braindead.wsmsgbroker.protocol;

import fr.braindead.wsmsgbroker.actions.client.Action;

/**
 * Created by leiko on 30/10/14.
 */
public class Unregister {

    private String action = Action.UNREGISTER.toString();
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
