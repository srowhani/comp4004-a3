package server.model;

import java.util.Map;

public class StateUpdate {
   public StateUpdate () {}

   public StateUpdate(String type) {
      this.type = type;
   }

   private String type;
   private Map content;

   public Map<Object, Object> getContent() {
      return content;
   }

   public void setContent(Map content) {
      this.content = content;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }
}
