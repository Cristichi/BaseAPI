package es.cristichi.baseapi.obj.rest;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import es.cristichi.baseapi.BaseAPIMain;
import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;
import es.cristichi.baseapi.obj.ldap.AuthToken.TokenCheckResult;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.json.simple.JSONObject;

public class HttpHandlerAdapter implements HttpHandler {
    protected final boolean requiresToken;
    protected final ScopeRequirement[] requiredScopes;

    protected HttpHandlerAdapter(boolean requiresToken, ScopeRequirement... requiredScopes) {
        this.requiresToken = requiresToken;
        if (!requiresToken && requiredScopes.length>0){
            throw new RuntimeException("Handlers can't check scopes without a token.");
        }
        this.requiredScopes = requiredScopes;
    }

    protected boolean checkGeneralScopes(TokenCheckResult tokenCheckResult) {
        if (!tokenCheckResult.user().hasScopes(tokenCheckResult.auth().getScopes())){
            return false;
        }
        for (ScopeRequirement scopeReq : requiredScopes) {
            boolean found = false;
            for (String gScope : tokenCheckResult.auth().getScopes()) {
                if (scopeReq.method().equals("") && scopeReq.scope().equals(gScope)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkScopes(String method, TokenCheckResult tokenCheckResult) {
        if (!tokenCheckResult.user().hasScopes(tokenCheckResult.auth().getScopes())){
            return false;
        }
        for (ScopeRequirement scopeReq : requiredScopes) {
            boolean found = false;
            for (String gScope : tokenCheckResult.auth().getScopes()) {
                if ((scopeReq.method().equals("") || scopeReq.method().equals(method))
                        && scopeReq.scope().equals(gScope)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void handle(HttpExchange request) throws IOException {
        String method = request.getRequestMethod();
        HttpResponse resObj = null;
        try {
            TokenCheckResult tokenResult = null;
            boolean ok = true;
            if (requiresToken) {
                ok = false;
                Headers headers = request.getRequestHeaders();

                if (headers.containsKey("Authorization")) {
                    List<String> authorization = headers.get("Authorization");
                    if (authorization.size() == 1) {
                        if (authorization.get(0).startsWith("Bearer ")) {
                            String bearerToken = authorization.get(0).substring("Bearer ".length());
                            TokenCheckResult check = DataStore.getInstance().checkToken(bearerToken);
                            switch (check.result()) {
                                case OK -> {
                                    if (checkGeneralScopes(check)) {
                                        tokenResult = check;
                                        ok = true;
                                    } else {
                                        resObj = new HttpResponse.JsonBuilder(403)
                                                .error("Forbidden",
                                                        "Missing scope.")
                                                .build();
                                    }
                                }
                                case EXPIRED -> {
                                    resObj = new HttpResponse.JsonBuilder(403)
                                            .error("Forbidden",
                                                    "Token expired.")
                                            .build();
                                }
                                case INVALID -> {
                                    resObj = new HttpResponse.JsonBuilder(401)
                                            .error("Unauthorized",
                                                    "Invalid token.")
                                            .build();
                                }
                            }
                        } else {
                            resObj = new HttpResponse.JsonBuilder(400)
                                    .error("Bad Request",
                                            "Value of header \"Authorization\" not accepted: " + authorization.get(0))
                                    .build();
                        }
                    } else {
                        resObj = new HttpResponse.JsonBuilder(400)
                                .error("Bad Request", "There can only be one Authorization header.")
                                .build();
                    }

                } else {
                    resObj = new HttpResponse.JsonBuilder(401)
                            .error("Unauthorized", "Please use Authorization to continue.")
                            .build();
                }
            }

            if (ok) {
                switch (method) {
                    case "GET" -> {
                        if (!requiresToken || checkScopes("GET", tokenResult)) {
                            resObj = handleGET(request, tokenResult.user());
                        } else {
                            resObj = new HttpResponse.JsonBuilder(403)
                                    .error("Forbidden",
                                            "Missing scope on token or user.")
                                    .build();
                        }
                    }
                    case "POST" -> {
                        if (!requiresToken || checkScopes("POST", tokenResult)) {
                            resObj = handlePOST(request, tokenResult.user());
                        } else {
                            resObj = new HttpResponse.JsonBuilder(403)
                                    .error("Forbidden",
                                            "Missing scope on token or user.")
                                    .build();
                        }
                    }
                    case "PUT" -> {
                        if (!requiresToken || checkScopes("PUT", tokenResult)) {
                            resObj = handlePUT(request, tokenResult.user());
                        } else {
                            resObj = new HttpResponse.JsonBuilder(403)
                                    .error("Forbidden",
                                            "Missing scope on token or user.")
                                    .build();
                        }
                    }
                    case "PATCH" -> {
                        if (!requiresToken || checkScopes("PATCH", tokenResult)) {
                            resObj = handlePATCH(request, tokenResult.user());
                        } else {
                            resObj = new HttpResponse.JsonBuilder(403)
                                    .error("Forbidden",
                                            "Missing scope on token or user.")
                                    .build();
                        }
                    }
                    case "DELETE" -> {
                        if (!requiresToken || checkScopes("DELETE", tokenResult)) {
                            resObj = handleDELETE(request, tokenResult.user());
                        } else {
                            resObj = new HttpResponse.JsonBuilder(403)
                                    .error("Forbidden",
                                            "Missing scope on token or user.")
                                    .build();
                        }
                    }
                    case "OPTIONS" -> {
                        if (!requiresToken || checkScopes("OPTIONS", tokenResult)) {
                            resObj = handleOPTIONS(request, tokenResult.user());
                        } else {
                            resObj = new HttpResponse.JsonBuilder(403)
                                    .error("Forbidden",
                                            "Missing scope on token or user.")
                                    .build();
                        }
                    }
                    default -> {
                        resObj = new HttpResponse.JsonBuilder(405)
                                .error(new UnsupportedOperationException("Unsupported method: " + method))
                                .build();
                    }
                }
            }
        } catch (Exception e) {
            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR, e);
            e.printStackTrace();
            resObj = new HttpResponse.JsonBuilder(405)
                    .error(e)
                    .build();
        } finally {
            if (resObj == null) {
                resObj = new HttpResponse.JsonBuilder(500)
                        .error(new Exception(
                                "Oh no, server error. We are sorry, please contact Cristichi to see what happened and let him solve it"))
                        .build();
            }
            request.getResponseHeaders().putAll(resObj.getHeaders());
            request.sendResponseHeaders(resObj.getStatus(), resObj.getResponse().getBytes().length);
            try (OutputStream os = request.getResponseBody()) {
                os.write(resObj.getResponse().getBytes());
            }
        }
    }

    protected HttpResponse handleGET(HttpExchange request, User requester) throws IOException {
        return new HttpResponse.JsonBuilder(405)
                .error(new UnsupportedOperationException("Unsupported method: GET"))
                .build();
    }

    protected HttpResponse handlePOST(HttpExchange request, User requester) throws IOException {
        return new HttpResponse.JsonBuilder(405)
                .error(new UnsupportedOperationException("Unsupported method: POST"))
                .build();
    }

    protected HttpResponse handlePUT(HttpExchange request, User requester) throws IOException {
        return new HttpResponse.JsonBuilder(405)
                .error(new UnsupportedOperationException("Unsupported method: PUT"))
                .build();
    }

    protected HttpResponse handlePATCH(HttpExchange request, User requester) throws IOException {
        return new HttpResponse.JsonBuilder(405)
                .error(new UnsupportedOperationException("Unsupported method: PATCH"))
                .build();
    }

    protected HttpResponse handleDELETE(HttpExchange request, User requester) throws IOException {
        return new HttpResponse.JsonBuilder(405)
                .error(new UnsupportedOperationException("Unsupported method: DELETE"))
                .build();
    }

    protected HttpResponse handleOPTIONS(HttpExchange request, User requester) throws IOException {
        return new HttpResponse.JsonBuilder(405)
                .error(new UnsupportedOperationException("Unsupported method: OPTIONS"))
                .build();
    }

    public static record ScopeRequirement(String method, String scope) {
        public ScopeRequirement(String scope) {
            this("", scope);
        }
    }

    public static class HttpResponse {
        private final int status;
        private final Headers headers;
        private final String response;

        public static HttpResponse fromJSON(int status, JSONObject json) {
            Headers headers = new Headers();
            headers.put("Content-type", List.of("application/json"));
            return new HttpResponse(status, headers, json);
        }

        public HttpResponse(int status, Object responseObject) {
            this.status = status;
            this.headers = new Headers();
            this.response = responseObject.toString();
        }

        public HttpResponse(int status, Headers headers, Object responseObject) {
            this.status = status;
            this.headers = headers;
            this.response = responseObject.toString();
        }

        public int getStatus() {
            return status;
        }

        public Headers getHeaders() {
            return headers;
        }

        public String getResponse() {
            return response;
        }

        @Override
        public String toString() {
            return "HttpResponse (status=%d)%nHeaders:%n%s%nBody:%n%s%n".formatted(status, headers, response);
        }

        public static class JsonBuilder {
            private final int status;
            private final JSONObject json;

            public JsonBuilder(int status) {
                this.status = status;
                this.json = new JSONObject();
            }

            public JsonBuilder error(Exception exc) {
                return error(exc.getClass().getName(), exc.getMessage());
            }

            public JsonBuilder error(String error, String msg) {
                put("error", error);
                put("msg", msg);
                return this;
            }

            @SuppressWarnings("unchecked")
            public JsonBuilder put(Object key, Object value) {
                json.put(key, value);
                return this;
            }

            public HttpResponse build() {
                Headers headers = new Headers();
                headers.put("Content-type", List.of("application/json"));
                return new HttpResponse(status, headers, json);
            }
        }
    }
}
