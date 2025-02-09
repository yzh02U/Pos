package com.example.poshaisan;

import javafx.scene.control.Alert;

/**
 * Utility class for generating custom JavaFX Alerts.
 */
public class CustomAlert {

    // Methods --------------------------------------------------

    /**
     * Generates a custom Alert with specified title, content, and type.
     *
     * @param title   the title of the alert window
     * @param content the main content or message of the alert
     * @param type    the type of the alert (e.g., INFORMATION, ERROR)
     * @return an Alert object configured with the specified properties
     */
    public Alert generateAlert(String title, String content,
                               Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert;
    }
}
