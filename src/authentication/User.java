package src.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {

    String username;
    String hashedPassword;

    public User(String username,String password){
        this.username=username;
        this.hashedPassword=hashPasswordString(password);
    }

    public String hashPasswordString(String password){

         try {
            // Creating MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            // Add input string to digest
            md.update(password.getBytes());
            
            // Get the hash's bytes
            byte[] bytes = md.digest();
            
            // Converting bytes to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            
            // Return complete hash
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // throws no such algorithm exception error
            throw new RuntimeException(e);
        }
    }

public String getUsername(){return username;};
public String getHashedPassword(){return hashedPassword;};


}
