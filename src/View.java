import java.awt.event.*;
import javax.swing.*;

class View extends JFrame{
    int stacksToMake;
    int stacksPerEnchant;
    double minSimilarity;
    int itemSource;

    JButton startButton;
    JProgressBar progressBar;

    /**
     * Constructor
     *
     * @param ae AutoEnchanter object that created this View object
     */
    View(AutoEnchanter ae) {
        setTitle("Auto Enchanter");
        setSize(400, 200);
        setLocation(100, 100);
        setLayout(null);

        String[] itemSourceChoices = { "Merchant", "Chest" };

        JTextField stacksToMakeField = labeledField("Enchanted Stacks to Make:", "1", 0);
        JTextField stacksPerEnchantField = labeledField("Stacks Per Enchant:", "5", 1);
        JTextField minSimilarityField = labeledField("Match Similarity:", "0.95", 2);
        JComboBox itemSourceField = labeledCombo("Item Source:", itemSourceChoices, 3);

        startButton = new JButton("Start");
        startButton.setBounds(200,10 + 4 * 30,140, 30);
        add(startButton);

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg) {
                stacksToMake = Integer.parseInt(stacksToMakeField.getText());
                stacksPerEnchant = Integer.parseInt(stacksPerEnchantField.getText());
                minSimilarity = Double.parseDouble(minSimilarityField.getText());
                itemSource = itemSourceField.getSelectedItem().equals("Merchant") ? 0 : 1;
                settingsEnabled(false);
                ae.run = true;
            }
        });
    }

    /**
     * Alternates the settings window between the start button, and the progress bar
     *
     * @param enabled true == enable, false == disable
     */
    void settingsEnabled(boolean enabled) {
        setEnabled(enabled);
        startButton.setVisible(enabled);

        if (enabled) {
            progressBar.setVisible(false);
        } else {
            progressBar = new JProgressBar(0, stacksToMake * 64);
            progressBar.setBounds(10, 10 + 4 * 30, 385, 30);
            add(progressBar);
        }
    }

    /**
     * Creates a JTextField, along with a corresponding JLabel
     *
     * @param labelText Text for the label
     * @param defaultValue Default value for the text field
     * @param order Order in which to display this field in relation to the other fields, with 0 == first
     * @return The JTextField object, for retrieving the input value in the future
     */
    private JTextField labeledField(String labelText, String defaultValue, int order) {
        JLabel label = new JLabel();
        label.setText(labelText);
        label.setBounds(10, 10 + order * 30, 190, 30);

        JTextField textField = new JTextField(defaultValue);
        textField.setBounds(200, 10 + order * 30, 200, 30);

        add(label);
        add(textField);

        return textField;
    }

    /**
     * Creates a JComboBox, along with a corresponding JLabel
     *
     * @param labelText Text for the label
     * @param comboValues Array of values for the combo box
     * @param order Order in which to display this box in relation to the other fields, with 0 == first
     * @return The JComboBox object, for retrieving the input value in the future
     */
    private JComboBox labeledCombo(String labelText, String[] comboValues, int order) {
        JLabel label = new JLabel();
        label.setText(labelText);
        label.setBounds(10, 10 + order * 30, 190, 30);

        JComboBox comboBox = new JComboBox(comboValues);
        comboBox.setBounds(200, 10 + order * 30, 200, 30);

        add(label);
        add(comboBox);

        return comboBox;
    }
}
