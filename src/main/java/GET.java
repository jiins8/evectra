import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// ... (resto de las importaciones)

public class GET {

    public static void main(String[] args) {
        try {
            // URL de la API del CRM para obtener la lista de contactos
            String apiUrl = "https://api.clientify.net/v1/contacts/";

            // Crea una URL y establece la conexión
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configura el método de la solicitud (GET)
            connection.setRequestMethod("GET");

            // Establece las cabeceras de la solicitud si es necesario (por ejemplo, autenticación)
            connection.setRequestProperty("Authorization", "Token c5b6325daa345e3c6d50c57bed7d52f3633a198b");

            // Obtiene la respuesta del servidor
            int responseCode = connection.getResponseCode();

            // Lee la respuesta del servidor
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();

                // Formatea la respuesta JSON para mejor legibilidad
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

                // Imprime la respuesta formateada
                System.out.println("Respuesta del servidor:\n" + prettyJson);
            } else {
                System.out.println("Error al realizar la solicitud. Código de respuesta: " + responseCode);
            }

            // Cierra la conexión
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
