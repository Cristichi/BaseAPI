package es.cristichi.baseapi.obj.ldap;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;

@SuppressWarnings("unchecked")
public class AuthToken extends JSONObject {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private AuthToken(String token, User user, LocalDateTime creation, long expiration, String... scopes){
        put("expiration", expiration);
        put("user", user.getEmail());
        put("token", token);
        put("creation", creation.format(dateFormatter));
        JSONArray jsonScopes = new JSONArray();
        for (String s : scopes) {
            jsonScopes.add(s);
        }
        put("scopes", jsonScopes);
    }

    public JSONObject toSendAccessToken() {
        JSONObject ret = new JSONObject();
        ret.put("token_type", "Bearer");
        ret.put("expires_in", getExpiration());
        ret.put("access_token", getToken());
        String scope = "";
        for (String s : getScopes()) {
            scope = scope.concat(",").concat(s);
        }
        ret.put("scope", scope);
        return ret;
    }

    public String getToken() {
        return getOrDefault("token", "").toString();
    }

    public String getUserEmail() {
        return getOrDefault("user", "").toString();
    }

    public String[] getScopes() {
        if (getOrDefault("scopes", new JSONArray()) instanceof JSONArray scopes) {
            if (scopes.toArray(new String[scopes.size()]) instanceof String[] scopesArray) {
                return scopesArray;
            }
        }
        throw new RuntimeException("Scopes are in the wrong format. Class: %s.".formatted(getOrDefault("admittedScopes", new JSONArray()).getClass().getCanonicalName()));
    }

    public long getExpiration() {
        return (long) getOrDefault("expiration", 0);
    }

    public LocalDateTime getCreationDateTime() {
        return LocalDateTime.parse(get("creation").toString(), dateFormatter);
    }

    private static final SecureRandom rng = new SecureRandom();
    private static final String charsAllowed = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long defaultExpirationSecs = 90000;

    public static AuthToken generate(User user, String... scopes) {
        String tokenStr;
        do {
            tokenStr = rng.ints(rng.nextInt(30, 50), 0, charsAllowed.length())
                    .mapToObj(i -> charsAllowed.charAt(i))
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
        } while (DataStore.getInstance().containsToken(tokenStr));

        AuthToken auth = new AuthToken(tokenStr, user, LocalDateTime.now(), defaultExpirationSecs, scopes);
        DataStore.getInstance().putToken(auth);
        return auth;
    }

    public static record CheckResult(Result result, AuthToken auth, User user){
    }

    public static enum Result {
        OK, EXPIRED, INVALID;
    }
}
