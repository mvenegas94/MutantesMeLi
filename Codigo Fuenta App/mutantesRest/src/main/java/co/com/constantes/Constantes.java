package co.com.constantes;

/**
 * Autor: Miguel Angel Venegas Rodriguez Version: 1.0 Fecha: 21/08/2021
 * Descripcion: Clase encargada de gestionar las constantes del servicio
 */
public class Constantes {

	private Constantes() {
		// Constructor
	}

	public static final String REGEX_ATCG = "([^A|T|C|G])";
	public static final char A = 'A';
	public static final char T = 'T';
	public static final char C = 'C';
	public static final char G = 'G';
	public static final String USUARIO = "meli";
	public static final String CLAVE = "abc12345678";
	public static final String URL_BASE_DATOS = "jdbc:mysql://mutantes.cpnf8imbovoc.us-east-2.rds.amazonaws.com:3306/MUTANTES_MELI";
	public static final String DRIVER = "com.mysql.cj.jdbc.Driver";

}
