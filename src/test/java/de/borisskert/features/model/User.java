package de.borisskert.features.model;

import java.time.LocalDate;
import java.util.Map;

public class User {

    public final String username;
    public final String email;
    public final LocalDate dateOfBirth;

    public User(String username, String email, LocalDate dateOfBirth) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }

    public static User from(Map<String, String> entry) {
        String username = entry.get("Username");
        String email = entry.get("Email");
        LocalDate birthDate = LocalDate.parse(entry.get("Day of Birth"));

        return new User(username, email, birthDate);
    }
}
