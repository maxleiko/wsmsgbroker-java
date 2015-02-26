package fr.braindead.wsmsgbroker.client;

import fr.braindead.wsmsgbroker.Response;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;

import java.io.IOException;

/**
 * Created by leiko on 30/10/14.
 */
public class TestClient {

    private WSMsgBrokerClient client0, client1;

    public void test() throws InterruptedException {
        client0 = new WSMsgBrokerClient("client0", "localhost", 9050) {
            @Override
            public void onUnregistered(String id) {
                System.out.println(id+" unregistered");
                this.close();
            }

            @Override
            public void onRegistered(String id) {
                System.out.println("Client0 registered");
                this.send(null, "client1", (from, answer) -> System.out.println("client0 got answer from " + from + "> " + answer));
            }

            @Override
            public void onMessage(Object msg, Response res) {
                System.out.println("client0> "+msg);
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Client0 ERROR");
                e.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("client0 closed "+code+", "+reason+", "+remote);
            }
        };

        client1 = new WSMsgBrokerClient("client1", "localhost", 9050) {
            @Override
            public void onUnregistered(String id) {
                System.out.println(id+" unregistered");
                this.close();
            }

            @Override
            public void onRegistered(String id) {
                System.out.println("Client1 registered");
                client0.send(new int[] {42, 3}, "client1");
            }

            @Override
            public void onMessage(Object msg, Response res) {
                System.out.println("client1> "+msg);
                System.out.println(msg.getClass().getTypeName());
                if (res != null) {
                    res.send("bar!");
                }
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Client1 ERROR");
                e.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("client1 closed "+code+", "+reason+", "+remote);
            }
        };
    }

    public void stop() {
        client0.unregister();
        client1.unregister();
    }

    // TODO do some clean JUnit tests
    public static void main(String[] args) throws IOException, InterruptedException {
        TestClient tester = new TestClient();
        tester.test();
//        new Thread(() -> {
//            try {
//                Thread.sleep(2000);
//                tester.stop();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }
}
