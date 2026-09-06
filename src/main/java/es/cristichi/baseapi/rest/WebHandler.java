package es.cristichi.baseapi.rest;

import com.sun.net.httpserver.HttpExchange;
import es.cristichi.baseapi.data.User;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
            URL resURL = WebHandler.class.getResource(resBasepath + path);
            if (resURL != null) {
                return new HttpResponse(200, Files.readString(Path.of(resURL.toURI())));
            } else {
                resURL = WebHandler.class.getResource(resBasepath + "/error/404.html");
                return new HttpResponse(200, Files.readString(Path.of(resURL.toURI())));
            }
        } catch (URISyntaxException ex) {
            System.getLogger(WebHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return new HttpResponse.JsonBuilder(500)
                    .error("Server Error", "Error in the server trying to get the HTML file")
                    .build();
        } catch (IOException ex) {
            return new HttpResponse.JsonBuilder(500)
                    .error("Server Error", "Error in the server trying to get the HTML file")
                    .build();
        } catch (Exception ex) {
            return new HttpResponse.JsonBuilder(500)
                    .error("Server Error", "Unexpected server error")
                    .build();
        }
    }
}
