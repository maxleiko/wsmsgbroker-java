package fr.braindead.wsmsgbroker.callback;

/**
 * Created by leiko on 30/10/14.
 */
public interface AnswerCallback {
    void execute(String from, Object answer);
}
