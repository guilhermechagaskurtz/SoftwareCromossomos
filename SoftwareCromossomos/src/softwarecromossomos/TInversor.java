package softwarecromossomos;

import ij.process.*;
import java.awt.image.*;
public class TInversor {

	public TInversor(){
	}

	/**
	 * inverte uma imagem BufferedImage
	 * 
	 * @param buffer	imagem que sera invertida
	 * @return 			imagem invertida
	 */
	public BufferedImage Inverte(BufferedImage buffer) {
		ImageProcessor ip = new ByteProcessor(buffer);
		
		ip.setRoi(0,0,ip.getWidth(),ip.getHeight());
		ip.flipVertical();
		
		BufferedImage retBuffer = (BufferedImage)ip.createImage();
		return retBuffer;
	}
	
	/**
	 * inverte uma imagem ImageProcessor
	 * 
	 * @param ip	imagem que sera invertida
	 */	
	public void InverteIP(ImageProcessor ip){
		ip.setRoi(0,0,ip.getWidth(),ip.getHeight());
		ip.flipVertical();
	}
}