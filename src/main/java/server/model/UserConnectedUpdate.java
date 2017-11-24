package server.model;

public class UserConnectedUpdate extends StateUpdate {
    @Override
    public String getType() {
        return "user_connected";
    }
}
