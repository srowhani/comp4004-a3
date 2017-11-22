package server;

import spark.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PokerGame {
    Map<Session, String> users = new ConcurrentHashMap();

}
