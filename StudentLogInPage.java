package default1;

// Nag-iimport ng mga required na JavaFX components, layout options, database utilities, at styling tools
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.sql.*;

public class StudentLogInPage extends Application {

    // Database connection details
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LMS Test;encrypt=true;trustServerCertificate=true";
    private static final String USER = "chris";
    private static final String PASSWORD = "Christian";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Student Login");

        // Dito nagse-set yung icon para sa window/dashboard 
        try {
            primaryStage.getIcons().add(new Image("file:///C:/Users/chris/Downloads/u_logo.jpg"));
        } catch (Exception e) {
            System.out.println("Icon not found or failed to load.");
        }

        // dito gumawa ng background gradient styling para sa UI
        Stop[] stops = new Stop[]{new Stop(0, Color.web("#003366")), new Stop(1, Color.web("#0066CC"))};
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

        // Root container ng buong login form
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Title label ng login form
        Label titleLabel = new Label("STUDENT LOG IN HERE");
        titleLabel.setFont(Font.font("Segoe UI", 30));
        titleLabel.setTextFill(Color.WHITE);

        // Role selection buttons (Admin or Student)
        HBox roleSelection = new HBox(10);
        roleSelection.setAlignment(Pos.CENTER);
        ToggleGroup roleGroup = new ToggleGroup();

        RadioButton adminRadio = new RadioButton("Admin");
        adminRadio.setFont(Font.font("Segoe UI", 16));
        adminRadio.setTextFill(Color.WHITE);
        adminRadio.setToggleGroup(roleGroup);

        RadioButton studentRadio = new RadioButton("Student");
        studentRadio.setFont(Font.font("Segoe UI", 16));
        studentRadio.setTextFill(Color.WHITE);
        studentRadio.setToggleGroup(roleGroup);
        studentRadio.setSelected(true); // Default na naka-select ang Student

        roleSelection.getChildren().addAll(adminRadio, studentRadio);

        adminRadio.setOnAction(e -> {
            primaryStage.close();
            try {
                new LogInPage().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ID Number input field
        Label idLabel = new Label("ID Number:");
        idLabel.setFont(Font.font("Segoe UI", 16));
        idLabel.setTextFill(Color.WHITE);
        TextField idField = new TextField();
        idField.setMaxWidth(300);
        idField.setFont(Font.font("Segoe UI", 16));

        // Password input field
        Label passLabel = new Label("Password:");
        passLabel.setFont(Font.font("Segoe UI", 16));
        passLabel.setTextFill(Color.WHITE);
        PasswordField passField = new PasswordField();
        passField.setMaxWidth(300);
        passField.setFont(Font.font("Segoe UI", 16));

        // Terms and conditions checkbox at label
        CheckBox termsBox = new CheckBox();
        Label termsLabel = new Label("I agree to the Terms and Conditions");
        termsLabel.setFont(Font.font("Segoe UI", 14));
        termsLabel.setTextFill(Color.WHITE);
        termsLabel.setCursor(javafx.scene.Cursor.HAND);

        // Kapag kinlick ang terms label, magpapakita ng alert na may full terms
        termsLabel.setOnMouseClicked(e -> {
        	Alert termsAlert = new Alert(Alert.AlertType.INFORMATION);
            termsAlert.setTitle("Terms and Conditions");
            termsAlert.setHeaderText("IMPACT: Inventory Management Platform for All Cleaning Tools");
            termsAlert.setContentText(
                    "1. Acceptance of Terms\nBy accessing and using the IMPACT LogInPage, you acknowledge and agree to these Terms and Conditions. If you do not agree, you may not proceed with logging in.\n\n" +
                    "2. User Accounts & Authentication\nUsers must provide accurate login credentials. Admin users must use the designated administrator login.\n\n" +
                    "3. Terms of Use\nAccess is granted for authorized personnel only. Any attempt to breach security or manipulate login credentials will result in immediate access restriction.\n\n" +
                    "4. Role Selection & Access Rights\nAdmin Users: Manage inventory and have full access. Student Users: Limited access for viewing inventory-related data.\n\n" +
                    "5. Agreement to Terms and Conditions\nUsers must agree to these terms before logging in.\n\n" +
                    "6. Security & Privacy\nThe platform does not store plaintext passwords. Data is encrypted to maintain confidentiality.\n\n" +
                    "7. Violations & Consequences\nUnauthorized login attempts may result in permanent access revocation.\n\n" +
                    "By logging into IMPACT, you confirm that you have read, understood, and agreed to these terms."
            );
            termsAlert.showAndWait();
            termsBox.setSelected(true);
        });

        // Hover effects para sa terms label
        termsLabel.setOnMouseEntered(e -> termsLabel.setTextFill(Color.LIGHTBLUE));
        termsLabel.setOnMouseExited(e -> termsLabel.setTextFill(Color.WHITE));

        // Container ng checkbox at label
        HBox termsContainer = new HBox(10, termsBox, termsLabel);
        termsContainer.setAlignment(Pos.CENTER);

        // Log In button setup
        Button loginBtn = new Button("Log In");
        loginBtn.setFont(Font.font("Segoe UI", 16));
        loginBtn.setStyle("-fx-background-color: white; -fx-text-fill: #003366;");
        loginBtn.setCursor(javafx.scene.Cursor.HAND);
        loginBtn.setEffect(new DropShadow());

        // Hover effect para sa login button
        loginBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e ->
            loginBtn.setStyle("-fx-background-color: #cce6ff; -fx-text-fill: #003366;"));
        loginBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e ->
            loginBtn.setStyle("-fx-background-color: white; -fx-text-fill: #003366;"));

        // Kapag kinlick ang Log In button
        loginBtn.setOnAction(e -> {
            if (!termsBox.isSelected()) {
                showAlert(Alert.AlertType.WARNING, "You must agree to the Terms and Conditions.");
                return;
            }

            String idNumber = idField.getText().trim();
            String password = passField.getText();

            // Check kung valid ang credentials
            if (validateCredentials(idNumber, password)) {
                showAlert(Alert.AlertType.INFORMATION, "Welcome Student!");
                primaryStage.close();
                javafx.application.Platform.runLater(() -> {
                    try {
                        StudentPage studentPage = new StudentPage();
                        studentPage.setStudentId(idNumber);
                        Stage studentStage = new Stage();
                        studentPage.start(studentStage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Student Credentials.");
            }
        });

        // Label para bumalik sa registration page
        Label backToRegister = new Label("Back to Registration");
        backToRegister.setFont(Font.font("Segoe UI", 14));
        backToRegister.setTextFill(Color.WHITE);
        backToRegister.setCursor(javafx.scene.Cursor.HAND);

        backToRegister.setOnMouseClicked(e -> {
            primaryStage.close();
            try {
                new RegistrationPage().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Hover effect para sa back to register label
        backToRegister.setOnMouseEntered(e -> backToRegister.setTextFill(Color.LIGHTBLUE));
        backToRegister.setOnMouseExited(e -> backToRegister.setTextFill(Color.WHITE));

        // Ina-add lahat ng UI components sa root VBox
        root.getChildren().addAll(
            titleLabel,
            roleSelection,
            idLabel, idField,
            passLabel, passField,
            termsContainer,
            loginBtn,
            backToRegister
        );

        // Setup ng scene at pagpapakita ng window
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // Function para i-validate kung tama ang ID at password gamit ang database
    private boolean validateCredentials(String idNumber, String password) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM dbo.users WHERE id_number = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, idNumber);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next(); // true kapag may matching record
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage());
        }
        return false;
    }

    // Para sa utility function which is magpapakita ng alert dialog 
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Login");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Entry point ng JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}
