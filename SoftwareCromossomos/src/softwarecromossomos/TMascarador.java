package softwarecromossomos;

import ij.process.*;
import ij.ImagePlus;
import java.awt.image.*;
import ij.IJ;

public class TMascarador {

    int w, h;

    public TMascarador() {
    }

    /**
     * aplica um contraste a imagem
     *
     * @param ipNova	imagem que sera aplicado o contraste
     */
    public void contraste(ImageProcessor ipNova) {
        for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
                int a = (int) (ipNova.get(u, v) * 1.5 + 0.5);
                if (a > 50) {
                    a = 255; 		// clamp to max. value
                }
                ipNova.set(u, v, a);
            }
        }
    }

    /**
     * retorna a cor que predomina na imagem
     *
     * @param ipNova	imagem que sera verificada
     * @return	nivel de cinza da cor predominante
     */
    public int corQuePredomina(ImageProcessor ipNova) {
        int sumBranco = 0, sumPreto = 0, corPredominante;
        for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
                int a = (int) ipNova.get(u, v);
                if (a > 127) {
                    sumBranco++;
                } else if (a <= 127) {
                    sumPreto++;
                }
            }
        }
        if (sumBranco > sumPreto) {
            corPredominante = 255;
        } else {
            corPredominante = 0;
        }
        return corPredominante;

    }

    /**
     * aplica uma erosao na imagem
     *
     * @param ipNova	imagem que sera aplicado a erosao
     * @param tipo	se a erosao sera de dentro pra fora ou de fora para dentro
     */
    public void erosao(ImageProcessor ipNova, String tipo) {

        int corPredominante = corQuePredomina(ipNova);
        if (tipo.equals("Inverso")) {
            corPredominante = 255 - corPredominante;
        }
        //FAZ A EROSAO
        int count, k, t, flag = 0, v, u;
        for (v = 0; v < h; v++) {
            for (u = 0; u < w; u++) {
                if ((int) ipNova.get(u, v) == corPredominante) {
                    //verifica se existe pixel da cor nao predominante em todas as direcoes, mas em nao mais que 5 pixels
                    //para cima
                    count = 0;
                    k = v;
                    while (k >= 0 && count <= 10 && (int) ipNova.get(u, k) == corPredominante) {
                        count++;
                        k--;
                    }
                    if (count > 10) {
                        flag = 1;
                    }
                    //para baixo
                    count = 0;
                    k = v;
                    while (k < h && count <= 10 && (int) ipNova.get(u, k) == corPredominante) {
                        count++;
                        k++;
                    }
                    if (count > 10) {
                        flag = 1;
                    }
                    //para esquerda
                    count = 0;
                    k = u;
                    while (k >= 0 && count <= 10 && (int) ipNova.get(k, v) == corPredominante) {
                        count++;
                        k--;
                    }
                    if (count > 10) {
                        flag = 1;
                    }
                    //para direita
                    count = 0;
                    k = u;
                    while (k < w && count <= 10 && (int) ipNova.get(k, v) == corPredominante) {
                        count++;
                        k++;
                    }
                    if (count > 10) {
                        flag = 1;
                    }

                    //para diagonal esquerda acima
                    count = 0;
                    k = u;
                    t = v;
                    while (k >= 0 && t >= 0 && count <= 10 && (int) ipNova.get(k, t) == corPredominante) {
                        count++;
                        k--;
                        t--;
                    }
                    if (count > 10) {
                        flag = 1;
                    }
                    //para diagonal direita acima
                    count = 0;
                    k = u;
                    t = v;
                    while (k >= 0 && t < h && count <= 10 && (int) ipNova.get(k, t) == corPredominante) {
                        count++;
                        k--;
                        t++;
                    }
                    if (count > 10) {
                        flag = 1;
                    }
                    //para diagonal esquerda abaixo
                    count = 0;
                    k = u;
                    t = v;
                    while (k < w && t >= 0 && count <= 10 && (int) ipNova.get(k, t) == corPredominante) {
                        count++;
                        k++;
                        t--;
                    }
                    if (count > 10) {
                        flag = 1;
                    }
                    //para diagonal direita abaixo
                    count = 0;
                    k = u;
                    t = v;
                    while (k < w && t < h && count <= 10 && (int) ipNova.get(k, t) == corPredominante) {
                        count++;
                        k++;
                        t++;
                    }
                    if (count > 10) {
                        flag = 1;
                    }
                    //se nao deu um break, entao quer dizer que aquela cor deve ser invertida:
                    if (flag == 0) {
                        ipNova.set(u, v, 255 - corPredominante);
                    }
                    flag = 0;
                }
            }
        }
    }

    /**
     * aplica um filtro de remocao de buracos na imagem
     *
     * @param ip	imagem que sera aplicado a remocao de buracos
     * @param foreground	cor dos objetos
     * @param background	cor do fundo
     */
    public void fillholes(ImageProcessor ip, int foreground, int background) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        FloodFiller ff = new FloodFiller(ip);
        ip.setColor(127);
        for (int y = 0; y < height; y++) {
            if (ip.getPixel(0, y) == background) {
                ff.fill(0, y);
            }
            if (ip.getPixel(width - 1, y) == background) {
                ff.fill(width - 1, y);
            }
        }
        for (int x = 0; x < width; x++) {
            if (ip.getPixel(x, 0) == background) {
                ff.fill(x, 0);
            }
            if (ip.getPixel(x, height - 1) == background) {
                ff.fill(x, height - 1);
            }
        }
        byte[] pixels = (byte[]) ip.getPixels();
        int n = width * height;
        for (int i = 0; i < n; i++) {
            if (pixels[i] == 127) {
                pixels[i] = (byte) background;
            } else {
                pixels[i] = (byte) foreground;
            }
        }
    }

    /**
     * cria a mascara de uma imagem
     *
     * @param buffer	imagem que sera criada a mascara
     * @return	mascara da imagem
     */
    public BufferedImage mascara(BufferedImage buffer) {

        System.gc();
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
        //cria uma imagem nova
        ImageProcessor ipNova = new ByteProcessor(w, h);
        ipNova.setValue(0);

        //e copia a original para ela
        for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
                int a = (int) (ip.get(u, v));
                ipNova.set(u, v, a);
            }
        }

        //faz a borracao
        ipNova.smooth();
        //encontra as bordas
        ipNova.findEdges();

        //contraste
        contraste(ipNova);
        //binario
        ipNova.threshold(127);

        fillholes(ipNova, 255, 0);

        ImagePlus imp = new ImagePlus("", ipNova);
        IJ.run(imp, "Minimum...", "kRadius=2");

        ipNova = imp.getProcessor();

        return (BufferedImage) ipNova.createImage();
    }

    /**
     * cria a mascara de uma imagem ImageProcessor
     *
     * @param ip	imagem que sera criada a mascara
     * @return	mascara ImageProcessor da imagem
     */
    public ImageProcessor mascaraIP(ImageProcessor ip) {

        //cria uma imagem nova
        ImageProcessor ipNova = new ByteProcessor(ip.getWidth(), ip.getHeight());
        ipNova.setValue(0);
        //e copia a original para ela
        for (int v = 0; v < ip.getHeight(); v++) {
            for (int u = 0; u < ip.getWidth(); u++) {
                int a = (int) (ip.get(u, v));
                ipNova.set(u, v, a);
            }
        }
        //faz a borracao
        ipNova.smooth();
        //encontra as bordas
        ipNova.findEdges();
        //contraste
        contraste(ipNova);
        //binario
        ipNova.threshold(127);
        //MINHA EROSAO / DILATACAO
        fillholes(ipNova, 255, 0);
        erosao(ipNova, "Inverso");
        return ipNova;
    }
}