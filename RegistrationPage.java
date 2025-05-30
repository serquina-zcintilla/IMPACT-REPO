package default1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Main class ng Registration Page, extending JavaFX Application class
public class RegistrationPage extends Application {

    // Database connection details (connection string, username, password)
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LMS Test;encrypt=true;trustServerCertificate=true";
    private static final String USER = "chris";
    private static final String PASSWORD = "Christian";

    // Entry point ng JavaFX application
    public static void main(String[] args) {
        launch(args);
    }

    // Main UI layout at logic ng Registration Page
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("I.M.P.A.C.T Registration");

        //Icon for the application window
        try {
            primaryStage.getIcons().add(new Image("file:///C:/Users/chris/Downloads/u_logo.jpg"));
        } catch (Exception e) {
            System.out.println("Icon not found or failed to load.");
        }

        // Root layout using BorderPane para may left at center na layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #003366;");

        // LEFT SECTION: logo, title, login prompt, back button 
        VBox leftSection = new VBox(20);
        leftSection.setAlignment(Pos.TOP_LEFT);
        leftSection.setPadding(new Insets(30, 20, 50, 30));

        // Load and show the logo image
        Image logoImage = new Image("file:src/resources/logo.png");
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(80);
        logoView.setPreserveRatio(true);

        // App title label
        Label appTitle = new Label("I.M.P.A.C.T.");
        appTitle.setFont(Font.font("Segoe UI", 36));
        appTitle.setTextFill(Color.WHITE);

        // Dito yung sa Label for redirecting para sa LoginPage once clicked
        Label loginPrompt = new Label("Already have an account? Click here to Log in.");
        loginPrompt.setFont(Font.font("Segoe UI", 14));
        loginPrompt.setTextFill(Color.LIGHTGRAY);
        loginPrompt.setCursor(Cursor.HAND);

        // Hover effect at redirect to LoginPage
        loginPrompt.setOnMouseEntered(e -> loginPrompt.setTextFill(Color.WHITE));
        loginPrompt.setOnMouseExited(e -> loginPrompt.setTextFill(Color.LIGHTGRAY));
        loginPrompt.setOnMouseClicked(e -> {
            primaryStage.close();
            Stage loginStage = new Stage();
            try {
                new StudentLogInPage().start(loginStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Region spacerForBack = new Region();
        VBox.setVgrow(spacerForBack, Priority.ALWAYS);

        // Back button to go back to WelcomePage
        Button backButton = new Button("\u2190 Back");
        backButton.setFont(Font.font("Segoe UI", 14));
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        backButton.setCursor(Cursor.HAND);

        // Hover effects sa back button for redirecting to WelcomePage
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #004080; -fx-text-fill: white;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;"));
        backButton.setOnAction(e -> {
            primaryStage.close();
            Stage welcomeStage = new Stage();
            try {
                new WelcomePage().start(welcomeStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        leftSection.getChildren().addAll(logoView, appTitle, loginPrompt, spacerForBack, backButton);
        root.setLeft(leftSection);

        VBox formSection = new VBox(15);
        formSection.setAlignment(Pos.CENTER);
        formSection.setPadding(new Insets(50));
        formSection.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        // Title text for registration form
        Label registerText = new Label("REGISTER YOUR ACCOUNT");
        registerText.setFont(Font.font("Segoe UI", 20));
        registerText.setTextFill(Color.BLACK);

        // Dito nakalagay yung mga input fields ni user
        TextField fullNameField = createTextField("Full Name");
        TextField idNumberField = createTextField("ID Number");
        PasswordField passwordField = createPasswordField("Password");
        PasswordField confirmPasswordField = createPasswordField("Confirm Password");
        TextField emailField = createTextField("Email (Optional)");

        // Register button at logic ng validation at registration
        Button registerButton = new Button("Register");
        registerButton.setFont(Font.font("Segoe UI", 16));
        registerButton.setStyle("-fx-background-color: #003366; -fx-text-fill: white;");
        registerButton.setOnMouseEntered(e -> registerButton.setStyle("-fx-background-color: #004080; -fx-text-fill: white;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle("-fx-background-color: #003366; -fx-text-fill: white;"));

        // Action kapag nag-click ang user sa Register button
        registerButton.setOnAction(e -> {
            String fullName = fullNameField.getText();
            String idNumber = idNumberField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String email = emailField.getText();

            // Validations for required fields, password match, password complexity
            if (fullName.isEmpty() || idNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please fill out all the required fields.");
            } else if (!password.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Passwords do not match. Please try again.");
            } else if (!isPasswordComplex(password)) {
                showAlert(Alert.AlertType.ERROR,
                        "Password must be at least 8 characters long and include:\n" +
                                "- Uppercase letter\n" +
                                "- Lowercase letter\n" +
                                "- Number\n" +
                                "- Special character (!@#$%^&*)");
            } else if (password.length() < 8 || password.length() > 12) {
                showAlert(Alert.AlertType.WARNING, "For better security, use a password between 12 and 16 characters.");
            } else {
                // Proceed with registration if validations passed
                registerAccount(fullName, idNumber, password, email, primaryStage);
            }
        });

        // Delete account button
        Button deleteButton = new Button("Delete Account");
        deleteButton.setFont(Font.font("Segoe UI", 16));
        deleteButton.setStyle("-fx-background-color: #cc0000; -fx-text-fill: white;");
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #990000; -fx-text-fill: white;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #cc0000; -fx-text-fill: white;"));

        // Delete account logic
        deleteButton.setOnAction(e -> {
            String idNumber = idNumberField.getText();
            if (idNumber.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Please enter ID Number to delete account.");
            } else {
                deleteAccount(idNumber);
            }
        });

        Region spacer = new Region();
        spacer.setMinHeight(40);

        // Redirect label for Admin login
        Label adminLabel = new Label("Log in as Admin");
        adminLabel.setFont(Font.font("Segoe UI", 16));
        adminLabel.setTextFill(Color.DARKBLUE);
        adminLabel.setCursor(Cursor.HAND);

        adminLabel.setOnMouseEntered(e -> adminLabel.setTextFill(Color.LIGHTGRAY));
        adminLabel.setOnMouseExited(e -> adminLabel.setTextFill(Color.DARKBLUE));
        adminLabel.setOnMouseClicked(e -> {
            primaryStage.close();
            Stage adminStage = new Stage();
            try {
                new LogInPage().start(adminStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        formSection.getChildren().addAll(registerText, fullNameField, idNumberField, passwordField, confirmPasswordField, emailField, registerButton, deleteButton, spacer, adminLabel);
        root.setCenter(formSection);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Helper method to create a styled text field
    private TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Segoe UI", 16));
        tf.setMaxWidth(300);
        return tf;
    }

    // Helper method to create a styled password field
    private PasswordField createPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setFont(Font.font("Segoe UI", 16));
        pf.setMaxWidth(300);
        return pf;
    }

    // Helper method to show alert dialogs
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Registration");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Password Restriction kung sino ang magreregister
    private boolean isPasswordComplex(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[_@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(regex);
    }

    // Ito po yung method in which magreregister ang account ni user in the SQL Server database
    private void registerAccount(String fullName, String idNumber, String password, String email, Stage stage) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO dbo.users (full_name, id_number, password, email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, fullName);
                stmt.setString(2, idNumber);
                stmt.setString(3, password);
                stmt.setString(4, email.isEmpty() ? null : email);
                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Account registered successfully!");
                stage.close();
                Stage loginStage = new Stage();
                new StudentLogInPage().start(loginStage);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Ito yung method in which deleting an account registered from the database
    private void deleteAccount(String idNumber) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM dbo.users WHERE id_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, idNumber);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Account with ID Number " + idNumber + " has been deleted.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "No account found with ID Number " + idNumber + ".");
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }
}
