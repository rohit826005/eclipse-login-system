package com.elililly.auth.ui;

import com.elililly.auth.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * JavaFX controller for the login screen.
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;

    private AuthenticationService authService;

    public void setAuthService(AuthenticationService authService) {
        this.authService = authService;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username/email and password.");
            return;
        }

        try {
            String sessionToken = authService.login(username, password);
            logger.info("Login successful for: {}", username);
            showSuccess("Login successful!");
            // In a full app, navigate to dashboard and pass sessionToken
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleOpenRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registration.fxml"));
            Parent root = loader.load();
            RegistrationController controller = loader.getController();
            controller.setAuthService(authService);
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Could not open registration form.");
            logger.error("Error opening registration: {}", e.getMessage());
        }
    }

    @FXML
    private void handleOpenPasswordRecovery() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/password_recovery.fxml"));
            Parent root = loader.load();
            PasswordRecoveryController controller = loader.getController();
            controller.setAuthService(authService);
            Stage stage = new Stage();
            stage.setTitle("Password Recovery");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Could not open password recovery form.");
            logger.error("Error opening password recovery: {}", e.getMessage());
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
    }
}
