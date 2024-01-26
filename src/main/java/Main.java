// ... (importaciones y otras clases)

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.sql.*;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        // Obtener información de la API de Clientify
        String apiResponse = obtenerInformacionDeAPI();

        // Convertir la respuesta JSON en objetos Java
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(apiResponse);
            // Lógica para mapear el JSON a tus clases (Contacto, Email, Phone, Address)
         //   List<Contacto> contactos = mappearJSONAContactos(jsonNode);

            // Guardar la información en la base de datos
           // guardarContactosEnBaseDeDatos(contactos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String obtenerInformacionDeAPI() {
        try {
            // URL de la API del CRM para obtener la lista de contactos
            String apiUrl = "https://api.clientify.net/v1/contacts/";

            // ... (Código para realizar la solicitud y obtener la respuesta)
            // ... (Ya lo tienes implementado en tu clase GET)

            return ""; // Reemplaza con la respuesta real de la API

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    /*
    private static List<Contacto> mappearJSONAContactos(JsonNode jsonNode) {
        // Lógica para mapear el JSON a instancias de la clase Contacto
        // ... (debes implementar esta lógica)
        // Puedes utilizar ObjectMapper para facilitar el mapeo

        return Collections.emptyList(); // Reemplaza con la lista real de contactos
    }

    private static void guardarContactosEnBaseDeDatos(List<Contacto> contactos) {
        Connection connection = null;

        try {
            // URL de conexión para SQLite con la ruta completa al archivo en el escritorio
            String url = "jdbc:sqlite:/C:\\Users\\jinsh\\Desktop\\prueba.db";

            // Conexión a la base de datos
            connection = DriverManager.getConnection(url);

            // Lógica para preparar y ejecutar las consultas SQL para insertar datos
            String insertContactoSQL = "INSERT INTO Contacto (first_name, last_name, title, company_name) VALUES (?, ?, ?, ?)";
            String insertEmailSQL = "INSERT INTO Email (contacto_id, email) VALUES (?, ?)";
            // ... (otros INSERTs para Phone y Address)

            try (PreparedStatement insertContactoStatement = connection.prepareStatement(insertContactoSQL);
                 PreparedStatement insertEmailStatement = connection.prepareStatement(insertEmailSQL)) {

                for (Contacto contacto : contactos) {
                    // Inserta en la tabla Contacto
                    insertContactoStatement.setString(1, contacto.getFirst_name());
                    insertContactoStatement.setString(2, contacto.getLast_name());
                    insertContactoStatement.setString(3, contacto.getTitle());
                    insertContactoStatement.setString(4, contacto.getCompany_name());
                    insertContactoStatement.executeUpdate();

                    // Obtiene el ID del contacto recién insertado
                    int contactoId = obtenerIdUltimoContactoInsertado(connection);

                    // Inserta en la tabla Email
                    for (Email email : contacto.getEmails()) {
                        insertEmailStatement.setInt(1, contactoId);
                        insertEmailStatement.setString(2, email.getEmail());
                        insertEmailStatement.executeUpdate();
                    }

                    // ... (otros bucles para Phone y Address)
                }
            }

            System.out.println("Datos guardados en la base de datos con éxito.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    // Cierra la conexión
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
*/
    private static int obtenerIdUltimoContactoInsertado(Connection connection) throws SQLException {
        // Obtiene el ID del último contacto insertado
        String selectLastContactoIdSQL = "SELECT last_insert_rowid()";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectLastContactoIdSQL);
             ResultSet resultSet = selectStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return -1; // Valor predeterminado si no se puede obtener el ID
        }
    }

    // ... (resto del código)
}
