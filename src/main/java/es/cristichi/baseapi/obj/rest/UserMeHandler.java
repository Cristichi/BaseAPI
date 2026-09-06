package es.cristichi.baseapi.obj.rest;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.obj.data.User;

import java.io.IOException;

public class UserMeHandler extends HttpHandlerAdapter {

    public UserMeHandler() {
        super(true);
    }

    @Override
    protected HttpResponse handleGET(HttpExchange request, User requester) throws IOException {
        return HttpResponse.fromJSON(200, requester.toThemselvesSafe());
    }
}
