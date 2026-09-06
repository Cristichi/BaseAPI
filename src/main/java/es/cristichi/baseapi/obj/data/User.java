/*
 */
package es.cristichi.baseapi.obj.data;

import java.util.Base64;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Cristichi
 */
public class User extends JSONObject {
    public User(String email, String username, String encryptedPsw, String... admittedScopes) {
        put("email", email);
        put("username", username);
        put("password", encryptedPsw);
        JSONArray scopes = new JSONArray();
        for (String s : admittedScopes) {
            scopes.add(s);
        }
        put("admittedScopes", scopes);
    }

    public String getEmail() {
        return getOrDefault("email", null).toString();
    }

    public String getUsername() {
        return getOrDefault("username", "").toString();
    }

    public String[] getAdmittedScopes() {
        if (getOrDefault("admittedScopes", new JSONArray()) instanceof JSONArray scopes) {
            if (scopes.toArray(new String[scopes.size()]) instanceof String[] scopesArray) {
                return scopesArray;
            }
        }
        throw new RuntimeException("Scopes are in the wrong format. Class: %s.".formatted(getOrDefault("admittedScopes", new JSONArray()).getClass().getCanonicalName()));
    }

    public boolean checkPsw(String password) {
        return getOrDefault("password", "").toString().equals(Base64.getEncoder()
                .encodeToString(password.getBytes()));
    }

    public JSONObject toOtherSafe() {
        JSONObject otherSafeJson = new JSONObject(this);
        otherSafeJson.remove("password");
        otherSafeJson.remove("admittedScopes");
        return otherSafeJson;
    }

    public JSONObject toThemselvesSafe() {
        JSONObject otherSafeJson = new JSONObject(this);
        otherSafeJson.remove("password");
        return otherSafeJson;
    }

    public boolean hasScopes(String... scopes) {
        for (String scope : scopes) {
            boolean hasIt = false;
            for (String admitted : getAdmittedScopes()) {
                if (scope.equals(admitted)) {
                    hasIt = true;
                    break;
                }
            }
            if (!hasIt) {
                return false;
            }
        }

        return true;
    }
}
