package default1;

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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LogInPage extends Application {

    // Constant credentials para sa admin (nakahash ang password for security)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD_HASH = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";

    // Main method para i-launch ang JavaFX app
    public static void main(String[] args) {
        launch(args);
    }

    // start method para i-setup ang GUI ng login window
    @Override
    public void start(Stage adminStage) {
        adminStage.setTitle("Admin Login"); // Title ng window

        // Try to load custom icon ng window
        try {
            adminStage.getIcons().add(new Image("file:///C:\\Users\\chris\\Downloads\\u_logo.jpg"));
        } catch (Exception e) {
            System.out.println("Icon not found or failed to load.");
        }

        // Gumagawa ng background gradient color
        Stop[] stops = new Stop[]{
                new Stop(0, Color.web("#003366")),
                new Stop(1, Color.web("#0066CC"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

        // Main layout container na naka-vertical stacking ng elements
        VBox root = new VBox(20); 
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Title label ng login page
        Label titleLabel = new Label("ADMIN LOG IN HERE");
        titleLabel.setFont(Font.font("Segoe UI", 30));
        titleLabel.setTextFill(Color.WHITE);

        // Role selection section (Admin or Student)
        HBox roleSelection = new HBox(10);
        roleSelection.setAlignment(Pos.CENTER);
        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton adminRadio = new RadioButton("Admin");
        adminRadio.setFont(Font.font("Segoe UI", 16));
        adminRadio.setTextFill(Color.WHITE);
        adminRadio.setToggleGroup(roleGroup);
        adminRadio.setSelected(true);

        RadioButton studentRadio = new RadioButton("Student");
        studentRadio.setFont(Font.font("Segoe UI", 16));
        studentRadio.setTextFill(Color.WHITE);
        studentRadio.setToggleGroup(roleGroup);
        roleSelection.getChildren().addAll(adminRadio, studentRadio);

        // Kapag pinili ang "Student", mag-oopen ng bagong login window para sa students
        studentRadio.setOnAction(e -> {
            adminStage.close();
            try {
                new StudentLogInPage().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Username label and input field
        Label userLabel = new Label("Username:");
        userLabel.setFont(Font.font("Segoe UI", 16));
        userLabel.setTextFill(Color.WHITE);
        TextField userField = new TextField();
        userField.setMaxWidth(300);
        userField.setFont(Font.font("Segoe UI", 16));

        // Password label and input field (nakasecure gamit PasswordField)
        Label passLabel = new Label("Password:");
        passLabel.setFont(Font.font("Segoe UI", 16));
        passLabel.setTextFill(Color.WHITE);
        PasswordField passField = new PasswordField();
        passField.setMaxWidth(300);
        passField.setFont(Font.font("Segoe UI", 16));

        // Checkbox para sa Terms and Conditions + clickable label
        CheckBox termsBox = new CheckBox();
        Label termsLabel = new Label("I agree to the Terms and Conditions");
        termsLabel.setFont(Font.font("Segoe UI", 14));
        termsLabel.setTextFill(Color.WHITE);
        termsLabel.setCursor(javafx.scene.Cursor.HAND);
        termsLabel.setOnMouseClicked(e -> showTermsAndConditions(termsBox));
        termsLabel.setOnMouseEntered(e -> termsLabel.setTextFill(Color.LIGHTBLUE));
        termsLabel.setOnMouseExited(e -> termsLabel.setTextFill(Color.WHITE));

        // I-container ang checkbox at label
        HBox termsContainer = new HBox(10, termsBox, termsLabel);
        termsContainer.setAlignment(Pos.CENTER);

        // Button para mag-login + hover effects
        Button loginBtn = new Button("Log In");
        loginBtn.setFont(Font.font("Segoe UI", 16));
        loginBtn.setStyle("-fx-background-color: white; -fx-text-fill: #003366;");
        loginBtn.setCursor(javafx.scene.Cursor.HAND);
        loginBtn.setEffect(new DropShadow());

        loginBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e ->
                loginBtn.setStyle("-fx-background-color: #cce6ff; -fx-text-fill: #003366;"));
        loginBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e ->
                loginBtn.setStyle("-fx-background-color: white; -fx-text-fill: #003366;"));

        // Kapag pinindot ang login button, iche-check ang credentials
        loginBtn.setOnAction(e -> {
            if (!termsBox.isSelected()) {
                showAlert(Alert.AlertType.WARNING, "You must agree to the Terms and Conditions.");
                return;
            }

            String username = userField.getText().trim();
            String password = passField.getText();
            String hashedInput = hashPassword(password);

            // I-validate kung admin credentials ang pumasok
            if (username.equals(ADMIN_USERNAME) && hashedInput.equals(ADMIN_PASSWORD_HASH)) {
                showAlert(Alert.AlertType.INFORMATION, "Login successful!");
                adminStage.close();
                Stage inventoryStage = new Stage();
                try {
                    new CleaningInventory1().start(inventoryStage); // Punta sa main inventory system
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid credentials. Try again.");
            }
        });

        // Label para bumalik sa Registration Page
        Label backToRegister = new Label("Back to Registration");
        backToRegister.setFont(Font.font("Segoe UI", 14));
        backToRegister.setTextFill(Color.WHITE);
        backToRegister.setCursor(javafx.scene.Cursor.HAND);
        backToRegister.setOnMouseClicked(e -> {
            adminStage.close();
            try {
                new RegistrationPage().start(new Stage()); // Open registration page
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Hover effects sa registration label
        backToRegister.setOnMouseEntered(e -> backToRegister.setTextFill(Color.LIGHTBLUE));
        backToRegister.setOnMouseExited(e -> backToRegister.setTextFill(Color.WHITE));

        root.getChildren().addAll(
                titleLabel,
                roleSelection,
                userLabel, userField,
                passLabel, passField,
                termsContainer,
                loginBtn,
                backToRegister
        );

        // Scene setup at pagpapakita ng stage
        Scene scene = new Scene(root, 800, 600);
        adminStage.setScene(scene);
        adminStage.setResizable(false);
        adminStage.show();
    }

    // Method para i-hash ang password gamit SHA-256 algorithm
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Method para magpakita ng alert messages (info, warning, error)
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Login");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method para ipakita ang Terms and Conditions sa isang alert box
    private void showTermsAndConditions(CheckBox termsBox) {
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
        termsBox.setSelected(true); // Auto-check kapag nabasa na
    }
}
