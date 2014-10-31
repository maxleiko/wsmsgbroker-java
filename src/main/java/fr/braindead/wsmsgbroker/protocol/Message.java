package fr.braindead.wsmsgbroker.protocol;

import fr.braindead.wsmsgbroker.actions.client.Action;

/**
 * Created by leiko on 30/10/14.
 */
public class Message {

    private String action = Action.MESSAGE.toString();
    private String ack;
    private Object message;
    private String from;

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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
