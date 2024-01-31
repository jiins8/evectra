// ... (importaciones y otras clases)

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        realizarGET();
    }


    public static Connection connectionSQLite() {
        Connection connection = null;

        try {
            // URL de conexión para SQLite con la ruta completa al archivo en el escritorio
            String url = "jdbc:sqlite:/C:\\Users\\jinsh\\Desktop\\prueba.db";

            // Conexión a la base de datos
            connection = DriverManager.getConnection(url);

            // Imprime un mensaje cuando la conexión se ha establecido con éxito
            System.out.println("Conexión establecida con éxito.");

            // Realiza operaciones en la base de datos...

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection; // Devuelve la conexión
    }


    public static void realizarGET() {
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

                // Formatea la respuesta JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

                // Imprime la respuesta formateada
                System.out.println("Respuesta del servidor:\n" + prettyJson);
                insertarContactosEnDB(jsonNode, connectionSQLite());
                connection.disconnect();
            } else {
                System.out.println("Error al realizar la solicitud. Código de respuesta: " + responseCode);
            }

            // Cierra la conexión
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertarContactosEnDB(JsonNode jsonNode, Connection connection) {
        try {
            // Verifica si la conexión a la base de datos es nula
            if (connection == null) {
                System.out.println("Error: La conexión a la base de datos es nula.");
                return;
            }

            // Obtiene el array de contactos del JSON
            JsonNode contactosArray = jsonNode.get("results");

            // Verifica si el array de contactos es nulo
            if (contactosArray == null || !contactosArray.isArray()) {
                System.out.println("Error: El JSON no contiene un array de contactos válido.");
                return;
            }

            // Itera a través de los contactos y realiza la inserción en la base de datos
            for (JsonNode contactoNode : contactosArray) {
                // Extrae la información del contacto
                JsonNode idNode = contactoNode.get("id");
                int id = (idNode != null) ? idNode.asInt() : 0;

                JsonNode nombreNode = contactoNode.get("first_name");
                String nombre = (nombreNode != null) ? nombreNode.asText() : "";

                JsonNode apellidoNode = contactoNode.get("last_name");
                String apellido = (apellidoNode != null) ? apellidoNode.asText() : "";

                JsonNode titleNode = contactoNode.get("title");
                String title = (titleNode != null) ? titleNode.asText() : "";

                JsonNode companyNameNode = contactoNode.path("company_name");
                String companyName = (companyNameNode != null) ? companyNameNode.asText() : "";

                JsonNode phonesArrayNode = contactoNode.path("phones");
                String phoneNumber = "";
                if (phonesArrayNode.isArray() && phonesArrayNode.size() > 0) {
                    JsonNode phoneNode = phonesArrayNode.get(0).get("phone");
                    phoneNumber = (phoneNode != null) ? phoneNode.asText() : "";
                }

                JsonNode emailsArrayNode = contactoNode.path("emails");
                String email = "";
                if (emailsArrayNode.isArray() && emailsArrayNode.size() > 0) {
                    JsonNode emailNode = emailsArrayNode.get(0).get("email");
                    email = (emailNode != null) ? emailNode.asText() : "";
                }

                JsonNode addressesArrayNode = contactoNode.path("addresses");
                String address = "";
                if (addressesArrayNode.isArray() && addressesArrayNode.size() > 0) {
                    JsonNode addressNode = addressesArrayNode.get(0).get("country");
                    address = (addressNode != null) ? addressNode.asText() : "";
                }

                // Prepara la consulta SQL para insertar el contacto en la base de datos
                String sql = "INSERT OR REPLACE INTO contactos (id, first_name, last_name, title, company_name, phone, email, country) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1,id);
                    preparedStatement.setString(2, nombre);
                    preparedStatement.setString(3, apellido);
                    preparedStatement.setString(4, title);
                    preparedStatement.setString(5, companyName);
                    preparedStatement.setString(6, phoneNumber);
                    preparedStatement.setString(7, email);
                    preparedStatement.setString(8, address);

                    // ... establece más parámetros según la estructura de tu base de datos y JSON

                    // Ejecuta la consulta
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Error al insertar el contacto en la base de datos.");
                }
            }
            System.out.println("Contactos insertados en la base de datos correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}