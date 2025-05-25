// Package declaration para sa default1 package
package default1;

// Import necessary JavaFX and Java SQL libraries
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;

public class StudentPage extends Application {

    // Database connection details
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LMS Test;encrypt=true;trustServerCertificate=true";
    private static final String USER = "chris";
    private static final String PASSWORD = "Christian";
    private static String currentStudentId;

    // Setter method para ma-set ang currently logged-in student ID
    public static void setStudentId(String studentId) {
        currentStudentId = studentId;
    }

    // TableView para sa inventory items
    private TableView<Item> tableView;

    @Override
    public void start(Stage primaryStage) {
        // Set title ng main window
        primaryStage.setTitle("Student Dashboard");

        // Try to load icon image for the window
        try {
            primaryStage.getIcons().add(new Image("file:///C:\\Users\\chris\\Downloads\\IMPACT LOGO.jpg"));
        } catch (Exception e) {
            System.out.println("Icon not found or failed to load.");
        }

        // Sidebar menu setup
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #006699;");

        // Menu title styling
        Label sidebarTitle = new Label("Menu");
        sidebarTitle.setFont(Font.font("Segoe UI", 18));
        sidebarTitle.setTextFill(Color.WHITE);

        // Menu buttons for user actions
        Button viewButton = createSidebarButton("📋 View Tools");
        Button borrowButton = createSidebarButton("👜 Borrow Tool");
        Button returnButton = createSidebarButton("🔁 Return Tool");
        Button logoutButton = createSidebarButton("🚪 Log Out");

        // Add menu components to sidebar
        sidebar.getChildren().addAll(sidebarTitle, viewButton, borrowButton, returnButton, logoutButton);

        // Header/title ng main content
        Label titleLabel = new Label("Student Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", 28));
        titleLabel.setTextFill(Color.web("#006699"));
        HBox header = new HBox(titleLabel);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);

        // TableView configuration para mag-display ng inventory data
        tableView = new TableView<>();
        tableView.setPlaceholder(new Label("Click 'View Tools' to display inventory."));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Define table columns and bind them sa Item class fields
        TableColumn<Item, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Item, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Item, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        // Add all columns sa table
        tableView.getColumns().addAll(idCol, nameCol, qtyCol, unitCol);

        // VBox container for header and table
        VBox contentBox = new VBox(10, header, tableView);
        contentBox.setPadding(new Insets(20));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Main layout container
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);      // sidebar on the left
        root.setCenter(contentBox); // content in the center

        // Set actions for each sidebar button
        viewButton.setOnAction(e -> displayInventory());
        borrowButton.setOnAction(e -> borrowTool());
        returnButton.setOnAction(e -> returnTool());

        // Log out confirmation and redirect to login page
        logoutButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Log Out");
            alert.setContentText("Are you sure you want to log out?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    closeDatabaseConnection(); // Log the successful DB close
                    primaryStage.close();
                    javafx.application.Platform.runLater(() -> {
                        try {
                            new StudentLogInPage().start(new Stage());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            });
        });

        // Finalize and show the scene
        Scene scene = new Scene(root, 850, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Helper method to create sidebar-styled buttons
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", 14));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #CCE5FF;");
        return btn;
    }

    // Method to display tools available in inventory
    private void displayInventory() {
        ObservableList<Item> data = FXCollections.observableArrayList();
        String query = "SELECT ItemID AS ID, Name, Quantity AS Qty, Unit FROM dbo.inventory WHERE Quantity > 0";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.add(new Item(
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        rs.getInt("Qty"),
                        rs.getString("Unit")
                ));
            }
            tableView.setItems(data);

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    // Method para mag-borrow ng tool
    private void borrowTool() {
        if (currentStudentId == null) {
            showError("Student ID is not set. Please log in again.");
            return;
        }

        TextInputDialog itemDialog = new TextInputDialog();
        itemDialog.setTitle("Borrow Tool");
        itemDialog.setContentText("Enter Item ID:");
        String itemIdStr = itemDialog.showAndWait().orElse("").trim();

        if (itemIdStr.isEmpty()) return;

        TextInputDialog qtyDialog = new TextInputDialog();
        qtyDialog.setTitle("Borrow Tool");
        qtyDialog.setContentText("Enter Quantity:");
        String qtyStr = qtyDialog.showAndWait().orElse("").trim();

        if (qtyStr.isEmpty()) return;

        try {
            int itemId = Integer.parseInt(itemIdStr);
            int qty = Integer.parseInt(qtyStr);

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                conn.setAutoCommit(false);

                PreparedStatement checkStmt = conn.prepareStatement("SELECT Quantity FROM dbo.inventory WHERE ItemID = ?");
                checkStmt.setInt(1, itemId);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next() || rs.getInt("Quantity") < qty) {
                    showError("Not enough quantity or item doesn't exist.");
                    conn.rollback();
                    return;
                }

                PreparedStatement updateStmt = conn.prepareStatement("UPDATE dbo.inventory SET Quantity = Quantity - ? WHERE ItemID = ?");
                updateStmt.setInt(1, qty);
                updateStmt.setInt(2, itemId);
                updateStmt.executeUpdate();

                PreparedStatement mergeStmt = conn.prepareStatement(
                    "MERGE dbo.borrowed_tools AS target " +
                    "USING (SELECT ? AS StudentID, ? AS ItemID, ? AS Quantity) AS source " +
                    "ON (target.StudentID = source.StudentID AND target.ItemID = source.ItemID) " +
                    "WHEN MATCHED THEN " +
                    "   UPDATE SET Quantity = target.Quantity + source.Quantity " +
                    "WHEN NOT MATCHED THEN " +
                    "   INSERT (StudentID, ItemID, Quantity) VALUES (source.StudentID, source.ItemID, source.Quantity);"
                );
                mergeStmt.setString(1, currentStudentId);
                mergeStmt.setInt(2, itemId);
                mergeStmt.setInt(3, qty);
                mergeStmt.executeUpdate();

                conn.commit();
                displayInventory();
                showInfo("Tool borrowed successfully!");

            } catch (SQLException ex) {
                showError("Error: " + ex.getMessage());
            }

        } catch (NumberFormatException e) {
            showError("Invalid number format.");
        }
    }

    // Method para mag-return ng tool
    private void returnTool() {
        if (currentStudentId == null) {
            showError("Student ID is not set. Please log in again.");
            return;
        }

        TextInputDialog itemDialog = new TextInputDialog();
        itemDialog.setTitle("Return Tool");
        itemDialog.setContentText("Enter Item ID to return:");
        String itemIdStr = itemDialog.showAndWait().orElse("").trim();

        if (itemIdStr.isEmpty()) return;

        TextInputDialog qtyDialog = new TextInputDialog();
        qtyDialog.setTitle("Return Tool");
        qtyDialog.setContentText("Enter Quantity to return:");
        String qtyStr = qtyDialog.showAndWait().orElse("").trim();

        if (qtyStr.isEmpty()) return;

        try {
            int itemId = Integer.parseInt(itemIdStr);
            int qtyToReturn = Integer.parseInt(qtyStr);

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                conn.setAutoCommit(false);

                PreparedStatement checkStmt = conn.prepareStatement("SELECT Quantity FROM dbo.borrowed_tools WHERE StudentID = ? AND ItemID = ?");
                checkStmt.setString(1, currentStudentId);
                checkStmt.setInt(2, itemId);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    showError("You have not borrowed this item.");
                    conn.rollback();
                    return;
                }

                int borrowedQty = rs.getInt("Quantity");
                if (qtyToReturn > borrowedQty) {
                    showError("Return quantity exceeds borrowed quantity.");
                    conn.rollback();
                    return;
                }

                if (qtyToReturn == borrowedQty) {
                    PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM dbo.borrowed_tools WHERE StudentID = ? AND ItemID = ?");
                    deleteStmt.setString(1, currentStudentId);
                    deleteStmt.setInt(2, itemId);
                    deleteStmt.executeUpdate();
                } else {
                    PreparedStatement updateBorrowStmt = conn.prepareStatement("UPDATE dbo.borrowed_tools SET Quantity = Quantity - ? WHERE StudentID = ? AND ItemID = ?");
                    updateBorrowStmt.setInt(1, qtyToReturn);
                    updateBorrowStmt.setString(2, currentStudentId);
                    updateBorrowStmt.setInt(3, itemId);
                    updateBorrowStmt.executeUpdate();
                }

                PreparedStatement updateInventoryStmt = conn.prepareStatement("UPDATE dbo.inventory SET Quantity = Quantity + ? WHERE ItemID = ?");
                updateInventoryStmt.setInt(1, qtyToReturn);
                updateInventoryStmt.setInt(2, itemId);
                updateInventoryStmt.executeUpdate();

                conn.commit();
                displayInventory();
                showInfo("Tool returned successfully!");

            } catch (SQLException ex) {
                showError("Error: " + ex.getMessage());
            }

        } catch (NumberFormatException e) {
            showError("Invalid number format.");
        }
    }

    // Error message alert
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Info message alert
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method para i-close properly ang database connection at mag-log sa console
    private void closeDatabaseConnection() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database: " + e.getMessage());
        }
    }

    // Item class - Model para sa TableView data
    public static class Item {
        private final Integer id;
        private final String name;
        private final Integer quantity;
        private final String unit;

        public Item(Integer id, String name, Integer quantity, String unit) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public Integer getId() { return id; }
        public String getName() { return name; }
        public Integer getQuantity() { return quantity; }
        public String getUnit() { return unit; }
    }

    // Main method - entry point ng program
    public static void main(String[] args) {
        setStudentId("S001"); // Temporary set ng student ID for testing
        launch(args);
    }
}
