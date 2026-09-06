package es.cristichi.baseapi;

import com.sun.net.httpserver.HttpServer;

import es.cristichi.baseapi.obj.io.DataStore;
import es.cristichi.baseapi.obj.rest.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    
    public static void shutdown() {
        try {
            DataStore.getInstance().saveToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            if (server != null){
                server.stop(3);
            }
            if (window != null){
                window.dispose();
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            DataStore.init();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            window = new JFrame("Base API");
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
            try {
                window.getContentPane().add(new JLabel(Files.readString(Path.of(BaseAPIMain.class.getResource("/ui/mainWindow.html").toURI()))));
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
                window.getContentPane().setLayout(new BorderLayout());
                window.getContentPane().add(new JLabel("You are seeing this because there was an error trying to take the real UI for this."+
                                                "You are not missing much."), BorderLayout.NORTH);
                window.getContentPane().add(new JLabel("Close this window to close the API."), BorderLayout.CENTER);
                window.getContentPane().add(new JLabel("UI design is my passion."), BorderLayout.SOUTH);
            }
            window.pack();
            window.setLocationRelativeTo(null);
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
            shutdown();
        }
    }
}