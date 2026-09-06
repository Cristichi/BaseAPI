/*
 */
package es.cristichi.baseapi.obj.rest.ldap;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;
import org.json.simple.JSONObject;

import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;

/**
 *
 * @author Cristichi
 */
public record AuthToken(User user, LocalDateTime creation, long expiration, String... scopes) {
    public JSONObject toJSON(String token) {
        JSONObject ret = new JSONObject();
        ret.put("token_type", "Bearer");
        ret.put("expires_in", expiration);
        ret.put("access_token", token);
        String scope = "";
        for (String s : scopes) {
            scope = scope.concat(",").concat(s);
        }
        ret.put("scope", scope);
        return ret;
    }

    private static final SecureRandom rng = new SecureRandom();
    private static final String charsAllowed = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long defaultExpirationSecs = 90000;

    public static Map.Entry<String, AuthToken> generate(User user, String... scopes) {
        String tokenStr;
        do {
            tokenStr = rng.ints(rng.nextInt(30, 50), 0, charsAllowed.length())
                    .mapToObj(i -> charsAllowed.charAt(i))
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
        } while (DataStore.getInstance().containsToken(tokenStr));

        AuthToken auth = new AuthToken(user, LocalDateTime.now(), defaultExpirationSecs, scopes);
        DataStore.getInstance().putToken(tokenStr, auth);
        return new AbstractMap.SimpleEntry<>(tokenStr, auth);
    }

    public static Map.Entry<Result, AuthToken> check(String token) {
        AuthToken auth = DataStore.getInstance().getToken(token);
        if (auth == null) {
            return new AbstractMap.SimpleEntry<>(Result.INVALID, null);
        }
        LocalDateTime now = LocalDateTime.now();
        if (auth.creation.plusSeconds(auth.expiration).compareTo(now) < 0) {
            DataStore.getInstance().removeToken(token);
            return new AbstractMap.SimpleEntry<>(Result.EXPIRED, null);
        }
        return new AbstractMap.SimpleEntry<>(Result.OK, auth);
    }

    public static enum Result {
        OK, EXPIRED, INVALID;
    }
}
