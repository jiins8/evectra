import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionSQL {

    public static void main(String[] args) {
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
}
