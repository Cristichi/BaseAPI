package es.cristichi.baseapi.obj.rest;

import com.sun.net.httpserver.HttpExchange;

import es.cristichi.baseapi.BaseAPIMain;
import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.io.DataStore;

import java.io.IOException;
import java.net.URI;

public class WebHandler extends HttpHandlerAdapter {
    protected final String resourceBase;
    protected final String notFoundPath;

    public WebHandler(String resourceBase, String notFoundPath) {
        super(false);
        this.resourceBase = resourceBase;
        this.notFoundPath = notFoundPath;
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
                String page = DataStore.getResourceFileContent(notFoundPath);
                return new HttpResponse(404, page);
            }
        } catch (Exception e) {
            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR, "Error trying to get the web page from resources.", e);
            return new HttpResponse.JsonBuilder(500)
                    .error("Server Error", "Unexpected server error")
                    .build();
        }
    }
}
