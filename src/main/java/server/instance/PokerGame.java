package server.instance;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import server.model.StateUpdate;
import server.model.entity.BotEntity;
import server.model.entity.PlayerEntity;
import server.model.entity.RoomEntity;
import server.model.entity.UserEntity;
import server.model.entity.algo.Algorithm;
import server.model.entity.algo.FlushAlgorithm;
import server.model.entity.algo.HighCardAlgorithm;
import server.model.entity.algo.StraightAlgorithm;

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
    private Map<String, Class> strats;
    private PokerGame() {
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        sessions = new ConcurrentHashMap();
        roomEntityMap = new ConcurrentHashMap();

        strats = new HashMap();
        strats.put("type_high_card", HighCardAlgorithm.class);
        strats.put("type_flush", FlushAlgorithm.class);
        strats.put("type_straight", StraightAlgorithm.class);

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
        } else if (type.equals("room_add_ai")) {
            roomAddAi(session, update);
        } else if (type.equals("start_game")) {
            startGame(session, update);
        }
    }

    private void playRound(String roomId) {
        RoomEntity room = roomEntityMap.get(roomId);

        room.getDealer().get_deck()._init();

        // Deal cards
        room.getPlayers().forEach(player -> room.getDealer().deal(player.getHand()));

        room.getUsers().forEach(user -> {
            StateUpdate cardUpdate = new StateUpdate("got_cards");
            List<String> cards = user.getHand().stream()
                .map(card -> card.toString()).collect(Collectors.toList());
            cardUpdate.put("player_cards", cards);
            try {
                user.get_session().getRemote().sendString(objectMapper.writeValueAsString(cardUpdate));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        PlayerEntity p = room.getHost();
        if (p.getType().equals("user")) {
            UserEntity u = (UserEntity) p;
            StateUpdate uTurn = new StateUpdate("turn_update");
            Map<Object, Object> v = new HashMap();
            v.put("user_id", u.get_username());
            uTurn.setContent(v);
            room.getUsers().stream().forEach(user -> {
                try {
                    user.get_session().getRemote().sendString(objectMapper.writeValueAsString(uTurn));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {

        }
    }

    private void startGame(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> payload = update.getContent();
        String roomId = String.valueOf(payload.get("room_id"));
        this.roomEntityMap.get(roomId).setState("game_started");
        playRound(roomId);
        StateUpdate gameStarted = new StateUpdate("game_started");
        session.getRemote().sendString(objectMapper.writeValueAsString(gameStarted));
    }

    private void roomAddAi(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> payload = update.getContent();
        String aiType = String.valueOf(payload.get("ai_type"));
        String roomId = String.valueOf(payload.get("room_id"));

        BotEntity bot = new BotEntity();
        bot.setUsername(String.valueOf(payload.get("name")));
        try {
            bot.setAlgorithm((Algorithm) strats.get(aiType).newInstance());
        } catch (Exception e) {
            session.getRemote().sendString(objectMapper.writeValueAsString(new StateUpdate("bot_instance_failed")));
        }

        RoomEntity roomEntity = roomEntityMap.get(roomId);

        if (roomEntity.canHandleOneMore()) {
            roomEntity.addPlayer(bot);
            Map<String, Object> v = new HashMap();
            v.put("available_rooms", roomEntityMap.keySet().stream().map(key -> {
                Map<Object, Object> t = new HashMap();
                t.put("capacity", roomEntityMap.get(key).getCapacity());
                t.put("num_users", roomEntityMap.get(key).getPlayers().size());
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
            c.put("room_host", roomEntity.getHost().get_username());
            c.put("joined_user", bot.get_username());
            c.put("users", roomEntity.getPlayers().stream().map(z -> z.get_username()).collect(Collectors.toList()));
            u.setContent(c);

            roomEntity.getUsers().forEach(user -> {
                try {
                    user.get_session().getRemote().sendString(objectMapper.writeValueAsString(u));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        // Room ready, tell admin to start
        if (roomEntity.getPlayers().size() == roomEntity.getCapacity()) {
            System.out.println("room_ready");
            StateUpdate roomReady = new StateUpdate("room_ready");
            roomEntity.getHost().get_session().getRemote().sendString(objectMapper.writeValueAsString(roomReady));
        }
    }

    private void attemptJoinRoom(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> payload = update.getContent();
        String roomId = String.valueOf(payload.get("room_id"));
        String username = String.valueOf(payload.get("username"));

        RoomEntity roomEntity = roomEntityMap.get(roomId);
        UserEntity userEntity = new UserEntity(session, username);

        if (roomEntity.getState() == "game_started") {
            return;
        }

        if (roomEntity.getUsers().size() == 0) {
            roomEntity.setHost(userEntity);
            System.out.println("room_host_update");

            StateUpdate hostUpdate = new StateUpdate("room_host_update");

            Map<Object, Object> v = new HashMap();

            v.put("current_room_host", userEntity.get_username());
            v.put("current_room_id", roomId);

            hostUpdate.setContent(v);

            userEntity.get_session().getRemote().sendString(objectMapper.writeValueAsString(hostUpdate));
        }

        if (roomEntity.getPlayers().size() < roomEntity.getCapacity()) {
            roomEntityMap.get(roomId).addPlayer(userEntity);

            Map<String, Object> v = new HashMap();
            v.put("available_rooms", roomEntityMap.keySet().stream().map(key -> {
                Map<Object, Object> t = new HashMap();
                t.put("capacity", roomEntityMap.get(key).getCapacity());
                t.put("num_users", roomEntityMap.get(key).getPlayers().size());
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
            c.put("room_host", roomEntity.getHost().get_username());
            c.put("joined_user", userEntity.get_username());
            c.put("users", roomEntity.getPlayers().stream().map(z -> z.get_username()).collect(Collectors.toList()));
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
        // Room ready, tell admin to start
        if (roomEntity.getPlayers().size() == roomEntity.getCapacity()) {
            System.out.println("room_ready");
            StateUpdate roomReady = new StateUpdate("room_ready");
            roomEntity.getHost().get_session().getRemote().sendString(objectMapper.writeValueAsString(roomReady));
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

        roomEntity.getPlayers().add(host);
        Map<String, Object> v = new HashMap();
        v.put("available_rooms", roomEntityMap.keySet().stream().map(key -> {
            Map<Object, Object> t = new HashMap();
            t.put("capacity", roomEntityMap.get(key).getCapacity());
            t.put("num_users", roomEntityMap.get(key).getPlayers().size());
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
        c.put("joined_user", host.get_username());
        c.put("room_host", host.get_username());
        c.put("users", roomEntity.getPlayers().stream().map(z -> z.get_username()).collect(Collectors.toList()));
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
                roomEntityMap.get(key).getPlayers().remove(userEntityOptional.get());
                if (!roomEntityMap.get(key).getUsers().isEmpty()) {
                    if (roomEntityMap.get(key).getHost().get_session().equals(session)) {
                        UserEntity newHost = roomEntityMap.get(key).getUsers().get(0);
                        roomEntityMap.get(key).setHost(newHost);
                        System.out.println("room_host_update");
                        StateUpdate hostUpdate = new StateUpdate("room_host_update");

                        Map<Object, Object> v = new HashMap();

                        v.put("current_room_host", newHost.get_username());

                        hostUpdate.setContent(v);
                        roomEntityMap.get(key).getUsers().forEach(user -> {
                            try {
                                user.get_session().getRemote().sendString(objectMapper.writeValueAsString(hostUpdate));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }

                StateUpdate updateUserList = new StateUpdate();
                Map<Object, Object> v = new HashMap();
                v.put("users", roomEntityMap.get(key).getPlayers().stream().map(user -> user.get_username()).collect(Collectors.toList()));
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
            v.put("num_users", roomEntityMap.get(key).getPlayers().size());
            v.put("room_id", key);
            return v;
        }).collect(Collectors.toList()));
        catchUp.setContent(content);
        session.getRemote().sendString(serialize(catchUp));
    }

    public void addSession(Session session) {
        System.out.println("adding session" + session);
        sessions.put(session, "");
    }
}
