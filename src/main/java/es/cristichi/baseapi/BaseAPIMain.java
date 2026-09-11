package es.cristichi.baseapi;

import com.sun.net.httpserver.HttpServer;

import es.cristichi.baseapi.obj.io.DataStore;
import es.cristichi.baseapi.obj.rest.*;
import es.cristichi.baseapi.obj.rest.admin.AdminUserHandler;
import es.cristichi.baseapi.obj.rest.admin.ServerAdminHandler;
import es.cristichi.baseapi.obj.rest.auth.AuthHandler;
import es.cristichi.baseapi.obj.rest.user.NewUserHandler;
import es.cristichi.baseapi.obj.rest.user.UserHandler;
import es.cristichi.baseapi.obj.rest.user.UserMeHandler;

import java.net.InetSocketAddress;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class BaseAPIMain {
    private static JFrame window = null;
    private static HttpServer server = null;
    
    public static void shutdown(int delay) {
        try {
            DataStore.getInstance().saveToFile();
        } catch (Exception e) {
            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR, "Error trying to save to file.", e);
        }
        new Thread(() -> {
            if (server != null){
                server.stop(delay);
            }
            if (window != null){
                window.dispose();
            }
        }).start();
    }
    
    public static void shutdown() {
        shutdown(3);
    }

    public static void main(String[] args) {
        System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.INFO, "Starting.");
        try {
            DataStore.init();
        } catch (Exception e) {
            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR,"Error trying to initialize DataStore.", e);
            System.exit(1);
        }
        try {
            window = new JFrame("Base API");
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.INFO, "Window is closing. Saving data and closing server.");
                    try {
                        DataStore.getInstance().saveToFile();
                    } catch (Exception error) {
                        System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR, "Error trying to save to DataStore.", error);
                    }
                    if (server != null){
                        server.stop(2);
                    }
                }
            });
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            try {
                window.getContentPane().add(new JLabel(DataStore.getResourceFileContent("/ui/mainWindow.html")));
            } catch (Exception e) {
                System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR, "Error trying to get the UI page.", e);
                window.getContentPane().setLayout(new BorderLayout());
                window.getContentPane().add(new JLabel("You are seeing this because there was an error trying to take the real UI for this."+
                                                "You are not missing much."), BorderLayout.NORTH);
                window.getContentPane().add(new JLabel("Close this window to close the API."), BorderLayout.CENTER);
                window.getContentPane().add(new JLabel("UI design is my passion."), BorderLayout.SOUTH);
            }
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.INFO, "Creating server...");
            server = HttpServer.create(new InetSocketAddress("localhost", 935), 0);
            
            server.createContext("/", new WebHandler("/web", "/weberror/404.html"));
            
            server.createContext("/auth/token", new AuthHandler());
            
            server.createContext("/api/user", new UserHandler());
            server.createContext("/api/user/me", new UserMeHandler());
            server.createContext("/api/user/register", new NewUserHandler());
            
            server.createContext("/api/admin/server", new ServerAdminHandler());
            server.createContext("/api/admin/user", new AdminUserHandler());
            
            server.start();
            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.INFO, "Server running on \"{0}\".", server.getAddress().toString());
        } catch (Exception e) {
            System.getLogger(BaseAPIMain.class.getName()).log(System.Logger.Level.ERROR, "Error trying to create the server", e);
            shutdown(0);
        }
    }
}