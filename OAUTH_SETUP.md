# 🔐 OAuth Configuration Setup

## Overview
This project uses Facebook and Google OAuth for social authentication. For security reasons, OAuth credentials are stored in a separate configuration file that is **not committed to GitHub**.

## 🚀 Quick Setup

### Step 1: Create OAuth Configuration File
1. Navigate to `src/main/resources/`
2. Copy `oauth.properties.template` to `oauth.properties`
3. Edit `oauth.properties` with your actual credentials

### Step 2: Configure Facebook OAuth
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create or select your app
3. Get your **App ID** and **App Secret**
4. Add them to `oauth.properties`:
   ```properties
   facebook.app.id=YOUR_ACTUAL_FACEBOOK_APP_ID
   facebook.app.secret=YOUR_ACTUAL_FACEBOOK_APP_SECRET
   ```

### Step 3: Configure Google OAuth
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Navigate to APIs & Services > Credentials
3. Get your **Client ID** and **Client Secret**
4. Add them to `oauth.properties`:
   ```properties
   google.client.id=YOUR_ACTUAL_GOOGLE_CLIENT_ID
   google.client.secret=YOUR_ACTUAL_GOOGLE_CLIENT_SECRET
   ```

## 📂 File Structure
```
src/main/resources/
├── oauth.properties          ← Your actual credentials (NOT in GitHub)
└── oauth.properties.template ← Template file (safe for GitHub)
```

## ⚠️ Security Notes
- **NEVER** commit `oauth.properties` to GitHub
- The file is already added to `.gitignore`
- Use the template file as reference
- Keep your credentials secure and don't share them

## 🔧 Testing
After configuration, the application will:
- ✅ Load credentials securely from `oauth.properties`
- ✅ Validate all credentials are present
- ✅ Show error messages if credentials are missing
- ✅ Work with both Facebook and Google OAuth

## 🚨 Troubleshooting
If you see OAuth configuration errors:
1. Check that `oauth.properties` exists in `src/main/resources/`
2. Verify all credentials are filled in (not YOUR_ACTUAL_...)
3. Ensure no extra spaces or special characters
4. Restart the application after making changes

## 📋 Example oauth.properties
```properties
# Facebook OAuth Configuration
facebook.app.id=1234567890123456
facebook.app.secret=abcdef1234567890abcdef1234567890

# Google OAuth Configuration  
google.client.id=123456789012-abcdefghijklmnop.apps.googleusercontent.com
google.client.secret=GOCSPX-AbCdEfGhIjKlMnOpQrStUvWx
google.scope=openid email profile
```
