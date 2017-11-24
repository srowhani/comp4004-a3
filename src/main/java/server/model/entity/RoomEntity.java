package server.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomEntity {
    private UserEntity host;
    private int capacity;

    private List<PlayerEntity> players;

    public RoomEntity() {
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
}
