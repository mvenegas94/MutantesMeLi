package co.com.pojos;

/**
 * Autor: Miguel Angel Venegas Rodriguez Version: 1.0 Fecha: 21/08/2021
 * Descripcion: Clase encargada de definir la estructura de salida del servicio POST
 */
public class Salida {

	private boolean esMutante;
	private String descripcion;

	public Salida() {
		// Constructor
	}

	public boolean isEsMutante() {
		return esMutante;
	}

	public void setEsMutante(boolean esMutante) {
		this.esMutante = esMutante;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

}
