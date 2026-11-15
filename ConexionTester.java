import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Aplicación de escritorio que permite probar la conexión a una base de datos
 * mediante JDBC, mostrando en pantalla el resultado de la prueba.
 */
public class ConexionTester extends JFrame {

    private static final Color VERDE = new Color(0, 128, 0);
    private static final Color ROJO = new Color(190, 0, 0);

    private final JTextField txtUrl = new JTextField("jdbc:sqlite:inventario.db");
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JButton btnProbar = new JButton("Probar conexión");
    private final JTextArea areaResultado = new JTextArea(8, 40);

    public ConexionTester() {
        super("Probador de conexión JDBC");
        construirInterfaz();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel de formulario (GridLayout: 3 filas, 2 columnas)
        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 5, 8));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        panelFormulario.add(new JLabel("URL de conexión:"));
        panelFormulario.add(txtUrl);
        panelFormulario.add(new JLabel("Nombre de usuario:"));
        panelFormulario.add(txtUsuario);
        panelFormulario.add(new JLabel("Contraseña:"));
        panelFormulario.add(txtPassword);

        // Panel con el botón
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnProbar);

        // Panel superior que agrupa formulario y botón
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelBoton, BorderLayout.SOUTH);

        // Área de resultados dentro de un JScrollPane
        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(areaResultado);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultado de la prueba"));

        add(panelNorte, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Evento del botón
        btnProbar.addActionListener(this::probarConexion);

        pack();
        setLocationRelativeTo(null);
    }

    private void probarConexion(ActionEvent e) {
        String url = txtUrl.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (url.isEmpty()) {
            mostrarResultado("Debe ingresar la URL de conexión.", ROJO);
            return;
        }

        try {
            // Carga explícita del driver (puede lanzar ClassNotFoundException)
            String driver = obtenerDriver(url);
            if (driver != null) {
                Class.forName(driver);
            }

            // try-with-resources: la conexión se cierra sola, haya éxito o error
            try (Connection conexion = obtenerConexion(url, usuario, password)) {
                mostrarResultado("Conexión exitosa a: " + url, VERDE);
            }

        } catch (ClassNotFoundException ex) {
            mostrarResultado("Error del driver: no se encontró la clase "
                    + ex.getMessage() + "\nVerifique que el .jar esté agregado al proyecto.", ROJO);
        } catch (SQLException ex) {
            mostrarResultado("Fallo de conexión.\nDetalle técnico: " + ex.getMessage(), ROJO);
        }
    }

    private Connection obtenerConexion(String url, String usuario, String password)
            throws SQLException {
        if (usuario.isEmpty()) {
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

    private void mostrarResultado(String mensaje, Color color) {
        areaResultado.setForeground(color);
        areaResultado.setText(mensaje);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConexionTester().setVisible(true));
    }
}