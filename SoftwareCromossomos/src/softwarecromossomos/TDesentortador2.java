package softwarecromossomos;

/**
 * @(#)TDesentortador.java
 *
 *
 * @author 
 * @version 1.00 2008/8/18
 */
import ij.process.*;
import ij.gui.Roi;
import ij.gui.PolygonRoi;
import ij.*;
import ij.plugin.Straightener;
import ij.plugin.filter.GaussianBlur;
import java.util.Vector;
import mestradocromossomos3.TPonto;

public class TDesentortador2 {

	Vector<TPonto> limites = new Vector<TPonto>();
	Vector<TPonto> divisoes = new Vector<TPonto>();
	Vector<TPonto> pontos = new Vector<TPonto>();
	Vector<TPonto> caminho = new Vector<TPonto>();
	Vector<ImageProcessor> ret = new Vector<ImageProcessor>();

    public TDesentortador2() {
    }
    
	/**
	 * calcula a distancia entre dois pontos
	 * 
	 * @param xIni		valor x do ponto inicial
	 * @param yIni		valor y do ponto inicial
	 * @param xFim		valor x do ponto final
	 * @param yFim		valor y do ponto final
	 * @return 			distancia entre os pontos
	 */     
	public double distancia(int xIni, int yIni, int xFim, int yFim){
		double dist;
		double xAoQuadrado = (xFim-xIni)*(xFim-xIni);
		double yAoQuadrado = (yFim-yIni)*(yFim-yIni);
		dist = Math.sqrt(xAoQuadrado+yAoQuadrado);
		return dist;
	}
	
	/**
	 * calcula o numero de pontos limites e divisoes das retas presentes na imagem
	 * 
	 * @param ip		imagem
	 */ 	
    public void contaLimitesEdivisoes(ImageProcessor ip){
    int i,j,m,n,count;
    limites.clear();
    divisoes.clear();
    pontos.clear();
		for(i=0; i<ip.getHeight();i++){
			for(j=0; j<ip.getWidth();j++){
				count=0;
				if((int)ip.get(j,i)==0){
					for(m=-1;m<=1;m++){
						for(n=-1;n<=1;n++){
							if(!(n==0&&m==0)){
								if(j+n>=0&&i+m>=0&&j+n<ip.getWidth()&&i+m<ip.getHeight()&&
								  (int)ip.get(j+n,i+m)==0) 
									count++;	
							}
						}
					}	
				}
				if(count==1){
					limites.addElement(new TPonto(i,j));
				}
				if(count>=3){
					divisoes.addElement(new TPonto(i,j));
				}
				if(count==2){
					pontos.addElement(new TPonto(i,j));
				}
			}
		}
    }
    
	/**
	 * determina o caminho que os pontos devem percorrer do inicio ao fim
	 * atraves de um algoritmo de saida do labirinto
	 *
	 */ 
    public void determinaCaminho(){
    	TPonto p,q;
    	int k,menor;
    	double dist,menorDist;
    	caminho.clear();
    	if(limites.size()==2){
    		p=(TPonto)limites.get(0);
    		caminho.addElement(new TPonto(p.getLinha(),p.getColuna()));
    		//pega o ponto que estiver mais perto
    		while(true){
    			q=(TPonto)caminho.get(caminho.size()-1);
    			menor=0;
    			menorDist=10000;
	    		for(k=0; k<pontos.size();k++){
	    			p=(TPonto)pontos.get(k);
	    			dist=distancia(p.getLinha(),p.getColuna(),q.getLinha(),q.getColuna());
	    			if(dist<menorDist){
	    				menorDist=dist;
	    				menor=k;
	    			}		
	    		}
	    		//mas agora compara com outro ponto limite para ver se nao chegou no fim
	  			p=(TPonto)limites.get(1);
	    		dist=distancia(p.getLinha(),p.getColuna(),q.getLinha(),q.getColuna());
	    		//se a distancia for menor do que a ja encontrada, quer dizer que 
	    		//chegou no fim do caminho
	    		if(dist<menorDist){
	    			caminho.addElement(new TPonto(p.getLinha(),p.getColuna()));
	    			break;
	    		}
	    		//se nao chegou, adiciona ao caminho o ponto de menor distancia e 
	    		//remove ele do vetor pontos
	    		else{
	    			p=(TPonto)pontos.get(menor);
	    			caminho.addElement(new TPonto(p.getLinha(),p.getColuna()));
	    			pontos.removeElementAt(menor);
	    		}
    		}
    	}
    }

	/**
	 * verifica se o ponto p pertence a reta formada pelos pontos p1 e p2
	 * 
	 * @param p1		ponto 1 formador da reta
	 * @param p2		ponto 2 formador da reta
	 * @param p			ponto a ser verificado
	 * @return			verdadeiro ou falso dependendo se o ponto pertence ou nao a reta
	 */ 
   	public boolean pertenceAreta(TPonto p1, TPonto p2, TPonto p){
   		int a,b,x,y;
   		if(p1.getColuna()==p2.getColuna() && p1.getColuna()==p.getColuna())
   			return true;
   		if(p1.getColuna()==p2.getColuna() && p1.getColuna()!=p.getColuna())
   			return false;
   		if(p1.getLinha()==p2.getLinha() && p1.getLinha()==p.getLinha())
   			return true;
   		if(p1.getLinha()==p2.getLinha() && p1.getLinha()!=p.getLinha())
   			return false;
   		a=(p1.getLinha()-p2.getLinha())/(p1.getColuna()-p2.getColuna());
		b=p2.getLinha()-((p1.getLinha()-p2.getLinha())/(p1.getColuna()-p2.getColuna()))*p2.getColuna();
		x=p.getColuna();
		y=a*x+b;
		if(y==p.getLinha())
			return true;
		return false;
   	}
   	
	/**
	 * adiciona pontos na reta da imagem de forma que ela aumente seu tamanho nas pontas
	 * 
	 * @param ipMask						mascara da imagem original
	 * @param ip							imagem original
	 * @param p1							posicao 1 (antepenultima ou segunda) do caminho
	 * @param p2							posicao 2 (ultima ou primeira) do caminho
	 * @param ehParaAdicionarNoFim			verifica se eh para adicionar no fim ou no inicio da reta
	 */ 
   	public void adicionaPontos(ImageProcessor ipMask,ImageProcessor ip, int p1, int p2, boolean ehParaAdicionarNoFim){
   		TPonto p,q;
   		int a,b,x,y;
   		int count=0;
    	//Adiciona outros pontos na linha
    	try{
	    	p=caminho.get(p1);
	    	q=caminho.get(p2);
	    	//se for uma reta paralela a y
	    	if(p.getColuna()==q.getColuna()){
	    		x=p.getColuna();
	    		//em direcao pra baixo
	    		if(p.getLinha()>q.getLinha()){
	    			y=p.getLinha()+1;
	    			//SEMPRE VERIFICA SE NAO SAIU DA PARTE BRANCA DA MASCARA TAMBEM
	    			while((y<=ip.getHeight()-1)&&((int)ipMask.get(x,y)==255)){
	    				if(ehParaAdicionarNoFim) caminho.addElement(new TPonto(y,x));
	    				else caminho.add(0,new TPonto(y,x));
	    				ip.set(x,y,0);
	    				y++;
	    				count++;
	    			}
	    		}
	    		//em direcao pra cima
	    		if(q.getLinha()>p.getLinha()){
	    			y=p.getLinha()-1;
	    			//SEMPRE VERIFICA SE NAO SAIU DA PARTE BRANCA DA MASCARA TAMBEM
	    			while((y>=0)&&((int)ipMask.get(x,y)==255)){
	    				if(ehParaAdicionarNoFim) caminho.addElement(new TPonto(y,x));
	    				else caminho.add(0,new TPonto(y,x));
	    				ip.set(x,y,0);
	    				y--;
	    				count++;
	    			}
	    		}
	    	}
	    	//se nao for, entao pode calcular a equacao
	    	else{
		    	a=(p.getLinha()-q.getLinha())/(p.getColuna()-q.getColuna());
		    	b=q.getLinha()-((p.getLinha()-q.getLinha())/(p.getColuna()-q.getColuna()))*q.getColuna();
		    	//se for para direita
		    	if(p.getColuna()>q.getColuna()){
		    		x=p.getColuna()+1;
		    		while(x<=ip.getWidth()-1){
		    			y=a*x+b;
		    			//SEMPRE VERIFICA SE NAO SAIU DA PARTE BRANCA DA MASCARA TAMBEM
		    			if((y>=0 && y<=ip.getHeight()-1)&&((int)ipMask.get(x,y)==255)){
		    				if(ehParaAdicionarNoFim) caminho.addElement(new TPonto(y,x));
	    					else caminho.add(0,new TPonto(y,x));
		    				ip.set(x,y,0);
		    				x++;
		    				count++;
		    			}
		    			else break;
		    		}
		    	}
		    	//se for para a esquerda
		    	else{
		    		x=p.getColuna()-1;
		    		while(x>=0){
		    			y=a*x+b;
		    			//SEMPRE VERIFICA SE NAO SAIU DA PARTE BRANCA DA MASCARA TAMBEM
		    			if((y>=0 && y<=ip.getHeight()-1)&&((int)ipMask.get(x,y)==255)){
		    				if(ehParaAdicionarNoFim) caminho.addElement(new TPonto(y,x));
	    					else caminho.add(0,new TPonto(y,x));
		    				ip.set(x,y,0);
		    				x--;
		    				count++;
		    			}
		    			else break;
		    		}		    		
		    	}
	    	}
    	}catch(Exception e){
    	}   		
   	}
   	
	/**
	 * desentorta a imagem do cromossomo
	 * 
	 * @param ip			imagem original
	 * @param ipSkeleton	esqueleto da imagem
	 * @return				Imagem desentortada
	 */    	
   	public ImageProcessor desentortar(ImageProcessor ip, ImageProcessor ipSkeleton){
   		TPonto ponto;
   		int j;
		for(int i=1; i<caminho.size(); i++){
			for(j=0; j<6 && i<caminho.size()-1; j++){
				ponto = (TPonto)caminho.get(i);
				ipSkeleton.set(ponto.getColuna(),ponto.getLinha(),255);
				caminho.removeElementAt(i);
			}
		}
		int xPoints[] = new int[caminho.size()];
		int yPoints[] = new int[caminho.size()];
		int nPoints = 0;
		for(int i=0; i<caminho.size(); i++){
			ponto = (TPonto)caminho.get(i);
			xPoints[i]=ponto.getColuna();
			yPoints[i]=ponto.getLinha();
			nPoints++;	
		}
   		//tira as bordas da imagem
   		for(int i=0; i<ip.getHeight();i++){
   			ip.set(0,i,0);
   			ip.set(ip.getWidth()-1,i,0);
   		}
   		for(int i=0; i<ip.getWidth();i++){
   			ip.set(i,0,0);
   			ip.set(i,ip.getHeight()-1,0);
   		}
   		ImagePlus imp = new ImagePlus("",ip);
   		
   		imp.setRoi(new PolygonRoi(xPoints,yPoints,nPoints,Roi.POLYLINE));
   		Straightener straightener = new Straightener();
   		ImageProcessor ip2 = straightener.straightenLine(imp,30);
   		return ip2;
   	}
   	
	/**
	 * metodo principal que aplica os filtros antes de desentortar a imagem
	 * 
	 * @param ip			mascara da imagem
	 * @param ipOriginal	imagem original
	 * @return				imagens desentortadas
	 */    	
	public Vector<ImageProcessor> vai(ImageProcessor ip, ImageProcessor ipOriginal){
		int i,j;
		Vector<ImageProcessor> retorno = new Vector<ImageProcessor>();
		ImageProcessor ipSkeleton;
		ipSkeleton = ip.duplicate();
		//tira as partes brancas que encostam nas bordas
		for(i=0; i<ipSkeleton.getHeight(); i++){
			ipSkeleton.set(0,i,0);
			ipSkeleton.set(ip.getWidth()-1,i,0);
		}
		for(j=0; j<ipSkeleton.getWidth(); j++){
			ipSkeleton.set(j,0,0);
			ipSkeleton.set(j,ip.getHeight()-1,0);
		}
		//faz isso para focar a aparecer 2 limites
		for(i=0;i<10;i++){
			ipSkeleton.smooth();
			ipSkeleton.threshold(127);
		}
		ipSkeleton.dilate();
		ipSkeleton.erode();
		
		GaussianBlur g = new GaussianBlur();
		g.blurGaussian(ipSkeleton,3,3,0.002);
		ipSkeleton.threshold(127);
		
		ImagePlus imp = new ImagePlus("",ipSkeleton);
		IJ.run(imp,"Median...","kRadius=5");
		ipSkeleton=imp.getProcessor();
		ipSkeleton.threshold(127);
		
		//Inverte as cores
		ipSkeleton.invert();
		((ByteProcessor)ipSkeleton).skeletonize();	
		contaLimitesEdivisoes(ipSkeleton);
		if(limites.size()==2){
			determinaCaminho();
			adicionaPontos(ip,ipSkeleton,caminho.size()-1,caminho.size()-2,true);
			adicionaPontos(ip,ipSkeleton,0,1,false);
			ipOriginal = desentortar(ipOriginal,ipSkeleton);
			retorno.add(ipOriginal.convertToByte(true));
			return retorno;
		}
		else{
			return retorno;
		}
	}
}