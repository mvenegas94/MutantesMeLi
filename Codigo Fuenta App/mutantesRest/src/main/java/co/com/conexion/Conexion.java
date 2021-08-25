package co.com.conexion;

import static co.com.constantes.Constantes.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Autor: Miguel Angel Venegas Rodriguez Version: 1.0 Fecha: 21/08/2021
 * Descripcion: Clase encargada de gestionar la conexion a la base de datos
 */
public class Conexion {

	Connection conn;
	Logger logger = Logger.getLogger(Conexion.class.getName());

	public Conexion() {
		// Auto-generated constructor stub
	}

	/**
	 * Metodo encargado de establecer conexion con la base de datos AWS
	 * 
	 * @return
	 */
	public Connection obtenerConexion() {

		try {
			Class.forName(DRIVER);
			conn = DriverManager.getConnection(URL_BASE_DATOS, USUARIO, CLAVE);

		} catch (SQLException | ClassNotFoundException e) {
			logger.log(Level.INFO, "Error en la conexion de la base de datos " + e);
		}

		return conn;
	}

	/**
	 * Metodo encargado de cerrar la conexion con la base de datos AWS
	 */
	public void cerrarConexion() {
		try {
			conn.close();
		} catch (SQLException e) {
			logger.log(Level.INFO, "Error al cerra la conexion a la base de datos: " + e);
		}
	}
}
