package es.cristichi.baseapi.obj.rest.admin;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.BaseAPIMain;
import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.rest.HttpHandlerAdapter;

import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ServerAdminHandler extends HttpHandlerAdapter {

    public ServerAdminHandler() {
        super(true, new ScopeRequirement("admin_execute"));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected HttpResponse handlePOST(HttpExchange request, User admin) throws IOException {
        String body = new String(request.getRequestBody().readAllBytes());
        try {
            JSONObject bodyJson = (JSONObject) new JSONParser().parse(body);
            if (bodyJson.getOrDefault("secret", "").equals("none") && bodyJson.containsKey("operation")) {
                Object op = bodyJson.getOrDefault("operation", "");
                if (op instanceof JSONObject opObj) {
                    if (opObj.getOrDefault("required", "").equals("muscle")
                            && opObj.getOrDefault("name", "") instanceof String opName) {
                        switch (opName) {
                            case "shutdown" -> {
                                if (opObj.getOrDefault("chimichanga", "") instanceof String chimichangaStr) {
                                    try {
                                        // Just unnecesary requirements
                                        Integer.valueOf(chimichangaStr);
                                        BaseAPIMain.shutdown();
                                        System.getLogger(ServerAdminHandler.class.getName()).log(
                                                System.Logger.Level.TRACE, "Admin {0} requested shutdown.",
                                                admin.getEmail());
                                        return new HttpResponse(200, "{}");
                                    } catch (NumberFormatException _) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return new HttpResponse.JsonBuilder(500)
                    .error(e)
                    .build();
        }

        return new HttpResponse.JsonBuilder(400)
                .error("Nope", "Nice try.")
                .build();
    }
}
