package UI;

import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JButton;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import java.util.LinkedList;

public class App extends JFrame {
    private final static JTextArea outputText = new JTextArea();

    public App() {
        super("Navigation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showGUI();

        setVisible(true);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void showGUI() {
        JPanel panel = new JPanel();

        Dimension textPaneSize = new Dimension(125, 25);

        Map map = new Map();

        // create all UI components
        JTextPane scrTextPane = createTextPane("1", textPaneSize); // for input of the starting point
        JTextPane dstTextPane = createTextPane("2", textPaneSize); // for input of the ending point

        // add all components
        panel.add(createButton("Calculate path", textPaneSize,
                e -> calculate(map, scrTextPane.getText(), dstTextPane.getText()))); // add calculate button
        panel.add(createButton("reset", new Dimension(75, 25), e -> {
            map.reset();
            outputText.setText("");
        })); // add reset button
        panel.add(createButton("delete", new Dimension(75, 25), e -> map.delete())); // add delete button
        panel.add(scrTextPane);
        panel.add(dstTextPane);

        add(panel, BorderLayout.NORTH);
        add(outputText, BorderLayout.CENTER);
        add(map, BorderLayout.SOUTH);
    }

    private JTextPane createTextPane(String text, Dimension size) {
        JTextPane textPane = new JTextPane();
        textPane.setText(text);
        textPane.setPreferredSize(size);

        return textPane;
    }

    private JButton createButton(String label, Dimension size, ActionListener actionListener) {
        JButton button = new JButton(label);
        button.setPreferredSize(size);
        button.addActionListener(actionListener);

        return button;
    }

    private void calculate(Map map, String scrText, String dstText) {
        try {
            if (map.calculatePath(Integer.parseInt(scrText), Integer.parseInt(dstText), new LinkedList<>()))
                outputText.setText("");
            else
                outputText.setText("No path found.");
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            outputText.setText("Input Invalid.");
        }
    }

    public static void main(String[] args) {
        new App();
    }
}
