package es.cristichi.baseapi.obj.rest.user;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.exc.ResourceNotFoundException;
import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;
import es.cristichi.baseapi.obj.rest.HttpHandlerAdapter;

import java.io.IOException;
import java.net.URI;

public class UserHandler extends HttpHandlerAdapter {
    public UserHandler() {
        super(new String[]{"GET"}, true, new ScopeRequirement("GET", "user_read"));
    }

    @Override
    protected HttpResponse handleGET(HttpExchange request, User requester) throws IOException {
        URI uri = request.getRequestURI();
        String query = uri.toString().substring("/api/user/".length());
        if (query.length() > 0) {
            User user = DataStore.getInstance().getUser(query);
            if (user == null) {
                return new HttpResponse.JsonBuilder(404)
                        .error(new ResourceNotFoundException("User not found"))
                        .build();
            }
            return HttpResponse.fromJSON(200, user.toOtherSafe());
        } else {
            return new HttpResponse.JsonBuilder(400)
                    .error("Parameter Not Found",
                            "Please provide the user's email")
                    .build();
        }
    }
}
