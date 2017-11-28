package server.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomEntity {
    private String state;

    private UserEntity host;
    private DealerEntity _dealer;
    private int capacity;

    int currentUserIndex = 0;
    private List<PlayerEntity> players;

    public RoomEntity() {
        this.state = "not_started";
        this._dealer = new DealerEntity();
        this.capacity = 0;
        this.players = new ArrayList();
    }

    public UserEntity getHost() {
        return host;
    }

    public void setHost(UserEntity host) {
        this.host = host;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<PlayerEntity> getPlayers() {
        return players;
    }


    public boolean canHandleOneMore () {
        return this.getPlayers().size() + 1 <= this.getCapacity();
    }

    public void addPlayer(PlayerEntity p) {
        this.players.add(p);
    }

    public List<UserEntity> getUsers () {
        return players.stream()
            .filter(player -> player.getType().equals("user"))
            .map(p -> (UserEntity) p)
            .collect(Collectors.toList());
    }

    public List<BotEntity> getBots () {
        return players.stream()
                .filter(player -> player.getType().equals("bot"))
                .map(p -> (BotEntity) p)
                .collect(Collectors.toList());
    }

    public DealerEntity getDealer() {
        return _dealer;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UserEntity next () {
        try {
            return getUsers().get(getUsers().size() - currentUserIndex++ - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

    }
}
