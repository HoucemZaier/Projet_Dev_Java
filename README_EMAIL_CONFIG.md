# Configuration Email - PlaNova Transport

## 📧 Instructions de Configuration

### 1. Configuration Gmail

Pour que l'envoi d'emails fonctionne, vous devez configurer un compte Gmail avec authentification en deux étapes :

#### Étapes :
1. **Créez un compte Gmail** (ou utilisez-en un existant)
2. **Activez l'authentification en deux étapes** :
   - Allez dans les paramètres Google
   - Section "Sécurité"
   - Activez "Validation en deux étapes"
3. **Générez un mot de passe d'application** :
   - Dans la même section "Sécurité"
   - Cliquez sur "Mots de passe des applications"
   - Sélectionnez "Autre (nom personnalisé)"
   - Donnez un nom (ex: "PlaNova Transport")
   - Copiez le mot de passe généré (16 caractères)

### 2. Configuration dans l'Application

#### Option 1: Via l'interface graphique
1. Lancez l'application
2. Allez dans le menu "Configuration" → "Paramètres Email"
3. Entrez votre email Gmail
4. Entrez le **mot de passe d'application** (pas votre mot de passe normal)
5. Cliquez sur "Tester la connexion"
6. Si le test réussit, cliquez sur "Sauvegarder"

#### Option 2: Manuellement
Le fichier `email_config.properties` sera créé automatiquement avec ce format :
```properties
smtp.host=smtp.gmail.com
smtp.port=587
email.username=votre_email@gmail.com
email.password=votre_mot_de_passe_application
email.from=PlaNova Transport <votre_email@gmail.com>
```

### 3. Dépendances Maven

Assurez-vous que votre `pom.xml` contient les dépendances JavaMail :
```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>
```

### 4. Sécurité

- ✅ Les mots de passe sont stockés localement
- ✅ Utilisation de TLS pour la connexion sécurisée
- ✅ Validation des emails avant envoi
- ✅ Messages d'erreur détaillés pour le débogage

### 5. Test de Configuration

Pour tester si tout fonctionne :
```java
EmailService emailService = new EmailService();
if (emailService.testConnection()) {
    System.out.println("✅ Configuration email valide");
} else {
    System.out.println("❌ Configuration email invalide");
}
```

### 6. Dépannage

#### Problèmes courants :
- **"Authentication failed"** : Vérifiez que vous utilisez un mot de passe d'application, pas votre mot de passe Gmail
- **"Connection timed out"** : Vérifiez votre connexion internet et les paramètres firewall
- **"535-5.7.8 Username and Password not accepted"** : Activez l'authentification en deux étapes et générez un nouveau mot de passe d'application

#### Port utilisé :
- **Port 587** avec STARTTLS (recommandé)
- **Port 465** avec SSL/TLS (alternative)

### 7. Support

Pour toute question sur la configuration email :
- 📧 Email technique : support@planova.tn
- 📞 Téléphone : +216 XX XXX XXX
- 🕐 Horaires support : Lun-Ven 8h-18h

---

## 📋 Fichiers Concernés

### Nouveaux fichiers créés :
1. `src/main/resources/LocationDialog.fxml` - Interface de location
2. `src/main/java/Controllers/LocationDialogController.java` - Contrôleur de location
3. `src/main/java/utils/Services/EmailService.java` - Service d'envoi d'emails
4. `src/main/java/utils/Services/EmailConfigService.java` - Gestion configuration email
5. `src/main/resources/EmailConfigDialog.fxml` - Interface configuration email
6. `src/main/java/Controllers/EmailConfigDialogController.java` - Contrôleur configuration email

### Fichiers modifiés :
1. `src/main/java/Controllers/AfficheClientPrive.java` - Ajout fonctionnalité location
2. `pom.xml` - Ajout dépendances JavaMail

### Fichiers existants utilisés :
1. `src/main/java/Models/TransportPrive.java` - Modèle de données
2. `src/main/java/utils/Services/ServiceTransportPrive.java` - Service base de données

---

## 🚀 Fonctionnalités Implémentées

### ✅ Interface Client
- Mini-interface de location avec design professionnel
- Validation d'email en temps réel
- Bouton valider bleu comme demandé
- Messages de confirmation intégrés

### ✅ Envoi Email
- Email HTML professionnel et moderne
- Informations complètes du véhicule
- Date de location automatique
- Footer "All right reserved by PlaNova"

### ✅ Gestion Base de Données
- Mise à jour automatique de l'état à "indisponible"
- Gestion des erreurs de base de données
- Rechargement automatique des données

### ✅ Sécurité
- Configuration email sécurisée
- Validation des entrées utilisateur
- Gestion des erreurs robuste

---

## 🎯 Utilisation

1. **Configurer l'email** une première fois
2. **Lancer l'application**
3. **Aller dans "Location de transports privés"**
4. **Cliquer sur "Louer Voiture"** pour un véhicule disponible
5. **Entrer l'email** et valider
6. **Recevoir la confirmation** par email
7. **Le véhicule devient indisponible** automatiquement

L'application est maintenant **prête à l'emploi** avec toutes les fonctionnalités demandées ! 🎉
