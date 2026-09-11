package es.cristichi.baseapi.obj.rest.admin;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.exc.ResourceNotFoundException;
import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;
import es.cristichi.baseapi.obj.rest.HttpHandlerAdapter;

import java.io.IOException;
import java.net.URI;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AdminUserHandler extends HttpHandlerAdapter {
    public AdminUserHandler() {
        super(new String[]{"GET", "POST", "DELETE"}, true,
            new ScopeRequirement("GET", "admin_read"),
            new ScopeRequirement("POST", "admin_write"),
            new ScopeRequirement("DELETE", "admin_delete"));
    }

    @Override
    protected HttpResponse handleGET(HttpExchange request, User requester) throws IOException {
        URI uri = request.getRequestURI();
        String query = uri.toString().substring("/api/admin/user/".length());
        if (query.length() > 0) {
            User user = DataStore.getInstance().getUser(query);
            if (user == null) {
                return new HttpResponse.JsonBuilder(404)
                        .error(new ResourceNotFoundException("User not found"))
                        .put("user", query)
                        .build();
            }
            return HttpResponse.fromJSON(200, user.toThemselvesSafe());
        } else {
            return new HttpResponse.JsonBuilder(400)
                    .error("Parameter Not Found",
                            "Please provide the user's email")
                    .build();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected HttpResponse handlePOST(HttpExchange request, User admin) throws IOException {
        URI uri = request.getRequestURI();
        String query = uri.toString().substring("/api/admin/user/".length());
        if (query.length() > 0) {
            User user = DataStore.getInstance().getUser(query);
            if (user == null) {
                return new HttpResponse.JsonBuilder(404)
                        .error(new ResourceNotFoundException("User not found"))
                        .put("user", query)
                        .build();
            }

            String body = new String(request.getRequestBody().readAllBytes());
            try {
                JSONObject bodyJson = (JSONObject) new JSONParser().parse(body);
                if (bodyJson.containsKey("admittedScopes")) {
                    user.put("admittedScopes", bodyJson.get("admittedScopes"));
                }
                if (bodyJson.containsKey("username")) {
                    user.put("username", bodyJson.get("username"));
                }
                DataStore.getInstance().putUser(user);
                return HttpResponse.fromJSON(200, user.toThemselvesSafe());
            } catch (ParseException e) {
                return new HttpResponse.JsonBuilder(500)
                        .error(e)
                        .build();
            }
        } else {
            return new HttpResponse.JsonBuilder(400)
                    .error("Parameter Not Found",
                            "Please provide the user's email")
                    .build();
        }
    }

    @Override
    protected HttpResponse handleDELETE(HttpExchange request, User requester) throws IOException {
        URI uri = request.getRequestURI();
        String query = uri.toString().substring("/api/admin/user/".length());
        if (query.length() > 0) {
            User user = DataStore.getInstance().getUser(query);
            if (user == null) {
                return new HttpResponse.JsonBuilder(404)
                        .error(new ResourceNotFoundException("User not found"))
                        .put("user", query)
                        .build();
            }
            return HttpResponse.fromJSON(200, user.toThemselvesSafe());
        } else {
            return new HttpResponse.JsonBuilder(400)
                    .error("Parameter Not Found",
                            "Please provide the user's email")
                    .build();
        }
    }
}
