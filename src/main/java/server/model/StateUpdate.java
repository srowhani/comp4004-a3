package server.model;

import java.util.HashMap;
import java.util.Map;

public class StateUpdate {

   public StateUpdate () {
      content = new HashMap();
   }

   public StateUpdate(String type) {
      this();
      this.type = type;
   }

   private String type;
   private Map<Object, Object> content;

   public void put (String key, Object value) {
      content.put(key, value);
   }
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
