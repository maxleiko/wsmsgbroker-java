package fr.braindead.wsmsgbroker.client;

import fr.braindead.wsmsgbroker.Response;
import fr.braindead.wsmsgbroker.WSMsgBrokerClient;
import fr.braindead.wsmsgbroker.callback.AnswerCallback;

import java.io.IOException;
import java.net.URI;

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

    public void another() throws Exception {
        new WSMsgBrokerClient("client0", "localhost", 9050) {
            @Override
            public void onUnregistered(String id) {
                System.out.println(this.getId()+" unregistered");
            }

            @Override
            public void onRegistered(String id) {
                System.out.println(this.getId()+" registered");
                this.send("foo", "client1", (from, answer) -> {
                    System.out.println(this.getId()+" response: "+answer+" ("+from+")");
                });
            }

            @Override
            public void onMessage(Object msg, Response res) {
                System.out.println(this.getId()+" message: "+msg);
            }

            @Override
            public void onError(Exception e) {
                System.out.println("error: "+e.getMessage());
            }

            @Override
            public void onClose(int code, String reason) {
                System.out.println("closed: "+code);
            }
        };

        new WSMsgBrokerClient("client1", "localhost", 9050) {
            @Override
            public void onUnregistered(String id) {
                System.out.println(this.getId()+" unregistered");
            }

            @Override
            public void onRegistered(String id) {
                System.out.println(this.getId()+" registered");
            }

            @Override
            public void onMessage(Object msg, Response res) {
                System.out.println(this.getId()+" message: "+msg);
                res.send("response!");
                this.send("bar", "client0");
            }

            @Override
            public void onError(Exception e) {
                System.out.println("error: "+e.getMessage());
            }

            @Override
            public void onClose(int code, String reason) {
                System.out.println("closed: "+code);
            }
        };
    }

    public void stop() {
        client0.unregister();
        client1.unregister();
    }

    // TODO do some clean JUnit tests
    public static void main(String[] args) throws Exception {
        TestClient tester = new TestClient();
        tester.another();
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