package es.cristichi.baseapi.obj.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.rest.ldap.AuthToken;

/**
 * Example DataStore class that provides access to user data. Pretend this class
 * accesses a database.
 */
public class DataStore implements Serializable {
    private static final String FILENAME = "data.bin";
    private final String folderPath;

    private Map<String, User> userMap;    
    private HashMap<String, AuthToken> authMap;

    // this class is a singleton and should not be instantiated directly!
    private static DataStore instance = null;

    public static void init() throws IOException {
        if (instance == null){
            try {
                instance = new DataStore();
            } catch (ClassNotFoundException e) {
                throw new IOException("Could not read the objects in the file (old version?)", e);
            }
        }
    }

    public static DataStore getInstance() {
        return instance;
    }

    // private constructor so people know to use the getInstance() function instead
    private DataStore() throws IOException, ClassNotFoundException {
        folderPath = "%s/Documents/Base API/".formatted(System.getProperty("user.home"));
        File saveFile = new File(folderPath, FILENAME);
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        } else if (saveFile.exists()){
            readFromFile();
        } else {
            // dummy data
            userMap = new HashMap<>(2);
            User adminCris = new User("cristichi@hotmail.es", "Cristichi",
                    Base64.getEncoder().encodeToString("nepe".getBytes()),
                    "admin", "user_read");
            putUser(adminCris);
            putUser(new User("timmy@cristichi.es", "Timmy Neutrón",
                    Base64.getEncoder().encodeToString("1234".getBytes()),
                    "user_read"));
            saveToFile();
        }

        authMap = new HashMap<>(40);
    }

    public void saveToFile() throws FileNotFoundException, IOException{
        File saveFile = new File(folderPath, FILENAME);
        writeObject(new ObjectOutputStream(new FileOutputStream(saveFile)));
    }

    public void readFromFile() throws FileNotFoundException, IOException, ClassNotFoundException{
        File saveFile = new File(folderPath, FILENAME);
        readObject(new ObjectInputStream(new FileInputStream(saveFile)));
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(userMap.size());
        for (User u : userMap.values()) {
            out.writeObject(u);
        }

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            int uSize = in.readInt();
            userMap = new HashMap<>(uSize);
            for (int i = 0; i < uSize; i++){
                putUser((User) in.readObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    public User getUser(String email) {
        return userMap.get(email);
    }

    public void putUser(User person) {
        userMap.put(person.getEmail(), person);
    }

    public boolean containsToken(String token){
        return authMap.containsKey(token);
    }

    public AuthToken putToken(AuthToken auth){
        return authMap.put(auth.getToken(), auth);
    }

    public AuthToken getToken(String token) {
        return authMap.get(token);
    }

    public AuthToken removeToken(String token) {
        return authMap.remove(token);
    }
}
