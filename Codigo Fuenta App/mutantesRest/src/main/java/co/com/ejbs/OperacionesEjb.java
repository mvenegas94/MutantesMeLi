package co.com.ejbs;

import static co.com.constantes.Constantes.A;
import static co.com.constantes.Constantes.C;
import static co.com.constantes.Constantes.G;
import static co.com.constantes.Constantes.REGEX_ATCG;
import static co.com.constantes.Constantes.T;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.com.conexion.Conexion;
import co.com.pojos.Salida;
import co.com.pojos.SalidaServicioGet;

/**
 * Autor: Miguel Angel Venegas Rodriguez Version: 1.0 Fecha: 21/08/2021
 * Descripcion: Clase encargada de realizar las diversas operaciones de logica
 * de negocio
 */

public class OperacionesEjb {

	private char[][] matrizDectectada;
	private HashMap<Integer, Integer> mapaColumnas;
	private HashMap<String, Integer[]> mapaDiagonalesDerechaSubiendo;
	private HashMap<String, Integer[]> mapaDiagonalesDerechaBajando;
	String cadenaADN;
	Conexion conexion;
	Logger logger = Logger.getLogger(OperacionesEjb.class.getName());

	/**
	 * Default constructor.
	 */
	public OperacionesEjb() {
		conexion = new Conexion();
	}

	/**
	 * Metodo que se encarga de realizar los llamados para encontrar mutantes segun
	 * su cadena de ADN
	 * 
	 * @param dna --> String[]
	 * @return respuestaServicio --> boolean
	 */
	public boolean isMutant(String[] dna) {
		Salida respuestaServicio = new Salida();
		cadenaADN = "";
		try {

			// Se realizan validaciones necesarias para determinar si la cdena de ADN es
			// valida
			Object[] respuestaValidaciones = validacionesIniciales(dna);
			boolean peticionValida = (boolean) respuestaValidaciones[0];
			String mensajeValidaciones = (String) respuestaValidaciones[1];

			if (peticionValida) {
				// Como la cadena de ADN enviada es valida, se revisa si la cadena ya fue
				// analizada, consultandola en base de datos
				int existeCadenaAdn = existeCadenaAdn(cadenaADN);

				if (existeCadenaAdn == 0 || existeCadenaAdn == 1) {
					respuestaServicio.setEsMutante(existeCadenaAdn == 1);
					respuestaServicio.setDescripcion("Cadena de AND ya existe en la base de datos.");
					return respuestaServicio.isEsMutante();
				}

				// Si la cadena de ADN es valida, y no existe en base de datos, se busca si es
				// mutante
				Object[] respuestaCoincidencias = buscarCoincidencias();
				boolean esmutante = (boolean) respuestaCoincidencias[0];
				String mensajeEsMutaante = (String) respuestaCoincidencias[1];
				respuestaServicio.setEsMutante(esmutante);
				respuestaServicio.setDescripcion(mensajeEsMutaante);

				// Se realiza insercion de cadena ADN analizada
				insertTableMutantes(respuestaServicio.isEsMutante(), cadenaADN);

			} else {
				respuestaServicio.setEsMutante(peticionValida);
				respuestaServicio.setDescripcion(mensajeValidaciones);
			}

		} catch (Exception e) {
			respuestaServicio.setEsMutante(false);
			respuestaServicio.setDescripcion("Se perento un error en el consumo del servicio --> " + e);

			if (conexion != null) {
				conexion.cerrarConexion();
			}

		}

		// Para este caso, solo retorno el boolean segun lo solicitado, sin embargo
		// tambien se deja un mensaje informativo, por si se deseara mostrar y tambien
		// se deja creada la clase de Salida.
		return respuestaServicio.isEsMutante();
	}

	/**
	 * Metodo que se encarga de realizar un insercion en la tabla TABLA_MUTANTES
	 * 
	 * @param esMutante
	 * @param cadenaADN
	 */
	public void insertTableMutantes(boolean esMutante, String cadenaADN) {
		Statement stmt;
		try {
			// Establecemos conexion con la base de datos
			stmt = conexion.obtenerConexion().createStatement();
			stmt.executeUpdate("INSERT INTO TABLA_MUTANTES (ES_MUTANTE, CADENA_ADN) VALUES (" + esMutante + ", '"
					+ cadenaADN + "')");
			conexion.cerrarConexion();
		} catch (SQLException e) {
			logger.log(Level.INFO, "Error al insertar cadena de ADN en base de datos " + e);
			conexion.cerrarConexion();

		} finally {
			conexion.cerrarConexion();
		}
	}

	/**
	 * Metodo encargado de validar si ya se valido una cadena con antelacion, para
	 * no volver a analizarla 0 = Es humano 1 = Es mutante 2 = No existe en la base
	 * de datos
	 * 
	 * @param cadenaADN
	 * @return
	 */
	public int existeCadenaAdn(String cadenaADN) {
		int salida = 2;
		Statement stmt;
		try {
			String sentencia = "SELECT * FROM TABLA_MUTANTES WHERE CADENA_ADN = '" + cadenaADN + "'";

			stmt = conexion.obtenerConexion().createStatement();
			ResultSet rs = stmt.executeQuery(sentencia);

			int contador = 0;
			while (rs.next()) {
				boolean esMutante = rs.getBoolean("ES_MUTANTE");
				contador++;
				salida = esMutante ? 1 : 0;
			}

			if (contador == 0) {
				return 2;
			}

			conexion.cerrarConexion();
			return salida;
		} catch (SQLException e) {
			logger.log(Level.INFO, "Error al consultar la cadena de ADN en base de datos " + e);
			conexion.cerrarConexion();
			return 2;
		} finally {
			conexion.cerrarConexion();
		}

	}

	/**
	 * Metodo encargado de realiar las estadisticas de busqueda de mutantes respecto
	 * a humanos
	 * 
	 * @return
	 */
	public SalidaServicioGet estadisticas() {

		SalidaServicioGet salida = new SalidaServicioGet();

		Statement stmt;
		try {
			String sentencia = "SELECT ES_MUTANTE, COUNT(ES_MUTANTE) CANTIDAD FROM TABLA_MUTANTES GROUP BY ES_MUTANTE";

			stmt = conexion.obtenerConexion().createStatement();
			ResultSet rs = stmt.executeQuery(sentencia);

			boolean existenMutantes = false;
			boolean existenHumanos = false;
			while (rs.next()) {

				if (rs.getBoolean("ES_MUTANTE")) {
					salida.setCount_mutant_dna(new BigDecimal(rs.getString("CANTIDAD")));
					existenMutantes = true;
				} else {
					salida.setCount_human_dna(new BigDecimal(rs.getLong("CANTIDAD")));
					existenHumanos = true;
				}

			}

			// Se valida si existen mutantes y humanos en la base de datos esto con el fin
			// de evitar division entre 0
			if (existenMutantes && existenHumanos) {

				// Se garantiza que el ratio maximo sea 100%
				if (salida.getCount_mutant_dna().compareTo(salida.getCount_human_dna()) == 1) {
					salida.setRatio(new BigDecimal("1.0"));
				} else {
					BigDecimal ratio = salida.getCount_mutant_dna().setScale(1)
							.divide(salida.getCount_human_dna().setScale(1), 3);
					salida.setRatio(ratio);
				}

			} else if (!existenMutantes && !existenHumanos) {
				salida.setCount_mutant_dna(new BigDecimal("0"));
				salida.setCount_human_dna(new BigDecimal("0"));
				salida.setRatio(new BigDecimal("0.0"));

			} else if (existenMutantes && !existenHumanos) {
				salida.setCount_human_dna(new BigDecimal("0"));
				salida.setRatio(new BigDecimal("1.0"));

			} else {
				salida.setCount_mutant_dna(new BigDecimal("0"));
				salida.setRatio(new BigDecimal("0.0"));

			}

			conexion.cerrarConexion();
			return salida;

		} catch (SQLException e) {
			logger.log(Level.INFO, "Error al consultar la cadena de ADN en base de datos " + e);
			conexion.cerrarConexion();

			salida.setCount_mutant_dna(new BigDecimal("0.0"));
			salida.setCount_human_dna(new BigDecimal("0.0"));
			salida.setRatio(new BigDecimal("0.0"));
			return salida;
		} finally {
			conexion.cerrarConexion();
		}

	}

	/**
	 * Metodo que se encarga de garantizar que la peticion enviada sea correcta true
	 * = Peticion valida false = Peticion invalida, ademas se anexa mensaje para
	 * saber el posible fallo
	 * 
	 * @param dna
	 * @return
	 */
	public Object[] validacionesIniciales(String[] datosPeticion) {

		Object[] respestaValidaciones = new Object[2];
		StringBuilder cadenaTemporal = new StringBuilder();

		// Se valida si la peticion es nula
		if (datosPeticion == null || datosPeticion.length == 0) {
			respestaValidaciones[0] = false;
			respestaValidaciones[1] = "Parametros vacios.";
			return respestaValidaciones;
		}

		// Se crea matriz NxN
		matrizDectectada = new char[datosPeticion[0].length()][datosPeticion.length];
		int contador = 0;

		for (String filaActual : datosPeticion) {
			// voy almacenando la cadena de ADN, para almacenarla en base de datos
			cadenaTemporal.append(filaActual);

			// Se valida que la matriz sea cuadrada NxN
			if (filaActual.length() != datosPeticion.length) {
				respestaValidaciones[0] = false;
				respestaValidaciones[1] = "La secuencia de ADN no es cuadrada.";
				return respestaValidaciones;
			}

			// Se valida si la cadena de ADN tiene las letras validas, pero en minuscula
			if (filaActual.contains("a") || filaActual.contains("t") || filaActual.contains("c")
					|| filaActual.contains("g")) {
				respestaValidaciones[0] = false;
				respestaValidaciones[1] = "La secuencia de ADN contiene caracteres distintos a A,T,C,G.";
				return respestaValidaciones;
			}

			// Se valida que la matriz solo contenga las letras A,T,C,G
			Pattern p = Pattern.compile(REGEX_ATCG);
			Matcher m = p.matcher(filaActual);
			if (m.find()) {
				respestaValidaciones[0] = false;
				respestaValidaciones[1] = "La secuencia de ADN contiene caracteres distintos a A,T,C,G.";
				return respestaValidaciones;
			}

			// Despues de que la fila pase las validaciones, se agrega a la matriz que sera
			// usada posteriormente
			matrizDectectada[contador] = filaActual.toCharArray();
			contador++;
		}

		cadenaADN = cadenaTemporal.toString();

		respestaValidaciones[0] = true;
		respestaValidaciones[1] = "Peticion OK.";
		return respestaValidaciones;
	}

	/**
	 * Metodo pirncipal que se encarga de determinar si segun se cadena de ADN el
	 * individuo es HUMANO o MUTANTE
	 * 
	 * @param dna
	 * @return
	 */
	public Object[] buscarCoincidencias() {
		Object[] respestaCoincidencias = new Object[2];

		// Que tan larga se desea la secuencia, para este caso es 4 letras iguales
		int cantidadLetras = 4;
		// Cuantas coincidencias se desean, para este caso piden mas de una coincidencia
		int cantidadCoincidencias = 2;
		// Cuantas coincidencias ha encontrado el sistema
		int coincidenciasEncontradas = 0;
		// Se definen os condicionales para ver si encontro alguna secuencia
		boolean encontroAAAA;
		boolean encontroTTTT;
		boolean encontroCCCC;
		boolean encontroGGGG;

		// Se definen variables para saber si puede seguir buscando en una fila
		int volverBuscarFila = 4;

		// Se definen variables para saber si puede seguir buscando en una columna
		mapaColumnas = new HashMap<>();

		// Se definen variables para saber si puede seguir buscando en una diagonal
		mapaDiagonalesDerechaSubiendo = new HashMap<>();
		mapaDiagonalesDerechaBajando = new HashMap<>();
		boolean volverBuscarCol = true;
		int numeroDiagonalDS = 0;
		int numeroDiagonalDB = 0;

		// Se recorre la matriz previamente creada para buscar coincidencias en filas,
		// columnas y diagonales
		for (int fila = 0; fila < matrizDectectada.length; fila++) {
			numeroDiagonalDS = fila + 1;
			numeroDiagonalDB = matrizDectectada.length - fila;
			for (int columna = 0; columna < matrizDectectada[fila].length; columna++) {

				volverBuscarFila++;
				// Se realiza busqueda por fila
				encontroAAAA = true;
				encontroTTTT = true;
				encontroCCCC = true;
				encontroGGGG = true;

				if (columna + (cantidadLetras - 1) < matrizDectectada[fila].length && volverBuscarFila >= 4) {

					for (int letras = 0; letras < cantidadLetras; letras++) {
						if (A != matrizDectectada[fila][columna + letras]) {
							encontroAAAA = false;
						}

						if (T != matrizDectectada[fila][columna + letras]) {
							encontroTTTT = false;
						}

						if (C != matrizDectectada[fila][columna + letras]) {
							encontroCCCC = false;
						}

						if (G != matrizDectectada[fila][columna + letras]) {
							encontroGGGG = false;
						}
					}
					// Encontro coincidencia en una fila
					if (encontroAAAA || encontroTTTT || encontroCCCC || encontroGGGG) {
						coincidenciasEncontradas++;
						volverBuscarFila = 0;
					}
				}

				// Se realiza busqueda por columna
				encontroAAAA = true;
				encontroTTTT = true;
				encontroCCCC = true;
				encontroGGGG = true;
				volverBuscarCol = columnaValidaBusqueda(columna);
				if (fila + (cantidadLetras - 1) < matrizDectectada.length && volverBuscarCol) {

					for (int letras = 0; letras < cantidadLetras; letras++) {
						if (A != matrizDectectada[fila + letras][columna]) {
							encontroAAAA = false;
						}
						if (T != matrizDectectada[fila + letras][columna]) {
							encontroTTTT = false;
						}
						if (C != matrizDectectada[fila + letras][columna]) {
							encontroCCCC = false;
						}
						if (G != matrizDectectada[fila + letras][columna]) {
							encontroGGGG = false;
						}
					}
					// Encontro coincidencia en una columna
					if (encontroAAAA || encontroTTTT || encontroCCCC || encontroGGGG) {
						coincidenciasEncontradas++;
						mapaColumnas.put(columna, 0);
					}
				}

				// Diagonal Derecha Subiendo
				encontroAAAA = true;
				encontroTTTT = true;
				encontroCCCC = true;
				encontroGGGG = true;
				if ((fila - (cantidadLetras - 1) >= 0)
						&& (columna + (cantidadLetras - 1) < matrizDectectada[fila].length)
						&& diagonaDSValidaBusqueda(fila, columna, numeroDiagonalDS)) {

					for (int letras = 0; letras < cantidadLetras; letras++) {
						if (A != matrizDectectada[fila - letras][columna + letras]) {
							encontroAAAA = false;
						}
						if (T != matrizDectectada[fila - letras][columna + letras]) {
							encontroTTTT = false;
						}
						if (C != matrizDectectada[fila - letras][columna + letras]) {
							encontroCCCC = false;
						}
						if (G != matrizDectectada[fila - letras][columna + letras]) {
							encontroGGGG = false;
						}
					}
					// Encontro coincidencia en una diagonal
					if (encontroAAAA || encontroTTTT || encontroCCCC || encontroGGGG) {
						coincidenciasEncontradas++;
						Integer[] posiciones = new Integer[3];
						posiciones[0] = fila;
						posiciones[1] = columna;
						posiciones[2] = numeroDiagonalDS;
						mapaDiagonalesDerechaSubiendo.put(String.valueOf(fila) + String.valueOf(columna), posiciones);
					}
				}

				// Diagonal Derecha Bajando
				encontroAAAA = true;
				encontroTTTT = true;
				encontroCCCC = true;
				encontroGGGG = true;
				if ((fila + (cantidadLetras - 1) < matrizDectectada.length)
						&& (columna + (cantidadLetras - 1) < matrizDectectada[fila].length)
						&& diagonaDBValidaBusqueda(fila, columna, numeroDiagonalDB)) {

					for (int letras = 0; letras < cantidadLetras; letras++) {
						if (A != matrizDectectada[fila + letras][columna + letras]) {
							encontroAAAA = false;
						}
						if (T != matrizDectectada[fila + letras][columna + letras]) {
							encontroTTTT = false;
						}
						if (C != matrizDectectada[fila + letras][columna + letras]) {
							encontroCCCC = false;
						}
						if (G != matrizDectectada[fila + letras][columna + letras]) {
							encontroGGGG = false;
						}
					}
					// Encontro coincidencia en una diagonal
					if (encontroAAAA || encontroTTTT || encontroCCCC || encontroGGGG) {
						coincidenciasEncontradas++;
						Integer[] posiciones = new Integer[3];
						posiciones[0] = fila;
						posiciones[1] = columna;
						posiciones[2] = numeroDiagonalDB;
						mapaDiagonalesDerechaBajando.put(String.valueOf(fila) + String.valueOf(columna), posiciones);
					}
				}

				numeroDiagonalDS++;
				numeroDiagonalDB++;
			}

			// Si encuentra 2 coincidencias, deja de buscar en la cadena de ADN
			if (coincidenciasEncontradas >= cantidadCoincidencias) {
				break;
			}
		}

		if (coincidenciasEncontradas >= cantidadCoincidencias) {
			respestaCoincidencias[0] = true;
			respestaCoincidencias[1] = "Es mutante.";

		} else {
			respestaCoincidencias[0] = false;
			respestaCoincidencias[1] = "Es humano.";
		}

		return respestaCoincidencias;
	}

	/**
	 * Este metodo se encarga de validar si la columna actual es valida para seguir
	 * buscando coincidencias o no
	 * 
	 * @param numeroColumna
	 * @return
	 */
	public boolean columnaValidaBusqueda(int numeroColumna) {
		try {

			if (mapaColumnas == null || mapaColumnas.size() == 0) {
				return true;
			}

			if (mapaColumnas.get(numeroColumna) == null || mapaColumnas.get(numeroColumna) >= 3) {
				mapaColumnas.remove(numeroColumna);
				return true;
			} else {
				if (mapaColumnas.get(numeroColumna) != null) {
					mapaColumnas.put(numeroColumna, mapaColumnas.get(numeroColumna) + 1);
				}

				return false;
			}
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * Este metodo se encarga de validar si la Diagonal Derecha Subiendo es valida.
	 * 
	 * @param columnaActual
	 * @param filaActual
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean diagonaDSValidaBusqueda(int filaActual, int columnaActual, int diagonalActual) {
		try {

			// Se recorren las diagonales ya encontradas para que no se solapen las
			// coincidencias
			if (mapaDiagonalesDerechaSubiendo == null || mapaDiagonalesDerechaSubiendo.size() == 0) {
				return true;
			}

			for (Map.Entry valor : mapaDiagonalesDerechaSubiendo.entrySet()) {
				String clave = (String) valor.getKey();
				Integer[] posiciones = (Integer[]) valor.getValue();
				if (posiciones[2] == diagonalActual) {

					if (filaActual - posiciones[0] >= 4 && posiciones[1] - columnaActual >= 4) {
						mapaDiagonalesDerechaSubiendo.remove(clave);
						return true;
					} else {
						return false;
					}
				}
			}

			return true;
		} catch (Exception e) {
			return true;
		}

	}

	/**
	 * Este metodo se encarga de validar si la Diagonal Derecha Bajando es valida
	 * 
	 * @param columnaActual
	 * @param filaActual
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean diagonaDBValidaBusqueda(int filaActual, int columnaActual, int diagonalActual) {
		try {

			// Se recorren las diagonales ya encontradas para que no se solapen las
			// coincidencias
			if (mapaDiagonalesDerechaBajando == null || mapaDiagonalesDerechaBajando.size() == 0) {
				return true;
			}

			for (Map.Entry valor : mapaDiagonalesDerechaBajando.entrySet()) {
				String clave = (String) valor.getKey();
				Integer[] posiciones = (Integer[]) valor.getValue();

				if (posiciones[2] == diagonalActual) {
					if (filaActual - posiciones[0] >= 4 && columnaActual - posiciones[1] >= 4) {
						mapaDiagonalesDerechaBajando.remove(clave);
						return true;
					} else {
						return false;
					}
				}
			}

			return true;
		} catch (Exception e) {
			return true;
		}

	}

	// Metodos GET y SET

	public char[][] getMatrizDectectada() {
		return matrizDectectada;
	}

	public void setMatrizDectectada(char[][] matrizDectectada) {
		this.matrizDectectada = matrizDectectada;
	}

	public Map<Integer, Integer> getMapaColumnas() {
		return mapaColumnas;
	}

	public void setMapaColumnas(Map<Integer, Integer> mapaColumnas) {
		this.mapaColumnas = (HashMap<Integer, Integer>) mapaColumnas;
	}

	public Map<String, Integer[]> getMapaDiagonalesDerechaSubiendo() {
		return mapaDiagonalesDerechaSubiendo;
	}

	public void setMapaDiagonalesDerechaSubiendo(Map<String, Integer[]> mapaDiagonalesDerechaSubiendo) {
		this.mapaDiagonalesDerechaSubiendo = (HashMap<String, Integer[]>) mapaDiagonalesDerechaSubiendo;
	}

	public Map<String, Integer[]> getMapaDiagonalesDerechaBajando() {
		return mapaDiagonalesDerechaBajando;
	}

	public void setMapaDiagonalesDerechaBajando(Map<String, Integer[]> mapaDiagonalesDerechaBajando) {
		this.mapaDiagonalesDerechaBajando = (HashMap<String, Integer[]>) mapaDiagonalesDerechaBajando;
	}

	public String getCadenaADN() {
		return cadenaADN;
	}

	public void setCadenaADN(String cadenaADN) {
		this.cadenaADN = cadenaADN;
	}

	public Conexion getConexion() {
		return conexion;
	}

	public void setConexion(Conexion conexion) {
		this.conexion = conexion;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
