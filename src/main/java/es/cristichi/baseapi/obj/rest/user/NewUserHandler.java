package es.cristichi.baseapi.obj.rest.user;

import java.io.IOException;
import java.util.Base64;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;
import es.cristichi.baseapi.obj.rest.HttpHandlerAdapter;

public class NewUserHandler extends HttpHandlerAdapter {
    // Some credits to the pattern. Probably not perfect, but good enough!
    // Source - https://stackoverflow.com/a/48725527
    // Posted by shimatai
    // Retrieved 2026-09-07, License - CC BY-SA 3.0
    private final Pattern EMAIL_REGEX = Pattern.compile(
            "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
            Pattern.CASE_INSENSITIVE);

    public NewUserHandler() {
        super(new String[]{"POST"}, false);
    }

    @Override
    protected HttpResponse handlePOST(HttpExchange request, User ignore) throws IOException {
        String body = new String(request.getRequestBody().readAllBytes());
        try {
            JSONObject bodyJson = (JSONObject) new JSONParser().parse(body);
            if (!bodyJson.containsKey("email")) {
                return new HttpResponse.JsonBuilder(400)
                        .error("Missing Field", "Missing \"email\".")
                        .build();
            }
            String email = bodyJson.get("email").toString();
            if (!EMAIL_REGEX.matcher(email).matches()) {
                return new HttpResponse.JsonBuilder(400)
                        .error("Incorrect Field", "Please use a valid email.")
                        .build();
            }
            if (!bodyJson.containsKey("username")) {
                return new HttpResponse.JsonBuilder(400)
                        .error("Missing Field", "Missing \"username\".")
                        .build();
            }
            if (!bodyJson.containsKey("password")) {
                return new HttpResponse.JsonBuilder(400)
                        .error("Missing Field", "Missing \"password\".")
                        .build();
            }
            if (DataStore.getInstance().containsUser(email)) {
                return new HttpResponse.JsonBuilder(409)
                        .error("Conflict", "Email already registered.")
                        .build();
            }
            User user = new User(
                    email,
                    bodyJson.get("username").toString(),
                    Base64.getEncoder().encodeToString(bodyJson.get("password").toString().getBytes()),
                    new String[] { "user_read" });
            DataStore.getInstance().putUser(user);
            return HttpResponse.fromJSON(200, user.toThemselvesSafe());
        } catch (ParseException e) {
            return new HttpResponse.JsonBuilder(500)
                    .error(e)
                    .build();
        }
    }
}
