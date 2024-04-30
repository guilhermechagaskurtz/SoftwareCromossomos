package softwarecromossomos;

import java.util.Vector;
import mestradocromossomos3.TPonto;

public class TDna{
	Vector<TCromossomo> cromossomos = new Vector<TCromossomo>();
	private int numCromossomos=0;

	/**
	 * adiciona o cromossomo ao dna
	 * 
	 * @param c		cromossomo
	 */
	public void adicionaCromossomo(TCromossomo c){
		int i;
		TCromossomo cc = new TCromossomo();
		
		for(i=0; i< c.getPixelsSize(); i++){
			TPonto p = new TPonto();
			p = c.getPixelsAt(i);
			cc.adicionaPixels(p.getLinha(),p.getColuna());
		}
		cc.setEsquerda(c.getEsquerda());
		cc.setDireita(c.getDireita());
		cc.setAcima(c.getAcima());
		cc.setAbaixo(c.getAbaixo());
		cromossomos.addElement(cc);
		numCromossomos++;
	}
	
	/**
	 * retorna um cromossomo do dna
	 * 
	 * @param i		posicao do cromossomo
	 * @return 		cromossomo da posicao i
	 */	
	public TCromossomo getCromossomoAt(int i){
		return (TCromossomo)cromossomos.get(i);
	}
	
	/**
	 * retorna o tamanho do vector de cromossomos
	 * 
	 * @return 		tamanho do vector de cromossomos
	 */	
	public int getCromossomoSize(){
		return cromossomos.size();
	}

	/**
	 * retorna o numero de cromossomos
	 * 
	 * @return 		numero de cromossomos
	 */	
	public int getNumCromossomos(){
		return numCromossomos;
	}
}