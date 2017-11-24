package server.model.entity;

import java.util.ArrayList;
import java.util.List;

public class RoomEntity {
    private UserEntity host;
    private int capacity;
    private List<UserEntity> users;

    public RoomEntity() {
        this.capacity = 0;
        this.users = new ArrayList();
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

    public List<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }
}
