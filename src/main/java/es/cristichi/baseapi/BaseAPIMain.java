package es.cristichi.baseapi;

import com.sun.net.httpserver.HttpServer;
import es.cristichi.baseapi.rest.*;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author Cristichi
 */
public class BaseAPIMain {
    private static HttpServer server = null;
    private static boolean stopping = false;
    
    public static void shutdown(){
        if (!stopping){
            stopping = true;
            new Thread(() -> {
                if (server != null){
                   server.stop(3);
                }
            }).start();
        }
    }

    public static void main(String[] args) {        
        try {
            System.out.println("Creating server...");
            server = HttpServer.create(new InetSocketAddress("localhost", 935), 0);
            
            server.createContext("/", new WebHandler());
            
            server.createContext("/auth/token", new AuthHandler());
            
            server.createContext("/api/user", new UserHandler());
            server.createContext("/api/user/me", new UserMeHandler());
            server.createContext("/api/admin/server", new ServerAdminHandler());
            
            System.out.println("Starting server...");
            server.start();
            System.out.printf("Server running on \"%s\".%n", server.getAddress().toString());
        } catch (IOException ex) {
            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR, "Error while starting server.", ex);
            if (server != null){
               server.stop(5);
            }
        }
    }
}