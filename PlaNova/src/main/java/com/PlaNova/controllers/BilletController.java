package com.PlaNova.controllers;

import atlantafx.base.theme.Styles;
import com.PlaNova.models.Billet;
import com.PlaNova.services.BilletService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.*;

public class BilletController implements Initializable {

    @FXML private StackPane contentStack;
    @FXML private TableView<Billet> billetTable;
    @FXML private TextField searchField;
    @FXML private Label totalLabel;
    @FXML private Label publicLabel;
    @FXML private Label privateLabel;

    @FXML private CheckBox selectAllCheck;
    @FXML private Label selectionLabel;
    @FXML private Button batchDeleteBtn;
    @FXML private MenuButton columnToggleBtn;
    @FXML private Button densityBtn;
    @FXML private Button undoBtn;

    @FXML private HBox formOverlay;
    @FXML private Region backdrop;
    @FXML private VBox formPanel;
    @FXML private Label formTitle;
    @FXML private TextField dbField;
    @FXML private TextField idvField;
    @FXML private TextField numPlaceField;
    @FXML private TextField idDestinationField;
    @FXML private TextField idTransportPubField;
    @FXML private TextField idTransportPrivField;
    @FXML private Button saveBtn;

    @FXML private Label dbError;
    @FXML private Label idvError;
    @FXML private Label numPlaceError;
    @FXML private Label idDestinationError;
    @FXML private Label idTransportPubError;
    @FXML private Label idTransportPrivError;

    @FXML private VBox notificationArea;

    private BilletService service;
    private ObservableList<Billet> billetList;
    private FilteredList<Billet> filteredList;
    private Billet editingBillet;

    private final Set<Billet> checkedItems = new LinkedHashSet<>();
    private final Deque<List<Billet>> undoStack = new ArrayDeque<>();
    private boolean compactMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        service = new BilletService();
        billetList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(billetList, p -> true);

        setupTable();
        setupSearch();
        setupFormValidation();
        setupSelectAll();

        backdrop.setOnMouseClicked(e -> hideForm());

        Platform.runLater(() -> {
            loadData();
            setupColumnToggle();
        });
    }

    private static class SafeIntegerStringConverter extends IntegerStringConverter {
        @Override
        public Integer fromString(String value) {
            try { return super.fromString(value); }
            catch (NumberFormatException e) { return null; }
        }
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        billetTable.setEditable(true);
        billetTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Billet, Void> checkCol = new TableColumn<>();
        checkCol.setPrefWidth(40);
        checkCol.setMaxWidth(40);
        checkCol.setSortable(false);
        checkCol.setEditable(false);
        checkCol.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    Billet b = getTableView().getItems().get(getIndex());
                    if (cb.isSelected()) checkedItems.add(b);
                    else checkedItems.remove(b);
                    updateSelectionUI();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Billet b = getTableView().getItems().get(getIndex());
                cb.setSelected(checkedItems.contains(b));
                setGraphic(cb);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Billet, Void> numCol = new TableColumn<>("#");
        numCol.setPrefWidth(45);
        numCol.setMaxWidth(50);
        numCol.setSortable(false);
        numCol.setEditable(false);
        numCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
                if (!empty) setStyle("-fx-alignment: CENTER; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
            }
        });

        TableColumn<Billet, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("idBillet"));
        idCol.setPrefWidth(55);
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setEditable(false);

        TableColumn<Billet, String> dbCol = new TableColumn<>("Departure");
        dbCol.setCellValueFactory(new PropertyValueFactory<>("db"));
        dbCol.setPrefWidth(130);
        dbCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dbCol.setOnEditCommit(e -> {
            Billet b = e.getRowValue();
            String newVal = e.getNewValue();
            if (newVal == null || newVal.trim().length() < 2) {
                showNotification("Departure must be at least 2 characters", false);
                billetTable.refresh(); return;
            }
            b.setDb(newVal.trim());
            saveInline(b);
        });

        TableColumn<Billet, String> idvCol = new TableColumn<>("Arrival");
        idvCol.setCellValueFactory(new PropertyValueFactory<>("idv"));
        idvCol.setPrefWidth(130);
        idvCol.setCellFactory(TextFieldTableCell.forTableColumn());
        idvCol.setOnEditCommit(e -> {
            Billet b = e.getRowValue();
            String newVal = e.getNewValue();
            if (newVal == null || newVal.trim().length() < 2) {
                showNotification("Arrival must be at least 2 characters", false);
                billetTable.refresh(); return;
            }
            b.setIdv(newVal.trim());
            saveInline(b);
        });

        TableColumn<Billet, String> placeCol = new TableColumn<>("Seat #");
        placeCol.setCellValueFactory(new PropertyValueFactory<>("numPlace"));
        placeCol.setPrefWidth(80);
        placeCol.setStyle("-fx-alignment: CENTER;");
        placeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        placeCol.setOnEditCommit(e -> {
            Billet b = e.getRowValue();
            String newVal = e.getNewValue();
            if (newVal == null || newVal.trim().isEmpty()) {
                showNotification("Seat # must not be empty", false);
                billetTable.refresh(); return;
            }
            b.setNumPlace(newVal.trim());
            saveInline(b);
        });

        TableColumn<Billet, Integer> destCol = new TableColumn<>("Dest. ID");
        destCol.setCellValueFactory(new PropertyValueFactory<>("idDestination"));
        destCol.setPrefWidth(80);
        destCol.setStyle("-fx-alignment: CENTER;");
        destCol.setCellFactory(TextFieldTableCell.forTableColumn(new SafeIntegerStringConverter()));
        destCol.setOnEditCommit(e -> {
            Billet b = e.getRowValue();
            Integer newVal = e.getNewValue();
            if (newVal == null || newVal <= 0) {
                showNotification("Destination ID must be positive", false);
                billetTable.refresh(); return;
            }
            b.setIdDestination(newVal);
            saveInline(b);
        });

        TableColumn<Billet, Integer> pubCol = new TableColumn<>("Public T.");
        pubCol.setCellValueFactory(new PropertyValueFactory<>("idTransportPub"));
        pubCol.setPrefWidth(80);
        pubCol.setStyle("-fx-alignment: CENTER;");
        pubCol.setCellFactory(TextFieldTableCell.forTableColumn(new SafeIntegerStringConverter()));
        pubCol.setOnEditCommit(e -> {
            Billet b = e.getRowValue();
            Integer newVal = e.getNewValue();
            if (newVal == null || newVal < 0) {
                showNotification("Public Transport ID cannot be negative", false);
                billetTable.refresh(); return;
            }
            b.setIdTransportPub(newVal);
            saveInline(b);
        });

        TableColumn<Billet, Integer> privCol = new TableColumn<>("Private T.");
        privCol.setCellValueFactory(new PropertyValueFactory<>("idTransportPriv"));
        privCol.setPrefWidth(80);
        privCol.setStyle("-fx-alignment: CENTER;");
        privCol.setCellFactory(TextFieldTableCell.forTableColumn(new SafeIntegerStringConverter()));
        privCol.setOnEditCommit(e -> {
            Billet b = e.getRowValue();
            Integer newVal = e.getNewValue();
            if (newVal == null || newVal < 0) {
                showNotification("Private Transport ID cannot be negative", false);
                billetTable.refresh(); return;
            }
            b.setIdTransportPriv(newVal);
            saveInline(b);
        });

        TableColumn<Billet, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setSortable(false);
        actionsCol.setEditable(false);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actions = new HBox(8, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("edit-btn");
                deleteBtn.getStyleClass().add("delete-btn");
                actions.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> showEditForm(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actions);
            }
        });

        billetTable.getColumns().addAll(checkCol, numCol, idCol, dbCol, idvCol, placeCol, destCol, pubCol, privCol, actionsCol);
        billetTable.getStyleClass().addAll(Styles.BORDERED, Styles.STRIPED);
        billetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        VBox placeholderBox = new VBox(8);
        placeholderBox.setAlignment(Pos.CENTER);
        Label emptyIcon = new Label("\u2708");
        emptyIcon.setStyle("-fx-font-size: 40px; -fx-text-fill: #d1d5db;");
        Label emptyText = new Label("No billets found");
        emptyText.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16px; -fx-font-weight: 600;");
        Label emptyHint = new Label("Click '+ New Billet' to add one, or adjust your search.");
        emptyHint.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
        placeholderBox.getChildren().addAll(emptyIcon, emptyText, emptyHint);
        billetTable.setPlaceholder(placeholderBox);

        billetTable.setRowFactory(tv -> {
            TableRow<Billet> row = new TableRow<>();
            ContextMenu ctx = new ContextMenu();

            MenuItem editItem = new MenuItem("Edit in Form");
            editItem.setOnAction(e -> showEditForm(row.getItem()));

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> handleDelete(row.getItem()));

            MenuItem dupItem = new MenuItem("Duplicate");
            dupItem.setOnAction(e -> handleDuplicate(row.getItem()));

            MenuItem copyItem = new MenuItem("Copy to Clipboard");
            copyItem.setOnAction(e -> copyToClipboard(row.getItem()));

            MenuItem toggleItem = new MenuItem("Toggle Selection");
            toggleItem.setOnAction(e -> {
                Billet b = row.getItem();
                if (checkedItems.contains(b)) checkedItems.remove(b);
                else checkedItems.add(b);
                billetTable.refresh();
                updateSelectionUI();
            });

            ctx.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(),
                dupItem, copyItem, new SeparatorMenuItem(), toggleItem);

            row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(ctx)
            );

            row.setOnMouseEntered(e -> {
                if (row.getItem() != null) {
                    Billet b = row.getItem();
                    Tooltip tip = new Tooltip(
                        "ID: " + b.getIdBillet() + "\n" +
                        "Departure: " + b.getDb() + "\n" +
                        "Arrival: " + b.getIdv() + "\n" +
                        "Seat: " + b.getNumPlace() + "\n" +
                        "Dest. ID: " + b.getIdDestination()
                    );
                    tip.setShowDelay(Duration.millis(600));
                    tip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(row, tip);
                }
            });

            return row;
        });

        SortedList<Billet> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(billetTable.comparatorProperty());
        billetTable.setItems(sortedList);
    }

    private void setupSelectAll() {
        selectAllCheck.setOnAction(e -> {
            if (selectAllCheck.isSelected()) {
                checkedItems.addAll(filteredList);
            } else {
                checkedItems.clear();
            }
            billetTable.refresh();
            updateSelectionUI();
        });
    }

    private void updateSelectionUI() {
        int count = checkedItems.size();
        selectionLabel.setText(count + " selected");
        batchDeleteBtn.setVisible(count > 0);
        batchDeleteBtn.setManaged(count > 0);
    }

    private void setupColumnToggle() {
        columnToggleBtn.getItems().clear();
        for (TableColumn<Billet, ?> col : billetTable.getColumns()) {
            String name = col.getText();
            if (name != null && !name.isEmpty()) {
                CheckMenuItem item = new CheckMenuItem(name);
                item.setSelected(true);
                item.setOnAction(e -> col.setVisible(item.isSelected()));
                columnToggleBtn.getItems().add(item);
            }
        }
    }


    private void saveInline(Billet b) {
        try {
            service.modify(b);
            showNotification("Saved Billet #" + b.getIdBillet(), true);
            billetTable.refresh();
            updateStats();
        } catch (Exception e) {
            showNotification("Inline save failed: " + e.getMessage(), false);
            loadData();
        }
    }

    private void handleDuplicate(Billet b) {
        try {
            Billet dup = new Billet(
                b.getDb(), b.getIdv(), b.getNumPlace(),
                b.getIdDestination(), b.getIdTransportPub(), b.getIdTransportPriv()
            );
            service.add(dup);
            loadData();
            showNotification("Duplicated Billet #" + b.getIdBillet(), true);
        } catch (Exception e) {
            showNotification("Duplicate failed: " + e.getMessage(), false);
        }
    }

    @FXML
    void handleBatchDelete(ActionEvent event) {
        if (checkedItems.isEmpty()) {
            showNotification("No items selected", false);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Batch Delete");
        alert.setHeaderText("Delete " + checkedItems.size() + " billet(s)?");
        alert.setContentText("This action cannot be undone. Select 'OK' to proceed.");
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/billets.css").toExternalForm());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            undoStack.push(new ArrayList<>(checkedItems));
            undoBtn.setVisible(true);
            undoBtn.setManaged(true);
            int count = checkedItems.size();
            for (Billet b : new ArrayList<>(checkedItems)) {
                try { service.delete(b); } catch (Exception ignored) {}
            }
            checkedItems.clear();
            selectAllCheck.setSelected(false);
            loadData();
            updateSelectionUI();
            showNotification(count + " billet(s) deleted", true);
        }
    }

    @FXML
    void handleExportCsv(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Billets to CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName("billets.csv");
        File file = fc.showSaveDialog(contentStack.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.println("ID,Departure,Arrival,Seat,DestinationID,PublicTransport,PrivateTransport");
                for (Billet b : filteredList) {
                    writer.println(
                        b.getIdBillet() + "," +
                        escapeCsv(b.getDb()) + "," +
                        escapeCsv(b.getIdv()) + "," +
                        escapeCsv(b.getNumPlace()) + "," +
                        b.getIdDestination() + "," +
                        b.getIdTransportPub() + "," +
                        b.getIdTransportPriv()
                    );
                }
                showNotification("Exported " + filteredList.size() + " rows to CSV", true);
            } catch (Exception e) {
                showNotification("Export failed: " + e.getMessage(), false);
            }
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private void copyToClipboard(Billet b) {
        String text = String.join("\t",
            String.valueOf(b.getIdBillet()),
            b.getDb(), b.getIdv(),
            b.getNumPlace(),
            String.valueOf(b.getIdDestination()),
            String.valueOf(b.getIdTransportPub()),
            String.valueOf(b.getIdTransportPriv())
        );
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
        showNotification("Copied to clipboard", true);
    }

    @FXML
    void handleDensityToggle(ActionEvent event) {
        compactMode = !compactMode;
        if (compactMode) {
            billetTable.setFixedCellSize(30);
            densityBtn.setText("Comfortable");
        } else {
            billetTable.setFixedCellSize(-1);
            densityBtn.setText("Compact");
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        if (event != null && event.getSource() instanceof Button btn) {
            RotateTransition rotate = new RotateTransition(Duration.millis(500), btn);
            rotate.setByAngle(360);
            rotate.play();
        }
        checkedItems.clear();
        selectAllCheck.setSelected(false);
        billetTable.getSortOrder().clear();
        loadData();
        updateSelectionUI();
        showNotification("Data refreshed", true);
    }

    @FXML
    void handleUndo(ActionEvent event) {
        if (undoStack.isEmpty()) {
            showNotification("Nothing to undo", false);
            return;
        }
        List<Billet> items = undoStack.pop();
        int restored = 0;
        for (Billet b : items) {
            try { service.add(b); restored++; } catch (Exception ignored) {}
        }
        loadData();
        showNotification("Restored " + restored + " item(s)", true);
        if (undoStack.isEmpty()) {
            undoBtn.setVisible(false);
            undoBtn.setManaged(false);
        }
    }

    @FXML
    void handleClearSearch(ActionEvent event) {
        searchField.clear();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(billet -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return billet.getDb().toLowerCase().contains(filter) ||
                       billet.getIdv().toLowerCase().contains(filter) ||
                       billet.getNumPlace().toLowerCase().contains(filter) ||
                       String.valueOf(billet.getIdBillet()).contains(filter);
            });
            updateStats();
        });
    }

    private void setupFormValidation() {
        dbField.textProperty().addListener((obs, o, n) -> {
            if (dbError.isVisible()) validateDb();
        });
        idvField.textProperty().addListener((obs, o, n) -> {
            if (idvError.isVisible()) validateIdv();
        });
        numPlaceField.textProperty().addListener((obs, o, n) -> {
            if (numPlaceError.isVisible()) validateNumPlace();
        });
        idDestinationField.textProperty().addListener((obs, o, n) -> {
            if (idDestinationError.isVisible()) validateIdDestination();
        });
        idTransportPubField.textProperty().addListener((obs, o, n) -> {
            if (idTransportPubError.isVisible()) validateIdTransportPub();
        });
        idTransportPrivField.textProperty().addListener((obs, o, n) -> {
            if (idTransportPrivError.isVisible()) validateIdTransportPriv();
        });
    }

    private void loadData() {
        try {
            billetList.clear();
            billetList.addAll(service.show());
            updateStats();
        } catch (Exception e) {
            showNotification("Failed to load billets: " + e.getMessage(), false);
        }
    }

    private void updateStats() {
        totalLabel.setText(String.valueOf(filteredList.size()));

        long pub = filteredList.stream()
            .filter(b -> b.getIdTransportPub() > 0)
            .count();
        publicLabel.setText(String.valueOf(pub));

        long priv = filteredList.stream()
            .filter(b -> b.getIdTransportPriv() > 0)
            .count();
        privateLabel.setText(String.valueOf(priv));
    }

    @FXML
    void showAddForm(ActionEvent event) {
        editingBillet = null;
        formTitle.setText("New Billet");
        saveBtn.setText("Save Billet");
        clearForm();
        showFormOverlay();
    }

    private void showEditForm(Billet b) {
        editingBillet = b;
        formTitle.setText("Edit Billet");
        saveBtn.setText("Update Billet");

        dbField.setText(b.getDb());
        idvField.setText(b.getIdv());
        numPlaceField.setText(b.getNumPlace());
        idDestinationField.setText(String.valueOf(b.getIdDestination()));
        idTransportPubField.setText(String.valueOf(b.getIdTransportPub()));
        idTransportPrivField.setText(String.valueOf(b.getIdTransportPriv()));

        clearErrors();
        showFormOverlay();
    }

    private void showFormOverlay() {
        formOverlay.setVisible(true);
        formOverlay.setManaged(true);

        formPanel.setTranslateX(420);
        backdrop.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(320), formPanel);
        slide.setFromX(420);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1));

        FadeTransition fade = new FadeTransition(Duration.millis(250), backdrop);
        fade.setFromValue(0);
        fade.setToValue(1);

        new ParallelTransition(slide, fade).play();
    }

    @FXML
    void hideForm() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(280), formPanel);
        slide.setFromX(0);
        slide.setToX(420);
        slide.setInterpolator(Interpolator.SPLINE(0.55, 0, 1, 0.45));

        FadeTransition fade = new FadeTransition(Duration.millis(220), backdrop);
        fade.setFromValue(1);
        fade.setToValue(0);

        ParallelTransition pt = new ParallelTransition(slide, fade);
        pt.setOnFinished(e -> {
            formOverlay.setVisible(false);
            formOverlay.setManaged(false);
        });
        pt.play();
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateForm()) return;

        try {
            String numPlace = numPlaceField.getText().trim();
            int idDest = Integer.parseInt(idDestinationField.getText().trim());
            int idPub = Integer.parseInt(idTransportPubField.getText().trim());
            int idPriv = Integer.parseInt(idTransportPrivField.getText().trim());

            if (editingBillet == null) {
                Billet b = new Billet(
                    dbField.getText().trim(),
                    idvField.getText().trim(),
                    numPlace, idDest, idPub, idPriv
                );
                service.add(b);
                showNotification("Billet added successfully!", true);
            } else {
                editingBillet.setDb(dbField.getText().trim());
                editingBillet.setIdv(idvField.getText().trim());
                editingBillet.setNumPlace(numPlace);
                editingBillet.setIdDestination(idDest);
                editingBillet.setIdTransportPub(idPub);
                editingBillet.setIdTransportPriv(idPriv);
                service.modify(editingBillet);
                showNotification("Billet #" + editingBillet.getIdBillet() + " updated!", true);
            }
            loadData();
            hideForm();
        } catch (SQLDataException e) {
            showNotification("Error: " + e.getMessage(), false);
        }
    }

    private void handleDelete(Billet b) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Billet");
        alert.setHeaderText("Delete Billet #" + b.getIdBillet() + "?");
        alert.setContentText("This action cannot be undone.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/css/billets.css").toExternalForm()
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                undoStack.push(List.of(b));
                undoBtn.setVisible(true);
                undoBtn.setManaged(true);
                checkedItems.remove(b);
                service.delete(b);
                loadData();
                updateSelectionUI();
                showNotification("Billet #" + b.getIdBillet() + " deleted.", true);
            } catch (SQLDataException e) {
                showNotification("Error deleting: " + e.getMessage(), false);
            }
        }
    }

    @FXML
    void navigateToDestinations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Destinations.fxml"));
            Parent root = loader.load();
            contentStack.getScene().setRoot(root);
        } catch (IOException e) {
            showNotification("Failed to open Destinations page: " + e.getMessage(), false);
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        clearErrors();

        if (!validateDb()) valid = false;
        if (!validateIdv()) valid = false;
        if (!validateNumPlace()) valid = false;
        if (!validateIdDestination()) valid = false;
        if (!validateIdTransportPub()) valid = false;
        if (!validateIdTransportPriv()) valid = false;

        return valid;
    }

    private boolean validateDb() {
        String val = dbField.getText();
        if (val == null || val.trim().isEmpty()) {
            showFieldError(dbError, "Departure info is required");
            dbField.getStyleClass().add("input-error");
            return false;
        }
        if (val.trim().length() < 2) {
            showFieldError(dbError, "Must be at least 2 characters");
            dbField.getStyleClass().add("input-error");
            return false;
        }
        clearFieldError(dbError);
        dbField.getStyleClass().remove("input-error");
        return true;
    }

    private boolean validateIdv() {
        String val = idvField.getText();
        if (val == null || val.trim().isEmpty()) {
            showFieldError(idvError, "Arrival info is required");
            idvField.getStyleClass().add("input-error");
            return false;
        }
        if (val.trim().length() < 2) {
            showFieldError(idvError, "Must be at least 2 characters");
            idvField.getStyleClass().add("input-error");
            return false;
        }
        clearFieldError(idvError);
        idvField.getStyleClass().remove("input-error");
        return true;
    }

    private boolean validateNumPlace() {
        String val = numPlaceField.getText();
        if (val == null || val.trim().isEmpty()) {
            showFieldError(numPlaceError, "Seat number is required");
            numPlaceField.getStyleClass().add("input-error");
            return false;
        }
        clearFieldError(numPlaceError);
        numPlaceField.getStyleClass().remove("input-error");
        return true;
    }

    private boolean validateIdDestination() {
        String val = idDestinationField.getText();
        if (val == null || val.trim().isEmpty()) {
            showFieldError(idDestinationError, "Destination ID is required");
            idDestinationField.getStyleClass().add("input-error");
            return false;
        }
        try {
            int num = Integer.parseInt(val.trim());
            if (num <= 0) {
                showFieldError(idDestinationError, "Must be a positive number");
                idDestinationField.getStyleClass().add("input-error");
                return false;
            }
        } catch (NumberFormatException e) {
            showFieldError(idDestinationError, "Must be a valid number");
            idDestinationField.getStyleClass().add("input-error");
            return false;
        }
        clearFieldError(idDestinationError);
        idDestinationField.getStyleClass().remove("input-error");
        return true;
    }

    private boolean validateIdTransportPub() {
        String val = idTransportPubField.getText();
        if (val == null || val.trim().isEmpty()) {
            showFieldError(idTransportPubError, "Public transport ID is required");
            idTransportPubField.getStyleClass().add("input-error");
            return false;
        }
        try {
            int num = Integer.parseInt(val.trim());
            if (num < 0) {
                showFieldError(idTransportPubError, "Cannot be negative");
                idTransportPubField.getStyleClass().add("input-error");
                return false;
            }
        } catch (NumberFormatException e) {
            showFieldError(idTransportPubError, "Must be a valid number");
            idTransportPubField.getStyleClass().add("input-error");
            return false;
        }
        clearFieldError(idTransportPubError);
        idTransportPubField.getStyleClass().remove("input-error");
        return true;
    }

    private boolean validateIdTransportPriv() {
        String val = idTransportPrivField.getText();
        if (val == null || val.trim().isEmpty()) {
            showFieldError(idTransportPrivError, "Private transport ID is required");
            idTransportPrivField.getStyleClass().add("input-error");
            return false;
        }
        try {
            int num = Integer.parseInt(val.trim());
            if (num < 0) {
                showFieldError(idTransportPrivError, "Cannot be negative");
                idTransportPrivField.getStyleClass().add("input-error");
                return false;
            }
        } catch (NumberFormatException e) {
            showFieldError(idTransportPrivError, "Must be a valid number");
            idTransportPrivField.getStyleClass().add("input-error");
            return false;
        }
        clearFieldError(idTransportPrivError);
        idTransportPrivField.getStyleClass().remove("input-error");
        return true;
    }

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        TranslateTransition shake = new TranslateTransition(Duration.millis(60), errorLabel.getParent());
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.setFromX(0);
        shake.setToX(4);
        shake.play();
    }

    private void clearFieldError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    private void clearErrors() {
        for (Label err : new Label[]{dbError, idvError, numPlaceError, idDestinationError, idTransportPubError, idTransportPrivError}) {
            clearFieldError(err);
        }
        dbField.getStyleClass().remove("input-error");
        idvField.getStyleClass().remove("input-error");
        numPlaceField.getStyleClass().remove("input-error");
        idDestinationField.getStyleClass().remove("input-error");
        idTransportPubField.getStyleClass().remove("input-error");
        idTransportPrivField.getStyleClass().remove("input-error");
    }

    private void clearForm() {
        dbField.clear();
        idvField.clear();
        numPlaceField.clear();
        idDestinationField.clear();
        idTransportPubField.clear();
        idTransportPrivField.clear();
        clearErrors();
    }

    private void showNotification(String message, boolean success) {
        HBox notification = new HBox(10);
        notification.setAlignment(Pos.CENTER_LEFT);
        notification.getStyleClass().add(success ? "notification-success" : "notification-error");
        notification.setMaxWidth(380);

        Label icon = new Label(success ? "\u2713" : "\u2717");
        icon.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label text = new Label(message);
        text.setWrapText(true);
        text.setMaxWidth(320);

        notification.getChildren().addAll(icon, text);
        notificationArea.getChildren().add(notification);

        notification.setTranslateX(400);
        notification.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), notification);
        slideIn.setFromX(400);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1));

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        new ParallelTransition(slideIn, fadeIn).play();

        PauseTransition pause = new PauseTransition(Duration.seconds(3.5));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), notification);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notification);
            slideOut.setByX(400);
            ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
            out.setOnFinished(ev -> notificationArea.getChildren().remove(notification));
            out.play();
        });
        pause.play();
    }
}
