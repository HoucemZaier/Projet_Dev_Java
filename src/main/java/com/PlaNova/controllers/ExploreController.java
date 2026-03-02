package com.PlaNova.controllers;

import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLDataException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.PlaNova.models.Activite;
import com.PlaNova.models.Billet;
import com.PlaNova.models.Destination;
import com.PlaNova.models.Hotel;
import com.PlaNova.models.Reservation;
import com.PlaNova.models.ReservationDTO;
import com.PlaNova.services.ActiviteService;
import com.PlaNova.services.AiService;
import com.PlaNova.services.BilletService;
import com.PlaNova.services.DestinationService;
import com.PlaNova.services.HotelService;
import com.PlaNova.services.PdfExportService;
import com.PlaNova.services.PexelsService;
import com.PlaNova.services.ReservationService;
import com.PlaNova.services.StripeService;
import com.PlaNova.services.VoiceService;
import com.PlaNova.services.WeatherService;
import com.PlaNova.utils.SessionManager;
import com.sun.net.httpserver.HttpServer;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class ExploreController {

    @FXML
     private SplitPane mainSplitPane;
     @FXML
     private VBox inspectorDrawer;
     @FXML
     private javafx.scene.control.ScrollPane inspectorScrollPane;
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
     @FXML
     private HBox galleryPane;
     @FXML
     private Label galleryLabel;

     // ── Night mode, pagination, grid toggle, scroll-to-top ──
     @FXML
     private Button nightModeBtn;
     @FXML
     private Button gridToggleBtn;
     @FXML
     private Button scrollTopBtn;
     @FXML
     private Label destCountLabel;
     @FXML
     private Label pageInfoLabel;
     @FXML
     private HBox paginationBar;
     @FXML
     private Button prevPageBtn;
     @FXML
     private Button nextPageBtn;
     @FXML
     private HBox pageNumbersBox;
     @FXML
     private ComboBox<String> sortComboBox;
     @FXML
     private ComboBox<String> perPageComboBox;
     @FXML
     private Label greetingLabel;
     @FXML
     private Button favSidebarBtn;
     @FXML
     private Button homeSidebarBtn;

     private Set<Integer> favoriteIds = new HashSet<>();
     private boolean showFavoritesOnly = false;

     private boolean nightMode = false;
     private boolean compactGrid = false;
     private int currentPage = 1;
     private int itemsPerPage = 8;
     private List<VBox> lastFilteredCards = new ArrayList<>();

     private AiService aiService;
     private VoiceService voiceService;
     private WeatherService weatherService;
     private PdfExportService pdfExportService;
     private boolean isRecording = false;

     // ── Image cache: avoids reloading the same URL/path repeatedly ──
     private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();

     private static Image getCachedImage(String url, double w, double h, boolean bg) {
         return imageCache.computeIfAbsent(url, k -> new Image(k, w, h, true, true, bg));
     }
     private static Image getCachedImage(String url, boolean bg) {
         return imageCache.computeIfAbsent(url, k -> new Image(k, bg));
     }

     @FXML
     private StackPane fullscreenOverlay;
     @FXML
     private ImageView fullscreenImageView;

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

     // Lazy getters — only create service when first needed
     private AiService getAiService() {
         if (aiService == null) aiService = new AiService();
         return aiService;
     }
     private VoiceService getVoiceService() {
         if (voiceService == null) voiceService = new VoiceService();
         return voiceService;
     }
     private WeatherService getWeatherService() {
         if (weatherService == null) weatherService = new WeatherService();
         return weatherService;
     }
     private PdfExportService getPdfExportService() {
         if (pdfExportService == null) pdfExportService = new PdfExportService();
         return pdfExportService;
     }

     private PexelsService pexelsService;
     private PexelsService getPexelsService() {
         if (pexelsService == null) pexelsService = new PexelsService();
         return pexelsService;
     }

     @FXML
     public void initialize() {
         // 1. Basic Setup — only essential services created now
         destinationService = new DestinationService();
         reservationService = new ReservationService();
         hotelService = new HotelService();
         activiteService = new ActiviteService();
         // AI, Voice, Weather, PDF services are lazy-loaded on first use
         mainSplitPane.getItems().remove(inspectorScrollPane);

         roomTypeComboBox.getItems().setAll("simple", "delux", "suite", "triple");
         countryFilterComboBox.getItems().clear();
         countryFilterComboBox.getItems().add("All Countries");
         countryFilterComboBox.getSelectionModel().selectFirst();

         // 2. Setup Debouncer for Search (Performance King)
         debounceTimer = new PauseTransition(Duration.millis(300));
         debounceTimer.setOnFinished(e -> filterDestinations());

         searchField.textProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; debounceTimer.playFromStart(); });
         countryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> { currentPage = 1; filterDestinations(); });

         // Sort & per-page selectors
         sortComboBox.getItems().setAll("Default", "Name A\u2192Z", "Name Z\u2192A", "Country A\u2192Z", "Country Z\u2192A");
         sortComboBox.setValue("Default");
         sortComboBox.valueProperty().addListener((obs, o, n) -> { currentPage = 1; filterDestinations(); });

         perPageComboBox.getItems().setAll("4", "8", "12", "16");
         perPageComboBox.setValue("8");
         perPageComboBox.valueProperty().addListener((obs, o, n) -> {
             try { itemsPerPage = Integer.parseInt(n); } catch (Exception ex) { itemsPerPage = 8; }
             currentPage = 1;
             filterDestinations();
         });

         updateGreeting();

         // 2b. Scroll-to-top button visibility (after scene is ready)
         Platform.runLater(() -> {
             ScrollPane sp = findScrollPane(destinationsFlowPane);
             if (sp != null) {
                 sp.vvalueProperty().addListener((obs2, old2, val2) -> {
                     scrollTopBtn.setVisible(val2.doubleValue() > 0.15);
                 });
             }
         });

         // 3. Load Data Asynchronously
         CompletableFuture.runAsync(() -> {
             try {
                 List<Destination> list = destinationService.show();
                 Set<String> countries = list.stream().map(Destination::getPays).collect(Collectors.toSet());

                 Platform.runLater(() -> {
                     cachedDestinations.clear();
                     cardCache.clear();
                     cachedDestinations.addAll(list);
                     countryFilterComboBox.getItems().addAll(countries);

                     // Auto-fetch Pexels images for destinations with no image,
                     // then build all cards
                     autoFillMissingImages(list, () -> {
                         Platform.runLater(() -> {
                             for (Destination d : list) {
                                 VBox card = createDestinationCard(d);
                                 cardCache.put(d.getIdDestination(), card);
                             }
                             filterDestinations();
                         });
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
         List<Destination> filtered = new ArrayList<>();
         String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
         String selectedCountry = countryFilterComboBox.getValue();

         for (Destination d : cachedDestinations) {
             boolean matchSearch = keyword.isEmpty() ||
                     d.getNomDestination().toLowerCase().contains(keyword) ||
                     (d.getPays() != null && d.getPays().toLowerCase().contains(keyword));

             boolean matchCountry = selectedCountry == null || "All Countries".equals(selectedCountry) ||
                     (d.getPays() != null && d.getPays().equals(selectedCountry));

             boolean matchFav = !showFavoritesOnly || favoriteIds.contains(d.getIdDestination());

             if (matchSearch && matchCountry && matchFav) {
                 filtered.add(d);
             }
         }

         // Sort based on selected option
         String sortVal = sortComboBox != null ? sortComboBox.getValue() : null;
         if ("Name A\u2192Z".equals(sortVal))
             filtered.sort((a, b) -> a.getNomDestination().compareToIgnoreCase(b.getNomDestination()));
         else if ("Name Z\u2192A".equals(sortVal))
             filtered.sort((a, b) -> b.getNomDestination().compareToIgnoreCase(a.getNomDestination()));
         else if ("Country A\u2192Z".equals(sortVal))
             filtered.sort((a, b) -> String.valueOf(a.getPays()).compareToIgnoreCase(String.valueOf(b.getPays())));
         else if ("Country Z\u2192A".equals(sortVal))
             filtered.sort((a, b) -> String.valueOf(b.getPays()).compareToIgnoreCase(String.valueOf(a.getPays())));

         List<VBox> matchedCards = new ArrayList<>();
         for (Destination d : filtered) {
             VBox card = cardCache.get(d.getIdDestination());
             if (card != null) matchedCards.add(card);
         }

         lastFilteredCards = matchedCards;
         int totalItems = matchedCards.size();
         int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
         if (currentPage > totalPages) currentPage = totalPages;

         int fromIndex = (currentPage - 1) * itemsPerPage;
         int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);
         List<VBox> pageCards = matchedCards.subList(fromIndex, toIndex);

         // Update FlowPane with current page cards + staggered fade-in
         destinationsFlowPane.getChildren().setAll(pageCards);
         for (int i = 0; i < pageCards.size(); i++) {
             VBox card = pageCards.get(i);
             card.setOpacity(0);
             FadeTransition ft = new FadeTransition(Duration.millis(250), card);
             ft.setFromValue(0);
             ft.setToValue(1);
             ft.setDelay(Duration.millis(i * 50));
             ft.play();
         }

         // Update counter
         String countText = totalItems + " destination" + (totalItems != 1 ? "s" : "");
         if (showFavoritesOnly) countText = "\u2764\uFE0F " + countText;
         destCountLabel.setText(countText);

         // Update pagination UI
         updatePaginationBar(totalPages);
     }

     private void updatePaginationBar(int totalPages) {
         pageNumbersBox.getChildren().clear();
         prevPageBtn.setDisable(currentPage <= 1);
         nextPageBtn.setDisable(currentPage >= totalPages);

         if (totalPages <= 1) {
             paginationBar.setVisible(false);
             paginationBar.setManaged(false);
             pageInfoLabel.setText("");
             return;
         }

         paginationBar.setVisible(true);
         paginationBar.setManaged(true);
         pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);

         // Show page number buttons (max 7 visible)
         int startPage = Math.max(1, currentPage - 3);
         int endPage = Math.min(totalPages, startPage + 6);
         if (endPage - startPage < 6) startPage = Math.max(1, endPage - 6);

         for (int i = startPage; i <= endPage; i++) {
             final int page = i;
             Button btn = new Button(String.valueOf(i));
             btn.getStyleClass().add(i == currentPage ? "page-num-btn-active" : "page-num-btn");
             btn.setOnAction(e -> { currentPage = page; filterDestinations(); });
             pageNumbersBox.getChildren().add(btn);
         }
     }

     @FXML
     private void prevPage() {
         if (currentPage > 1) {
             currentPage--;
             filterDestinations();
         }
     }

     @FXML
     private void nextPage() {
         int totalPages = Math.max(1, (int) Math.ceil((double) lastFilteredCards.size() / itemsPerPage));
         if (currentPage < totalPages) {
             currentPage++;
             filterDestinations();
         }
     }

     @FXML
     private void toggleNightMode() {
         nightMode = !nightMode;
         BorderPane root = (BorderPane) mainSplitPane.getParent();
         if (nightMode) {
             if (!root.getStyleClass().contains("night-mode")) {
                 root.getStyleClass().add("night-mode");
             }
             nightModeBtn.setText("\u2600");
         } else {
             root.getStyleClass().remove("night-mode");
             nightModeBtn.setText("\uD83C\uDF19");
         }
     }

     @FXML
     private void toggleGridSize() {
         compactGrid = !compactGrid;
         if (compactGrid) {
             destinationsFlowPane.setHgap(15);
             destinationsFlowPane.setVgap(15);
             gridToggleBtn.setText("\u2B1C");
             // Resize cards to compact
             for (VBox card : cardCache.values()) {
                 card.setPrefWidth(220);
                 card.setMaxWidth(220);
                 // Resize image inside the card
                 Node stackNode = card.getChildren().get(0);
                 if (stackNode instanceof StackPane sp && !sp.getChildren().isEmpty()
                         && sp.getChildren().get(0) instanceof ImageView iv) {
                     iv.setFitWidth(220);
                     iv.setFitHeight(160);
                     iv.setClip(new Rectangle(220, 160) {{ setArcWidth(30); setArcHeight(30); }});
                 }
             }
         } else {
             destinationsFlowPane.setHgap(25);
             destinationsFlowPane.setVgap(25);
             gridToggleBtn.setText("\u229E");
             // Restore cards to normal
             for (VBox card : cardCache.values()) {
                 card.setPrefWidth(300);
                 card.setMaxWidth(300);
                 Node stackNode = card.getChildren().get(0);
                 if (stackNode instanceof StackPane sp && !sp.getChildren().isEmpty()
                         && sp.getChildren().get(0) instanceof ImageView iv) {
                     iv.setFitWidth(300);
                     iv.setFitHeight(220);
                     iv.setClip(new Rectangle(300, 220) {{ setArcWidth(30); setArcHeight(30); }});
                 }
             }
         }
         filterDestinations();
     }

     @FXML
     private void scrollToTop() {
         ScrollPane scrollPane = findScrollPane(destinationsFlowPane);
         if (scrollPane != null) {
             scrollPane.setVvalue(0);
         }
     }

     @FXML
     private void handleLucky() {
         if (cachedDestinations.isEmpty()) return;
         int idx = (int) (Math.random() * cachedDestinations.size());
         Destination lucky = cachedDestinations.get(idx);
         VBox card = cardCache.get(lucky.getIdDestination());
         Image img = null;
         if (card != null) {
             Node stackNode = card.getChildren().get(0);
             if (stackNode instanceof StackPane sp && !sp.getChildren().isEmpty()
                     && sp.getChildren().get(0) instanceof ImageView iv) {
                 img = iv.getImage();
             }
         }
         updateDetails(lucky, img);
         showOverlay("\uD83C\uDFB2 Feeling Lucky!",
                 "You got: " + lucky.getNomDestination() + " in " + lucky.getPays()
                         + "!\n\nThe destination details are now open in the side panel.",
                 false, null);
     }

     @FXML
     private void toggleFavoritesFilter() {
         showFavoritesOnly = !showFavoritesOnly;
         if (favSidebarBtn != null) {
             favSidebarBtn.setStyle(showFavoritesOnly
                     ? "-fx-background-color: rgba(255,100,100,0.3); -fx-text-fill: #ff6b6b; -fx-font-size: 20px; -fx-cursor: hand; -fx-background-radius: 15; -fx-min-width: 40; -fx-min-height: 40;"
                     : "");
         }
         currentPage = 1;
         filterDestinations();
     }

     private void updateGreeting() {
         int hour = LocalTime.now().getHour();
         String greeting;
         if (hour < 12) greeting = "Good Morning \u2600\uFE0F";
         else if (hour < 17) greeting = "Good Afternoon \uD83C\uDF24";
         else greeting = "Good Evening \uD83C\uDF19";
         greetingLabel.setText(greeting + " \u2014 Explore Destinations");
     }

     private ScrollPane findScrollPane(Node node) {
         Node current = node;
         while (current != null) {
             if (current.getParent() instanceof ScrollPane sp) return sp;
             current = current.getParent();
         }
         return null;
     }

     /**
      * For every destination that has no image (null/empty/local-only),
      * fetch one from Pexels, update the model + DB, then run the callback.
      * Destinations that already have an http URL are left untouched.
      */
     private void autoFillMissingImages(List<Destination> destinations, Runnable onDone) {
         List<Destination> needsImage = destinations.stream()
                 .filter(d -> {
                     String img = d.getImage();
                     return img == null || img.isEmpty()
                             || (!img.startsWith("http") && !img.startsWith("file:"));
                 })
                 .collect(Collectors.toList());

         if (needsImage.isEmpty()) {
             onDone.run();
             return;
         }

         CompletableFuture.runAsync(() -> {
             PexelsService pexels = getPexelsService();
             List<CompletableFuture<Void>> futures = new ArrayList<>();

             for (Destination d : needsImage) {
                 String query = d.getNomDestination() + " " + d.getPays() + " travel";
                 CompletableFuture<Void> f = pexels.findBestImage(query)
                         .thenAccept(url -> {
                             if (url != null) {
                                 d.setImage(url);
                                 // Persist to DB so we don't look it up again
                                 try {
                                     destinationService.modify(d);
                                 } catch (Exception ignored) {}
                             }
                         });
                 futures.add(f);
             }

             // Wait for all fetches to finish, then build cards
             CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                     .thenRun(onDone);
         });
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
         } else if (imagePath.startsWith("http") || imagePath.startsWith("file:")) {
             // Remote URL (Pexels, Cloudinary, etc.) — use as-is
         } else if (imagePath.contains("images/")) {
             imagePath = "/images/" + imagePath.substring(imagePath.lastIndexOf("images/") + 7);
         } else if (!imagePath.startsWith("/")) {
             imagePath = "/images/" + imagePath;
         }

         try {
             String resolvedUrl;
             if (imagePath.startsWith("http") || imagePath.startsWith("file:")) {
                 // For Cloudinary URLs, request a resized thumbnail (300w) for cards
                 if (imagePath.contains("res.cloudinary.com") && imagePath.contains("/upload/")) {
                     resolvedUrl = imagePath.replace("/upload/", "/upload/w_300,q_auto,f_auto/");
                 } else {
                     resolvedUrl = imagePath;
                 }
             } else {
                 resolvedUrl = getClass().getResource(imagePath).toExternalForm();
             }
             Image img = getCachedImage(resolvedUrl, true);
             imageView.setImage(img);

             // If background loading fails, log the error and swap to placeholder
             img.errorProperty().addListener((obs, wasError, isError) -> {
                 if (isError) {
                     System.err.println("[IMG_ERR] Failed to load: " + resolvedUrl
                             + " | " + img.getException());
                     try {
                         imageView.setImage(getCachedImage(
                                 getClass().getResource("/images/logo.png").toExternalForm(), false));
                     } catch (Exception ignored) {}
                 }
             });
         } catch (Exception e) {
             try {
                 imageView.setImage(getCachedImage(
                         getClass().getResource("/images/logo.png").toExternalForm(), false));
             } catch (Exception ignored) {
             }
         }

         Rectangle clip = new Rectangle(300, 220);
         clip.setArcWidth(30);
         clip.setArcHeight(30);
         imageView.setClip(clip);

         // Heart (favorite) toggle
         Button heartBtn = new Button(favoriteIds.contains(d.getIdDestination()) ? "\u2764\uFE0F" : "\uD83E\uDD0D");
         heartBtn.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 20; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand; -fx-padding: 0;");
         StackPane.setAlignment(heartBtn, Pos.TOP_LEFT);
         StackPane.setMargin(heartBtn, new Insets(10, 0, 0, 10));
         heartBtn.setOnAction(e -> {
             int id = d.getIdDestination();
             if (favoriteIds.contains(id)) {
                 favoriteIds.remove(id);
                 heartBtn.setText("\uD83E\uDD0D");
             } else {
                 favoriteIds.add(id);
                 heartBtn.setText("\u2764\uFE0F");
             }
             if (showFavoritesOnly) { currentPage = 1; filterDestinations(); }
         });

         stackPane.getChildren().addAll(imageView, heartBtn);

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
         if (!mainSplitPane.getItems().contains(inspectorScrollPane)) {
             mainSplitPane.getItems().add(inspectorScrollPane);
             mainSplitPane.setDividerPositions(0.72);
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
         getWeatherService().getWeather(d.getNomDestination()).thenAccept(weather -> {
             Platform.runLater(() -> weatherLabel.setText("⛅ " + weather));
         });

         // Load photo gallery from Pexels
         galleryPane.getChildren().clear();
         galleryLabel.setText("Loading gallery...");
         String query = d.getNomDestination() + " " + d.getPays() + " travel";
         getPexelsService().searchImages(query, 6).thenAccept(urls -> {
             Platform.runLater(() -> {
                 galleryPane.getChildren().clear();
                 if (urls.isEmpty()) {
                     galleryLabel.setText("");
                 } else {
                     galleryLabel.setText("📸 Gallery — " + d.getNomDestination());
                     for (String url : urls) {
                         ImageView iv = new ImageView();
                         iv.setFitHeight(80);
                         iv.setFitWidth(110);
                         iv.setPreserveRatio(true);
                         iv.setSmooth(true);
                         Rectangle galClip = new Rectangle(110, 80);
                         galClip.setArcWidth(12);
                         galClip.setArcHeight(12);
                         iv.setClip(galClip);
                         iv.setImage(new Image(url, 110, 80, true, true, true));
                         iv.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");
                         // Click gallery thumbnail → open fullscreen
                         iv.setOnMouseClicked(e -> showFullscreenImage(url));
                         galleryPane.getChildren().add(iv);
                     }
                 }
             });
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
                 getPdfExportService().exportItinerary(title, content, file);
                 showOverlay("Success", "Itinerary successfully exported to:\n" + file.getAbsolutePath(), false, null);
             } catch (Exception e) {
                 showOverlay("Export Error", "Failed to create PDF: " + e.getMessage(), false, null);
             }
         }
     }

     @FXML
     public void closeInspectorDrawer() {
         mainSplitPane.getItems().remove(inspectorScrollPane);
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
     private void openMainImageFullscreen() {
         if (detailImageView.getImage() != null) {
             fullscreenImageView.setImage(detailImageView.getImage());
             fullscreenOverlay.setVisible(true);
         }
     }

     private void showFullscreenImage(String url) {
         fullscreenImageView.setImage(new Image(url, true));
         fullscreenOverlay.setVisible(true);
     }

     @FXML
     private void closeFullscreen() {
         fullscreenOverlay.setVisible(false);
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
                 getWeatherService().getWeather(detectedCity).thenAccept(weather -> {
                     getAiService().getTravelPlan(mood, finalContext, weather).thenAccept(plan -> {
                         Platform.runLater(() -> {
                             hideLoading();
                             showOverlay("Your Personalized Itinerary", plan, false, null);
                             exportPdfBtn.setVisible(true);
                         });
                     }).exceptionally(ex -> handleAiError(ex));
                 });
             } else {
                 getAiService().getTravelPlan(mood, finalContext, "No specific destination weather available.")
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
     private void handleAiImageMatch() {
         FileChooser fc = new FileChooser();
         fc.setTitle("Upload Image for AI Matching");
         fc.getExtensionFilters().addAll(
                 new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
         File file = fc.showOpenDialog(mainSplitPane.getScene().getWindow());
         if (file == null) return;

         showLoading("\uD83E\uDDE0 AI is analyzing the image...");

         getAiService().identifyImage(file).thenAccept(jsonResult -> {
             Platform.runLater(() -> {
                 hideLoading();
                 try {
                     com.google.gson.JsonObject result = new com.google.gson.Gson()
                             .fromJson(jsonResult, com.google.gson.JsonObject.class);

                     if (result.has("error")) {
                         showOverlay("AI Match", "Could not identify this image: " + result.get("error").getAsString(), false, null);
                         return;
                     }

                     String aiName = result.has("name") ? result.get("name").getAsString() : "";
                     String aiCountry = result.has("country") ? result.get("country").getAsString() : "";

                     if (aiName.isEmpty()) {
                         showOverlay("AI Match", "AI could not identify a place in this image.", false, null);
                         return;
                     }

                     // Fuzzy-match against cached destinations
                     Destination bestMatch = null;
                     int bestScore = 0;
                     String aiNameLower = aiName.toLowerCase();
                     String aiCountryLower = aiCountry != null ? aiCountry.toLowerCase() : "";

                     for (Destination d : cachedDestinations) {
                         String dbName = d.getNomDestination().toLowerCase();
                         String dbCountry = d.getPays().toLowerCase();
                         int score = 0;

                         if (dbName.equals(aiNameLower)) score += 100;
                         else if (dbName.contains(aiNameLower) || aiNameLower.contains(dbName)) score += 70;
                         else {
                             for (String word : aiNameLower.split("\\s+")) {
                                 if (word.length() > 2 && dbName.contains(word)) score += 30;
                             }
                         }

                         if (!aiCountryLower.isEmpty()) {
                             if (dbCountry.equals(aiCountryLower)) score += 25;
                             else if (dbCountry.contains(aiCountryLower) || aiCountryLower.contains(dbCountry)) score += 15;
                         }

                         if (score > bestScore) {
                             bestScore = score;
                             bestMatch = d;
                         }
                     }

                     if (bestMatch != null && bestScore >= 30) {
                         updateDetails(bestMatch, null);
                         showOverlay("AI Match", "\u2728 Matched: \"" + bestMatch.getNomDestination()
                                 + "\" in " + bestMatch.getPays() + "!\n\nAI identified: " + aiName + " (" + aiCountry + ")", false, null);
                     } else {
                         showOverlay("AI Match", "\uD83C\uDF0D AI identified: \"" + aiName + "\" (" + aiCountry
                                 + ")\n\nThis destination is not in the database yet.", false, null);
                     }
                 } catch (Exception e) {
                     showOverlay("AI Match Error", "Failed to parse AI response: " + e.getMessage(), false, null);
                 }
             });
         }).exceptionally(ex -> {
             Platform.runLater(() -> {
                 hideLoading();
                 showOverlay("AI Match Error", "AI analysis failed: " + ex.getMessage(), false, null);
             });
             return null;
         });
     }

     @FXML
     private void onVoiceSearchClick() {
         if (!isRecording) {
             try {
                 getVoiceService().startRecording("mood_recording.wav");
                 isRecording = true;
                 voiceBtn.setText("🔴");
                 voiceBtn.setStyle("-fx-background-color: rgba(255,0,0,0.2); -fx-background-radius: 50;");
             } catch (Exception e) {
                 showOverlay("Microphone Error", "Could not access microphone: " + e.getMessage(), false, null);
             }
         } else {
             getVoiceService().stopRecording();
             isRecording = false;
             voiceBtn.setText("🎙️");
             voiceBtn.setStyle("-fx-background-color: transparent;");

             showLoading("Transcribing your voice...");

             getAiService().transcribeAudio(getVoiceService().getAudioFile()).thenAccept(text -> {
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
                     getPdfExportService().exportBilletPdf("Billet Confirmation", pdfContent, file);
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

     @FXML
     private void handleHome() {
         showFavoritesOnly = false;
         if (favSidebarBtn != null) {
             favSidebarBtn.setStyle("");
         }
         currentPage = 1;
         filterDestinations();
     }
}
