package es.cristichi.baseapi;

import com.sun.net.httpserver.HttpServer;
import es.cristichi.baseapi.rest.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Cristichi
 */
public class BaseAPIMain {
    private static JFrame window = null;
    private static HttpServer server = null;
    private static boolean stopping = false;
    
    public static void shutdown() {
        if (!stopping){
            stopping = true;
            window.dispose();
        }
    }

    public static void main(String[] args) {        
        try {
            window = new JFrame("Base API");
            window.setSize(500, 400);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Window is closing. Closing server.");
                    if (server != null){
                        server.stop(2);
                    }
                }
            });
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            window.setLayout(new BorderLayout());
            window.add(new JLabel("Close this window to close the API."), BorderLayout.CENTER);
            window.add(new JLabel("UI design is my passion."), BorderLayout.SOUTH);
            window.setVisible(true);

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