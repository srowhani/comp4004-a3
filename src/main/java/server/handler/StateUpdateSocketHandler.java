package server.handler;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import server.instance.PokerGame;
import java.io.IOException;

@WebSocket
public class StateUpdateSocketHandler {
    PokerGame gameInstance;

    public StateUpdateSocketHandler () {
        gameInstance = PokerGame.getInstance();
    }

    @OnWebSocketError
    public void throwError(Throwable error) {
        error.printStackTrace();
    }

    @OnWebSocketConnect
    public void onConnect (final Session session) throws IOException {
        gameInstance.addSession(session);
        gameInstance.catchThemUp(session);
    }

    @OnWebSocketClose
    public void onClose (Session session, int statusCode, String reason) throws IOException {
        if (gameInstance.getSessions().containsKey(session)) {
            gameInstance.removeSession(session);
        }
    }

    @OnWebSocketMessage
    public void onMessage (Session session, String msg) {
        try {
            gameInstance.handleUpdate(session, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

