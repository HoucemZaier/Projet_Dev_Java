package com.PlaNova.controllers;

import com.PlaNova.models.Destination;
import com.PlaNova.models.Hotel;
import com.PlaNova.models.Reservation;
import com.PlaNova.models.ReservationDTO;
import com.PlaNova.services.DestinationService;
import com.PlaNova.services.HotelService;
import com.PlaNova.services.ReservationService;
import com.PlaNova.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Alert;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.SQLDataException;
import java.util.List;

public class ExploreController {

    @FXML
    private SplitPane mainSplitPane;
    @FXML
    private VBox inspectorDrawer;
    @FXML
    private ImageView detailImageView;
    @FXML
    private Label detailTitleLabel;
    @FXML
    private Label detailDescLabel;
    @FXML
    private FlowPane destinationsFlowPane;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> countryFilterComboBox;
    @FXML
    private DatePicker checkinDatePicker;
    @FXML
    private DatePicker checkoutDatePicker;
    @FXML
    private ComboBox<Hotel> hotelComboBox;
    @FXML
    private ComboBox<String> roomTypeComboBox;

    private DestinationService destinationService;
    private ReservationService reservationService;
    private HotelService hotelService;
    private Destination currentSelectedDestination;

    @FXML
    public void initialize() {
        destinationService = new DestinationService();
        reservationService = new ReservationService();
        hotelService = new HotelService();

        // Hide inspector drawer initially
        mainSplitPane.getItems().remove(inspectorDrawer);

        roomTypeComboBox.getItems().addAll("simple", "delux", "suite", "triple");

        // Setup filter dropdowns
        countryFilterComboBox.getItems().add("All Countries");

        countryFilterComboBox.getSelectionModel().selectFirst();

        try {
            List<Destination> allDestinations = destinationService.show();
            Set<String> countries = allDestinations.stream().map(Destination::getPays).collect(Collectors.toSet());
            countryFilterComboBox.getItems().addAll(countries);
        } catch (SQLDataException e) {
            System.err.println("Error loading countries for filter: " + e.getMessage());
        }

        // Add listeners for dynamic filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterDestinations());
        countryFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> filterDestinations());

        filterDestinations();
        restoreSession();
    }

    private void restoreSession() {
        ReservationDTO panier = SessionManager.getCurrentReservation();
        if (panier.getDestinationId() != 0) {
            try {
                // Find the destination object
                List<Destination> list = destinationService.show();
                Destination d = list.stream()
                        .filter(dest -> dest.getIdDestination() == panier.getDestinationId())
                        .findFirst().orElse(null);

                if (d != null) {
                    updateDetails(d, null); // This will populate hotels etc

                    // Restore UI fields
                    checkinDatePicker.setValue(panier.getStartDate());
                    checkoutDatePicker.setValue(panier.getEndDate());

                    if (panier.getHotelId() != 0) {
                        Hotel h = hotelComboBox.getItems().stream()
                                .filter(hotel -> hotel.getIdHotel() == panier.getHotelId())
                                .findFirst().orElse(null);
                        hotelComboBox.setValue(h);
                    }

                    roomTypeComboBox.setValue(panier.getRoomType());
                }
            } catch (SQLDataException ignored) {
            }
        }
    }

    private void filterDestinations() {
        destinationsFlowPane.getChildren().clear();
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedCountry = countryFilterComboBox.getValue();

        try {
            List<Destination> list = destinationService.show();

            for (Destination d : list) {
                // Search filter (keyword)
                boolean matchSearch = keyword.isEmpty() ||
                        d.getNomDestination().toLowerCase().contains(keyword) ||
                        (d.getPays() != null && d.getPays().toLowerCase().contains(keyword));

                // Country filter
                boolean matchCountry = selectedCountry == null || "All Countries".equals(selectedCountry) ||
                        (d.getPays() != null && d.getPays().equals(selectedCountry));

                if (matchSearch && matchCountry) {
                    VBox card = createDestinationCard(d);
                    destinationsFlowPane.getChildren().add(card);
                }
            }
        } catch (SQLDataException e) {
            System.err.println("DB Error: " + e.getMessage());
        }
    }

    private VBox createDestinationCard(Destination d) {
        VBox card = new VBox();
        card.getStyleClass().add("nature-card");
        card.setPrefWidth(300);
        card.setMaxWidth(300);

        StackPane stackPane = new StackPane();
        ImageView imageView = new ImageView();
        imageView.setFitHeight(220);
        imageView.setFitWidth(300);
        imageView.getStyleClass().add("card-image");

        // Use placeholder if picture doesn't exist
        String imagePath = d.getImage();
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = "/images/pexels-maksim-smirnov-27565989-32234331.jpg";
        } else if (!imagePath.startsWith("/") && !imagePath.startsWith("http") && !imagePath.startsWith("file:")) {
            imagePath = "/images/" + imagePath;
        }

        try {
            Image image;
            if (imagePath.startsWith("http") || imagePath.startsWith("file:")) {
                image = new Image(imagePath);
            } else {
                image = new Image(getClass().getResource(imagePath).toExternalForm());
            }
            imageView.setImage(image);
        } catch (Exception e) {
            try {
                // Generic placeholder fallback
                Image placeholder = new Image(getClass().getResource("/images/logo.PNG").toExternalForm());
                imageView.setImage(placeholder);
            } catch (Exception ignored) {
            }
        }

        Rectangle clip = new Rectangle(300, 220);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        imageView.setClip(clip);

        Circle circle = new Circle(15, javafx.scene.paint.Color.web("#1E91D6"));
        StackPane.setAlignment(circle, Pos.TOP_RIGHT);
        StackPane.setMargin(circle, new Insets(10, 10, 0, 0));

        stackPane.getChildren().addAll(imageView, circle);

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(15));

        Label titleLabel = new Label(d.getNomDestination());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);

        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label infoLabel = new Label(d.getPays());
        infoLabel.getStyleClass().add("card-info");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        infoBox.getChildren().addAll(infoLabel, spacer);

        Button detailsBtn = new Button("View Details");
        detailsBtn.getStyleClass().add("glass-button");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> updateDetails(d, imageView.getImage()));

        contentBox.getChildren().addAll(titleLabel, infoBox, detailsBtn);

        card.getChildren().addAll(stackPane, contentBox);
        return card;
    }

    private void updateDetails(Destination d, Image img) {
        if (!mainSplitPane.getItems().contains(inspectorDrawer)) {
            mainSplitPane.getItems().add(inspectorDrawer);
            mainSplitPane.setDividerPositions(0.75);
        }

        currentSelectedDestination = d;
        detailTitleLabel.setText(d.getNomDestination());
        if (img != null) {
            detailImageView.setImage(img);
        } else {
            try {
                Image placeholder = new Image(getClass().getResource("/images/logo.PNG").toExternalForm());
                detailImageView.setImage(placeholder);
            } catch (Exception ignored) {
            }
        }

        // Clear previously selected dates & combo boxes
        checkinDatePicker.setValue(null);
        checkoutDatePicker.setValue(null);
        roomTypeComboBox.getSelectionModel().clearSelection();
        hotelComboBox.getItems().clear();

        try {
            List<Hotel> availableHotels = hotelService.getHotelsByDestinationOrVille(d.getIdDestination(),
                    d.getNomDestination());
            if (availableHotels.isEmpty()) {
                hotelComboBox.setPromptText("No hotels available here");
                hotelComboBox.setDisable(true);
            } else {
                hotelComboBox.getItems().addAll(availableHotels);
                hotelComboBox.setPromptText("Select Hotel");
                hotelComboBox.setDisable(false);
            }
        } catch (SQLDataException e) {
            System.err.println("Failed to fetch hotels: " + e.getMessage());
        }

        String desc = "Country: " + d.getPays() + "\n\n" +
                "An exceptional journey awaits you in " + d.getNomDestination()
                + ". Experience nature and breathtaking views unlike anywhere else.\n"
                + "Select your preferred travel dates below to get started!";

        detailDescLabel.setText(desc);
    }

    @FXML
    public void closeInspectorDrawer() {
        mainSplitPane.getItems().remove(inspectorDrawer);
    }

    @FXML
    public void onReserveClick() {
        if (currentSelectedDestination == null) {
            showAlert(AlertType.WARNING, "No Destination Selected", "Please select a destination from the list first.");
            return;
        }

        if (checkinDatePicker.getValue() == null || checkoutDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, "Missing Dates", "Please select both a check-in and check-out date.");
            return;
        }

        if (checkinDatePicker.getValue().isAfter(checkoutDatePicker.getValue())) {
            showAlert(AlertType.ERROR, "Invalid Dates", "Check-in date cannot be after Check-out date.");
            return;
        }

        Hotel selectedHotel = hotelComboBox.getValue();
        if (selectedHotel == null) {
            showAlert(AlertType.WARNING, "No Hotel Selected", "Please choose a hotel for your stay.");
            return;
        }

        // Gather all info into out Application-level DTO Selection Model
        ReservationDTO panier = new ReservationDTO();
        panier.setDestinationId(currentSelectedDestination.getIdDestination());
        panier.setDestinationName(currentSelectedDestination.getNomDestination());
        panier.setStartDate(checkinDatePicker.getValue());
        panier.setEndDate(checkoutDatePicker.getValue());

        // Map Hotel selection
        panier.setHotelId(selectedHotel.getIdHotel());
        panier.setHotelPricePerNight(Math.max(50.0, 50.0 * selectedHotel.getNombreEtoile())); // Adjust price based on
                                                                                              // hotel stars

        String selectedRoom = roomTypeComboBox.getValue();
        if (selectedRoom != null) {
            panier.setRoomType(selectedRoom);
            if ("simple".equals(selectedRoom))
                panier.setRoomId(1);
            else if ("delux".equals(selectedRoom))
                panier.setRoomId(2);
            else if ("suite".equals(selectedRoom))
                panier.setRoomId(3);
            else if ("triple".equals(selectedRoom))
                panier.setRoomId(4);
        } else {
            panier.setRoomType("Not selected");
            panier.setRoomId(null);
        }

        panier.setTransportType(SessionManager.getCurrentReservation().getTransportType());
        panier.setTransportCost(SessionManager.getCurrentReservation().getTransportCost());
        panier.setTransportId(SessionManager.getCurrentReservation().getTransportId());

        // Calculate total via Panier DTO
        double total = panier.calculateTotal();

        // Show the summary Panier basket and Ask for confirmation
        String basketDetails = "Here are your reservation details (Panier):\n\n" +
                "Destination: " + panier.getDestinationName() + "\n" +
                "Dates: " + panier.getStartDate() + " to " + panier.getEndDate() + "\n" +
                "Hotel Rate: €" + panier.getHotelPricePerNight() + " / night\n" +
                "Room Type: " + panier.getRoomType() + "\n" +
                "Transport: " + panier.getTransportType() + "\n" +
                "----------------------------------------\n" +
                "Total Amount: €" + total + "\n\n" +
                "Do you wish to confirm this reservation?";

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reservation Basket (Panier)");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(basketDetails);

        try {
            confirmAlert.getDialogPane().getStylesheets()
                    .add(getClass().getResource("/css/explore.css").toExternalForm());
            confirmAlert.getDialogPane().getStyleClass().add("glass-dialog");
        } catch (Exception ignored) {
        }

        javafx.scene.control.ButtonType confirmBtn = new javafx.scene.control.ButtonType("Confirm",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType cancelBtn = new javafx.scene.control.ButtonType("Cancel",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(confirmBtn, cancelBtn);

        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == confirmBtn) {
            // Save to database
            try {
                Reservation res = new Reservation();
                res.setIdUtilisateur(1); // Hardcoded for now (assuming an ID of 1 exists in utilisateur table)
                res.setIdDestination(panier.getDestinationId());
                res.setIdHotel(panier.getHotelId());
                res.setIdChambre(panier.getRoomId());

                // Map transport type string avoiding "Not selected"
                String transport = panier.getTransportType();
                res.setTransportType(transport != null && transport.equals("Not selected") ? null : transport);
                res.setIdTransport(panier.getTransportId());

                res.setDateDebut(panier.getStartDate());
                res.setDateFin(panier.getEndDate());
                res.setPrixTotal(total);
                res.setStatus("en_attente");

                reservationService.add(res);

                showAlert(AlertType.INFORMATION, "Success",
                        "Reservation successfully confirmed and added to the database!");

                // Reset selection form & close popup after successful booking
                roomTypeComboBox.getSelectionModel().clearSelection();
                hotelComboBox.getSelectionModel().clearSelection();
                SessionManager.clearSession();
                closeInspectorDrawer();

            } catch (SQLDataException e) {
                showAlert(AlertType.ERROR, "Database Error",
                        "Failed to save reservation to database:\n" + e.getMessage());
            }
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/explore.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("glass-dialog");
        } catch (Exception ignored) {
        }
        alert.showAndWait();
    }

    @FXML
    public void onChooseTransportClick() {
        if (currentSelectedDestination == null) {
            showAlert(AlertType.WARNING, "No Destination Selected", "Please select a destination first.");
            return;
        }

        // Save current partial progress to session
        ReservationDTO panier = SessionManager.getCurrentReservation();
        panier.setDestinationId(currentSelectedDestination.getIdDestination());
        panier.setDestinationName(currentSelectedDestination.getNomDestination());
        panier.setStartDate(checkinDatePicker.getValue());
        panier.setEndDate(checkoutDatePicker.getValue());

        Hotel h = hotelComboBox.getValue();
        if (h != null) {
            panier.setHotelId(h.getIdHotel());
            panier.setHotelPricePerNight(Math.max(50.0, 50.0 * h.getNombreEtoile()));
        }

        panier.setRoomType(roomTypeComboBox.getValue());

        // Switch to transportation view
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/ui/Transportation.fxml"));
            javafx.scene.Parent root = loader.load();
            mainSplitPane.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            showAlert(AlertType.ERROR, "Loader Error", "Could not load Transportation view: " + e.getMessage());
        }
    }
}
