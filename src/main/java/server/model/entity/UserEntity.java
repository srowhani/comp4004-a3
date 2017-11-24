package server.model.entity;

import org.eclipse.jetty.websocket.api.Session;

public class UserEntity implements PlayerEntity {
    private Session _session;
    private String _username;

    public UserEntity(Session session, String username) {
        _session = session;
         _username = username;

    }

    public String get_username() {
        return _username;
    }

    public Session get_session() {
        return _session;
    }

    @Override
    public String getType() {
        return "user";
    }
}
