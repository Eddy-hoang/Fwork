export const DEFAULT_GOOGLE_CLIENT_ID = "128615141346-shmjhu5cf93essj1b4dg53plntldqa38.apps.googleusercontent.com";

/**
 * Returns the Chrome Extension OAuth redirect URL (https://<extension-id>.chromiumapp.org/)
 */
export function getGoogleRedirectUri(): string {
  if (typeof chrome !== 'undefined' && chrome.identity && chrome.identity.getRedirectURL) {
    return chrome.identity.getRedirectURL();
  }
  return 'https://extension-id.chromiumapp.org/';
}

/**
 * Initiates the Google OAuth2 web authentication flow and extracts the Google ID Token.
 */
export async function getGoogleIdToken(clientId = DEFAULT_GOOGLE_CLIENT_ID): Promise<string> {
  if (typeof chrome === 'undefined' || !chrome.identity || !chrome.identity.launchWebAuthFlow) {
    throw new Error('Google Sign-In is only supported when running inside Google Chrome.');
  }

  const redirectUri = chrome.identity.getRedirectURL();
  const nonce = Math.random().toString(36).substring(2) + Date.now().toString(36);

  const authUrl = new URL('https://accounts.google.com/o/oauth2/v2/auth');
  authUrl.searchParams.set('client_id', clientId);
  authUrl.searchParams.set('response_type', 'id_token');
  authUrl.searchParams.set('redirect_uri', redirectUri);
  authUrl.searchParams.set('scope', 'openid email profile');
  authUrl.searchParams.set('nonce', nonce);
  authUrl.searchParams.set('prompt', 'select_account');

  const responseUrl = await new Promise<string>((resolve, reject) => {
    chrome.identity.launchWebAuthFlow(
      {
        url: authUrl.toString(),
        interactive: true,
      },
      (redirectResult) => {
        if (chrome.runtime.lastError) {
          return reject(new Error(chrome.runtime.lastError.message));
        }
        if (!redirectResult) {
          return reject(new Error('Google sign-in was cancelled.'));
        }
        resolve(redirectResult);
      }
    );
  });

  // Extract id_token from the hash fragment: #id_token=...&authuser=0
  const hashPart = responseUrl.split('#')[1] || '';
  const searchParams = new URLSearchParams(hashPart);
  const idToken = searchParams.get('id_token');

  if (!idToken) {
    const errorMsg = searchParams.get('error') || 'Failed to extract Google ID token.';
    throw new Error(`Google Auth error: ${errorMsg}`);
  }

  return idToken;
}
