package softwarecromossomos;

import java.util.Vector;
import mestradocromossomos3.TPonto;

public class TCromossomo{
	private Vector<TPonto> pixels = new Vector<TPonto>();
	
	int maisEsquerda,maisDireita,maisAcima,maisAbaixo;
	
	public TCromossomo(){
	}
	
	/**
	 * Adiciona um pixel ao cromossomo
	 * 
	 * @param linha		linha do pixel
	 * @param coluna	coluna do pixel
	 */	
	public void adicionaPixels(int linha, int coluna){
		pixels.add(new TPonto(linha,coluna));
	}
	
	/**
	 * Retorna o tamanho do vector de pixeis
	 * 
	 * @return			tamanho do vector de pixeis
	 */	
	public int getPixelsSize(){
		return pixels.size();
	}
	
	/**
	 * Retorna o respectivo ponto i do vector de pixeis
	 * 
	 * @param i			posicao do ponto no vector
	 * @return			ponto na posicao i do vector de pixeis
	 */		
	public TPonto getPixelsAt(int i){
		return (TPonto)pixels.get(i);
	}

	/**
	 * encontra os pixeis mais a esquerda, direita, acima e abaixo
	 * 
	 */
	public void encontraOsMais(){
		encontraMaisEsquerda();
		encontraMaisDireita();
		encontraMaisAcima();
		encontraMaisAbaixo();
	}
	
	/**
	 * encontra o pixel mais a esquerda
	 * 
	 */	
	public void encontraMaisEsquerda(){
		int i,maisEsq = 10000;
		for(i=0; i<pixels.size(); i++){
			TPonto p = (TPonto)pixels.get(i);	
			if(p.getColuna() < maisEsq) maisEsq = p.getColuna();
		}
		maisEsquerda = maisEsq;
	}
	
	/**
	 * encontra o pixel mais a direita
	 * 
	 */		
	public void encontraMaisDireita(){
		int i,maisDir = -10000;
		for(i=0; i<pixels.size(); i++){
			TPonto p = (TPonto)pixels.get(i);	
			if(p.getColuna() > maisDir) maisDir = p.getColuna();
		}
		maisDireita = maisDir;
	}
	
	/**
	 * encontra o pixel mais acima
	 * 
	 */		
	public void encontraMaisAcima(){
		int i,maisAc = 10000;
		for(i=0; i<pixels.size(); i++){
			TPonto p = (TPonto)pixels.get(i);	
			if(p.getLinha() < maisAc) maisAc = p.getLinha();
		}
		maisAcima = maisAc;
	}
	
	/**
	 * encontra o pixel mais abaixo
	 * 
	 */		
	public void encontraMaisAbaixo(){
		int i,maisAb = -10000;
		for(i=0; i<pixels.size(); i++){
			TPonto p = (TPonto)pixels.get(i);	
			if(p.getLinha() > maisAb) maisAb = p.getLinha();
		}
		maisAbaixo = maisAb;
	}		
		
	/**
	 * Retorna o pixel mais a esquerda
	 * 
	 * @return			pixel mais a esquerda
	 */				
	public int getEsquerda(){
		return maisEsquerda;
	}
		
	/**
	 * Retorna o pixel mais a direita
	 * 
	 * @return	pixel mais a direita
	 */	
	public int getDireita(){
		return maisDireita;
	}
		
	/**
	 * Retorna o pixel mais acima
	 * 
	 * @return	pixel mais acima
	 */	
	public int getAcima(){
		return maisAcima;
	}
		
	/**
	 * Retorna o pixel mais a abaixo
	 * 
	 * @return	pixel mais a abaixo
	 */	
	public int getAbaixo(){
		return maisAbaixo;
	}	

	/**
	 * define o pixel mais a esquerda
	 * 
	 * @param x posicao do pixel mais a esquerda
	 */			
	public void setEsquerda(int x){
		maisEsquerda = x;
	}
	
	/**
	 * define o pixel mais a direita
	 * 
	 * @param x posicao do pixel mais a direita
	 */	
	public void setDireita(int x){
		maisDireita = x;
	}
	
	/**
	 * define o pixel mais acima
	 * 
	 * @param x posicao do pixel mais acima
	 */	
	public void setAcima(int x){
		maisAcima = x;
	}
	
	/**
	 * define o pixel mais abaixo
	 * 
	 * @param x posicao do pixel mais abaixo
	 */	
	public void setAbaixo(int x){
		maisAbaixo = x;
	}				
}