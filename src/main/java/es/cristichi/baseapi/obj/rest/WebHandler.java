package es.cristichi.baseapi.obj.rest;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;

import java.io.IOException;
import java.net.URI;

public class WebHandler extends HttpHandlerAdapter {
    public WebHandler() {
        super(false);
    }

    @Override
    protected HttpResponse handleGET(HttpExchange request, User ignored) throws IOException {
        try {
            String resBasepath = "/web";
            URI uri = request.getRequestURI();
            String path = uri.toString();
            if (path.endsWith("/")) {
                path = path.concat("index.html");
            }
            try {
                String page = DataStore.getResourceFileContent(resBasepath + path);
                return new HttpResponse(200, page);
            } catch(Exception e){
                String page = DataStore.getResourceFileContent(resBasepath + "/error/404.html");
                return new HttpResponse(200, page);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new HttpResponse.JsonBuilder(500)
                    .error("Server Error", "Unexpected server error")
                    .build();
        }
    }
}
