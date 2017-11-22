package server.model.parser;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Json {
    private static final String jsonParseScript = new java.util.Scanner(Json.class.getResourceAsStream("jsonparser.js"), "UTF-8").useDelimiter("\\A").next();

    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    static {
        try {
            engine.eval(jsonParseScript);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class JsonParseException extends RuntimeException {
        public JsonParseException(Exception e) {
            super(e);
        }
    }

    public static <T> T parse(String json, Class<T> interfaceType) {
        try {
            return (T) ((Invocable) engine).invokeFunction("parse", interfaceType.getName(), json);
        } catch (Exception e) {
            throw new JsonParseException(e);
        }

    }
}