package server.instance;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import server.model.StateUpdate;
import server.model.entity.*;
import server.model.entity.algo.Algorithm;
import server.model.entity.algo.StrategyOne;
import server.model.entity.algo.StrategyTwo;

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
    private RoomEntity currentRoom;
    private Map<String, Class> strats;
    private PokerGame() {
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        sessions = new ConcurrentHashMap();
        roomEntityMap = new ConcurrentHashMap();

        strats = new HashMap();

        strats.put("type_one", StrategyOne.class);

        strats.put("type_two", StrategyTwo.class);

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
        } else if (type.equals("action_fold")) {
            userFold(session, update);
        } else if (type.equals("action_hold")) {
            userHold(session, update);
        } else if (type.equals("improve_cards")) {
            userImproveCards(session, update);
        }
    }

    private void userImproveCards(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> payload = update.getContent();
        String user_id = String.valueOf(payload.get("user_id"));
        String room_id = String.valueOf(payload.get("room_id"));
        String improveCards = String.valueOf(payload.get("cards_to_improve"));

        RoomEntity room = roomEntityMap.get(room_id);
        UserEntity user = room.getUsers().stream().filter(u -> u.get_username().equals(user_id)).findFirst().get();

        user.getHand().set_cards(user.getHand().get_cards().stream().filter(cardEntity -> !improveCards.contains(cardEntity.toString())).collect(Collectors.toList()));

        user.getHand().get_cards().forEach(card -> {
            card.setPubliclyVisible(false);
        });

        room.getDealer().deal(user.getHand().get_cards(), improveCards.split(" ").length, true);
        StateUpdate newCards = new StateUpdate("attempt_improve_cards");
        newCards.put("user_id", user.get_username());
        newCards.put("cards", user.getHand().get_cards().stream().map(card -> card.toString()).collect(Collectors.toList()));
        UserEntity nextUser = room.next();

        if (nextUser != null) {
            room.getUsers().forEach(u -> {
                try {
                    u.get_session().getRemote().sendString(objectMapper.writeValueAsString(newCards));
                    StateUpdate n = new StateUpdate("turn_update");
                    n.put("user_id", nextUser.get_username());
                    u.get_session().getRemote().sendString(objectMapper.writeValueAsString(n));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            evaluateWinner(room);
        }


        StateUpdate cardUpdate = new StateUpdate("got_cards");
        List<String> cards = user.getHand().get_cards().stream()
                .map(card -> {
                    card.setPubliclyVisible(true);
                    return card.toString();
                }).collect(Collectors.toList());
        cardUpdate.put("player_cards", cards);
        user.get_session().getRemote().sendString(objectMapper.writeValueAsString(cardUpdate));

    }

    private void userHold(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> payload = update.getContent();
        String roomId = String.valueOf(payload.get("room_id"));
        RoomEntity roomEntity = roomEntityMap.get(roomId);

        UserEntity u = roomEntity.next();

        if (u != null) {
            StateUpdate turn = new StateUpdate("turn_update");
            turn.put("user_id", u.get_username());
            roomEntity.getUsers().forEach(ur -> {
                try {
                    ur.get_session().getRemote().sendString(objectMapper.writeValueAsString(turn));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            evaluateWinner(roomEntity);
        }
    }

    private void evaluateWinner(RoomEntity roomEntity) {
        Algorithm evalAlgo = new Algorithm() {
            @Override
            public boolean shouldHold(HandEntity mHand, RoomEntity room, List<HandEntity> otherHands, boolean goingFirst) {
                return false;
            }
        };
        List<Map<Object, Object>> sorted = roomEntity
            .getPlayers()
            .stream()
            .sorted((u1, u2) -> evalAlgo.compare(u2, u1))
            .map(p -> {
                Map<Object, Object> v = new HashMap();
                v.put("user_id", p.get_username());
                v.put("hand", p.getHand().get_cards().stream().map(h -> {
                    h.setPubliclyVisible(true);
                    return h.toString();
                }).collect(Collectors.toList()));
                v.put("rank", evalAlgo.getRanking(p.getHand()));
                return v;
            }).collect(Collectors.toList());


        StateUpdate winnerUpdate = new StateUpdate("winner");
        winnerUpdate.put("user_list", sorted);

        roomEntity.getUsers().forEach(u -> {
            try {
                u.get_session().getRemote().sendString(objectMapper.writeValueAsString(winnerUpdate));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void userFold(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> payload = update.getContent();
        String roomId = String.valueOf(payload.get("room_id"));
        String username = String.valueOf(payload.get("user_id"));

        RoomEntity roomEntity = roomEntityMap.get(roomId);
        PlayerEntity player = roomEntity.getPlayers().stream().filter(u -> u.get_username().equals(username)).findFirst().get();
        player.fold();

        UserEntity u = roomEntity.next();

        if (u != null) {
            StateUpdate turn = new StateUpdate("turn_update");
            turn.put("user_id", u.get_username());
            roomEntity.getUsers().forEach(ur -> {
                try {
                    ur.get_session().getRemote().sendString(objectMapper.writeValueAsString(turn));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            evaluateWinner(roomEntity);
        }
    }

    private void playRound(String roomId) throws IOException {
        RoomEntity room = roomEntityMap.get(roomId);

        room.getDealer().get_deck()._init();
        // Deal cards
        room.getPlayers().forEach(player -> room.getDealer().deal(player.getHand().get_cards(), 5, false));
        // Update personal cards
        room.getUsers().forEach(user -> {
            StateUpdate cardUpdate = new StateUpdate("got_cards");
            List<String> cards = user.getHand().get_cards().stream()
                .map(card -> {
                    card.setPubliclyVisible(true);
                    return card.toString();
                }).collect(Collectors.toList());
            cardUpdate.put("player_cards", cards);
            try {
                user.get_session().getRemote().sendString(objectMapper.writeValueAsString(cardUpdate));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println("log: bot_hands");
        for (int i = 0 ; i < room.getBots().size(); i++) {
            BotEntity bot = room.getBots().get(i);
            List<HandEntity> otherHands = room.getPlayers().stream().filter(p -> !p.get_username().equals(bot.get_username())).map(p -> p.getHand()).collect(Collectors.toList());
            boolean shouldHold = bot.getAlgorithm().shouldHold(bot.getHand(), room, otherHands, i == 0);

            if (!shouldHold) {
                 bot.fold();
                 StateUpdate a = new StateUpdate("fold");
                 a.put("user_id", bot.get_username());
                 a.put("cards", bot.getHand().get_cards().stream().map(c -> {
                     c.setPubliclyVisible(true);
                     return c.toString();
                 }).collect(Collectors.toList()));
                 room.getUsers().forEach(u -> {
                     try {
                         u.get_session().getRemote().sendString(objectMapper.writeValueAsString(a));
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 });
            }
        }
        System.out.println("log: user_hands");

        UserEntity nextUser = room.next();

        if (nextUser != null) {
            if (room.getPlayers().stream().filter(p -> !p.get_username().equals(nextUser.get_username())).allMatch(p -> p.hasFolded())) {
                evaluateWinner(room);
                return;
            }

            StateUpdate uTurn = new StateUpdate("turn_update");
            Map<Object, Object> v = new HashMap();
            v.put("user_id", nextUser.get_username());
            uTurn.setContent(v);
            room.getUsers().stream().forEach(user -> {
                try {
                    user.get_session().getRemote().sendString(objectMapper.writeValueAsString(uTurn));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
//        if (p.getType().equals("user")) {
//            UserEntity u = (UserEntity) p;

//        } else {
//
//        }
    }

    private void startGame(Session session, StateUpdate update) throws IOException {
        Map<Object, Object> payload = update.getContent();
        String roomId = String.valueOf(payload.get("room_id"));
        currentRoom = roomEntityMap.get(roomId);
        this.roomEntityMap.get(roomId).setState("game_started");
        currentRoom.getUsers().forEach(p -> {
            p.getHand().set_cards(new ArrayList());
        });
        playRound(roomId);
        StateUpdate gameStarted = new StateUpdate("game_started");
        currentRoom.getUsers().forEach(u -> {
            try {
                u.get_session().getRemote().sendString(objectMapper.writeValueAsString(gameStarted));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
        if (payload.containsKey("start_cards")) {
            String[] cards = String.valueOf(payload.get("start_cards")).split(" ");
            for (String c : cards) {
                Map<String, Integer> cMap = new HashMap();
                cMap.put("A", 1);
                cMap.put("J", 11);
                cMap.put("Q", 12);
                cMap.put("K", 13);

                String value = c.charAt(0) + "";
                String suit = c.charAt(1) + "";
                int v = cMap.computeIfAbsent(value, s -> Integer.parseInt(s));

                CardEntity cc = new CardEntity(suit, v, value);
                cc.setPubliclyVisible(true);
                bot.getHand().get_cards().add(cc);
            }
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
            session.getRemote().sendString(objectMapper.writeValueAsString(new StateUpdate("game_already_running")));
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
