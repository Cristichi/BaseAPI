package es.cristichi.baseapi.rest;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import es.cristichi.baseapi.data.User;
import es.cristichi.baseapi.obj.DataStore;
import es.cristichi.baseapi.rest.ldap.AuthToken;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class AuthHandler extends HttpHandlerAdapter {

    public AuthHandler() {
        super(false);
    }
    
    @Override
    protected HttpResponse handlePOST(HttpExchange request, User ignored) throws IOException {
        Headers headers = request.getRequestHeaders();
        
        if (!headers.containsKey("Authorization")){
            return new HttpResponse.JsonBuilder(401)
                    .error("Unauthorized", "Please use Authorization to continue.")
                    .build();
        }        
        if (!headers.containsKey("Content-type")){
            return new HttpResponse.JsonBuilder(400)
                    .error("Bad Request", "What do you say the body is supposed to be?")
                    .build();
        }
        
        List<String> authorization = headers.get("Authorization");
        if (authorization.size() != 1){
            return new HttpResponse.JsonBuilder(400)
                    .error("Bad Request", "There can only be one Authorization header.")
                    .build();
        }
        if (!authorization.get(0).startsWith("Basic ")){
            return new HttpResponse.JsonBuilder(400)
                    .error("Bad Request",
                            "Value of header \"Authorization\" not accepted: "+authorization.get(0))
                    .build();
        }
        
        String[] userPass = new String(Base64.getDecoder().decode(authorization.get(0).substring("Basic ".length()))).split(":", 2);
        User user = DataStore.getInstance().getUser(userPass[0]);
        if (user == null || !user.checkPsw(userPass[1])){
            return new HttpResponse.JsonBuilder(401)
                    .error("Unauthorized", "Unknown user or not authorized for the given scopes.")
                    .build();
        }
        
        List<String> contentType = headers.get("Content-type");
        if (contentType.size() != 1){
            return new HttpResponse.JsonBuilder(400)
                    .error("Bad Request", "Content-type header has to be only form encoded.")
                    .build();
        }
        if (!contentType.get(0).equals("application/x-www-form-urlencoded")){
            return new HttpResponse.JsonBuilder(400)
                    .error("Bad Request",
                            "Value of header \"Content-type\" not accepted: "+contentType.get(0))
                    .build();
        }
        String body = new String(request.getRequestBody().readAllBytes());
        String grantType = null;
        String scope = null;
        StringTokenizer bodyTokens = new StringTokenizer(body, "&", false);
        while (bodyTokens.hasMoreTokens()) {
            String bodyToken = bodyTokens.nextToken();
            String[] values = bodyToken.split("=", 2);
            if (values.length>1){
                switch (values[0]) {
                    case "grant_type" -> {
                        grantType = values[1];
                    }
                    case "scope" -> {
                        scope = values[1];
                    }
                }
            }
        }
        if (grantType == null || !grantType.equals("client_credentials")){
            return new HttpResponse.JsonBuilder(400)
                    .error("Bad Request", "Invalid grant type.")
                    .build();
        }
        if (scope == null || scope.isEmpty()){
            return new HttpResponse.JsonBuilder(400)
                    .error("Bad Request", "No scopes selected.")
                    .build();
        }
        String[] scopes = scope.split("%2C");
        if (!user.hasScopes(scopes)){
            return new HttpResponse.JsonBuilder(401)
                    .error("Unauthorized", "Unknown user or not authorized for the given scopes.")
                    .build();
        }
        
        Map.Entry<String, AuthToken> authToken = AuthToken.generate(user, scopes);

        return new HttpResponse(200, authToken.getValue().toJSON(authToken.getKey()));
    }
}
