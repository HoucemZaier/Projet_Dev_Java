package Controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class RejectGuideDialogController implements Initializable {

    @FXML private Text titleText;
    @FXML private Text subtitleText;
    @FXML private TextArea reasonTextArea;
    @FXML private Button cancelBtn;
    @FXML private Button confirmBtn;

    private Consumer<String> onConfirmCallback;
    private String candidateName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupValidation();
        addEntranceAnimation();
    }

    public void setCandidateName(String name) {
        this.candidateName = name;
        String displayName = extractNameWithoutId(name);
        subtitleText.setText("Raison du rejet pour " + displayName);
    }

    public void setOnConfirmCallback(Consumer<String> callback) {
        this.onConfirmCallback = callback;
    }

    private String extractNameWithoutId(String nameWithId) {
        if (nameWithId.contains("(ID:")) {
            return nameWithId.substring(0, nameWithId.indexOf("(ID:")).trim();
        }
        return nameWithId;
    }

    private void setupValidation() {
        confirmBtn.setDisable(true);
        reasonTextArea.textProperty().addListener((obs, oldText, newText) -> {
            boolean hasReason = newText != null && !newText.trim().isEmpty() && newText.trim().length() >= 10;
            confirmBtn.setDisable(!hasReason);
        });
    }

    private void addEntranceAnimation() {
        titleText.setOpacity(0);
        subtitleText.setOpacity(0);
        reasonTextArea.setOpacity(0);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(200), new KeyValue(titleText.opacityProperty(), 1)),
            new KeyFrame(Duration.millis(400), new KeyValue(subtitleText.opacityProperty(), 1)),
            new KeyFrame(Duration.millis(600), new KeyValue(reasonTextArea.opacityProperty(), 1))
        );
        timeline.play();
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        String reason = reasonTextArea.getText().trim();

        if (reason.isEmpty() || reason.length() < 10) {
            return;
        }

        if (onConfirmCallback != null) {
            onConfirmCallback.accept(reason);
        }
        closeDialog();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
