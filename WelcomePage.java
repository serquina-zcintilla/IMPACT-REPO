// Welcome Page
package default1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class WelcomePage {
    public static void main(String[] args) {
 
        // Create the frame
        JFrame welcomeFrame = new JFrame("Welcome to I.M.P.A.C.T");
        welcomeFrame.setSize(800, 600);
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeFrame.setLayout(new BorderLayout()); // Use BorderLayout
       
        ImageIcon icon = new ImageIcon("C:\\Users\\zcint\\Downloads\\download (5).png");
        welcomeFrame.setIconImage(icon.getImage());

        // ===== Top Navigation Bar =====
        JPanel navBar = new JPanel();
        navBar.setBackground(Color.WHITE);
        navBar.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel appNameLabel = new JLabel("I.M.P.A.C.T");
        appNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appNameLabel.setForeground(new Color(0, 51, 102));

        navBar.add(Box.createRigidArea(new Dimension(20, 0))); // Margin on the left
        navBar.add(appNameLabel);

        welcomeFrame.add(navBar, BorderLayout.NORTH);

        // ===== Center Gradient Panel =====
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 51, 102),
                        getWidth(), getHeight(), new Color(0, 102, 204));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Optional: draw small dots (background pattern)
                g2d.setColor(new Color(255, 255, 255, 80)); // White with transparency
                int dotSize = 6;
                int gap = 30;
                for (int y = 50; y < getHeight(); y += gap) {
                    for (int x = 50; x < getWidth(); x += gap) {
                        g2d.fillOval(x, y, dotSize, dotSize);
                    }
                }
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Stack vertically

        // Welcome message
        JLabel welcomeLabel = new JLabel("WELCOME TO I.M.P.A.C.T", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tag line
        JLabel taglineLabel = new JLabel("\"Where efficiency meets accuracy\"", JLabel.CENTER);
        taglineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        taglineLabel.setForeground(Color.WHITE);
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Get Started Button
        JButton getStartedButton = new JButton("Get started →");
        getStartedButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        getStartedButton.setBackground(Color.WHITE);
        getStartedButton.setForeground(new Color(0, 51, 102));
        getStartedButton.setFocusPainted(false);
        getStartedButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        getStartedButton.addActionListener((ActionEvent e) -> {
            welcomeFrame.dispose(); // Close welcome window
            RegistrationPage.main(null); // Navigate to next page
        });

        // Hover effect for button
        getStartedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                getStartedButton.setBackground(new Color(204, 229, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                getStartedButton.setBackground(Color.WHITE);
            }
        });

        // Add components to center panel
        panel.add(Box.createVerticalGlue());
        panel.add(welcomeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(taglineLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 50)));
        panel.add(getStartedButton);
        panel.add(Box.createVerticalGlue());

        welcomeFrame.add(panel, BorderLayout.CENTER);

        // Show frame
        welcomeFrame.setLocationRelativeTo(null); // Center on screen
        welcomeFrame.setVisible(true);
    }
}
