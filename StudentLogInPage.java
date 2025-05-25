package default1;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.sql.*;

public class StudentLogInPage extends Application {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LMS Test;encrypt=true;trustServerCertificate=true";
    private static final String USER = "chris";
    private static final String PASSWORD = "Christian";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Student Login");

        try {
            primaryStage.getIcons().add(new Image("file:///C:\\Users\\chris\\Downloads\\IMPACT LOGO.jpg"));
        } catch (Exception e) {
            System.out.println("Icon not found.");
        }

        // ====== MAIN ROOT LAYOUT ======
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #003366, #0066CC);");

        // ====== LEFT SECTION ======
        VBox leftSection = new VBox(20);
        leftSection.setPadding(new Insets(40));
        leftSection.setAlignment(Pos.CENTER_LEFT);

        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        ImageView logo = new ImageView(new Image("file:///C:/Users/chris/Downloads/u_logo.jpg"));
        logo.setFitHeight(50);
        logo.setFitWidth(50);
        Label impactText = new Label("I.M.P.A.C.T");
        impactText.setFont(Font.font("Segoe UI", 24));
        impactText.setTextFill(Color.WHITE);
        logoBox.getChildren().addAll(logo, impactText);

        Label pageTitle = new Label("STUDENT Log in");
        pageTitle.setFont(Font.font("Segoe UI", 28));
        pageTitle.setTextFill(Color.WHITE);

        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton adminBtn = new RadioButton("Admin");
        RadioButton studentBtn = new RadioButton("Student");
        adminBtn.setToggleGroup(roleGroup);
        studentBtn.setToggleGroup(roleGroup);
        studentBtn.setSelected(true);
        styleToggle(adminBtn);
        styleToggle(studentBtn);

        HBox toggleBox = new HBox(adminBtn, studentBtn);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        toggleBox.setSpacing(0);
        toggleBox.setPadding(new Insets(10));
        toggleBox.setBackground(new Background(new BackgroundFill(Color.web("#e6f0ff"), new CornerRadii(30), Insets.EMPTY)));

        adminBtn.setOnAction(e -> {
            primaryStage.close();
            try {
                new LogInPage().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        leftSection.getChildren().addAll(logoBox, pageTitle, toggleBox);

        // ====== RIGHT SECTION - FORM AREA ======
        VBox formBox = new VBox(20);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(40));
        formBox.setBackground(new Background(new BackgroundFill(Color.web("#003d66", 0.7), new CornerRadii(30), Insets.EMPTY)));

        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("Segoe UI", 20));
        userLabel.setTextFill(Color.WHITE);
        TextField idField = new TextField();
        idField.setPromptText("Enter ID");
        idField.setFont(Font.font("Segoe UI", 16));
        idField.setMaxWidth(300);

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Segoe UI", 20));
        passLabel.setTextFill(Color.WHITE);
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter Password");
        passField.setFont(Font.font("Segoe UI", 16));
        passField.setMaxWidth(300);

        CheckBox termsBox = new CheckBox();
        Label termsLabel = new Label("Terms and Conditions");
        termsLabel.setFont(Font.font("Segoe UI", 14));
        termsLabel.setTextFill(Color.WHITE);
        termsLabel.setCursor(javafx.scene.Cursor.HAND);

        termsLabel.setOnMouseClicked(e -> {
            Alert termsAlert = new Alert(Alert.AlertType.INFORMATION);
            termsAlert.setTitle("Terms and Conditions");
            termsAlert.setHeaderText("IMPACT: Inventory Management Platform for All Cleaning Tools");
            termsAlert.setContentText(
                "1. Acceptance of Terms\n" +
                "By accessing and using the IMPACT LogInPage, you acknowledge and agree to these Terms and Conditions. If you do not agree, you may not proceed with logging in.\n\n" +

                "2. User Accounts & Authentication\n" +
                "Users must provide accurate login credentials. Admin users must use the designated administrator login.\n\n" +

                "3. Terms of Use\n" +
                "Access is granted for authorized personnel only. Any attempt to breach security or manipulate login credentials will result in immediate access restriction.\n\n" +

                "4. Role Selection & Access Rights\n" +
                "Admin Users: Manage inventory and have full access.\n" +
                "Student Users: Limited access for viewing inventory-related data.\n\n" +

                "5. Agreement to Terms and Conditions\n" +
                "Users must agree to these terms before logging in.\n\n" +

                "6. Security & Privacy\n" +
                "The platform does not store plaintext passwords. Data is encrypted to maintain confidentiality.\n\n" +

                "7. Violations & Consequences\n" +
                "Unauthorized login attempts may result in permanent access revocation.\n\n" +

                "By logging into IMPACT, you confirm that you have read, understood, and agreed to these terms."
            );
            termsAlert.showAndWait();
            termsBox.setSelected(true);
        });

        termsLabel.setOnMouseEntered(e -> termsLabel.setTextFill(Color.LIGHTBLUE));
        termsLabel.setOnMouseExited(e -> termsLabel.setTextFill(Color.WHITE));

        HBox termsContainer = new HBox(10, termsBox, termsLabel);
        termsContainer.setAlignment(Pos.CENTER_LEFT);

        Button loginBtn = new Button("Log In");
        loginBtn.setFont(Font.font("Segoe UI", 16));
        loginBtn.setCursor(javafx.scene.Cursor.HAND);
        loginBtn.setStyle("-fx-background-color: #cce6ff; -fx-text-fill: #003366;");
        loginBtn.setEffect(new DropShadow());
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color: white; -fx-text-fill: #003366;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-color: #cce6ff; -fx-text-fill: #003366;"));

        loginBtn.setOnAction(e -> {
            if (!termsBox.isSelected()) {
                showAlert(Alert.AlertType.WARNING, "You must agree to the Terms and Conditions.");
                return;
            }

            String idNumber = idField.getText().trim();
            String password = passField.getText();

            if (validateCredentials(idNumber, password)) {
                showAlert(Alert.AlertType.INFORMATION, "Welcome Student!");
                primaryStage.close();
                javafx.application.Platform.runLater(() -> {
                    try {
                        StudentPage studentPage = new StudentPage();
                        studentPage.setStudentId(idNumber);
                        studentPage.start(new Stage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Student Credentials.");
            }
        });

        Label backToRegister = new Label("Back to Registration");
        backToRegister.setFont(Font.font("Segoe UI", 14));
        backToRegister.setTextFill(Color.WHITE);
        backToRegister.setCursor(javafx.scene.Cursor.HAND);
        backToRegister.setOnMouseEntered(e -> backToRegister.setTextFill(Color.LIGHTBLUE));
        backToRegister.setOnMouseExited(e -> backToRegister.setTextFill(Color.WHITE));
        backToRegister.setOnMouseClicked(e -> {
            primaryStage.close();
            try {
                new RegistrationPage().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        formBox.getChildren().addAll(userLabel, idField, passLabel, passField, loginBtn, termsContainer, backToRegister);
        mainLayout.setLeft(leftSection);
        mainLayout.setCenter(formBox);

        Scene scene = new Scene(mainLayout, 950, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void styleToggle(RadioButton rb) {
        rb.setFont(Font.font("Segoe UI", 18));
        rb.setPadding(new Insets(10, 20, 10, 20));
        rb.setBackground(null);
        rb.setTextFill(Color.web("#333"));
        rb.setStyle("-fx-background-radius: 30;");
    }

    private boolean validateCredentials(String idNumber, String password) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connection successful"); // ✅ Console message on successful DB connection

            String query = "SELECT * FROM dbo.users WHERE id_number = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, idNumber);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage());
        }
        return false;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Login");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
