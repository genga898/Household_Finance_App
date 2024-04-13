package models;

import androidx.annotation.NonNull;

import java.util.List;

public class User {
    public String name;
    public String emailAddress;
    public String password;
    public List<Goal> goals;
    public String role;

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(@NonNull String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public List<Goal> getGoals() {
        return goals;
    }
}
