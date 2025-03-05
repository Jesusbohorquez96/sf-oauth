package com.sf.SAMLAssertionGen.GenerateSamlToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class GenerateToken {

    public static String getOAuthToken(String oauthTokenUrl, String clientId, String companyId, String userId, String privateKey) throws Exception {
        String signedSAMLAssertion = GenerateSaml.generateSignedSAMLAssertion(clientId, userId, oauthTokenUrl, privateKey);

        if (signedSAMLAssertion == null || signedSAMLAssertion.isEmpty()) {
            throw new RuntimeException("Failed to generate SAML Assertion");
        }
        String postData = "company_id=" + companyId +
                "&client_id=" + clientId +
                "&grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer" +
                "&assertion=" + signedSAMLAssertion;

        URL url = new URL(oauthTokenUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return extractToken(response);
        } else {
            throw new RuntimeException("Error in OAuth request: HTTP " + responseCode + "\n" + response);
        }
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static String extractToken(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.optString("access_token", "Token not found in response");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token from response: " + jsonResponse, e);
        }
    }
}
