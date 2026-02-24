# OAuth Implementation Setup

## Facebook OAuth Setup ✅ CONFIGURED

Your Facebook App is already configured with:
- **App ID:** 925022893757906
- **App Secret:** e93e7c5a45f1c523b353a70bdaf5b5b9

**CRITICAL:** You need to add this specific redirect URI to your Facebook App settings:
1. Go to https://developers.facebook.com/apps/925022893757906/fb-login/settings/
2. Navigate to "Facebook Login" → "Settings"  
3. Add this exact redirect URI:
   ```
   https://127.0.0.1:8081/auth/facebook/callback
   ```
4. Make sure the app is in "Live" mode (not Development mode)
5. Save the settings

**Note:** The application now prioritizes port 8081 to match your Facebook app configuration.

## Google OAuth Setup ⚠️ NEEDS CONFIGURATION

To enable Google authentication, you need to:

1. **Create a Google Project:**
   - Go to https://console.developers.google.com/
   - Create a new project or select existing one

2. **Enable Google+ API:**
   - Go to "APIs & Services" → "Library"
   - Search for "Google+ API" and enable it

3. **Create OAuth Credentials:**
   - Go to "APIs & Services" → "Credentials"
   - Click "Create Credentials" → "OAuth 2.0 Client ID"
   - Choose "Web application"
   - Add Authorized redirect URI: `https://127.0.0.1:8080/auth/google/callback`

4. **Update Code:**
   - Replace `"your_google_client_id"` with your actual Google Client ID in `SocialAuthService.java`

## How It Works

1. **User clicks Facebook/Google button**
2. **HTTPS callback server starts** on `127.0.0.1:8080`
3. **Browser opens** OAuth URL (Facebook/Google login page)
4. **User authenticates** with their Facebook/Google account
5. **OAuth provider redirects** to our HTTPS callback server
6. **Server receives** authorization code
7. **Application creates/finds** user account in database
8. **User is logged in** and redirected to explore interface

## Security Notes

- Uses HTTPS for OAuth callbacks (required by Facebook/Google)
- Self-signed certificate for development (browser may show warning)
- In production, use proper SSL certificate
- App secrets should be stored in environment variables

## Testing

You can test the OAuth flow with:
- Real Facebook/Google accounts
- The demo dialog will appear after OAuth to enter user info
- User accounts are created automatically in your database
- Email uniqueness is enforced
