package com.elililly.auth;

import com.elililly.auth.config.Configuration;
import com.elililly.auth.database.DatabaseConnectionManager;
import com.elililly.auth.database.DatabaseMigration;
import com.elililly.auth.repository.DatabaseUserRepository;
import com.elililly.auth.repository.FileUserRepository;
import com.elililly.auth.repository.UserRepository;
import com.elililly.auth.service.AuthenticationService;
import com.elililly.auth.ui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point.
 */
public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        Configuration config = Configuration.getInstance();

        UserRepository userRepository;
        if (config.getStorageType() == Configuration.StorageType.DATABASE) {
            DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
            new DatabaseMigration(connectionManager).migrate();
            userRepository = new DatabaseUserRepository(connectionManager);
            logger.info("Using DATABASE storage");
        } else {
            userRepository = new FileUserRepository();
            logger.info("Using FILE storage");
        }

        AuthenticationService authService = new AuthenticationService(userRepository);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setAuthService(authService);

        primaryStage.setTitle("Eclipse Login System");
        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
