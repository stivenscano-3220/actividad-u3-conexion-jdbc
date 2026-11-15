import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Contiene la lógica de validación de una conexión JDBC, sin depender de la
 * interfaz gráfica. Recibe los datos de conexión y devuelve un resultado.
 */
public class ProbadorConexion {

    /** Resultado de una prueba: indica si fue exitosa y el mensaje a mostrar. */
    public static class Resultado {
        private final boolean exitoso;
        private final String mensaje;

        public Resultado(boolean exitoso, String mensaje) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
        }

        public boolean isExitoso() {
            return exitoso;
        }

        public String getMensaje() {
            return mensaje;
        }
    }

    /** Intenta abrir y cerrar una conexión con los datos recibidos. */
    public Resultado probar(String url, String usuario, String password) {
        if (url == null || url.isBlank()) {
            return new Resultado(false, "Debe ingresar la URL de conexión.");
        }

        try {
            // Carga explícita del driver (puede lanzar ClassNotFoundException)
            String driver = obtenerDriver(url);
            if (driver != null) {
                Class.forName(driver);
            }

            // try-with-resources: la conexión se cierra sola, haya éxito o error
            try (Connection conexion = obtenerConexion(url, usuario, password)) {
                return new Resultado(true, "Conexión exitosa a: " + url);
            }

        } catch (ClassNotFoundException ex) {
            return new Resultado(false, "Error del driver: no se encontró la clase "
                    + ex.getMessage() + "\nVerifique que el .jar esté agregado al proyecto.");
        } catch (SQLException ex) {
            return new Resultado(false, "Fallo de conexión.\nDetalle técnico: " + ex.getMessage());
        }
    }

    private Connection obtenerConexion(String url, String usuario, String password)
            throws SQLException {
        if (usuario == null || usuario.isEmpty()) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, usuario, password);
    }

    /** Devuelve el nombre de la clase del driver según el prefijo de la URL. */
    private String obtenerDriver(String url) {
        if (url.startsWith("jdbc:sqlite:")) return "org.sqlite.JDBC";
        if (url.startsWith("jdbc:mysql:")) return "com.mysql.cj.jdbc.Driver";
        if (url.startsWith("jdbc:postgresql:")) return "org.postgresql.Driver";
        return null;
    }
}