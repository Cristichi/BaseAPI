package es.cristichi.baseapi.obj.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import es.cristichi.baseapi.obj.data.User;
import es.cristichi.baseapi.obj.ldap.AuthToken;
import es.cristichi.baseapi.obj.ldap.AuthToken.CheckResult;
import es.cristichi.baseapi.obj.ldap.AuthToken.Result;

public class DataStore implements Serializable {
    private static final String FILENAME = "data.bin";
    private final String folderPath;

    private HashMap<String, User> userMap;
    private HashMap<String, AuthToken> authMap;

    private static DataStore instance = null;

    public static void init() throws IOException {
        if (instance == null) {
            try {
                instance = new DataStore();
            } catch (Exception e) {
                throw new IOException("Could not read the objects in the file (old version?)", e);
            }
        }
    }

    public static DataStore getInstance() {
        return instance;
    }

    public static String getResourceFileContent(String resourcePath) throws IOException {
        try (var in = DataStore.class.getResourceAsStream(resourcePath)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private DataStore() throws IOException, ClassNotFoundException {
        folderPath = "%s/Documents/Base API/".formatted(System.getProperty("user.home"));
        File saveFile = new File(folderPath, FILENAME);
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        } else if (saveFile.exists()) {
            readFromFile();
        } else {
            // dummy data
            userMap = new HashMap<>(2);
            User adminCris = new User("admin@example.com", "Admin User",
                    Base64.getEncoder().encodeToString("nepe".getBytes()),
                    "admin", "user_read");
            putUser(adminCris);
            putUser(new User("timmy@example.com", "Timmy Cantón",
                    Base64.getEncoder().encodeToString("1234".getBytes()),
                    "user_read"));

            authMap = new HashMap<>(5);
            saveToFile();
        }
    }

    public void saveToFile() throws FileNotFoundException, IOException {
        File saveFile = new File(folderPath, FILENAME);
        writeObject(new ObjectOutputStream(new FileOutputStream(saveFile)));
    }

    public void readFromFile() throws FileNotFoundException, IOException, ClassNotFoundException {
        File saveFile = new File(folderPath, FILENAME);
        readObject(new ObjectInputStream(new FileInputStream(saveFile)));
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(userMap);
        out.writeObject(authMap);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            userMap = (HashMap<String, User>) in.readObject();
            authMap = (HashMap<String, AuthToken>) in.readObject();

            if (authMap == null){
                authMap = new HashMap<>(50);
            } else {
                // Let's remove the invalid ones, liked expired.
                Collection<AuthToken> readAuths = Collections.unmodifiableCollection(authMap.values());
                for (AuthToken auth : readAuths) {
                    CheckResult check = checkToken(auth.getToken());
                    if (!check.result().equals(Result.OK)) {
                        authMap.remove(auth.getToken());
                    }
                }
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

    public boolean containsToken(String token) {
        return authMap.containsKey(token);
    }

    public AuthToken putToken(AuthToken auth) {
        return authMap.put(auth.getToken(), auth);
    }

    public AuthToken getToken(String token) {
        return authMap.get(token);
    }

    public AuthToken removeToken(String token) {
        return authMap.remove(token);
    }

    public CheckResult checkToken(String token) {
        AuthToken auth = getToken(token);
        if (auth == null) {
            return new CheckResult(Result.INVALID, null, null);
        }
        User user = getUser(auth.getUserEmail());
        if (user == null) {
            return new CheckResult(Result.INVALID, null, null);
        }
        LocalDateTime now = LocalDateTime.now();
        if (auth.getCreationDateTime().plusSeconds(auth.getExpiration()).compareTo(now) < 0) {
            removeToken(token);
            return new CheckResult(Result.EXPIRED, null, null);
        }
        return new CheckResult(Result.OK, auth, user);
    }
}
