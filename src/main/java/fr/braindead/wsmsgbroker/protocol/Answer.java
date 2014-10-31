package fr.braindead.wsmsgbroker.protocol;

import fr.braindead.wsmsgbroker.actions.client.Action;

/**
 * Created by leiko on 30/10/14.
 */
public class Answer {

    private String action = Action.ANSWER.toString();
    private String ack;
    private Object message;
    private String from;
    private String to;

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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
