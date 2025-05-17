// student page for borrowing/returning tools

package default1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class StudentPage {
    // db connection settings
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LMS_TST;encrypt=true;trustServerCertificate=true";
    private static final String USER = "LMS_ADMIN";
    private static final String PASSWORD = "Zcintilla1005";

    private static JScrollPane scrollPane;
    private static JTable table;

    // sample student id
    private static final String currentStudentId = "S12345"; // example only!!

    public static void main(String[] args) {
        // frame setup
        JFrame frame = new JFrame("🧼 Student Dashboard - Cleaning Tools");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // set window icon
        ImageIcon icon = new ImageIcon("C:\\Users\\zcint\\Downloads\\download (5).png");
        frame.setIconImage(icon.getImage());

        // header design
        JPanel header = new JPanel();
        header.setBackground(new Color(0, 102, 153));
        JLabel title = new JLabel("Student Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        header.add(title);
        frame.add(header, BorderLayout.NORTH);

        // table scroll area
        scrollPane = new JScrollPane();
        frame.add(scrollPane, BorderLayout.CENTER);

        // bottom buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton viewButton = new JButton("📋 View Tools");
        JButton borrowButton = new JButton("👜 Borrow Tool");
        JButton returnButton = new JButton("🔁 Return Tool");
        JButton exitButton = new JButton("🚪 Exit");

        // style buttons
        JButton[] buttons = { viewButton, borrowButton, returnButton, exitButton };
        for (JButton btn : buttons) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            btn.setBackground(new Color(204, 229, 255));
            btn.setFocusPainted(false);
            buttonPanel.add(btn);
        }

        frame.add(buttonPanel, BorderLayout.SOUTH);

        // button actions
        viewButton.addActionListener(e -> displayInventory());
        borrowButton.addActionListener(e -> borrowTool());
        returnButton.addActionListener(e -> returnTool());
        exitButton.addActionListener(e -> {
            frame.dispose();
            LogInPage.main(new String[]{}); // back to login
        });

        // center window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // show tools with quantity > 0
    private static void displayInventory() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT ItemID AS 'ID', Name AS 'Name', Quantity AS 'Qty', Unit AS 'Unit' FROM dbo.inventory WHERE Quantity > 0";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                // get column headers
                String[] columnNames = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    columnNames[i - 1] = meta.getColumnLabel(i);
                }

                // add data to table
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    model.addRow(row);
                }

                // style table
                table = new JTable(model);
                table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                table.setRowHeight(25);
                table.setEnabled(false);

                JTableHeader header = table.getTableHeader();
                header.setBackground(new Color(0, 102, 153));
                header.setForeground(Color.WHITE);
                header.setFont(new Font("Segoe UI", Font.BOLD, 14));

                table.setBackground(new Color(224, 242, 255));
                table.setForeground(Color.BLACK);
                table.setGridColor(Color.LIGHT_GRAY);

                scrollPane.setViewportView(table);
            }
        } catch (SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    // borrow tool logic
    private static void borrowTool() {
        String itemIdStr = JOptionPane.showInputDialog("Enter Item ID to borrow:");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) return;

        String qtyStr = JOptionPane.showInputDialog("Enter quantity to borrow:");
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int itemId = Integer.parseInt(itemIdStr.trim());
            int quantity = Integer.parseInt(qtyStr.trim());

            if (quantity <= 0) {
                showError("Quantity must be greater than 0.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                conn.setAutoCommit(false); // start transaction

                // check current stock
                String checkStock = "SELECT Quantity FROM inventory WHERE ItemID = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkStock)) {
                    checkStmt.setInt(1, itemId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        int stock = rs.getInt("Quantity");
                        if (stock < quantity) {
                            showError("Not enough stock. Available: " + stock);
                            return;
                        }
                    } else {
                        showError("Invalid Item ID.");
                        return;
                    }
                }

                // deduct stock
                String updateInventory = "UPDATE inventory SET Quantity = Quantity - ? WHERE ItemID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateInventory)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setInt(2, itemId);
                    updateStmt.executeUpdate();
                }

                // record borrow (insert or update)
                String mergeBorrow = "MERGE borrowed_tools AS target " +
                        "USING (SELECT ? AS StudentID, ? AS ItemID) AS source " +
                        "ON target.StudentID = source.StudentID AND target.ItemID = source.ItemID " +
                        "WHEN MATCHED THEN UPDATE SET Quantity = target.Quantity + ? " +
                        "WHEN NOT MATCHED THEN INSERT (StudentID, ItemID, Quantity) VALUES (?, ?, ?);";
                try (PreparedStatement borrowStmt = conn.prepareStatement(mergeBorrow)) {
                    borrowStmt.setString(1, currentStudentId);
                    borrowStmt.setInt(2, itemId);
                    borrowStmt.setInt(3, quantity);
                    borrowStmt.setString(4, currentStudentId);
                    borrowStmt.setInt(5, itemId);
                    borrowStmt.setInt(6, quantity);
                    borrowStmt.executeUpdate();
                }

                conn.commit(); // end transaction
                JOptionPane.showMessageDialog(null, "Tool borrowed successfully!");
                displayInventory();
            }

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers.");
        } catch (SQLException e) {
            showError("Database Error: " + e.getMessage());
        }
    }

    // return tool logic
    private static void returnTool() {
        String itemIdStr = JOptionPane.showInputDialog("Enter Item ID to return:");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) return;

        String qtyStr = JOptionPane.showInputDialog("Enter quantity to return:");
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int itemId = Integer.parseInt(itemIdStr.trim());
            int quantity = Integer.parseInt(qtyStr.trim());

            if (quantity <= 0) {
                showError("Quantity must be greater than 0.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                conn.setAutoCommit(false); // start transaction

                // check borrowed quantity
                String checkBorrow = "SELECT Quantity FROM borrowed_tools WHERE StudentID = ? AND ItemID = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkBorrow)) {
                    checkStmt.setString(1, currentStudentId);
                    checkStmt.setInt(2, itemId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        int borrowedQty = rs.getInt("Quantity");
                        if (borrowedQty < quantity) {
                            showError("You are returning more than you borrowed. You only borrowed: " + borrowedQty);
                            return;
                        }
                    } else {
                        showError("No record found of you borrowing this item.");
                        return;
                    }
                }

                // add back to inventory
                String updateInventory = "UPDATE inventory SET Quantity = Quantity + ? WHERE ItemID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateInventory)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setInt(2, itemId);
                    updateStmt.executeUpdate();
                }

                // subtract from borrowed
                String updateBorrow = "UPDATE borrowed_tools SET Quantity = Quantity - ? WHERE StudentID = ? AND ItemID = ?";
                try (PreparedStatement returnStmt = conn.prepareStatement(updateBorrow)) {
                    returnStmt.setInt(1, quantity);
                    returnStmt.setString(2, currentStudentId);
                    returnStmt.setInt(3, itemId);
                    returnStmt.executeUpdate();
                }

                // remove rows with 0 quantity
                String deleteZero = "DELETE FROM borrowed_tools WHERE Quantity = 0";
                try (Statement cleanupStmt = conn.createStatement()) {
                    cleanupStmt.executeUpdate(deleteZero);
                }

                conn.commit(); // end transaction
                JOptionPane.showMessageDialog(null, "Tool returned successfully!");
                displayInventory();
            }

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers.");
        } catch (SQLException e) {
            showError("Database Error: " + e.getMessage());
        }
    }

    // show error message
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
