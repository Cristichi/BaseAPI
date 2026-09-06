package es.cristichi.baseapi.rest;

import com.sun.net.httpserver.HttpExchange;
import es.cristichi.baseapi.obj.DataStore;
import es.cristichi.baseapi.data.User;
import es.cristichi.baseapi.exc.ResourceNotFoundException;
import java.io.IOException;
import java.net.URI;

public class UserHandler extends HttpHandlerAdapter {

    public UserHandler() {
        super(true, "user_read");
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
            return HttpResponse.fromJSON(200, user);
        } else {
            return new HttpResponse.JsonBuilder(400)
                    .error("Parameter Not Found",
                            "Please provide the user's email")
                    .build();
        }
    }

    @Override
    protected HttpResponse handlePOST(HttpExchange request, User ignored) throws IOException {
        return new HttpResponse.JsonBuilder(501)
                .error("Not Yet Implemented", "We are working on it.")
                .build();
    }
}
