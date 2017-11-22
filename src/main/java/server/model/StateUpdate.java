package server.model;


import java.util.Map;

public interface StateUpdate {
   String type();
   Map body();
}
