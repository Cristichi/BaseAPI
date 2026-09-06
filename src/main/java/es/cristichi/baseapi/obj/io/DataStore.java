package es.cristichi.baseapi.obj.io;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import es.cristichi.baseapi.obj.data.User;

/**
 * Example DataStore class that provides access to user data. Pretend this class
 * accesses a database.
 */
public class DataStore {

    //Map of names to Person instances.
    private final Map<String, User> userMap = new HashMap<>();

    //this class is a singleton and should not be instantiated directly!
    private static final DataStore instance = new DataStore();

    public static DataStore getInstance() {
        return instance;
    }

    //private constructor so people know to use the getInstance() function instead
    private DataStore() {
        //dummy data
        User adminCris = new User("cristichi@hotmail.es", "Cristichi",
                Base64.getEncoder().encodeToString("nepe".getBytes()),
                "admin", "user_read");
        putUser(adminCris);
        putUser(new User("timmy@cristichi.es", "Timmy Neutrón",
                Base64.getEncoder().encodeToString("1234".getBytes()),
                "user_read"));
    }

    public User getUser(String email) {
        return userMap.get(email);
    }

    public void putUser(User person) {
        userMap.put(person.getEmail(), person);
    }
}
