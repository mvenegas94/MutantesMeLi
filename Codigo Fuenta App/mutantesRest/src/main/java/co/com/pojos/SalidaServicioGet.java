package co.com.pojos;

import java.math.BigDecimal;

/**
 * Autor: Miguel Angel Venegas Rodriguez Version: 1.0 Fecha: 21/08/2021
 * Descripcion: Clase encargada de mapear la salida del servicio GET de negocio
 */
public class SalidaServicioGet {

	private BigDecimal count_mutant_dna;
	private BigDecimal count_human_dna;
	private BigDecimal ratio;

	public SalidaServicioGet() {
		// constructor stub
	}

	public SalidaServicioGet(BigDecimal count_mutant_dna, BigDecimal count_human_dna, BigDecimal ratio) {
		this.count_human_dna = count_human_dna;
		this.count_mutant_dna = count_mutant_dna;
		this.ratio = ratio;
	}

	public BigDecimal getCount_mutant_dna() {
		return count_mutant_dna;
	}

	public void setCount_mutant_dna(BigDecimal count_mutant_dna) {
		this.count_mutant_dna = count_mutant_dna;
	}

	public BigDecimal getCount_human_dna() {
		return count_human_dna;
	}

	public void setCount_human_dna(BigDecimal count_human_dna) {
		this.count_human_dna = count_human_dna;
	}

	public BigDecimal getRatio() {
		return ratio;
	}

	public void setRatio(BigDecimal ratio) {
		this.ratio = ratio;
	}

}
