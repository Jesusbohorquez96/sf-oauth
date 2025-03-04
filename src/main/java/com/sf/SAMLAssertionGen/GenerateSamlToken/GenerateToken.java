package com.sf.SAMLAssertionGen.GenerateSamlToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class GenerateToken {

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        String configPath = "config.properties";

        try (InputStream input = new FileInputStream(configPath)) {
            properties.load(input);
        }
        return properties;
    }

    public static String getOAuthToken(String signedSAMLAssertion) throws Exception {
        Properties properties = loadProperties();
        String tokenUrl = properties.getProperty("tokenUrl");
        String clientId = properties.getProperty("clientId");
        String companyId = properties.getProperty("companyId");

        if (tokenUrl == null || clientId == null || companyId == null || signedSAMLAssertion == null) {
            throw new RuntimeException("One or more required parameters are missing for OAuth token request.");
        }

        String postData = "company_id=" + companyId +
                "&client_id=" + clientId +
                "&grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer" +
                "&assertion=" + signedSAMLAssertion;

        URL url = new URL(tokenUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new RuntimeException("Error in OAuth request: HTTP " + responseCode + "\n" + errorResponse);
            }
        }
    }
}

