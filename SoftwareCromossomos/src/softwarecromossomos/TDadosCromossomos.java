package softwarecromossomos;

public class TDadosCromossomos {

	int indice,pontos;
	
	public TDadosCromossomos(){
		indice=0;
		pontos=0;
	}
	public TDadosCromossomos(int i,int p){
		indice=i;
		pontos=p;
	}
	
	/**
	 * define o indice do dado do cromossomo
	 * 
	 * @param i		indice
	 */ 	
	public void setIndice(int i){
		indice=i;
	}
		
	/**
	 * define o ponto do dado do cromossomo
	 * 
	 * @param p		ponto
	 */ 
	public void setPontos(int p){
		pontos=p;
	}
		
	/**
	 * retorna o indice do dado do cromossomo
	 * 
	 * @return		indice
	 */ 
	public int getIndice(){
		return indice;
	}
	
	/**
	 * retorna o ponto do dado do cromossomo
	 * 
	 * @return		ponto
	 */ 
	public int getPontos(){
		return pontos;
	}
	
	/**
	 * incrementa o numero de pontos do dado do cromossomo
	 * 
	 */ 
	public void incrementaPontos(){
		pontos++;
	}
}