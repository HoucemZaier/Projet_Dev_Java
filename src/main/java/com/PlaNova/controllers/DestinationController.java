package com.PlaNova.controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLDataException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.PlaNova.models.Destination;
import com.PlaNova.services.AiService;
import com.PlaNova.services.CloudinaryService;
import com.PlaNova.services.DestinationService;
import com.PlaNova.services.PexelsService;

import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class DestinationController implements Initializable {

    @FXML
    private StackPane contentStack;
    @FXML
    private TableView<Destination> destinationTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label totalLabel;
    @FXML
    private Label countriesLabel;

    @FXML
    private CheckBox selectAllCheck;
    @FXML
    private Label selectionLabel;
    @FXML
    private Button batchDeleteBtn;
    @FXML
    private MenuButton columnToggleBtn;
    @FXML
    private Button densityBtn;
    @FXML
    private Button undoBtn;

    @FXML
    private HBox formOverlay;
    @FXML
    private Region backdrop;
    @FXML
    private VBox formPanel;
    @FXML
    private Label formTitle;
    @FXML
    private TextField nameField;
    @FXML
    private TextField countryField;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Button saveBtn;

    @FXML
    private Label nameError;
    @FXML
    private Label countryError;

    @FXML
    private VBox notificationArea;

    private DestinationService service;
    private CloudinaryService cloudinaryService;
    private PexelsService pexelsService;
    private AiService aiService;
    private ObservableList<Destination> destinationList;
    private FilteredList<Destination> filteredList;
    private Destination editingDestination;
    private String selectedImagePath;

    private final Set<Destination> checkedItems = new LinkedHashSet<>();
    private final Deque<List<Destination>> undoStack = new ArrayDeque<>();
    private boolean compactMode = false;

    // ── Image cache: avoids re-downloading/re-decoding the same image on scroll ──
    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    private static Image getCachedImage(String url, double w, double h, boolean bg) {
        return imageCache.computeIfAbsent(url, k -> new Image(k, w, h, true, true, bg));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        service = new DestinationService();
        cloudinaryService = new CloudinaryService();
        pexelsService = new PexelsService();
        aiService = new AiService();
        destinationList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(destinationList, p -> true);

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

    // -----------------------Table-----------------------------------

    @SuppressWarnings("unchecked")
    private void setupTable() {
        destinationTable.setEditable(true);
        destinationTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Destination, Void> checkCol = new TableColumn<>();
        checkCol.setPrefWidth(40);
        checkCol.setMaxWidth(40);
        checkCol.setSortable(false);
        checkCol.setEditable(false);
        checkCol.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    Destination d = getTableView().getItems().get(getIndex());
                    if (cb.isSelected())
                        checkedItems.add(d);
                    else
                        checkedItems.remove(d);
                    updateSelectionUI();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Destination d = getTableView().getItems().get(getIndex());
                cb.setSelected(checkedItems.contains(d));
                setGraphic(cb);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Destination, Void> numCol = new TableColumn<>("#");
        numCol.setPrefWidth(45);
        numCol.setMaxWidth(50);
        numCol.setSortable(false);
        numCol.setEditable(false);
        numCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
                if (!empty)
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
            }
        });

        TableColumn<Destination, String> imgCol = new TableColumn<>("Image");
        imgCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        imgCol.setPrefWidth(75);
        imgCol.setSortable(false);
        imgCol.setEditable(false);
        imgCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            {
                iv.setFitHeight(40);
                iv.setFitWidth(55);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        // Use Cloudinary thumbnail transformation for faster loading
                        String thumbUrl = url;
                        if (url.contains("res.cloudinary.com") && url.contains("/upload/")) {
                            thumbUrl = url.replace("/upload/", "/upload/w_55,h_40,c_fill,q_auto,f_auto/");
                        }
                        iv.setImage(getCachedImage(thumbUrl, 55, 40, true));
                        setGraphic(iv);
                        setText(null);
                        setAlignment(Pos.CENTER);
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("-");
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });

        TableColumn<Destination, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("idDestination"));
        idCol.setPrefWidth(55);
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setEditable(false);

        TableColumn<Destination, String> nameCol = new TableColumn<>("Destination");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nomDestination"));
        nameCol.setPrefWidth(160);
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            Destination d = e.getRowValue();
            String newVal = e.getNewValue();
            if (newVal == null || newVal.trim().length() < 2) {
                showNotification("Name must be at least 2 characters", false);
                destinationTable.refresh();
                return;
            }
            d.setNomDestination(newVal.trim());
            saveInline(d);
        });

        TableColumn<Destination, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("pays"));
        countryCol.setPrefWidth(120);
        countryCol.setCellFactory(TextFieldTableCell.forTableColumn());
        countryCol.setOnEditCommit(e -> {
            Destination d = e.getRowValue();
            String newVal = e.getNewValue();
            if (newVal == null || newVal.trim().length() < 2) {
                showNotification("Country must be at least 2 characters", false);
                destinationTable.refresh();
                return;
            }
            d.setPays(newVal.trim());
            saveInline(d);
        });

        TableColumn<Destination, Void> actionsCol = new TableColumn<>("Actions");
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

        destinationTable.getColumns().addAll(
                checkCol, numCol, imgCol, idCol, nameCol, countryCol,
                actionsCol);
        destinationTable.getStyleClass().addAll(Styles.BORDERED, Styles.STRIPED);
        destinationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        VBox placeholderBox = new VBox(8);
        placeholderBox.setAlignment(Pos.CENTER);
        Label emptyIcon = new Label("\u2691");
        emptyIcon.setStyle("-fx-font-size: 40px; -fx-text-fill: #d1d5db;");
        Label emptyText = new Label("No destinations found");
        emptyText.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16px; -fx-font-weight: 600;");
        Label emptyHint = new Label("Click '+ New Destination' to add one, or adjust your search.");
        emptyHint.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
        placeholderBox.getChildren().addAll(emptyIcon, emptyText, emptyHint);
        destinationTable.setPlaceholder(placeholderBox);

        destinationTable.setRowFactory(tv -> {
            TableRow<Destination> row = new TableRow<>();
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
                Destination d = row.getItem();
                if (checkedItems.contains(d))
                    checkedItems.remove(d);
                else
                    checkedItems.add(d);
                destinationTable.refresh();
                updateSelectionUI();
            });

            ctx.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(),
                    dupItem, copyItem, new SeparatorMenuItem(), toggleItem);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(ctx));

            // Create tooltip once per row, update text lazily
            Tooltip rowTip = new Tooltip();
            rowTip.setShowDelay(Duration.millis(600));
            rowTip.setStyle("-fx-font-size: 12px;");
            Tooltip.install(row, rowTip);

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    rowTip.setText(
                            "ID: " + newItem.getIdDestination() + "\n" +
                                    "Name: " + newItem.getNomDestination() + "\n" +
                                    "Country: " + newItem.getPays());
                } else {
                    rowTip.setText("");
                }
            });

            return row;
        });

        SortedList<Destination> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(destinationTable.comparatorProperty());
        destinationTable.setItems(sortedList);
    }

    private void setupSelectAll() {
        selectAllCheck.setOnAction(e -> {
            if (selectAllCheck.isSelected()) {
                checkedItems.addAll(filteredList);
            } else {
                checkedItems.clear();
            }
            destinationTable.refresh();
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
        for (TableColumn<Destination, ?> col : destinationTable.getColumns()) {
            String name = col.getText();
            if (name != null && !name.isEmpty()) {
                CheckMenuItem item = new CheckMenuItem(name);
                item.setSelected(true);
                item.setOnAction(e -> col.setVisible(item.isSelected()));
                columnToggleBtn.getItems().add(item);
            }
        }
    }

    private void saveInline(Destination d) {
        try {
            service.modify(d);
            showNotification("Saved: " + d.getNomDestination(), true);
            destinationTable.refresh();
            updateStats();
        } catch (Exception e) {
            showNotification("Inline save failed: " + e.getMessage(), false);
            loadData();
        }
    }

    private void handleDuplicate(Destination d) {
        try {
            Destination dup = new Destination(
                    d.getNomDestination() + " (copy)",
                    d.getPays(),
                    d.getImage());
            service.add(dup);
            loadData();
            showNotification("Duplicated: " + d.getNomDestination(), true);
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
        alert.setHeaderText("Delete " + checkedItems.size() + " destination(s)?");
        alert.setContentText("This action cannot be undone. Select 'OK' to proceed.");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/destinations.css").toExternalForm());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            undoStack.push(new ArrayList<>(checkedItems));
            undoBtn.setVisible(true);
            undoBtn.setManaged(true);
            int count = checkedItems.size();
            for (Destination d : new ArrayList<>(checkedItems)) {
                try {
                    service.delete(d);
                } catch (Exception ignored) {
                }
            }
            checkedItems.clear();
            selectAllCheck.setSelected(false);
            loadData();
            updateSelectionUI();
            showNotification(count + " destination(s) deleted", true);
        }
    }

    @FXML
    void handleExportCsv(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Destinations to CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName("destinations.csv");
        File file = fc.showSaveDialog(contentStack.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.println("ID,Name,Country,Image");
                for (Destination d : filteredList) {
                    writer.println(
                            d.getIdDestination() + "," +
                                    escapeCsv(d.getNomDestination()) + "," +
                                    escapeCsv(d.getPays()) + "," +
                                    escapeCsv(d.getImage() != null ? d.getImage() : ""));
                }
                showNotification("Exported " + filteredList.size() + " rows to CSV", true);
            } catch (Exception e) {
                showNotification("Export failed: " + e.getMessage(), false);
            }
        }
    }

    private String escapeCsv(String val) {
        if (val == null)
            return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private void copyToClipboard(Destination d) {
        String text = String.join("\t",
                String.valueOf(d.getIdDestination()),
                d.getNomDestination(),
                d.getPays());
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
        showNotification("Copied to clipboard", true);
    }

    @FXML
    void handleDensityToggle(ActionEvent event) {
        compactMode = !compactMode;
        if (compactMode) {
            destinationTable.setFixedCellSize(30);
            densityBtn.setText("Comfortable");
        } else {
            destinationTable.setFixedCellSize(-1);
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
        destinationTable.getSortOrder().clear();
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
        List<Destination> items = undoStack.pop();
        int restored = 0;
        for (Destination d : items) {
            try {
                service.add(d);
                restored++;
            } catch (Exception ignored) {
            }
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

    @FXML
    void handleAiImageMatch(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Upload Image for AI Matching");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        File file = fc.showOpenDialog(contentStack.getScene().getWindow());
        if (file == null) return;

        showNotification("\uD83E\uDDE0 AI is analyzing the image...", true);

        aiService.identifyImage(file).thenAccept(jsonResult -> {
            Platform.runLater(() -> {
                try {
                    com.google.gson.JsonObject result = new com.google.gson.Gson()
                            .fromJson(jsonResult, com.google.gson.JsonObject.class);

                    if (result.has("error")) {
                        showNotification("AI could not identify the image: " + result.get("error").getAsString(), false);
                        return;
                    }

                    String aiName = result.has("name") ? result.get("name").getAsString() : "";
                    String aiCountry = result.has("country") ? result.get("country").getAsString() : "";

                    if (aiName.isEmpty()) {
                        showNotification("AI could not identify this place.", false);
                        return;
                    }

                    // Match against DB
                    Destination bestMatch = findBestMatch(aiName, aiCountry);

                    if (bestMatch != null) {
                        // Highlight the matched row in the table
                        destinationTable.getSelectionModel().clearSelection();
                        int idx = destinationTable.getItems().indexOf(bestMatch);
                        if (idx >= 0) {
                            destinationTable.getSelectionModel().select(idx);
                            destinationTable.scrollTo(idx);
                        }
                        showNotification("\u2728 AI matched: \"" + bestMatch.getNomDestination()
                                + "\" in " + bestMatch.getPays() + "!", true);
                    } else {
                        // No match — offer to create a new destination
                        showNotification("\uD83C\uDF0D AI identified: \"" + aiName
                                + "\" (" + aiCountry + ") — not in database. Opening form...", true);
                        editingDestination = null;
                        formTitle.setText("New Destination");
                        saveBtn.setText("Save Destination");
                        clearForm();
                        nameField.setText(aiName);
                        if (!aiCountry.isEmpty()) countryField.setText(aiCountry);
                        imagePreview.setImage(new Image(file.toURI().toString()));
                        selectedImagePath = file.toURI().toString();
                        showFormOverlay();
                    }
                } catch (Exception e) {
                    showNotification("AI analysis failed: " + e.getMessage(), false);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() ->
                showNotification("AI analysis error: " + ex.getMessage(), false));
            return null;
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(dest -> {
                if (newVal == null || newVal.isEmpty())
                    return true;
                String filter = newVal.toLowerCase();
                return dest.getNomDestination().toLowerCase().contains(filter) ||
                        dest.getPays().toLowerCase().contains(filter);
            });
            updateStats();
        });
    }

    private void setupFormValidation() {
        nameField.textProperty().addListener((obs, o, n) -> {
            if (nameError.isVisible())
                validateName();
        });
        countryField.textProperty().addListener((obs, o, n) -> {
            if (countryError.isVisible())
                validateCountry();
        });
    }

    private void loadData() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return service.show();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(list -> {
            Platform.runLater(() -> {
                destinationList.setAll(list);
                updateStats();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() ->
                showNotification("Failed to load destinations: " + ex.getMessage(), false));
            return null;
        });
    }

    private void updateStats() {
        totalLabel.setText(String.valueOf(filteredList.size()));

        long countries = filteredList.stream()
                .map(Destination::getPays)
                .distinct()
                .count();
        countriesLabel.setText(String.valueOf(countries));
    }

    @FXML
    void navigateToBillets(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Billets.fxml"));
            Parent root = loader.load();
            contentStack.getScene().setRoot(root);
        } catch (IOException e) {
            showNotification("Failed to open Billets page: " + e.getMessage(), false);
        }
    }

    @FXML
    void showAddForm(ActionEvent event) {
        editingDestination = null;
        formTitle.setText("New Destination");
        saveBtn.setText("Save Destination");
        clearForm();
        showFormOverlay();
    }

    private void showEditForm(Destination d) {
        editingDestination = d;
        formTitle.setText("Edit Destination");
        saveBtn.setText("Update Destination");

        nameField.setText(d.getNomDestination());
        countryField.setText(d.getPays());
        selectedImagePath = d.getImage();

        if (d.getImage() != null && !d.getImage().isEmpty()) {
            try {
                imagePreview.setImage(new Image(d.getImage(), true));
            } catch (Exception e) {
                imagePreview.setImage(null);
            }
        } else {
            imagePreview.setImage(null);
        }

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
        if (!validateForm())
            return;

        try {
            if (editingDestination == null) {
                Destination d = new Destination(
                        nameField.getText().trim(),
                        countryField.getText().trim(),
                        selectedImagePath != null ? selectedImagePath : "");
                service.add(d);
                showNotification("Destination \"" + d.getNomDestination() + "\" added successfully!", true);
            } else {
                editingDestination.setNomDestination(nameField.getText().trim());
                editingDestination.setPays(countryField.getText().trim());
                editingDestination.setImage(selectedImagePath != null ? selectedImagePath : "");
                service.modify(editingDestination);
                showNotification("Destination \"" + editingDestination.getNomDestination() + "\" updated!", true);
            }
            loadData();
            hideForm();
        } catch (SQLDataException e) {
            showNotification("Error: " + e.getMessage(), false);
        }
    }

    private void handleDelete(Destination d) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Destination");
        alert.setHeaderText("Delete \"" + d.getNomDestination() + "\"?");
        alert.setContentText("This action cannot be undone. The destination in "
                + d.getPays() + " will be permanently removed.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/destinations.css").toExternalForm());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                undoStack.push(List.of(d));
                undoBtn.setVisible(true);
                undoBtn.setManaged(true);
                service.delete(d);
                checkedItems.remove(d);
                loadData();
                updateSelectionUI();
                showNotification("\"" + d.getNomDestination() + "\" deleted successfully.", true);
            } catch (SQLDataException e) {
                showNotification("Error deleting: " + e.getMessage(), false);
            }
        }
    }

    @FXML
    void autoFindImage(ActionEvent event) {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showNotification("Enter a destination name first", false);
            return;
        }

        showNotification("Searching Pexels for \"" + name.trim() + "\"...", true);

        pexelsService.findBestImage(name.trim() + " travel destination").thenAccept(url -> {
            Platform.runLater(() -> {
                if (url != null) {
                    imagePreview.setImage(new Image(url, true));
                    selectedImagePath = url;
                    showNotification("Image found! (powered by Pexels)", true);
                } else {
                    showNotification("No images found for \"" + name.trim() + "\"", false);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() ->
                showNotification("Pexels search failed: " + ex.getMessage(), false));
            return null;
        });
    }

    @FXML
    void uploadImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Destination Image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File file = fc.showOpenDialog(contentStack.getScene().getWindow());
        if (file != null) {
            // Show local preview immediately so the user sees feedback
            imagePreview.setImage(new Image(file.toURI().toString()));

            if (cloudinaryService.isConfigured()) {
                // Upload to Cloudinary in background
                showNotification("Uploading image to cloud...", true);
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return cloudinaryService.uploadImage(file);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).thenAccept(url -> {
                    Platform.runLater(() -> {
                        selectedImagePath = url;
                        showNotification("Image uploaded to cloud!", true);
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() -> {
                        selectedImagePath = file.toURI().toString();
                        showNotification("Cloud upload failed, using local image.", false);
                        System.err.println("Cloudinary error: " + ex.getMessage());
                    });
                    return null;
                });
            } else {
                // Cloudinary not configured — fall back to local file path
                selectedImagePath = file.toURI().toString();
                showNotification("Using local image (Cloudinary not configured).", false);
            }

            // If name is empty, use AI to identify the place and match against DB
            String currentName = nameField.getText();
            if (currentName == null || currentName.trim().isEmpty()) {
                triggerAiImageMatch(file);
            }
        }
    }

    /**
     * Sends the uploaded image to Gemini AI to identify the destination,
     * then fuzzy-matches the result against existing database destinations.
     * Auto-fills name and country fields if a match is found.
     */
    private void triggerAiImageMatch(File imageFile) {
        showNotification("\uD83E\uDDE0 AI is analyzing the image...", true);

        aiService.identifyImage(imageFile).thenAccept(jsonResult -> {
            Platform.runLater(() -> {
                try {
                    com.google.gson.JsonObject result = new com.google.gson.Gson()
                            .fromJson(jsonResult, com.google.gson.JsonObject.class);

                    if (result.has("error")) {
                        showNotification("AI could not identify the image.", false);
                        return;
                    }

                    String aiName = result.has("name") ? result.get("name").getAsString() : "";
                    String aiCountry = result.has("country") ? result.get("country").getAsString() : "";

                    if (aiName.isEmpty()) {
                        showNotification("AI could not identify this place.", false);
                        return;
                    }

                    // Try to match against existing destinations in the database
                    Destination bestMatch = findBestMatch(aiName, aiCountry);

                    if (bestMatch != null) {
                        nameField.setText(bestMatch.getNomDestination());
                        countryField.setText(bestMatch.getPays());
                        showNotification("\u2728 AI matched: \"" + bestMatch.getNomDestination()
                                + "\" in " + bestMatch.getPays() + "!", true);
                    } else {
                        // No DB match — still fill with AI's best guess
                        nameField.setText(aiName);
                        if (!aiCountry.isEmpty()) {
                            countryField.setText(aiCountry);
                        }
                        showNotification("\uD83C\uDF0D AI identified: \"" + aiName
                                + "\" (" + aiCountry + ") — not in database yet.", true);
                    }
                } catch (Exception e) {
                    showNotification("AI analysis failed: " + e.getMessage(), false);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() ->
                showNotification("AI analysis error: " + ex.getMessage(), false));
            return null;
        });
    }

    /**
     * Fuzzy-matches an AI-identified name/country against all destinations in the DB.
     * Returns the best match, or null if no reasonable match is found.
     */
    private Destination findBestMatch(String aiName, String aiCountry) {
        try {
            List<Destination> allDestinations = service.show();
            String aiNameLower = aiName.toLowerCase();
            String aiCountryLower = aiCountry != null ? aiCountry.toLowerCase() : "";

            Destination bestMatch = null;
            int bestScore = 0;

            for (Destination d : allDestinations) {
                String dbName = d.getNomDestination().toLowerCase();
                String dbCountry = d.getPays().toLowerCase();
                int score = 0;

                // Exact name match
                if (dbName.equals(aiNameLower)) {
                    score += 100;
                }
                // DB name contains AI name or vice versa
                else if (dbName.contains(aiNameLower) || aiNameLower.contains(dbName)) {
                    score += 70;
                }
                // Check individual words overlap
                else {
                    String[] aiWords = aiNameLower.split("\\s+");
                    for (String word : aiWords) {
                        if (word.length() > 2 && dbName.contains(word)) {
                            score += 30;
                        }
                    }
                }

                // Country match bonus
                if (!aiCountryLower.isEmpty()) {
                    if (dbCountry.equals(aiCountryLower)) {
                        score += 25;
                    } else if (dbCountry.contains(aiCountryLower) || aiCountryLower.contains(dbCountry)) {
                        score += 15;
                    }
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = d;
                }
            }

            // Only return if score is strong enough (at least a partial name match)
            return bestScore >= 30 ? bestMatch : null;
        } catch (Exception e) {
            System.err.println("Error matching destination: " + e.getMessage());
            return null;
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        clearErrors();

        if (!validateName())
            valid = false;
        if (!validateCountry())
            valid = false;

        return valid;
    }

    private boolean validateName() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showFieldError(nameError, "Destination name is required");
            nameField.getStyleClass().add("input-error");
            return false;
        }
        if (name.trim().length() < 2) {
            showFieldError(nameError, "Name must be at least 2 characters");
            nameField.getStyleClass().add("input-error");
            return false;
        }
        if (name.trim().length() > 100) {
            showFieldError(nameError, "Name cannot exceed 100 characters");
            nameField.getStyleClass().add("input-error");
            return false;
        }
        clearFieldError(nameError);
        nameField.getStyleClass().remove("input-error");
        return true;
    }

    private boolean validateCountry() {
        String country = countryField.getText();
        if (country == null || country.trim().isEmpty()) {
            showFieldError(countryError, "Country is required");
            countryField.getStyleClass().add("input-error");
            return false;
        }
        if (country.trim().length() < 2) {
            showFieldError(countryError, "Country must be at least 2 characters");
            countryField.getStyleClass().add("input-error");
            return false;
        }
        clearFieldError(countryError);
        countryField.getStyleClass().remove("input-error");
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
        for (Label err : new Label[] { nameError, countryError }) {
            clearFieldError(err);
        }
        nameField.getStyleClass().remove("input-error");
        countryField.getStyleClass().remove("input-error");
    }

    private void clearForm() {
        nameField.clear();
        countryField.clear();
        imagePreview.setImage(null);
        selectedImagePath = null;
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
