package com.PlaNova.controllers;

import com.PlaNova.models.Destination;
import com.PlaNova.models.Hotel;
import com.PlaNova.models.Reservation;
import com.PlaNova.models.ReservationDTO;
import com.PlaNova.services.DestinationService;
import com.PlaNova.services.HotelService;
import com.PlaNova.services.ReservationService;
import com.PlaNova.services.StripeService;
import com.PlaNova.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SplitPane;
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
import javafx.application.Platform;
import java.sql.SQLDataException;
import java.util.List;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;

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

    // Overlay components
    @FXML
    private StackPane overlayContainer;
    @FXML
    private Label overlayTitle;
    @FXML
    private Label overlayContent;
    @FXML
    private Button overlayConfirmBtn;
    @FXML
    private Button overlayCancelBtn;
    @FXML
    private javafx.scene.control.ScrollPane overlayScrollPane;
    @FXML
    private VBox loadingContainer;
    @FXML
    private HBox overlayButtonBox;

    private Runnable currentOverlayAction = null;

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
        if (panier.getDestinationId() != null && panier.getDestinationId() != 0) {
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

                    if (panier.getHotelId() != null && panier.getHotelId() != 0) {
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

        showOverlay("Reservation Basket (Panier)", basketDetails, true, () -> {
            // Start background thread to avoid freezing the UI
            new Thread(() -> {
                try {
                    System.out.println("[DEBUG] Starting Reservation Thread...");
                    Reservation res = new Reservation();
                    res.setIdUtilisateur(1); // Hardcoded
                    res.setIdDestination(panier.getDestinationId());
                    res.setIdHotel(panier.getHotelId());
                    res.setIdChambre(panier.getRoomId());

                    String transport = panier.getTransportType();
                    res.setTransportType(transport != null && transport.equals("Not selected") ? null : transport);
                    res.setIdTransport(panier.getTransportId());

                    res.setDateDebut(panier.getStartDate());
                    res.setDateFin(panier.getEndDate());
                    res.setPrixTotal(total);
                    res.setStatus("Payé");

                    System.out.println("[DEBUG] Requesting Stripe Checkout Session...");
                    StripeService stripeService = new StripeService();
                    String sessionUrl = stripeService.createCheckoutSession(panier.getDestinationName(), total);
                    System.out.println("[DEBUG] Stripe Session URL received: " + sessionUrl);

                    // --- START LOCAL PAYMENT LISTENER ---
                    // This server waits for your browser to come back after the Pay button is
                    // clicked
                    try {
                        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 9090), 0);
                        server.createContext("/success", exchange -> {
                            System.out.println("[DEBUG] >> PAYMENT SUCCESS SIGNAL RECEIVED FROM BROWSER <<");

                            try {
                                // 1. SAVE TO DATABASE ONLY NOW
                                reservationService.add(res);
                                int finalId = res.getIdReservation();
                                System.out.println("[DEBUG] Database save successful. Generated ID: " + finalId);

                                String response = "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px;'>"
                                        +
                                        "<h1 style='color:#1E91D6;'>Payment Success!</h1>" +
                                        "<p>Your reservation has been saved to the database.</p>" +
                                        "<p>You can close this window now.</p>" +
                                        "</body></html>";
                                exchange.getResponseHeaders().set("Content-Type", "text/html");
                                exchange.sendResponseHeaders(200, response.length());
                                try (OutputStream os = exchange.getResponseBody()) {
                                    os.write(response.getBytes());
                                }

                                // 2. Update UI
                                Platform.runLater(() -> {
                                    showOverlay("Booking Confirmed!",
                                            "Payment received! Your reservation has been successfully booked.",
                                            false, null);

                                    // Reset UI as the session is complete
                                    roomTypeComboBox.getSelectionModel().clearSelection();
                                    hotelComboBox.getSelectionModel().clearSelection();
                                    SessionManager.clearSession();
                                    closeInspectorDrawer();
                                });

                            } catch (Exception e) {
                                System.err.println("[ERROR] Database save failed after payment: " + e.getMessage());
                                Platform.runLater(() -> {
                                    showOverlay("Database Error",
                                            "Payment received, but we failed to save to the database: "
                                                    + e.getMessage(),
                                            false, null);
                                });
                            } finally {
                                exchange.close();
                                server.stop(1); // Stop the listener
                            }
                        });

                        server.createContext("/cancel", exchange -> {
                            System.out.println("[DEBUG] User cancelled payment.");
                            String response = "Payment Cancelled. You can close this window.";
                            exchange.sendResponseHeaders(200, response.length());
                            exchange.getResponseBody().write(response.getBytes());
                            exchange.close();
                            server.stop(1);
                        });

                        server.start();
                        System.out.println("[DEBUG] Success listener started on port 9090.");

                    } catch (Exception e) {
                        System.err.println("[ERROR] Could not start payment listener: " + e.getMessage());
                        Platform.runLater(() -> {
                            showOverlay("Listener Error", "Could not start the secure payment tracker: "
                                    + e.getMessage() + ". Please check if port 9090 is being used by another app.",
                                    false, null);
                        });
                    }

                    Platform.runLater(() -> {
                        System.out.println("[DEBUG] Directing user to Stripe...");
                        hideLoading();
                        hideOverlay();
                        try {
                            // Robust Linux browser opening
                            String os = System.getProperty("os.name").toLowerCase();
                            if (os.contains("linux")) {
                                try {
                                    new ProcessBuilder("xdg-open", sessionUrl).start();
                                } catch (Exception e) {
                                    java.awt.Desktop.getDesktop().browse(new java.net.URI(sessionUrl));
                                }
                            } else if (java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(sessionUrl));
                            }

                            // Show waiting overlay with a MANUAL FALLBACK button
                            showOverlay("Waiting for Payment",
                                    "Stripe checkout has opened. Once you finish the payment, this app will update automatically.\n\n"
                                            +
                                            "If the app doesn't detect it after you pay, click the button below:",
                                    true, () -> {
                                        System.out.println("[DEBUG] Fallback Manual Save triggered by user.");
                                        new Thread(() -> {
                                            try {
                                                reservationService.add(res);
                                                Platform.runLater(() -> {
                                                    showOverlay("Booking Confirmed!",
                                                            "Your reservation has been saved manually.", false, null);
                                                    roomTypeComboBox.getSelectionModel().clearSelection();
                                                    hotelComboBox.getSelectionModel().clearSelection();
                                                    SessionManager.clearSession();
                                                    closeInspectorDrawer();
                                                });
                                            } catch (Exception ex) {
                                                Platform.runLater(() -> showOverlay("Error",
                                                        "Manual save failed: " + ex.getMessage(), false, null));
                                            }
                                        }).start();
                                    });

                        } catch (Exception e) {
                            System.err.println("[ERROR] Failed to open browser: " + e.getMessage());
                            showOverlay("Action Required",
                                    "Please visit this link to pay and finish the reservation: " + sessionUrl,
                                    false, null);
                        }
                    });

                } catch (SQLDataException e) {
                    System.err.println("[ERROR] Database Error: " + e.getMessage());
                    Platform.runLater(() -> {
                        hideLoading();
                        showOverlay("Database Error", "Failed to save: " + e.getMessage(), false, null);
                    });
                } catch (Exception e) {
                    System.err.println("[ERROR] General Error: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        hideLoading();
                        showOverlay("Payment Error", "Failed to start session: " + e.getMessage(), false, null);
                    });
                }
            }).start();
        });
    }

    private void showOverlay(String title, String content, boolean showCancel, Runnable onConfirm) {
        overlayTitle.setText(title);
        overlayContent.setText(content);
        overlayCancelBtn.setVisible(showCancel);
        overlayConfirmBtn.setText(showCancel ? "Confirm" : "OK");
        currentOverlayAction = onConfirm;

        // Ensure content is visible, loading is hidden
        overlayScrollPane.setVisible(true);
        overlayButtonBox.setVisible(true);
        loadingContainer.setVisible(false);
        overlayContainer.setVisible(true);
    }

    private void showLoading(String title) {
        overlayTitle.setText(title);
        overlayScrollPane.setVisible(false);
        overlayButtonBox.setVisible(false);
        loadingContainer.setVisible(true);
        overlayContainer.setVisible(true);
    }

    private void hideLoading() {
        loadingContainer.setVisible(false);
        overlayScrollPane.setVisible(true);
        overlayButtonBox.setVisible(true);
    }

    @FXML
    private void hideOverlay() {
        overlayContainer.setVisible(false);
    }

    @FXML
    private void handleOverlayConfirm() {
        if (currentOverlayAction != null) {
            // Switch to loading state instead of hiding immediately
            showLoading("Processing Reservation...");
            currentOverlayAction.run();
            currentOverlayAction = null;
        } else {
            hideOverlay();
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        // Fallback or legacy alerts now redirected to overlay
        showOverlay(title, content, false, null);
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
