/*
 */
package es.cristichi.baseapi.obj.data;

import java.util.Base64;
import org.json.simple.JSONObject;

/**
 *
 * @author Cristichi
 */
public class User extends JSONObject{
    private final String[] admittedScopes;
    private final String password;
    
    public User(String email, String username, String encryptedPsw, String... admittedScopes) {
        put("email", email);
        put("username", username);
        this.password = encryptedPsw;
        this.admittedScopes = admittedScopes;
    }

    public String getEmail() {
        return getOrDefault("email", null).toString();
    }

    public String getUsername() {
        return getOrDefault("username", "").toString();
    }

    public String[] getAdmittedScopes() {
        return admittedScopes;
    }
    
    public boolean checkPsw(String password){
        return this.password.equals(Base64.getEncoder()
                .encodeToString(password.getBytes()));
    }
    
    public boolean hasScopes(String... scopes){
        for (String scope : scopes){
            boolean hasIt = false;
            for (String admitted : admittedScopes){
                if (scope.equals(admitted)){
                    hasIt = true;
                    break;
                }
            }
            if (!hasIt){
                return false;
            }
        }
        
        return true;
    }
}
