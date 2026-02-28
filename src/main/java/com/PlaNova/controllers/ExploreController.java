package com.PlaNova.controllers;

import com.PlaNova.models.Destination;
import com.PlaNova.models.Hotel;
import com.PlaNova.models.Reservation;
import com.PlaNova.models.ReservationDTO;
import com.PlaNova.models.Activite;
import com.PlaNova.services.DestinationService;
import com.PlaNova.services.HotelService;
import com.PlaNova.services.ReservationService;
import com.PlaNova.services.StripeService;
import com.PlaNova.services.AiService;
import com.PlaNova.services.VoiceService;
import com.PlaNova.services.PdfExportService;
import com.PlaNova.services.WeatherService;
import com.PlaNova.services.ActiviteService;
import com.PlaNova.models.Billet;
import com.PlaNova.services.BilletService;
import com.PlaNova.utils.SessionManager;
import javafx.stage.FileChooser;
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
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

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
    @FXML
    private ComboBox<Activite> activiteComboBox;

    @FXML
    private TextField aiMoodField;
    @FXML
    private Button voiceBtn;
    @FXML
    private Label weatherLabel;

    private AiService aiService;
    private VoiceService voiceService;
    private WeatherService weatherService;
    private PdfExportService pdfExportService;
    private boolean isRecording = false;

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
    private Button exportPdfBtn;
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
    private ActiviteService activiteService;
    private Destination currentSelectedDestination;
    private List<Destination> cachedDestinations = new ArrayList<>();
    private Map<Integer, VBox> cardCache = new HashMap<>(); // Store pre-built card nodes
    private PauseTransition debounceTimer; // To slow down search triggering

    @FXML
    public void initialize() {
        // 1. Basic Setup
        destinationService = new DestinationService();
        reservationService = new ReservationService();
        hotelService = new HotelService();
        activiteService = new ActiviteService();
        aiService = new AiService();
        voiceService = new VoiceService();
        weatherService = new WeatherService();
        pdfExportService = new PdfExportService();
        mainSplitPane.getItems().remove(inspectorDrawer);

        roomTypeComboBox.getItems().setAll("simple", "delux", "suite", "triple");
        countryFilterComboBox.getItems().clear();
        countryFilterComboBox.getItems().add("All Countries");
        countryFilterComboBox.getSelectionModel().selectFirst();

        // 2. Setup Debouncer for Search (Performance King)
        debounceTimer = new PauseTransition(Duration.millis(300));
        debounceTimer.setOnFinished(e -> filterDestinations());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> debounceTimer.playFromStart());
        countryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterDestinations());

        // 3. Load Data Asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                List<Destination> list = destinationService.show();
                Set<String> countries = list.stream().map(Destination::getPays).collect(Collectors.toSet());

                Platform.runLater(() -> {
                    cachedDestinations.clear();
                    cardCache.clear();
                    cachedDestinations.addAll(list);

                    System.out.println("[DB_CHECK] Total destinations fetched from database: " + list.size());
                    for (Destination d : list) {
                        System.out.println(
                                "  >> Found in DB: " + d.getNomDestination() + " (ID: " + d.getIdDestination() + ")");
                    }

                    countryFilterComboBox.getItems().addAll(countries);

                    // Pre-build cards in background to avoid lag during search
                    CompletableFuture.runAsync(() -> {
                        for (Destination d : list) {
                            VBox card = createDestinationCard(d);
                            cardCache.put(d.getIdDestination(), card);
                        }
                        Platform.runLater(this::filterDestinations);
                    });

                    restoreSession();
                });
            } catch (Exception e) {
                System.err.println("Init error: " + e.getMessage());
            }
        });
    }

    private void restoreSession() {
        ReservationDTO panier = SessionManager.getCurrentReservation();
        if (panier.getDestinationId() != null && panier.getDestinationId() != 0) {
            try {
                Destination d = cachedDestinations.stream()
                        .filter(dest -> dest.getIdDestination() == panier.getDestinationId())
                        .findFirst().orElse(null);

                if (d != null) {
                    updateDetails(d, null);

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
            } catch (Exception ignored) {
            }
        }
    }

    private void filterDestinations() {
        List<VBox> matchedCards = new ArrayList<>();
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedCountry = countryFilterComboBox.getValue();

        for (Destination d : cachedDestinations) {
            boolean matchSearch = keyword.isEmpty() ||
                    d.getNomDestination().toLowerCase().contains(keyword) ||
                    (d.getPays() != null && d.getPays().toLowerCase().contains(keyword));

            boolean matchCountry = selectedCountry == null || "All Countries".equals(selectedCountry) ||
                    (d.getPays() != null && d.getPays().equals(selectedCountry));

            if (matchSearch && matchCountry) {
                VBox card = cardCache.get(d.getIdDestination());
                if (card != null) {
                    matchedCards.add(card);
                }
            }
        }

        // Update the FlowPane once with all nodes to avoid flicker
        destinationsFlowPane.getChildren().setAll(matchedCards);
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

        String imagePath = d.getImage();
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = "/images/pexels-maksim-smirnov-27565989-32234331.jpg";
        } else if (imagePath.contains("images/")) {
            imagePath = "/images/" + imagePath.substring(imagePath.lastIndexOf("images/") + 7);
        } else if (!imagePath.startsWith("/") && !imagePath.startsWith("http") && !imagePath.startsWith("file:")) {
            imagePath = "/images/" + imagePath;
        }

        try {
            Image image;
            if (imagePath.startsWith("http") || imagePath.startsWith("file:")) {
                image = new Image(imagePath, true); // true = BACKGROUND LOADING
            } else {
                image = new Image(getClass().getResource(imagePath).toExternalForm(), true);
            }
            imageView.setImage(image);
        } catch (Exception e) {
            try {
                Image placeholder = new Image(getClass().getResource("/images/logo.png").toExternalForm());
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
                Image placeholder = new Image(getClass().getResource("/images/logo.png").toExternalForm());
                detailImageView.setImage(placeholder);
            } catch (Exception ignored) {
            }
        }
        checkinDatePicker.setValue(null);
        checkoutDatePicker.setValue(null);
        roomTypeComboBox.getSelectionModel().clearSelection();
        hotelComboBox.getItems().clear();
        activiteComboBox.getItems().clear();
        hotelComboBox.setDisable(true);
        activiteComboBox.setDisable(true);
        hotelComboBox.setPromptText("Loading...");
        activiteComboBox.setPromptText("Loading...");

        CompletableFuture.runAsync(() -> {
            try {
                List<Hotel> availableHotels = hotelService.getHotelsByDestinationOrVille(d.getIdDestination(),
                        d.getNomDestination());
                List<Activite> availableActivites = activiteService.getActivitesByDestination(d.getIdDestination());

                Platform.runLater(() -> {
                    if (availableHotels.isEmpty()) {
                        hotelComboBox.setPromptText("No hotels available here");
                    } else {
                        hotelComboBox.getItems().addAll(availableHotels);
                        hotelComboBox.setPromptText("Select Hotel");
                        hotelComboBox.setDisable(false);
                    }

                    if (availableActivites.isEmpty()) {
                        activiteComboBox.setPromptText("No activities available here");
                    } else {
                        activiteComboBox.getItems().addAll(availableActivites);
                        activiteComboBox.setPromptText("Select Activity (Optional)");
                        activiteComboBox.setDisable(false);
                    }
                });
            } catch (SQLDataException e) {
                Platform.runLater(() -> {
                    System.err.println("Async Fetch Error: " + e.getMessage());
                    hotelComboBox.setPromptText("Error loading");
                    activiteComboBox.setPromptText("Error loading");
                });
            }
        });

        String desc = "Country: " + d.getPays() + "\n\n" +
                "An exceptional journey awaits you in " + d.getNomDestination()
                + ". Experience nature and breathtaking views unlike anywhere else.\n"
                + "Select your preferred travel dates below to get started!";

        detailDescLabel.setText(desc);

        weatherLabel.setText("Fetching weather...");
        weatherService.getWeather(d.getNomDestination()).thenAccept(weather -> {
            Platform.runLater(() -> weatherLabel.setText("⛅ " + weather));
        });
    }

    @FXML
    public void onExportPdfClick() {
        String title = overlayTitle.getText();
        String content = overlayContent.getText();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Itinerary as PDF");
        fileChooser.setInitialFileName(title.replaceAll("\\s+", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(overlayContainer.getScene().getWindow());
        if (file != null) {
            try {
                pdfExportService.exportItinerary(title, content, file);
                showOverlay("Success", "Itinerary successfully exported to:\n" + file.getAbsolutePath(), false, null);
            } catch (Exception e) {
                showOverlay("Export Error", "Failed to create PDF: " + e.getMessage(), false, null);
            }
        }
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

        ReservationDTO panier = new ReservationDTO();
        panier.setDestinationId(currentSelectedDestination.getIdDestination());
        panier.setDestinationName(currentSelectedDestination.getNomDestination());
        panier.setStartDate(checkinDatePicker.getValue());
        panier.setEndDate(checkoutDatePicker.getValue());

        panier.setHotelId(selectedHotel.getIdHotel());
        panier.setHotelPricePerNight(Math.max(50.0, 50.0 * selectedHotel.getNombreEtoile()));

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

        Activite selectedActivite = activiteComboBox.getValue();
        if (selectedActivite != null) {
            panier.setActiviteId(selectedActivite.getIdActivite());
            panier.setActiviteName(selectedActivite.getNom());
            panier.setActivitePrice(selectedActivite.getPrix());
        } else {
            panier.setActiviteId(null);
            panier.setActiviteName("None");
            panier.setActivitePrice(0.0);
        }

        panier.setTransportType(SessionManager.getCurrentReservation().getTransportType());
        panier.setTransportCost(SessionManager.getCurrentReservation().getTransportCost());
        panier.setTransportId(SessionManager.getCurrentReservation().getTransportId());

        double total = panier.calculateTotal();

        String basketDetails = "Here are your reservation details (Panier):\n\n" +
                "Destination: " + panier.getDestinationName() + "\n" +
                "Dates: " + panier.getStartDate() + " to " + panier.getEndDate() + "\n" +
                "Hotel Rate: €" + panier.getHotelPricePerNight() + " / night\n" +
                "Room Type: " + panier.getRoomType() + "\n" +
                "Activity: " + panier.getActiviteName() + " (€" + panier.getActivitePrice() + ")\n" +
                "Transport: " + panier.getTransportType() + "\n" +
                "----------------------------------------\n" +
                "Total Amount: €" + total + "\n\n" +
                "Do you wish to confirm this reservation?";

        showOverlay("Reservation Basket (Panier)", basketDetails, true, () -> {
            new Thread(() -> {
                try {
                    System.out.println("[DEBUG] Starting Reservation Thread...");
                    Reservation res = new Reservation();
                    res.setIdUtilisateur(1);
                    res.setIdDestination(panier.getDestinationId());
                    res.setIdHotel(panier.getHotelId());
                    res.setIdChambre(panier.getRoomId());

                    String transport = panier.getTransportType();
                    res.setTransportType(transport != null && transport.equals("Not selected") ? null : transport);
                    res.setIdTransport(panier.getTransportId());
                    res.setIdActivite(panier.getActiviteId());

                    res.setDateDebut(panier.getStartDate());
                    res.setDateFin(panier.getEndDate());
                    res.setPrixTotal(total);
                    res.setStatus("Payé");

                    System.out.println("[DEBUG] Requesting Stripe Checkout Session...");
                    StripeService stripeService = new StripeService();
                    String sessionUrl = stripeService.createCheckoutSession(panier.getDestinationName(), total);
                    System.out.println("[DEBUG] Stripe Session URL received: " + sessionUrl);
                    try {
                        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 9090), 0);
                        server.createContext("/success", exchange -> {
                            System.out.println("[DEBUG] >> PAYMENT SUCCESS SIGNAL RECEIVED FROM BROWSER <<");

                            try {
                                finalizeReservation(panier, res);
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
                                server.stop(1);
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

                            showOverlay("Waiting for Payment",
                                    "Stripe checkout has opened. Once you finish the payment, this app will update automatically.\n\n"
                                            +
                                            "If the app doesn't detect it after you pay, click the button below:",
                                    true, () -> {
                                        System.out.println("[DEBUG] Fallback Manual Save triggered by user.");
                                        new Thread(() -> {
                                            try {
                                                finalizeReservation(panier, res);
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
        exportPdfBtn.setVisible(false);
        overlayConfirmBtn.setText(showCancel ? "Confirm" : "OK");
        currentOverlayAction = onConfirm;
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
    private void onAiPlanClick() {
        String mood = aiMoodField.getText();
        if (mood == null || mood.trim().isEmpty()) {
            showOverlay("AI Assistant", "Please describe your mood or vibe first!", false, null);
            return;
        }

        showLoading("PlaNova AI is thinking...");

        try {
            String finalContext = cachedDestinations.stream()
                    .map((Destination d) -> d.getNomDestination() + " (Country: " + d.getPays() + ")")
                    .collect(Collectors.joining(", "));

            String detectedCity = cachedDestinations.stream()
                    .map(Destination::getNomDestination)
                    .filter(name -> mood.toLowerCase().contains(name.toLowerCase()))
                    .findFirst()
                    .orElse(null);

            if (detectedCity != null) {
                weatherService.getWeather(detectedCity).thenAccept(weather -> {
                    aiService.getTravelPlan(mood, finalContext, weather).thenAccept(plan -> {
                        Platform.runLater(() -> {
                            hideLoading();
                            showOverlay("Your Personalized Itinerary", plan, false, null);
                            exportPdfBtn.setVisible(true);
                        });
                    }).exceptionally(ex -> handleAiError(ex));
                });
            } else {
                aiService.getTravelPlan(mood, finalContext, "No specific destination weather available.")
                        .thenAccept(plan -> {
                            Platform.runLater(() -> {
                                hideLoading();
                                showOverlay("Your Personalized Itinerary", plan, false, null);
                                exportPdfBtn.setVisible(true);
                            });
                        }).exceptionally(ex -> handleAiError(ex));
            }

        } catch (Exception e) {
            hideLoading();
            showOverlay("Error", "Could not fetch destinations: " + e.getMessage(), false, null);
        }
    }

    private Void handleAiError(Throwable ex) {
        Platform.runLater(() -> {
            hideLoading();
            showOverlay("AI Error", "Failed to reach the travel assistant: " + ex.getMessage(), false, null);
        });
        return null;
    }

    @FXML
    private void onVoiceSearchClick() {
        if (!isRecording) {
            try {
                voiceService.startRecording("mood_recording.wav");
                isRecording = true;
                voiceBtn.setText("🔴");
                voiceBtn.setStyle("-fx-background-color: rgba(255,0,0,0.2); -fx-background-radius: 50;");
            } catch (Exception e) {
                showOverlay("Microphone Error", "Could not access microphone: " + e.getMessage(), false, null);
            }
        } else {
            voiceService.stopRecording();
            isRecording = false;
            voiceBtn.setText("🎙️");
            voiceBtn.setStyle("-fx-background-color: transparent;");

            showLoading("Transcribing your voice...");

            aiService.transcribeAudio(voiceService.getAudioFile()).thenAccept(text -> {
                Platform.runLater(() -> {
                    hideLoading();
                    if (!text.startsWith("Error")) {
                        aiMoodField.setText(text);
                        onAiPlanClick();
                    } else {
                        showOverlay("Voice Error", "Could not understand your voice: " + text, false, null);
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    hideLoading();
                    showOverlay("Voice Error", "Failed to process audio: " + ex.getMessage(), false, null);
                });
                return null;
            });
        }
    }

    @FXML
    private void handleOverlayConfirm() {
        if (currentOverlayAction != null) {
            showLoading("Processing Reservation...");
            currentOverlayAction.run();
            currentOverlayAction = null;
        } else {
            hideOverlay();
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        showOverlay(title, content, false, null);
    }

    @FXML
    public void onChooseTransportClick() {
        if (currentSelectedDestination == null) {
            showAlert(AlertType.WARNING, "No Destination Selected", "Please select a destination first.");
            return;
        }

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

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/ui/Transportation.fxml"));
            javafx.scene.Parent root = loader.load();
            mainSplitPane.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            showAlert(AlertType.ERROR, "Loader Error", "Could not load Transportation view: " + e.getMessage());
        }
    }

    @FXML
    public void openDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/ui/dashboard/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            mainSplitPane.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            showAlert(AlertType.ERROR, "Loader Error", "Could not load Dashboard view: " + e.getMessage());
        }
    }

    private void finalizeReservation(ReservationDTO panier, Reservation res) throws Exception {
        reservationService.add(res);

        BilletService billetService = new BilletService();
        Billet b = new Billet();
        b.setDb("User Location");
        b.setIdv(panier.getDestinationName());
        b.setNumPlace("S" + (int) (Math.random() * 100));
        b.setIdDestination(panier.getDestinationId());
        String tType = panier.getTransportType();
        int tId = panier.getTransportId() != null ? panier.getTransportId() : 0;
        b.setIdTransportPub("public".equals(tType) ? tId : 0);
        b.setIdTransportPriv("prive".equals(tType) ? tId : 0);
        billetService.add(b);

        String pdfContent = "BILLET RESERVATION\n\n" +
                "Departure: " + b.getDb() + "\n" +
                "Arrival: " + b.getIdv() + "\n" +
                "Seat Number: " + b.getNumPlace() + "\n" +
                "Destination: " + panier.getDestinationName() + "\n" +
                "Activity: " + panier.getActiviteName() + "\n" +
                "Transport Info: " + (tType != null ? tType : "None") + "\n\n" +
                "Payment Details: \n" +
                "Total Price: EUR " + res.getPrixTotal() + "\nStatus: Paid\n" +
                "Payment verified and successful via Stripe API.";

        Platform.runLater(() -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Billet PDF");
                fileChooser
                        .setInitialFileName("Billet_" + panier.getDestinationName().replaceAll("\\s+", "_") + ".pdf");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File file = fileChooser.showSaveDialog(overlayContainer.getScene().getWindow());
                if (file != null) {
                    pdfExportService.exportBilletPdf("Billet Confirmation", pdfContent, file);
                    showOverlay("Booking Confirmed!",
                            "Your reservation has been booked and your billet was saved successfully.\nEnjoy your trip to "
                                    + panier.getDestinationName() + "!",
                            false, null);
                } else {
                    showOverlay("Booking Confirmed!",
                            "Payment received! Your reservation has been successfully booked.\n(Billet PDF was not saved)",
                            false, null);
                }
            } catch (Exception e) {
                System.err.println("Error saving PDF: " + e.getMessage());
                showOverlay("Booking Confirmed!",
                        "Payment received! Database updated, but PDF creation failed.",
                        false, null);
            }

            roomTypeComboBox.getSelectionModel().clearSelection();
            hotelComboBox.getSelectionModel().clearSelection();
            activiteComboBox.getSelectionModel().clearSelection();
            SessionManager.clearSession();
            closeInspectorDrawer();
        });
    }
}
