package be.kuleuven.distributedsystems.cloud.auth;

import org.json.JSONObject;

import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Configuration
public class GooglePublicKeysFetcher {
    private Map<String, RSAPublicKey> publicKeys = new HashMap<>();

    public GooglePublicKeysFetcher() {
        fetchAndCachePublicKeys();
    }

    public List<RSAPublicKey> getPublicKeys() {
        return publicKeys.values().stream().toList();
    }

    public RSAPublicKey getPublicKeyById(String kid) {
        return publicKeys.get(kid);
    }

    public void fetchAndCachePublicKeys() {
        try {
            String serviceAccountEmail = "securetoken@system.gserviceaccount.com";
            String url = "https://www.googleapis.com/robot/v1/metadata/x509/" + serviceAccountEmail;

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());

                Iterator<String> keysIterator = jsonResponse.keys();
                while (keysIterator.hasNext()) {
                    String kid = keysIterator.next();
                    String value = jsonResponse.getString(kid);
                    publicKeys.put(kid, (RSAPublicKey) convertToRSAPublicKey(value));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static PublicKey convertToRSAPublicKey(String key) throws Exception {
        key = key.replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");  // Remove any whitespace

        byte[] certBytes = Base64.getDecoder().decode(key);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
        return cert.getPublicKey();
    }
}
