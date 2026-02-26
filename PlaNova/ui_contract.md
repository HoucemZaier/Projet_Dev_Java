# UI Contract: Explore View

## Introduction
This document defines the CSS classes and IDs used in the new `explore.fxml` designed for a desktop layout. It serves to maintain a contract between the FXML view and its backing CSS.

## General Structure
The view is a `BorderPane` acting as a 3-pane layout for desktop.
- **Top**: Navbar (`#glass-navbar` class)
- **Left**: Sidebar (`#glass-sidebar` class)
- **Center**: Main discover area (`Scrollpane` wrapping a `FlowPane`)
- **Right**: Inspector Drawer (`#inspectorDrawer` ID, `.glass-drawer` class)

## CSS Classes

### Structural
*   `.root-pane`: Applied to the base `BorderPane`. Sets the main global linear-gradient nature theme background (`#1B3A20` to `#2D5A27`).

### Glassmorphism Regions
These classes apply the signature semi-transparent look (`rgba(255, 255, 255, 0.15)`) with subtle borders. *Note: FXML applies a `<GaussianBlur>` effect node to the content behind these elements.*
*   `.glass-navbar`: Applied to the top `HBox`.
*   `.glass-sidebar`: Applied to the left `VBox`.
*   `.glass-drawer`: Applied to the right `VBox`.

### Controls
*   `.glass-search`: Applied to the main `TextField` Search bar. Slightly more opaque (`rgba(..., 0.2)`) and uses `#A3B18A` outline on focus.

### Cards (`.nature-card`)
The FlowPane holds several repeating structures for activities/destinations.
*   `.nature-card`: Applied to the root `VBox` of each item. Features white semi-transparent background, rounding, and DropShadow. 
    *   *Hover State*: Applies a slightly larger drop shadow (`rgba(0,0,0,0.3)`) and negative Y translation (`-fx-translate-y: -8`).
*   `.card-image`: Applied to the `ImageView` placeholder bounds.
*   `.card-title`: Applied to the `Label` heading (`#2D5A27`).
*   `.card-info`: Applied to the subtext `Label` describing the activity type/duration (`#D4A373`).
*   `.card-price`: Applied to the pricing `Label`.

## Component IDs (For Controller Binding)
*   `#inspectorDrawer`: Binds to the right-hand `VBox` drawer. The controller should be responsible for managing a `TranslateTransition` on this node to slide it in and out of view horizontally.
* *Note for Controller Development*: Implementing card zoom/swipes or scale transitions for the checkmarks should select the individual nodes built dynamically or explicitly added with new IDs as needed.
