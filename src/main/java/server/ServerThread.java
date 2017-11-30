package server;

import server.handler.StateUpdateSocketHandler;
import server.instance.PokerGame;
import spark.*;

import static spark.Spark.*;
public class ServerThread implements Runnable {
    @Override
    public void run() {
        staticFiles.location("/dist");
        staticFiles.expireTime(600);

        port(8081);

        webSocket("/game", StateUpdateSocketHandler.class);
        System.out.println("Serving on port 8081");
        init();
    }
}


