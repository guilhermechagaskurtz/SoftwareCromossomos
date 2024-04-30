package softwarecromossomos;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.*;
import java.awt.image.*;

public class TFiltro {

    int w, h;

    public TFiltro() {
    }

    public void contraste(ImageProcessor ip, double contrast) {
        long soma = 0;
        int cont = 0;
        double media, tempInt;

        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                soma += ip.get(x, y);
                cont++;
            }
        }

        media = (double) soma / (double) cont;

        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                tempInt = ip.get(x,y) - media;
                tempInt = (tempInt * contrast) + media;

                if(tempInt > 255) tempInt = 255;
                if(tempInt < 0) tempInt = 0;

                ip.set(x,y, (int)tempInt);
            }
        }
    }

    /**
     * aplica um filtro a imagem
     *
     * @param buffer	imagem na qual sera aplicado o filtro
     * @param tipo	tipo de filtro
     * @return imagem filtrada
     */
    public BufferedImage Filtrar(BufferedImage buffer, String tipo) {

        w = buffer.getWidth();
        h = buffer.getHeight();

        ImageProcessor ip = new ByteProcessor(w, h);

        Raster raster = buffer.getData();

        for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
                int a = raster.getSample(u, v, 0);
                ip.set(u, v, a);
            }
        }
        if (tipo.equals("Erodir")) {
            System.out.println("Vou erodir");
            ip.threshold(127);
            ip.erode();
        } else if (tipo.equals("Dilatar")) {
            ip.threshold(127);
            ip.dilate();
        } else if (tipo.equals("Bordas")) {
            ip.findEdges();
        } else if (tipo.equals("Acentuar")) {
            ip.sharpen();
        } else if (tipo.equals("Borrar")) {
            ip.smooth();
        } else if (tipo.equals("Contraste")) {
            contraste(ip, 1.5);
        }

        BufferedImage retBuffer = new BufferedImage(ip.getWidth(), ip.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wraster = retBuffer.getRaster();
        for (int v = 0; v < ip.getHeight(); v++) {
            for (int u = 0; u < ip.getWidth(); u++) {
                int cor = (int) (ip.get(u, v));
                wraster.setSample(u, v, 0, cor);
            }
        }
        return retBuffer;
    }
}
