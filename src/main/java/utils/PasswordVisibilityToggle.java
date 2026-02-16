package utils;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * Utility class to create password fields with visibility toggle functionality
 */
public class PasswordVisibilityToggle {

    /**
     * Creates a password field with a toggle button to show/hide the password
     * @param promptText The prompt text for the field
     * @param styleClass The CSS style class to apply
     * @return StackPane containing the password field and toggle button
     */
    public static PasswordToggleContainer createPasswordFieldWithToggle(String promptText, String styleClass) {
        // Create the password field and visible text field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(promptText);
        passwordField.getStyleClass().add(styleClass);

        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.getStyleClass().add(styleClass);
        textField.setVisible(false);
        textField.setManaged(false);

        // Create toggle button with eye icon
        Button toggleButton = new Button();
        toggleButton.setPrefSize(30, 30);
        toggleButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");

        // Create eye icons
        ImageView eyeClosedIcon = createEyeIcon("👁", false);
        ImageView eyeOpenIcon = createEyeIcon("👁", true);

        toggleButton.setGraphic(eyeClosedIcon);

        // Bind text properties
        textField.textProperty().bindBidirectional(passwordField.textProperty());

        // Toggle functionality
        toggleButton.setOnAction(e -> {
            if (passwordField.isVisible()) {
                // Switch to visible mode
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                textField.setVisible(true);
                textField.setManaged(true);
                toggleButton.setGraphic(eyeOpenIcon);
            } else {
                // Switch to hidden mode
                textField.setVisible(false);
                textField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                toggleButton.setGraphic(eyeClosedIcon);
            }
        });

        // Create container
        StackPane container = new StackPane();
        container.getChildren().addAll(passwordField, textField);

        // Position toggle button
        StackPane.setAlignment(toggleButton, javafx.geometry.Pos.CENTER_RIGHT);
        container.getChildren().add(toggleButton);
        toggleButton.setTranslateX(-10);

        return new PasswordToggleContainer(container, passwordField, textField, toggleButton);
    }

    private static ImageView createEyeIcon(String symbol, boolean open) {
        // For now, use text-based icons. In a real app, you'd use actual image files
        ImageView imageView = new ImageView();
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);

        // You can replace this with actual eye icon images if you have them
        // For now, we'll use Unicode symbols or FontAwesome icons via CSS
        return imageView;
    }

    /**
     * Container class that holds all components of the password toggle field
     */
    public static class PasswordToggleContainer {
        private final StackPane container;
        private final PasswordField passwordField;
        private final TextField textField;
        private final Button toggleButton;

        public PasswordToggleContainer(StackPane container, PasswordField passwordField,
                                     TextField textField, Button toggleButton) {
            this.container = container;
            this.passwordField = passwordField;
            this.textField = textField;
            this.toggleButton = toggleButton;
        }

        public StackPane getContainer() {
            return container;
        }

        public PasswordField getPasswordField() {
            return passwordField;
        }

        public TextField getTextField() {
            return textField;
        }

        public Button getToggleButton() {
            return toggleButton;
        }

        /**
         * Gets the current text value from whichever field is active
         */
        public String getText() {
            return passwordField.getText();
        }

        /**
         * Sets the text in both fields
         */
        public void setText(String text) {
            passwordField.setText(text);
        }

        /**
         * Sets the prompt text for both fields
         */
        public void setPromptText(String promptText) {
            passwordField.setPromptText(promptText);
            textField.setPromptText(promptText);
        }

        /**
         * Clears both fields
         */
        public void clear() {
            passwordField.clear();
            textField.clear();
        }
    }
}
