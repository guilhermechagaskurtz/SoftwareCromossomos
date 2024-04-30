package softwarecromossomos;

import java.awt.image.BufferedImage;

public class TZoom {

	private int fator;
	
	public TZoom(int f){
		fator = f;
	}
	
	/**
	 * apica um zoom in na imagem
	 * 
	 * @param bi	imagem que sera aplicado o zoom in
	 * @return		imagem com o zoom in aplicado
	 */	
	public BufferedImage zoomIn(BufferedImage bi){
		int width = bi.getWidth()*fator;
		int height = bi.getHeight()*fator;
		
		BufferedImage biScale = new BufferedImage(width, height, bi.getType());
		
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				biScale.setRGB(i, j, bi.getRGB(i/fator, j/fator));
		
		return biScale;
	}

	/**
	 * apica um zoom out na imagem
	 * 
	 * @param bi	imagem que sera aplicado o zoom out
	 * @return		imagem com o zoom out aplicado
	 */	
	public BufferedImage zoomOut(BufferedImage bi){
		int width = bi.getWidth()/fator;
		int height = bi.getHeight()/fator;
		
		BufferedImage biScale = new BufferedImage(width, height, bi.getType());
		
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				biScale.setRGB(i, j, bi.getRGB(i*fator, j*fator));
		
		return biScale;
	}
}