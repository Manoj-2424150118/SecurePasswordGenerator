import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Password Generator GUI Application
 * A comprehensive password generator with customizable options
 * Features: Length control, character sets, strength indicator, history
 * 
 * @author Your Name
 * @version 1.0
 */
public class PasswordGenerator extends JFrame {
    
    // Character sets for password generation
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    // GUI Components
    private JSlider lengthSlider;
    private JLabel lengthLabel;
    private JCheckBox lowercaseCheck, uppercaseCheck, numbersCheck, symbolsCheck;
    private JTextArea passwordArea;
    private JButton generateBtn, copyBtn, clearHistoryBtn;
    private JProgressBar strengthBar;
    private JLabel strengthLabel;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;
    private SecureRandom random;
    
    public PasswordGenerator() {
        random = new SecureRandom();
        initializeGUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Secure Password Generator v1.0");
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set application icon (optional)
        try {
            setIconImage(Toolkit.getDefaultToolkit().createImage("icon.png"));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }
    
    private void initializeGUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("🔐 Secure Password Generator", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 102, 153));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Center panel for controls
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Password length section
        JPanel lengthPanel = new JPanel(new BorderLayout());
        lengthPanel.setBorder(new TitledBorder("Password Length"));
        
        lengthSlider = new JSlider(4, 50, 12);
        lengthSlider.setMajorTickSpacing(10);
        lengthSlider.setMinorTickSpacing(2);
        lengthSlider.setPaintTicks(true);
        lengthSlider.setPaintLabels(true);
        
        lengthLabel = new JLabel("Length: 12 characters", JLabel.CENTER);
        lengthLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        lengthSlider.addChangeListener(e -> {
            int value = lengthSlider.getValue();
            lengthLabel.setText("Length: " + value + " characters");
        });
        
        lengthPanel.add(lengthLabel, BorderLayout.NORTH);
        lengthPanel.add(lengthSlider, BorderLayout.CENTER);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        centerPanel.add(lengthPanel, gbc);
        
        // Character options section
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        optionsPanel.setBorder(new TitledBorder("Include Characters"));
        
        lowercaseCheck = new JCheckBox("Lowercase (a-z)", true);
        uppercaseCheck = new JCheckBox("Uppercase (A-Z)", true);
        numbersCheck = new JCheckBox("Numbers (0-9)", true);
        symbolsCheck = new JCheckBox("Symbols (!@#$...)", false);
        
        // Style checkboxes
        Font checkboxFont = new Font("Arial", Font.PLAIN, 12);
        lowercaseCheck.setFont(checkboxFont);
        uppercaseCheck.setFont(checkboxFont);
        numbersCheck.setFont(checkboxFont);
        symbolsCheck.setFont(checkboxFont);
        
        optionsPanel.add(lowercaseCheck);
        optionsPanel.add(uppercaseCheck);
        optionsPanel.add(numbersCheck);
        optionsPanel.add(symbolsCheck);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        centerPanel.add(optionsPanel, gbc);
        
        // Generate button
        generateBtn = new JButton("Generate Password");
        generateBtn.setFont(new Font("Arial", Font.BOLD, 16));
        generateBtn.setBackground(new Color(135, 206, 235)); // Sky blue background
        generateBtn.setForeground(Color.BLACK); // Bold black text
        generateBtn.setFocusPainted(false);
        generateBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        generateBtn.addActionListener(new GeneratePasswordListener());
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(generateBtn, gbc);
        
        // Password display area
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(new TitledBorder("Generated Password"));
        
        passwordArea = new JTextArea(3, 40);
        passwordArea.setFont(new Font("Courier New", Font.BOLD, 14));
        passwordArea.setLineWrap(true);
        passwordArea.setWrapStyleWord(true);
        passwordArea.setBackground(new Color(245, 245, 245));
        passwordArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(passwordArea);
        displayPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Copy button
        copyBtn = new JButton("📋 Copy to Clipboard");
        copyBtn.setEnabled(false);
        copyBtn.addActionListener(new CopyPasswordListener());
        displayPanel.add(copyBtn, BorderLayout.SOUTH);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        centerPanel.add(displayPanel, gbc);
        
        // Password strength indicator
        JPanel strengthPanel = new JPanel(new BorderLayout());
        strengthPanel.setBorder(new TitledBorder("Password Strength"));
        
        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthBar.setString("Generate a password to see strength");
        
        strengthLabel = new JLabel("", JLabel.CENTER);
        strengthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        strengthPanel.add(strengthBar, BorderLayout.CENTER);
        strengthPanel.add(strengthLabel, BorderLayout.SOUTH);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        centerPanel.add(strengthPanel, gbc);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // History panel
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new TitledBorder("Password History"));
        historyPanel.setPreferredSize(new Dimension(200, 0));
        
        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Courier New", Font.PLAIN, 10));
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && historyList.getSelectedValue() != null) {
                passwordArea.setText(historyList.getSelectedValue());
                copyBtn.setEnabled(true);
                calculatePasswordStrength(historyList.getSelectedValue());
            }
        });
        
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        
        clearHistoryBtn = new JButton("Clear History");
        clearHistoryBtn.addActionListener(e -> {
            historyModel.clear();
            passwordArea.setText("");
            copyBtn.setEnabled(false);
            resetStrengthIndicator();
        });
        historyPanel.add(clearHistoryBtn, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.EAST);
    }
    
    private class GeneratePasswordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Validate that at least one character type is selected
            if (!lowercaseCheck.isSelected() && !uppercaseCheck.isSelected() && 
                !numbersCheck.isSelected() && !symbolsCheck.isSelected()) {
                
                JOptionPane.showMessageDialog(PasswordGenerator.this,
                    "Please select at least one character type!",
                    "Invalid Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String password = generateSecurePassword();
            passwordArea.setText(password);
            copyBtn.setEnabled(true);
            
            // Add to history
            historyModel.addElement(password);
            if (historyModel.size() > 20) { // Limit history to 20 items
                historyModel.removeElementAt(0);
            }
            
            // Calculate and display strength
            calculatePasswordStrength(password);
        }
    }
    
    private class CopyPasswordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String password = passwordArea.getText().trim();
            if (!password.isEmpty()) {
                StringSelection selection = new StringSelection(password);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
                
                // Show confirmation
                JOptionPane.showMessageDialog(PasswordGenerator.this,
                    "Password copied to clipboard!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private String generateSecurePassword() {
        StringBuilder charset = new StringBuilder();
        
        // Build character set based on selected options
        if (lowercaseCheck.isSelected()) charset.append(LOWERCASE);
        if (uppercaseCheck.isSelected()) charset.append(UPPERCASE);
        if (numbersCheck.isSelected()) charset.append(NUMBERS);
        if (symbolsCheck.isSelected()) charset.append(SYMBOLS);
        
        int length = lengthSlider.getValue();
        List<Character> passwordChars = new ArrayList<>();
        
        // Ensure at least one character from each selected type
        if (lowercaseCheck.isSelected()) {
            passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }
        if (uppercaseCheck.isSelected()) {
            passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (numbersCheck.isSelected()) {
            passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (symbolsCheck.isSelected()) {
            passwordChars.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        }
        
        // Fill remaining length with random characters
        for (int i = passwordChars.size(); i < length; i++) {
            passwordChars.add(charset.charAt(random.nextInt(charset.length())));
        }
        
        // Shuffle the password to avoid predictable patterns
        Collections.shuffle(passwordChars, random);
        
        // Convert to string
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }
        
        return password.toString();
    }
    
    private void calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            resetStrengthIndicator();
            return;
        }
        
        int score = 0;
        int length = password.length();
        
        // Length scoring
        if (length >= 8) score += 20;
        if (length >= 12) score += 10;
        if (length >= 16) score += 10;
        
        // Character variety scoring
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
        
        if (hasLower) score += 15;
        if (hasUpper) score += 15;
        if (hasNumber) score += 15;
        if (hasSymbol) score += 15;
        
        // Bonus for using all character types
        if (hasLower && hasUpper && hasNumber && hasSymbol) score += 10;
        
        // Update strength bar
        strengthBar.setValue(Math.min(score, 100));
        
        String strengthText;
        Color strengthColor;
        
        if (score < 30) {
            strengthText = "Very Weak";
            strengthColor = Color.RED;
        } else if (score < 50) {
            strengthText = "Weak";
            strengthColor = Color.ORANGE;
        } else if (score < 70) {
            strengthText = "Medium";
            strengthColor = Color.YELLOW;
        } else if (score < 90) {
            strengthText = "Strong";
            strengthColor = new Color(173, 255, 47);
        } else {
            strengthText = "Very Strong";
            strengthColor = Color.GREEN;
        }
        
        strengthBar.setString(strengthText + " (" + score + "/100)");
        strengthBar.setForeground(strengthColor);
        strengthLabel.setText("Estimated time to crack: " + getTimeEstimate(score));
        strengthLabel.setForeground(Color.BLACK); // Always black text for readability
    }
    
    private String getTimeEstimate(int score) {
        if (score < 30) return "Less than 1 second";
        if (score < 50) return "Few seconds to minutes";
        if (score < 70) return "Hours to days";
        if (score < 90) return "Months to years";
        return "Centuries or more";
    }
    
    private void resetStrengthIndicator() {
        strengthBar.setValue(0);
        strengthBar.setString("Generate a password to see strength");
        strengthBar.setForeground(Color.GRAY);
        strengthLabel.setText("");
        strengthLabel.setForeground(Color.BLACK); // Keep text black
    }
    
    public static void main(String[] args) {
        // Set look and feel - simple approach that works on all Java versions
        try {
            // Try to set system look and feel using string identifier
            String systemLF = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(systemLF);
        } catch (Exception e) {
            // If that fails, use the default Metal look and feel
            System.out.println("Using default look and feel");
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                new PasswordGenerator().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting application: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}