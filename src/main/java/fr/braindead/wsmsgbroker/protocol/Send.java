package fr.braindead.wsmsgbroker.protocol;

import fr.braindead.wsmsgbroker.actions.client.Action;

/**
 * Created by leiko on 30/10/14.
 */
public class Send {

    private String action = Action.SEND.toString();
    private String ack;
    private Object message;
    private String[] dest;

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public String[] getDest() {
        return dest;
    }

    public void setDest(String[] dest) {
        this.dest = dest;
    }
}
