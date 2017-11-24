package server.instance;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import server.model.StateUpdate;
import server.model.entity.RoomEntity;
import server.model.entity.UserEntity;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PokerGame {
    private static PokerGame _instance = new PokerGame();
    public static PokerGame getInstance() {
        return _instance;
    }

    private Map<Session, String> sessions;
    private ObjectMapper objectMapper;
    private Map<String, RoomEntity> roomEntityMap;

    private PokerGame() {
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        sessions = new ConcurrentHashMap();
        roomEntityMap = new ConcurrentHashMap();
    }

    public Map<Session, String> getSessions() {
        return sessions;
    }

    public void handleUpdate(Session session, String message) throws IOException {
        System.out.println("handleUpdate");

        StateUpdate update = deserialize(message);
        String type = update.getType();
        System.out.println(type);
        if (type.equals("host_room")) {
            hostGame(session, update);
        } else if (type.equals("attempt_join_room")) {
            attemptJoinRoom(session, update);
        }
    }

    private void attemptJoinRoom(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> payload = update.getContent();
        String roomId = String.valueOf(payload.get("room_id"));
        String username = String.valueOf(payload.get("username"));

        RoomEntity roomEntity = roomEntityMap.get(roomId);
        UserEntity userEntity = new UserEntity(session, username);

        if (roomEntity.getUsers().size() < roomEntity.getCapacity()) {
            roomEntityMap.get(roomId).getUsers().add(userEntity);

            Map<String, Object> v = new HashMap();
            v.put("available_rooms", roomEntityMap.keySet().stream().map(key -> {
                Map<Object, Object> t = new HashMap();
                t.put("capacity", roomEntityMap.get(key).getCapacity());
                t.put("num_users", roomEntityMap.get(key).getUsers().size());
                t.put("room_id", key);
                return t;
            }).collect(Collectors.toList()));

            StateUpdate newRoomAvailable = new StateUpdate();
            newRoomAvailable.setType("room_created");
            newRoomAvailable.setContent(v);

            System.out.println("Publishing room_created");

            for (Session s : sessions.keySet()) {
                s.getRemote().sendString(serialize(newRoomAvailable));
            }

            StateUpdate u = new StateUpdate();
            u.setType("join_room_success");
            Map<Object, Object> c = new HashMap();
            c.put("room_id", roomId);
            c.put("users", roomEntity.getUsers().stream().map(z -> z.get_username()).collect(Collectors.toList()));
            u.setContent(c);



            roomEntity.getUsers().forEach(user -> {
                try {
                    user.get_session().getRemote().sendString(objectMapper.writeValueAsString(u));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            session.getRemote().sendString("{type: 'fail_join_room_attempt'}");
        }

        if (roomEntity.getUsers().size() == roomEntity.getCapacity()) {
            // Notify host game is ready to go.
//            roomEntity.getHost().get_session().getRemote().sendString();
        }
    }

    private void hostGame(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> content = update.getContent();
        RoomEntity roomEntity = new RoomEntity();
        UserEntity host = new UserEntity(session, String.valueOf(content.get("host")));
        roomEntity.setHost(host);
        roomEntity.setCapacity(Integer.parseInt(String.valueOf(content.get("capacity"))));
        String roomId = UUID.randomUUID().toString();
        roomEntityMap.put(roomId, roomEntity);

        roomEntity.getUsers().add(host);
        Map<String, Object> v = new HashMap();
        v.put("available_rooms", roomEntityMap.keySet().stream().map(key -> {
            Map<Object, Object> t = new HashMap();
            t.put("capacity", roomEntityMap.get(key).getCapacity());
            t.put("num_users", roomEntityMap.get(key).getUsers().size());
            t.put("room_id", key);
            return t;
        }).collect(Collectors.toList()));
        StateUpdate newRoomAvailable = new StateUpdate();
        newRoomAvailable.setType("room_created");
        newRoomAvailable.setContent(v);

        System.out.println("Publishing room_created");

        for (Session s : sessions.keySet()) {
            s.getRemote().sendString(serialize(newRoomAvailable));
        }
        StateUpdate u = new StateUpdate();
        u.setType("join_room_success");
        Map<Object, Object> c = new HashMap();
        c.put("room_id", roomId);
        c.put("users", roomEntity.getUsers().stream().map(z -> z.get_username()).collect(Collectors.toList()));
        u.setContent(c);

        host.get_session().getRemote().sendString(objectMapper.writeValueAsString(u));
    }
    public void removeSession(Session session) throws IOException {
        StateUpdate update = new StateUpdate();
        update.setType("update_user_list");
        Map<String, List> content = new HashMap();
        update.setContent(content);

        sessions.remove(session);

        roomEntityMap.keySet().stream().forEach(key -> {
            Optional<UserEntity> userEntityOptional = roomEntityMap.get(key).getUsers().stream()
                    .filter(user -> user.get_session().equals(session)).findFirst();

            if (userEntityOptional.isPresent()) {
                roomEntityMap.get(key).getUsers().remove(userEntityOptional.get());
                if (!roomEntityMap.get(key).getUsers().isEmpty()) {
                    if (roomEntityMap.get(key).getHost().get_session().equals(session)) {
                        roomEntityMap.get(key).setHost(roomEntityMap.get(key).getUsers().get(0));
                    }
                }

                StateUpdate updateUserList = new StateUpdate();
                Map<Object, Object> v = new HashMap();
                v.put("users", roomEntityMap.get(key).getUsers().stream().map(user -> user.get_username()).collect(Collectors.toList()));
                updateUserList.setType("update_user_list");
                updateUserList.setContent(v);
                roomEntityMap.get(key).getUsers().forEach(user -> {
                    try {
                        user.get_session().getRemote().sendString(objectMapper.writeValueAsString(updateUserList));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        Map<String, Object> v = new HashMap();

        v.put("available_rooms", roomEntityMap.keySet().stream().map(key -> {
            Map<Object, Object> t = new HashMap();
            t.put("capacity", roomEntityMap.get(key).getCapacity());
            t.put("num_users", roomEntityMap.get(key).getUsers().size());
            t.put("room_id", key);
            return t;
        }).collect(Collectors.toList()));

        StateUpdate newRoomAvailable = new StateUpdate();
        newRoomAvailable.setType("room_created");
        newRoomAvailable.setContent(v);

        System.out.println("Publishing room_created");

        for (Session s : sessions.keySet()) {
            s.getRemote().sendString(serialize(newRoomAvailable));
        }
    }

    private String serialize (StateUpdate s) {
        try {
            return objectMapper.writeValueAsString(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private StateUpdate deserialize (String input) throws IOException {
        return objectMapper.readValue(input, StateUpdate.class);
    }

    public void catchThemUp(Session session) throws IOException {
        StateUpdate catchUp = new StateUpdate();
        catchUp.setType("catch_up");
        Map<String, Object> content = new HashMap();
        content.put("available_rooms", roomEntityMap.keySet().stream().map(key -> {
            Map<Object, Object> v = new HashMap();
            v.put("capacity", roomEntityMap.get(key).getCapacity());
            v.put("num_users", roomEntityMap.get(key).getUsers().size());
            v.put("room_id", key);
            return v;
        }).collect(Collectors.toList()));
        catchUp.setContent(content);
        session.getRemote().sendString(serialize(catchUp));
    }

    public void addSession(Session session) {
        sessions.put(session, "");
    }
}
