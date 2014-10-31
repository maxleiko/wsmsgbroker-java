package fr.braindead.wsmsgbroker.protocol;

import fr.braindead.wsmsgbroker.actions.client.Action;

/**
 * Created by leiko on 30/10/14.
 */
public class Register {

    private String action = Action.REGISTER.toString();
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
