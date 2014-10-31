package fr.braindead.wsmsgbroker.callback;

/**
 * Created by leiko on 30/10/14.
 */
public interface CloseCallback {
    void execute(int code, String reason, boolean remote);
}
