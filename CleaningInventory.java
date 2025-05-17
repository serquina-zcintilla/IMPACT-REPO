package default1;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.*;

// Admin's Page
public class CleaningInventory {
    private static JTable inventoryTable;

	 private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LMS_TST;encrypt=true;trustServerCertificate=true";
	 private static final String USER = "LMS_ADMIN";
	 private static final String PASSWORD = "Zcintilla1005";

    public static void main(String[] args) {
        JFrame frame = new JFrame("🧼 Admin Page");
        frame.setSize(800, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // set the bg color of the frame to light blue
        frame.getContentPane().setBackground(new Color(173, 216, 230)); // Light blue
        
        ImageIcon icon = new ImageIcon("C:\\Users\\zcint\\Downloads\\download (5).png");
        frame.setIconImage(icon.getImage());

        // title label
        JLabel titleLabel = new JLabel("🧼 Admin Page", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        titleLabel.setForeground(new Color(0, 0, 139)); // Dark blue for text color
        frame.add(titleLabel, BorderLayout.NORTH);

        // inventory Table
        inventoryTable = new JTable();
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inventoryTable.setRowHeight(30);
        inventoryTable.setSelectionBackground(new Color(173, 216, 230)); // Light blue for selection
        JScrollPane tableScrollPane = new JScrollPane(inventoryTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Inventory Details"));
        frame.add(tableScrollPane, BorderLayout.CENTER);

        // button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // define blue color for buttons
        Color buttonColor = new Color(70, 130, 180); // Steel blue color for buttons
        Color whiteColor = Color.WHITE; // White text color

        JButton listButton = new JButton("📋 View Inventory");
        JButton addButton = new JButton("➕ Add Item");
        JButton updateButton = new JButton("✏️ Update Item");
        JButton deleteButton = new JButton("❌ Delete Item");
        JButton lowStockButton = new JButton("📉 Low Stock Report");
        JButton exitButton = new JButton("🚪 Exit");

        // set button colors and font
        Font buttonFont = new Font("Segoe UI", Font.PLAIN, 14);
        for (JButton button : new JButton[]{listButton, addButton, updateButton, deleteButton, lowStockButton, exitButton}) {
            button.setFont(buttonFont);
            button.setBackground(buttonColor);
            button.setForeground(whiteColor);
            button.setFocusPainted(false); // Remove focus border
            buttonPanel.add(button);
        }

        frame.add(buttonPanel, BorderLayout.SOUTH);

        // action Listeners
        listButton.addActionListener(e -> listInventory());
        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        lowStockButton.addActionListener(e -> lowStockReport());
        exitButton.addActionListener(e -> System.exit(0));

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void listInventory() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT ItemID, Name, Quantity, Unit, Supplier FROM dbo.inventory";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                // create table model with data
                DefaultTableModel model = new DefaultTableModel();
                model.addColumn("Item ID");
                model.addColumn("Name");
                model.addColumn("Quantity");
                model.addColumn("Unit");
                model.addColumn("Supplier");

                // fill the table model with data
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("ItemID"),
                        rs.getString("Name"),
                        rs.getInt("Quantity"),
                        rs.getString("Unit"),
                        rs.getString("Supplier")
                    });
                }

                // set model to the JTable
                inventoryTable.setModel(model);

                // customize the table header with blue background
                JTableHeader header = inventoryTable.getTableHeader();
                header.setBackground(new Color(70, 130, 180)); // Steel blue color
                header.setFont(new Font("Segoe UI", Font.BOLD, 14));
                header.setForeground(Color.WHITE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private static void addItem() {
        String name = JOptionPane.showInputDialog("Item Name:");
        String qtyStr = JOptionPane.showInputDialog("Quantity:");
        String unit = JOptionPane.showInputDialog("Unit (e.g., bottles, packs):");
        String supplier = JOptionPane.showInputDialog("Supplier Name:");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO dbo.inventory (Name, Quantity, Unit, Supplier) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setInt(2, Integer.parseInt(qtyStr));
                stmt.setString(3, unit);
                stmt.setString(4, supplier);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Item added!");
                listInventory();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private static void updateItem() {
        String id = JOptionPane.showInputDialog("Enter Item ID to update:");
        String newQty = JOptionPane.showInputDialog("New Quantity:");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "UPDATE dbo.inventory SET Quantity = ? WHERE ItemID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, Integer.parseInt(newQty));
                stmt.setString(2, id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Item updated!");
                listInventory();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private static void deleteItem() {
        String id = JOptionPane.showInputDialog("Enter Item ID to delete:");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM dbo.inventory WHERE ItemID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Item deleted!");
                listInventory();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private static void lowStockReport() {
        StringBuilder list = new StringBuilder("📉 Low Stock Items:\n\n");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT ItemID, Name, Quantity FROM dbo.inventory WHERE Quantity < 5";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    list.append("ID: ").append(rs.getString("ItemID"))
                        .append(" | Name: ").append(rs.getString("Name"))
                        .append(" | Qty: ").append(rs.getInt("Quantity"))
                        .append("\n");
                }
            }
        } catch (SQLException e) {
            list.append("Error: ").append(e.getMessage());
        }

        JOptionPane.showMessageDialog(null, list.toString(), "Low Stock Report", JOptionPane.INFORMATION_MESSAGE);
    }
}