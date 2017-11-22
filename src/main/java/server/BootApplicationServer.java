package server;

import jdk.nashorn.internal.parser.JSONParser;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.model.StateUpdate;
import server.model.parser.Json;
import spark.Session;

import static spark.Spark.init;
import static spark.Spark.staticFileLocation;
import static spark.Spark.webSocket;

public class BootApplicationServer {
    static PokerGame gameInstance;

    @WebSocket
    class StateUpdateSocketHandler {
        @OnWebSocketConnect
        public void onConnect (Session session) {
        }
        @OnWebSocketClose
        public void onClose (Session session, int statusCode, String reason) {

        }

        @OnWebSocketMessage
        public void onMessage (Session session, String msg) {
            StateUpdate update = Json.parse(msg, StateUpdate.class);

        }
    }

    public static void main(String[] args) {
        gameInstance = new PokerGame();
        staticFileLocation("/client/dist");
        webSocket("/game", StateUpdateSocketHandler.class);
        init();
    }

}


