package es.cristichi.baseapi.obj.rest;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.obj.data.User;

import java.io.IOException;
import java.util.List;
import org.json.simple.JSONObject;

public class UserMeHandler extends HttpHandlerAdapter {

    public UserMeHandler() {
        super(true);
    }

    @Override
    protected HttpResponse handleGET(HttpExchange request, User requester) throws IOException {
        JSONObject userInfo = new JSONObject(requester);
        userInfo.put("scopes", List.of(requester.getAdmittedScopes()));
        return HttpResponse.fromJSON(200, userInfo);
    }
}
