package com.elililly.auth.ui;

import com.elililly.auth.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX controller for the password recovery screen.
 */
public class PasswordRecoveryController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryController.class);

    @FXML private TextField usernameOrEmailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Label messageLabel;

    private AuthenticationService authService;

    public void setAuthService(AuthenticationService authService) {
        this.authService = authService;
    }

    @FXML
    private void handleResetPassword() {
        String usernameOrEmail = usernameOrEmailField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmNewPasswordField.getText();

        if (usernameOrEmail.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        try {
            authService.resetPassword(usernameOrEmail, newPassword);
            showSuccess("Password reset successfully! You can now log in.");
            logger.info("Password reset for: {}", usernameOrEmail);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameOrEmailField.getScene().getWindow();
        stage.close();
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
