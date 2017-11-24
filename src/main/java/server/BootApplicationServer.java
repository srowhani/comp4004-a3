package server;

import server.handler.StateUpdateSocketHandler;
import server.instance.PokerGame;
import spark.Request;
import spark.Response;
import spark.Route;

import static spark.Spark.*;
public class BootApplicationServer {
    public static void main(String[] args) {


        staticFiles.location("/dist");
        staticFiles.expireTime(600);

        port(8081);

        webSocket("/game", StateUpdateSocketHandler.class);
        System.out.println("Serving on port 8081");
        init();
    }

}


