package es.cristichi.baseapi.obj.rest;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.BaseAPIMain;
import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;

import java.io.IOException;
import java.net.URI;

public class WebHandler extends HttpHandlerAdapter {
    protected String resourceBase;

    public WebHandler(String resourceBase) {
        super(false);
        this.resourceBase = resourceBase;
    }

    @Override
    protected HttpResponse handleGET(HttpExchange request, User ignored) throws IOException {
        try {
            URI uri = request.getRequestURI();
            String path = uri.toString();
            if (path.endsWith("/")) {
                path = path.concat("index.html");
            }
            try {
                String page = DataStore.getResourceFileContent(resourceBase + path);
                return new HttpResponse(200, page);
            } catch (Exception e) {
                String page = DataStore.getResourceFileContent(resourceBase + "/error/404.html");
                return new HttpResponse(404, page);
            }
        } catch (Exception e) {
            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR, e);
            e.printStackTrace();
            return new HttpResponse.JsonBuilder(500)
                    .error("Server Error", "Unexpected server error")
                    .build();
        }
    }
}
