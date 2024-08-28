package src.database;

import src.authentication.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase {

        private List<User> users = new ArrayList<>();

        private static final String FILE_OF_USER_DATA = "UserData.txt";

        /**
         * Adds a user to the database.
         * 
         * @param user the user to be added
         */
        public void addUserToDb(User user) {
                users.add(user);
                saveUser();
        }

        /**
         * Saves the user data to a file.
         * 
         * This method writes the username and hashed password of each user in the
         * database
         * to a file named "UserData.txt". Each user's data is written on a separate
         * line,
         * with the username and hashed password separated by a comma.
         * 
         * @throws IOException if an I/O error occurs while writing to the file
         */
        public void saveUser() {
                try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_OF_USER_DATA, true));
                        for (User user : users) {
                                writer.write(user.getUsername() + "," + user.getHashedPassword());
                                writer.newLine();
                        }
                        writer.close();
                        System.out.println("User registered successfully...");
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        /**
         * Authenticates a user.
         * 
         * This method takes a username and input password as parameters and
         * authenticates the user.
         * It checks if the provided username and password match the stored username and
         * hashed password
         * in the user database. If the authentication is successful, it returns true.
         * Otherwise, it returns false.
         * 
         * @param username      the username of the user to be authenticated
         * @param inputPassword the input password of the user to be authenticated
         * @return true if the authentication is successful, false otherwise
         */
        public boolean authenticateUser(String username, String inputPassword) {
                try (BufferedReader br = new BufferedReader(new FileReader(FILE_OF_USER_DATA))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                                String[] parts = line.split(",");
                                if (parts.length == 2) {
                                        String storedUsername = parts[0].trim();
                                        String storedHashedPassword = parts[1].trim();
                                        User user = new User(username, inputPassword);
                                        if (storedUsername.equals(username)) {
                                                if (user.getHashedPassword().equals(storedHashedPassword)) {
                                                        System.out.println(
                                                                        "Authentication successfull...");
                                                        return true;
                                                }
                                                System.out.println("Incorrect password. pls try again");
                                                // Authentication successful
                                        }
                                }
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
                System.out.println("Authentication fail");
                return false; // Authentication failed
        }

}
