// ... (importaciones y otras clases)

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.*;
import java.util.Collections;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws MalformedURLException {
        Scanner sc = new Scanner(System.in);
        menu();
        int option = sc.nextInt();
        while (option != 0) {
            switch (option) {
                case 1:
                    realizarGET();
                    break;
                case 2:
                    addContact(sc);
                    break;
                case 3:
            }
            menu();
            option = sc.nextInt();
        }
    }

    public static void menu() {
        System.out.println("Choose your options: " + "\n" +
                "1. Import contatacts to DB\n" +
                "2. Create new contact\n" +
                "0. Done");
    }

    public static Connection connectionSQLite() {
        Connection connection = null;

        try {
            String url = "jdbc:sqlite:/C:\\Users\\jinsh\\Desktop\\prueba.db";
            connection = DriverManager.getConnection(url);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static void realizarPOST(String apiUrl, String jsonInputString){
        try {
            // Crea una URL y establece la conexión
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configura la conexión para el método POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Token c5b6325daa345e3c6d50c57bed7d52f3633a198b");
            connection.setDoOutput(true);

            // Envia los datos del nuevo contacto en el cuerpo de la solicitud
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Obtiene la respuesta del servidor
            int responseCode = connection.getResponseCode();

            // Lee la respuesta del servidor
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();

                System.out.println("Contact added to Clientify successfully.");
            } else {
                System.out.println("Error adding contact to Clientify. Response code: " + responseCode);
            }
            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Error: Malformed URL - " + e.getMessage());
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while connecting to Clientify API.");
        }
    }



    public static void realizarGET() throws MalformedURLException {
        try {
            String apiUrl = "https://api.clientify.net/v1/contacts/";

            while (apiUrl != null) {
                // Crea una URL y establece la conexión
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Token c5b6325daa345e3c6d50c57bed7d52f3633a198b");

                // Obtiene la respuesta del servidor
                int responseCode = connection.getResponseCode();

                // Lee la respuesta del servidor
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();

                    // Formatea la respuesta JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(response.toString());
                    String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

                    //System.out.println("Respuesta del servidor:\n" + prettyJson);


                    insertarContactosEnDB(jsonNode, connectionSQLite());

                    // Obtiene la URL de la siguiente página
                    JsonNode nextNode = jsonNode.get("next");
                    apiUrl = (nextNode != null) ? nextNode.asText() : null;

                    // Cierra la conexión
                    connection.disconnect();
                } else {
                    System.out.println("Error al realizar la solicitud. Código de respuesta: " + responseCode);
                    break;
                }
                System.out.println("Insertando contactos en la base de datos");
            }
            System.out.println("Contactos insertados en la base de datos correctamente.");

        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Error: Malformed URL - " + e.getMessage());
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al abrir la conexión.");
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

                JsonNode createdNode = contactoNode.get("created");
                String created = (createdNode != null) ? createdNode.asText() : "";

                String sql = "INSERT OR REPLACE INTO contactos (id, first_name, last_name, title, company_name, phone, email, country, created) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, id);
                    preparedStatement.setString(2, nombre);
                    preparedStatement.setString(3, apellido);
                    preparedStatement.setString(4, title);
                    preparedStatement.setString(5, companyName);
                    preparedStatement.setString(6, phoneNumber);
                    preparedStatement.setString(7, email);
                    preparedStatement.setString(8, address);
                    preparedStatement.setString(9, created);

                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Error al insertar el contacto en la base de datos.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addContact(Scanner sc) {
        System.out.println("Enter first name");
        String firstName = sc.next();

        System.out.println("Eneter last name");
        String lastName = sc.next();

        System.out.println("Enter title:");
        String title = sc.next();

        System.out.println("Enter company name:");
        String companyName = sc.next();

        System.out.println("Enter phone number:");
        String phoneNumber = sc.next();

        System.out.println("Enter email:");
        String email = sc.next();

        System.out.println("Enter country:");
        String country = sc.next();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("first_name", firstName);
        jsonNode.put("last_name", lastName);
        jsonNode.put("title", title);
        jsonNode.put("company", companyName);
        jsonNode.put("phone", phoneNumber);
        jsonNode.put("email", email);
        jsonNode.put("country", country);
        String jsonInputString;
        try {
            jsonInputString = objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("Error converting JSON to string.");
            return;
        }
        realizarPOST("https://api.clientify.net/v1/contacts/", jsonInputString);

        try (Connection connection = connectionSQLite()) {
            String sql = "INSERT INTO contactos (first_name, last_name, title, company_name, phone, email, country, created) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, title);
                preparedStatement.setString(4, companyName);
                preparedStatement.setString(5, phoneNumber);
                preparedStatement.setString(6, email);
                preparedStatement.setString(7, country);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Contact added successfully.");
                } else {
                    System.out.println("Failed to add contact to the local database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error while connecting to the local database.");
        }
    }
}