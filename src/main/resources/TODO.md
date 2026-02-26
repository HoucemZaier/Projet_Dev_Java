# TODO: Enhance Login Interface Styling and Animations

- [x] Update login.css with professional styling (gradients, shadows, typography, button improvements)
- [x] Add animations and transitions (hover effects for buttons, focus styles for inputs, smooth transitions)
- [x] Ensure accessibility and responsiveness (better contrast, cursor changes)
- [x] Modify login.fxml to add "Forgot Password?" link and fix missing image (change to existing image or remove)
- [x] Change "Sign Up" to "Create Account" in login.fxml
- [x] Make login and create account windowed (not full screen) - requires controller update: set stage.setFullScreen(false) and stage.setMaximized(false)
- [x] Implement "Create Account" button in login controller to open create account window and close login
- [ ] Test the UI in JavaFX for functionality and appearance

# TODO: Make Create Account Page Same as Login

- [x] Update create.fxml to match login layout (same spacing, elements, buttons, social links)
- [x] Ensure input heights are the same as login (using same styleClass="input" with 45px height)
- [ ] Test the create account UI in JavaFX for consistency and appearance

# TODO: Beautify GestionUser Main Content with Animations (No TableView)

- [x] Replace TableView with a ScrollPane containing user cards (avatar, name, email, country, type, actions)
- [x] Add professional styling to user cards (shadows, gradients, hover effects)
- [x] Implement animations (card hover scale/translate, button hover, transitions)
- [x] Update dashboard.css with new styles for cards and animations
- [x] Enhance delete and edit buttons with better styling and animations
- [x] Make ComboBox beautiful and professional with animations
- [x] Add different colors for roles (moderateur/client/guide)
- [x] Make VBox accessible for detailed information (expandable card)
- [x] Change VBox to ListView for dynamic user list
- [x] Fix delete and edit button styles (icon-only, better gradients)
- [x] Ensure role colors are applied correctly
- [ ] Test the gestionUser UI in JavaFX for beauty and functionality
