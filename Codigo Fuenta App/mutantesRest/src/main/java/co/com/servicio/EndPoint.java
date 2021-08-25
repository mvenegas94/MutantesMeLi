package co.com.servicio;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import co.com.ejbs.OperacionesEjb;
import co.com.pojos.Entrada;
import co.com.pojos.SalidaServicioGet;

/**
 * Autor: Miguel Angel Venegas Rodriguez Version: 1.0 Fecha: 21/08/2021
 * Descripcion: Clase encargada de exponer los servicios rest
 */

@Path("servicios")
public class EndPoint {

	OperacionesEjb operaciones = new OperacionesEjb();

	/**
	 * Metodo encargado de dar estadisticas de busqueda de mutantes
	 * 
	 * @return
	 */
	@GET
	@Path("stats")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIt() {
		SalidaServicioGet salida = operaciones.estadisticas();
		return Response.ok(salida).build();
	}

	/**
	 * Metodo encargado de exponer el servicio post para determinar mutantes
	 * 
	 * @param dna
	 * @return
	 */
	@POST
	@Path("mutant")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response isMutant(Entrada dna) {
		boolean salida = operaciones.isMutant(dna.getDna());
		if (salida) {
			return Response.ok(salida).build();
		} else {
			return Response.status(HttpsURLConnection.HTTP_FORBIDDEN).build();
		}

	}
}
