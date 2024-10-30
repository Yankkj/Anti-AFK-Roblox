import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.AWTException;
import java.util.Random;

public class AFKBot {
    private static boolean running = false;
    private static int speed = 1000;
    private static int duration = 60000;
    private static int startDelay = 0;
    private static JFrame frame;
    private static JButton stopButton;
    private static JLabel titleLabel;
    private static JLabel statusLabel;
    private static Timer auraTimer;
    private static Color originalColor;

    
    private static final String hiddenMessage = "Created by Yankkj rsrs";
    private static final String secretFeature = "This bot can simulate multiple key presses!";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AFKBot::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("AntAFK");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 400);
        frame.setLayout(new GridBagLayout());
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(245, 245, 245));

        JMenuBar menuBar = new JMenuBar();
        JMenu aboutMenu = new JMenu("About");

        JMenuItem aboutItem = new JMenuItem("Info");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    "Made by Gon\nDiscord: gonxt9\nGitHub: Yankkj",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem descriptionItem = new JMenuItem("Description");
        descriptionItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    "AntAFK is a simple automation tool that simulates key presses to prevent your computer from going idle. "
                    + "You can customize the frequency of the key presses, the delay before starting, and the duration of operation. "
                    + "This tool is particularly useful for keeping your status active in online games or applications.\n\n"
                    + "Developed by Gon, this application allows you to choose different themes for the UI.",
                    "Description",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        aboutMenu.add(aboutItem);
        aboutMenu.add(descriptionItem);
        menuBar.add(aboutMenu);

        JMenu themeMenu = new JMenu("Themes");
        JMenuItem lightThemeItem = new JMenuItem("Light");
        JMenuItem darkThemeItem = new JMenuItem("Dark");
        JMenuItem blueThemeItem = new JMenuItem("Blue");
        JMenuItem greenThemeItem = new JMenuItem("Green");

        lightThemeItem.addActionListener(e -> applyLightTheme());
        darkThemeItem.addActionListener(e -> applyDarkTheme());
        blueThemeItem.addActionListener(e -> applyBlueTheme());
        greenThemeItem.addActionListener(e -> applyGreenTheme());

        themeMenu.add(lightThemeItem);
        themeMenu.add(darkThemeItem);
        themeMenu.add(blueThemeItem);
        themeMenu.add(greenThemeItem);
        menuBar.add(themeMenu);
        frame.setJMenuBar(menuBar);

        titleLabel = new JLabel("AntAFK", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        originalColor = new Color(60, 60, 60);
        titleLabel.setForeground(originalColor);

        statusLabel = new JLabel("Status: Stopped", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statusLabel.setForeground(Color.RED);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame.add(titleLabel, gbc);

        gbc.gridy = 1;
        frame.add(statusLabel, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        frame.add(new JLabel("Delay between actions:"), gbc);
        JTextField speedField = createNumericTextField("1");
        gbc.gridx = 1;
        frame.add(speedField, gbc);

        String[] speedOptions = {"Milliseconds", "Seconds"};
        JComboBox<String> speedUnit = new JComboBox<>(speedOptions);
        gbc.gridx = 2;
        frame.add(speedUnit, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(new JLabel("Delay before starting:"), gbc);
        JTextField startDelayField = createNumericTextField("1");
        gbc.gridx = 1;
        frame.add(startDelayField, gbc);

        JComboBox<String> startDelayUnit = new JComboBox<>(speedOptions);
        gbc.gridx = 2;
        frame.add(startDelayUnit, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        frame.add(new JLabel("Duration:"), gbc);
        JTextField durationField = createNumericTextField("60");
        gbc.gridx = 1;
        frame.add(durationField, gbc);

        String[] durationOptions = {"Seconds", "Continuous"};
        JComboBox<String> durationUnit = new JComboBox<>(durationOptions);
        gbc.gridx = 2;
        frame.add(durationUnit, gbc);

        JButton startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        frame.add(startButton, gbc);

        gbc.gridx = 1;
        frame.add(stopButton, gbc);

        startButton.addActionListener(e -> {
            try {
                int speedValue = Integer.parseInt(speedField.getText());
                String speedUnitValue = (String) speedUnit.getSelectedItem();
                speed = convertToMilliseconds(speedValue, speedUnitValue);

                int startDelayValue = Integer.parseInt(startDelayField.getText());
                String startDelayUnitValue = (String) startDelayUnit.getSelectedItem();
                startDelay = convertToMilliseconds(startDelayValue, startDelayUnitValue);

                if (durationUnit.getSelectedItem().equals("Continuous")) {
                    duration = -1; 
                } else {
                    int durationValue = Integer.parseInt(durationField.getText());
                    duration = durationValue * 1000;
                }

                Timer timer = new Timer(startDelay, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e1) {
                        startBot();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numbers.");
            }
        });

        stopButton.addActionListener(e -> stopBot());

        frame.setVisible(true);
    }

    private static JTextField createNumericTextField(String initialText) {
        JTextField textField = new JTextField(initialText, 10);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("\\d*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        return textField;
    }

    private static void startBot() {
        running = true;
        stopButton.setEnabled(true);
        statusLabel.setText("Status: Running");
        statusLabel.setForeground(Color.GREEN);

        new Thread(() -> {
            long endTime = duration == -1 ? Long.MAX_VALUE : System.currentTimeMillis() + duration;
            Random random = new Random();
            Robot robot;

            try {
                robot = new Robot();
                while (running && System.currentTimeMillis() < endTime) {
                    int key = random.nextInt(5);
                    switch (key) {
                        case 0 -> robot.keyPress(KeyEvent.VK_W);
                        case 1 -> robot.keyPress(KeyEvent.VK_A);
                        case 2 -> robot.keyPress(KeyEvent.VK_S);
                        case 3 -> robot.keyPress(KeyEvent.VK_D);
                        case 4 -> robot.keyPress(KeyEvent.VK_SPACE);
                    }
                    Thread.sleep(speed);
                }
            } catch (AWTException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                running = false;
                stopButton.setEnabled(false);
                statusLabel.setText("Status: Stopped");
                statusLabel.setForeground(Color.RED);
                if (auraTimer != null) {
                    auraTimer.stop();
                }
            }
        }).start();
    }

    private static void stopBot() {
        running = false;
    }

    private static int convertToMilliseconds(int value, String unit) {
        return switch (unit) {
            case "Milliseconds" -> value;
            case "Seconds" -> value * 1000;
            default -> value;
        };
    }

    private static void applyLightTheme() {
        changeTheme(new Color(245, 245, 245), new Color(60, 60, 60), Color.WHITE);
        titleLabel.setForeground(new Color(60, 60, 60));
    }

    private static void applyDarkTheme() {
        changeTheme(Color.BLACK, Color.WHITE, Color.LIGHT_GRAY);
        titleLabel.setForeground(Color.WHITE);
    }

    private static void applyBlueTheme() {
        changeTheme(new Color(173, 216, 230), new Color(0, 0, 139), Color.BLUE);
        titleLabel.setForeground(new Color(0, 0, 139));
    }

    private static void applyGreenTheme() {
        changeTheme(new Color(144, 238, 144), new Color(0, 100, 0), Color.GREEN);
        titleLabel.setForeground(new Color(0, 100, 0));
    }

    private static void changeTheme(Color background, Color foreground, Color buttonColor) {
        frame.getContentPane().setBackground(background);
        for (Component comp : frame.getContentPane().getComponents()) {
            if (comp instanceof JLabel) {
                comp.setForeground(foreground);
            }
        }
        SwingUtilities.updateComponentTreeUI(frame);
    }
}
