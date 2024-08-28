package src;

import src.authentication.User;
import src.database.ExecuteQueries;
import src.database.UserDatabase;

import java.util.Random;
import java.util.Scanner;

public class Main {

    /**
     * The main method is the entry point of the program. It allows the user to
     * interact with the application
     * by selecting options such as signing up, logging in, or exiting the program.
     *
     * @param args The command line arguments passed to the program.
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();
        UserDatabase userdb = new UserDatabase();

        while (true) {
            System.out.println("Select an option:");
            System.out.println("1: Sign Up");
            System.out.println("2: Login");
            System.out.println("3: Exit");

            System.out.print("Enter your input:");
            int choose = sc.nextInt();
            int captchcode = random.nextInt(10000);

            switch (choose) {
                case 1:
                    // Register the user
                    System.out.print("Enter your username: ");
                    String regUsername = sc.next();
                    System.out.print("Enter your password: ");
                    String regPassword = sc.next();
                    System.out.println("Captcha code is " + captchcode);
                    System.out.print("Enter the captcha: ");
                    int enteredCaptcha = sc.nextInt();

                    if (enteredCaptcha == captchcode) {
                        System.out.println("captcha matches");
                        // create new User
                        User user = new User(regUsername, regPassword);
                        userdb.addUserToDb(user);
                    } else {
                        System.out.println("Wrong captcha!! Please try again...");
                    }
                    break;
                case 2:

                    System.out.print("Enter your username: ");
                    String loginUsername = sc.next();
                    System.out.print("Enter your password: ");
                    String loginPassword = sc.next();
                    System.out.println("Captcha code is " + captchcode);
                    System.out.print("Enter the captcha:");
                    int enteredLoginCaptcha = sc.nextInt();
                    if (enteredLoginCaptcha == captchcode) {
                        Boolean isAuthenticated = userdb.authenticateUser(loginUsername, loginPassword);
                        if (isAuthenticated) {
                            ExecuteQueries executeQueries = new ExecuteQueries();
                            System.out.println("Enter the query");
                            executeQueries.execQuery();
                        }
                    } else {
                        System.out.println("Wrong captcha!! Please try again...");
                    }
                    break;
                case 3:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again... ");
            }
        }

    }
}
