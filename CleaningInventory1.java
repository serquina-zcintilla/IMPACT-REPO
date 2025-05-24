package default1;

// Import ng mga JavaFX at SQL libraries na gagamitin sa app
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CleaningInventory1 extends Application {

    private VBox mainLayout;

    // DATABASE CONNECTION DETAILS
    // Ito 'yung ginagamit para maka-connect sa SQL Server database
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LMS Test;encrypt=true;trustServerCertificate=true";
    private static final String USER = "chris";
    private static final String PASSWORD = "Christian";

    public static void main(String[] args) {
        // Entry point ng JavaFX application
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Setup ng primary stage (main window)
        stage.setTitle("Admin Dashboard");

        // Try to set icon ng app
        try {
            Image appIcon = new Image("file:C:\\\\Users\\\\chris\\\\Downloads\\\\u_logo.jpg");
            if (!appIcon.isError()) {
                stage.getIcons().add(appIcon);
            } else {
                System.err.println("Warning: Could not load application icon.");
            }
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }

        // Gumagawa ng Header Label
        Label header = new Label("ADMIN DASHBOARD");
        header.setFont(Font.font("Segoe UI", 34));
        header.setTextFill(Color.WHITE);

        // Wrapper para sa header para ma-center at may padding
        HBox headerBox = new HBox(header);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(30, 0, 20, 0));

        // Layout ng mga function cards (Add, Update, etc.)
        GridPane cardGrid = new GridPane();
        cardGrid.setHgap(40); // horizontal space between cards
        cardGrid.setVgap(40); // vertical space between cards
        cardGrid.setAlignment(Pos.CENTER);
        cardGrid.setPadding(new Insets(20));

        // Adding cards sa GridPane with their respective action handlers
        cardGrid.add(createCard("➕", "Add Tools", "Insert new cleaning tools", this::addItem), 0, 0);
        cardGrid.add(createCard("🔄", "Update Tools", "Modify tool information", this::updateItem), 1, 0);
        cardGrid.add(createCard("🗑️", "Delete Tools", "Remove tool from inventory", this::deleteItem), 2, 0);
        cardGrid.add(createCard("📉", "Low Stock Report", "View low quantity tools", this::lowStockReport), 0, 1);
        cardGrid.add(createCard("📋", "View Inventory", "See all available tools", this::displayInventory), 1, 1);
        cardGrid.add(createCard("🕒", "Show History", "View item change history", this::showHistory), 2, 1);
        cardGrid.add(createCard("➡️", "Log Out", "End admin session", () -> logout(stage)), 1, 2);

        // Main layout combining header and card grid
        mainLayout = new VBox(20, headerBox, cardGrid);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #001933, #004d99);");

        // Creating the scene at inilalagay sa stage
        Scene scene = new Scene(mainLayout, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    // Reusable card component for each dashboard function
    private VBox createCard(String icon, String title, String description, Runnable action) {
        // Icon Label
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI", 40));
        iconLabel.setTextFill(Color.web("#004d99"));

        // Title ng card
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI Semibold", 16));
        titleLabel.setTextFill(Color.WHITE);

        // Description ng card
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 12));
        descLabel.setTextFill(Color.LIGHTGRAY);

        // Layout ng card with styles and hover effects
        VBox card = new VBox(12, iconLabel, titleLabel, descLabel);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #cce6ff, #66b3ff); " +
                "-fx-background-radius: 18; " +
                "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.3), 10, 0, 4, 4);");

        // Event handlers para sa click at hover effects
        card.setOnMouseClicked(e -> action.run());
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: linear-gradient(to bottom right, #b3daff, #4da6ff);" +
                "-fx-background-radius: 18; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.3), 10, 0, 4, 4); -fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: linear-gradient(to bottom right, #cce6ff, #66b3ff);" +
                "-fx-background-radius: 18; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.3), 10, 0, 4, 4);"));

        return card;
    }

    // Setup ng mga columns sa inventory table
    private void setupTableColumns(TableView<InventoryItem> table) {
        TableColumn<InventoryItem, String> idCol = new TableColumn<>("Item ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("itemID"));

        TableColumn<InventoryItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<InventoryItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<InventoryItem, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<InventoryItem, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        table.getColumns().addAll(idCol, nameCol, qtyCol, unitCol, supplierCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No inventory data to display."));
    }

    // Simpleng method to show info alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Dialog box para mag-input ng text mula sa user
    private String promptInput(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(prompt);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        Optional<String> result = dialog.showAndWait();
        return result.filter(s -> !s.trim().isEmpty()).orElse(null);
    }

    // Logout function na may confirmation dialog
    private void logout(Stage stage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to log out as an Admin?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Log Out Confirmation");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            showAlert("Logged Out", "You have been logged out.");
            stage.close();
            try {
                new LogInPage().start(new Stage()); // Return sa login page
            } catch (Exception ex) {
                showAlert("Error", "Could not open login page: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // Gumagawa ng bagong modal window to show inventory table
    private void openInventoryTableWindow(String title, String placeholderMessage, ObservableList<InventoryItem> data) {
        Stage tableStage = new Stage();
        tableStage.setTitle(title);
        tableStage.initModality(Modality.APPLICATION_MODAL); // Block ang main window habang bukas ito

        // Try to set icon para sa inventory table window
        try {
            Image appIcon = new Image("file:C:/Users/zcint/Downloads/download (5).png");
            if (!appIcon.isError()) {
                tableStage.getIcons().add(appIcon);
            }
        } catch (Exception e) {
            System.err.println("Error loading icon for " + title + " window: " + e.getMessage());
        }

        TableView<InventoryItem> table = new TableView<>();
        setupTableColumns(table); // Use the helper method
        table.setItems(data);
        table.setPlaceholder(new Label(placeholderMessage));

        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400); // Set a preferred height for the table
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox root = new VBox(10, scrollPane);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #001933, #004d99);"); // Apply consistent styling

        Scene scene = new Scene(root, 800, 500);
        tableStage.setScene(scene);
        tableStage.showAndWait(); // Use showAndWait to block until the window is closed
    }

    private void addItem() {
        String name = promptInput("Item Name:");
        if (name == null) return;
        String qtyStr = promptInput("Quantity:");
        if (qtyStr == null) return;
        String unit = promptInput("Unit:");
        if (unit == null) return;
        String supplier = promptInput("Supplier:");
        if (supplier == null) return;

        try {
            int qty = Integer.parseInt(qtyStr);

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                // *** FIX: Removed manual itemID generation for IDENTITY column ***
                // The database will now handle the itemID generation automatically.
                // We're adding Statement.RETURN_GENERATED_KEYS to retrieve it after insertion.
                String insertSQL = "INSERT INTO inventory (name, quantity, unit, supplier) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, name);
                ps.setInt(2, qty);
                ps.setString(3, unit);
                ps.setString(4, supplier);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Retrieve the auto-generated itemID
                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    String generatedId = null;
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getString(1); // Get the first generated key
                    }

                    showAlert("Success", "Item added!" + (generatedId != null ? " New Item ID: " + generatedId : ""));
                    displayInventory(); // Show updated inventory in a new window
                    // Log history with the auto-generated ID
                    if (generatedId != null) {
                        logHistory("Added", generatedId, name, qty);
                    }
                } else {
                    showAlert("Error", "Failed to add item.");
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Quantity must be a number.");
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateItem() {
        String id = promptInput("Enter Item ID to update:");
        if (id == null) return;
        String newQtyStr = promptInput("New Quantity:");
        if (newQtyStr == null) return;

        try {
            int newQty = Integer.parseInt(newQtyStr);

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String checkSQL = "SELECT name, quantity FROM inventory WHERE itemID = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                checkStmt.setString(1, id);
                ResultSet rs = checkStmt.executeQuery();
                String itemName = null;
                int oldQty = 0;
                if (rs.next()) {
                    itemName = rs.getString("name");
                    oldQty = rs.getInt("quantity");
                } else {
                    showAlert("Error", "Item ID not found.");
                    return;
                }

                String updateSQL = "UPDATE inventory SET quantity = ? WHERE itemID = ?";
                PreparedStatement ps = conn.prepareStatement(updateSQL);
                ps.setInt(1, newQty);
                ps.setString(2, id);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    showAlert("Success", "Item updated!");
                    displayInventory(); // Show updated inventory in a new window
                    logHistory("Updated (Qty from " + oldQty + " to " + newQty + ")", id, itemName, newQty);
                } else {
                    showAlert("Error", "Update failed.");
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Quantity must be a number.");
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteItem() {
        String id = promptInput("Enter Item ID to delete:");
        if (id == null) return;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String checkSQL = "SELECT name, quantity FROM inventory WHERE itemID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
            checkStmt.setString(1, id);
            ResultSet rs = checkStmt.executeQuery();
            String itemName = null;
            int itemQty = 0;
            if (rs.next()) {
                itemName = rs.getString("name");
                itemQty = rs.getInt("quantity");
            } else {
                showAlert("Error", "Item ID not found.");
                return;
            }

            String deleteSQL = "DELETE FROM inventory WHERE itemID = ?";
            PreparedStatement ps = conn.prepareStatement(deleteSQL);
            ps.setString(1, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                showAlert("Success", "Item deleted!");
                displayInventory(); // Show updated inventory in a new window
                logHistory("Deleted", id, itemName, itemQty);
            } else {
                showAlert("Error", "Deletion failed.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayInventory() {
        ObservableList<InventoryItem> data = FXCollections.observableArrayList();
        String query = "SELECT ItemID AS ID, Name, Quantity AS Qty, Unit, Supplier FROM dbo.inventory WHERE Quantity > 0";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("ID");
                String name = rs.getString("Name");
                int qty = rs.getInt("Qty");
                String unit = rs.getString("Unit");
                String supplier = rs.getString("Supplier");

                InventoryItem item = new InventoryItem(id, name, qty, unit, supplier);
                data.add(item);
            }
            openInventoryTableWindow("Current Inventory (Tools)", "No tools in inventory.", data);

        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void lowStockReport() {
        ObservableList<InventoryItem> lowStockData = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT itemID, name, quantity, unit, supplier FROM inventory WHERE quantity < 5";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String id = rs.getString("itemID");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                String unit = rs.getString("unit");
                String supplier = rs.getString("supplier");

                InventoryItem item = new InventoryItem(id, name, qty, unit, supplier);
                lowStockData.add(item);
            }

            if (lowStockData.isEmpty()) {
                showAlert("Low Stock Report", "No low stock items currently.");
            }
            openInventoryTableWindow("Low Stock Report", "No low stock items.", lowStockData);

        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- History Functionality ---

    private void logHistory(String action, String itemID, String itemName, int quantity) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            createHistoryTableIfNotExists(conn);

            String insertHistorySQL = "INSERT INTO InventoryHistory (action, itemID, itemName, quantity, timestamp) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertHistorySQL);
            ps.setString(1, action);
            ps.setString(2, itemID);
            ps.setString(3, itemName);
            ps.setInt(4, quantity);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createHistoryTableIfNotExists(Connection conn) throws SQLException {
        String createTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='InventoryHistory' and xtype='U') " +
                                "CREATE TABLE InventoryHistory (" +
                                "    id INT PRIMARY KEY IDENTITY(1,1)," +
                                "    action VARCHAR(255) NOT NULL," +
                                "    itemID VARCHAR(50) NOT NULL," + // itemID stored as VARCHAR
                                "    itemName VARCHAR(255) NOT NULL," +
                                "    quantity INT NOT NULL," +
                                "    timestamp DATETIME NOT NULL" +
                                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    private void showHistory() {
        Stage historyStage = new Stage();
        historyStage.setTitle("Inventory History");
        historyStage.initModality(Modality.APPLICATION_MODAL);

        try {
            Image historyIcon = new Image("file:C:/Users/zcint/Downloads/download (5).png");
            if (!historyIcon.isError()) {
                historyStage.getIcons().add(historyIcon);
            } else {
                System.err.println("Warning: Could not load history icon. Check path: C:/Users/zcint/Downloads/download (5).png");
            }
        } catch (Exception e) {
            System.err.println("Error loading history icon: " + e.getMessage());
        }

        TableView<HistoryItem> historyTable = new TableView<>();
        ObservableList<HistoryItem> historyData = FXCollections.observableArrayList();

        TableColumn<HistoryItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<HistoryItem, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        TableColumn<HistoryItem, String> itemIDCol = new TableColumn<>("Item ID");
        itemIDCol.setCellValueFactory(new PropertyValueFactory<>("itemID"));
        TableColumn<HistoryItem, String> itemNameCol = new TableColumn<>("Item Name");
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        TableColumn<HistoryItem, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<HistoryItem, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        historyTable.getColumns().addAll(idCol, actionCol, itemIDCol, itemNameCol, quantityCol, timestampCol);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setPlaceholder(new Label("No history available."));

        ScrollPane historyScrollPane = new ScrollPane(historyTable);
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setPrefHeight(400);
        historyScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            createHistoryTableIfNotExists(conn);

            String sql = "SELECT * FROM InventoryHistory ORDER BY timestamp DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String action = rs.getString("action");
                String itemID = rs.getString("itemID");
                String itemName = rs.getString("itemName");
                int quantity = rs.getInt("quantity");
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                historyData.add(new HistoryItem(id, action, itemID, itemName, quantity, timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            }
            historyTable.setItems(historyData);
        } catch (SQLException e) {
            showAlert("Error", "Database error loading history: " + e.getMessage());
            e.printStackTrace();
        }

        VBox root = new VBox(10, historyScrollPane);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #001933, #004d99);");
        Scene scene = new Scene(root, 800, 500);
        historyStage.setScene(scene);
        historyStage.showAndWait();
    }

    public static class InventoryItem {
        private String itemID;
        private String name;
        private int quantity;
        private String unit;
        private String supplier;

        public InventoryItem(String itemID, String name, int quantity, String unit, String supplier) {
            this.itemID = itemID;
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.supplier = supplier;
        }

        public String getItemID() { return itemID; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public String getSupplier() { return supplier; }
    }

    public static class HistoryItem {
        private final int id;
        private final String action;
        private final String itemID;
        private final String itemName;
        private final int quantity;
        private final String timestamp;

        public HistoryItem(int id, String action, String itemID, String itemName, int quantity, String timestamp) {
            this.id = id;
            this.action = action;
            this.itemID = itemID;
            this.itemName = itemName;
            this.quantity = quantity;
            this.timestamp = timestamp;
        }

        public int getId() { return id; }
        public String getAction() { return action; }
        public String getItemID() { return itemID; }
        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
        public String getTimestamp() { return timestamp; }
    }
}