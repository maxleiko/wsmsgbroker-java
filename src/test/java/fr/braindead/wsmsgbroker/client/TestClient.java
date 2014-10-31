package fr.braindead.wsmsgbroker.client;

import java.io.IOException;

/**
 * Created by leiko on 30/10/14.
 */
public class TestClient {

    // TODO do some clean JUnit test when not tired
    public static void main(String[] args) throws InterruptedException, IOException {
        Client client0 = new ClientImpl("client0", "localhost", 9050);
        Client client1 = new ClientImpl("client1", "localhost", 9050);
        client1.onMessage((message, response) -> {
            System.out.println("client1> "+message);
            response.send("bar!");
        });
        client0.onRegistered((id) -> {
            System.out.println("Client0 registered");
            client0.send(42, "client1", (from, answer) -> System.out.println("client0 got answer from "+from+"> "+answer));
        });
        client1.onRegistered((id) -> {
            System.out.println("Client1 registered");
            client0.send(new int[] {42, 3}, "client1");
        });

        client1.connectBlocking();
        client0.connectBlocking();
    }
}
