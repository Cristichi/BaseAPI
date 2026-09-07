package es.cristichi.baseapi.obj.rest.user;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;
import es.cristichi.baseapi.obj.rest.HttpHandlerAdapter;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserMeHandler extends HttpHandlerAdapter {

    public UserMeHandler() {
        super(true);
    }

    @Override
    protected HttpResponse handleGET(HttpExchange request, User requester) throws IOException {
        return HttpResponse.fromJSON(200, requester.toThemselvesSafe());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected HttpResponse handlePOST(HttpExchange request, User requester) throws IOException {
        String body = new String(request.getRequestBody().readAllBytes());
        try {
            JSONObject bodyJson = (JSONObject) new JSONParser().parse(body);
            if (bodyJson.containsKey("username")) {
                requester.put("username", bodyJson.get("username"));
            }
            DataStore.getInstance().putUser(requester);
            return HttpResponse.fromJSON(200, requester.toThemselvesSafe());
        } catch (ParseException e) {
            return new HttpResponse.JsonBuilder(500)
                    .error(e)
                    .build();
        }
    }
}
