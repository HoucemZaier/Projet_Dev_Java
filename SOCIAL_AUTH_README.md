# Social Authentication Setup Guide

## Overview
This application supports Facebook and Google authentication for client users. Currently, it includes simulation features for testing. For production, follow these steps to integrate real OAuth.

## Facebook Integration

### 1. Create Facebook App
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app
3. Add "Facebook Login" product
4. Configure OAuth redirect URI: `http://localhost:8080/auth/facebook/callback`
5. Get your App ID and App Secret

### 2. Update Configuration
```java
// In SocialAuthService.java
private static final String FACEBOOK_APP_ID = "your_actual_facebook_app_id";
private static final String FACEBOOK_APP_SECRET = "your_actual_facebook_app_secret";
```

### 3. Required Dependencies (already added)
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

## Google Integration

### 1. Create Google OAuth App
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API and OAuth2 API
4. Create OAuth 2.0 credentials
5. Configure redirect URI: `http://localhost:8080/auth/google/callback`
6. Get your Client ID and Client Secret

### 2. Update Configuration
```java
// In SocialAuthService.java
private static final String GOOGLE_CLIENT_ID = "your_actual_google_client_id";
private static final String GOOGLE_CLIENT_SECRET = "your_actual_google_client_secret";
```

## Production OAuth Flow

### For Real Implementation:

1. **Replace Simulation Methods**: Remove `simulateFacebookOAuth()` and `simulateGoogleOAuth()`
2. **Add WebView Integration**: Use JavaFX WebView to handle OAuth flow
3. **Handle Redirects**: Capture OAuth callbacks and extract access tokens
4. **Secure Storage**: Store tokens securely (encrypted)

### Example OAuth URL Construction:

**Facebook:**
```
https://www.facebook.com/v18.0/dialog/oauth
?client_id={app-id}
&redirect_uri={redirect-uri}
&scope=email,public_profile
&response_type=code
```

**Google:**
```
https://accounts.google.com/oauth/v2/auth
?client_id={client-id}
&redirect_uri={redirect-uri}
&scope=openid email profile
&response_type=code
```

## Security Features Included

- ✅ Blocked user detection
- ✅ Email uniqueness validation
- ✅ Automatic client account creation
- ✅ Temporary password generation
- ✅ Status field integration
- ✅ Error handling for existing non-client accounts

## Client-Only Restriction

Social authentication is restricted to client accounts only:
- Creates new accounts as Client type
- Existing non-client accounts are rejected
- Blocked accounts cannot authenticate

## Testing

Current simulation allows testing with any email:
1. Click Facebook/Google login button
2. Enter test email in simulation dialog
3. System creates or authenticates client account
4. Redirects to explore interface

## Future Enhancements

- Add profile picture sync from social providers
- Implement refresh tokens
- Add account linking (connect social account to existing account)
- Enhanced error handling
- Rate limiting for authentication attempts
