package fr.braindead.wsmsgbroker.client;

import fr.braindead.wsmsgbroker.Response;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;

import java.io.IOException;

/**
 * Created by leiko on 30/10/14.
 *
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
                this.send("0", "client1");
            }

            @Override
            public void onMessage(Object msg, Response res) {
                System.out.println("client0> "+msg);
                this.send("0", "client1");
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Client0 ERROR");
                e.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason) {
                System.out.println("client0 closed " + code + ", " + reason);
            }
        };

        client1 = new WSMsgBrokerClient("client1", "localhost", 9050) {
            @Override
            public void onUnregistered(String id) {
                System.out.println(id + " unregistered");
                this.close();
            }

            @Override
            public void onRegistered(String id) {
                this.send("1", "client0");
            }

            @Override
            public void onMessage(Object msg, Response res) {
                System.out.println("client1> "+msg);
                this.send("1", "client0");
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Client1 ERROR");
                e.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason) {
                System.out.println("client1 closed "+code+", "+reason);
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