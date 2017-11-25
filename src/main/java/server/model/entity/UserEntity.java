package server.model.entity;

import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.List;

public class UserEntity implements PlayerEntity {
    private Session _session;
    private String _username;
    private List<CardEntity> _hand;
    public UserEntity(Session session, String username) {
        _session = session;
         _username = username;
        _hand = new ArrayList();
    }

    public String get_username() {
        return _username;
    }

    @Override
    public List<CardEntity> getHand() {
        return _hand;
    }

    public Session get_session() {
        return _session;
    }

    @Override
    public String getType() {
        return "user";
    }
}
