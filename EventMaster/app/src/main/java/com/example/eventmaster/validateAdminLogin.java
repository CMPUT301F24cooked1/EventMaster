package com.example.eventmaster;

/**
 *  Ensures the person accessing admin privileges has the correct login information
 */
public class validateAdminLogin {
    // A hardcoded password
    final String adminPassword = "";

    /**
     *
     * @param password a string the user inputs to check if they're an admin
     * @return if the user is an admin or not
     */
    public boolean checkIfAdmin(String password){
        return password.equals(adminPassword);
    }
}
