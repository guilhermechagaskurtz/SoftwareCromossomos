package softwarecromossomos;

/**
 * @(#)SaveImage.java
 *
 *
 * @author
 * @version 1.00 2008/10/22
 */
import java.awt.image.*;
import java.awt.Graphics;
import ij.process.*;
import java.awt.Font;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import mestradocromossomos3.Cariotipo;

public class SaveImage {

    public SaveImage() {
    }

    /**
     * converte um array de imagens para escala de cinza
     *
     * @param imagens	array de BufferedImage que sera convertido
     */
    public void converterEscalaCinza(BufferedImage imagens[]) {
        int i;
        for (i = 0; i < imagens.length; i++) {
            BufferedImage image = new BufferedImage(imagens[i].getWidth(), imagens[i].getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = image.getGraphics();
            g.drawImage(imagens[i], 0, 0, null);
            g.dispose();
            imagens[i] = image;
        }
    }

    /**
     * salva um conjunto de imagens em uma unica imagem
     *
     * @param cariotipo	cariotipo com as imagens a serem salvas
     */
    public void salvar(Cariotipo cariotipo, String onde) {
        int i, j;
        int numColunas = 10;
        ArrayList<ImageProcessor> img = cariotipo.getCromossomosImagens();
        //converterEscalaCinza(imagens);
        /*for (i = 0; i < imagens.length; i++) {
            img[i] = new ByteProcessor(imagens[i]);
        }*/
        //cria uma cópia de cada imagem, adicionando um espaço embaix para escrever o tipo
        for(i=0; i<img.size(); i++){
            ImageProcessor imgNova = new ByteProcessor(img.get(i).getWidth(),img.get(i).getHeight()+20);
            imgNova.setValue(255);
            imgNova.fill();
            //se for um cromossomo invertido pelo usuário, inverte ele pra salvar no cariótipo
            if(cariotipo.getCromossomo(i).isInvertido()){
                img.get(i).setRoi(0,0,img.get(i).getWidth(),img.get(i).getHeight());
		img.get(i).flipVertical();
            }
            imgNova.insert(img.get(i), 0, 0);
            imgNova.setColor(0);
            imgNova.setFont(new Font("Arial",Font.PLAIN,14));
            String tipo;
            if(cariotipo.getCromossomo(i).getTipo()==22){
                tipo = "X";
            }
            else if(cariotipo.getCromossomo(i).getTipo()==22){
                tipo = "Y";
            }
            else if(cariotipo.getCromossomo(i).getTipo() < 0){
                tipo = "--";
            }
            else{
                tipo = (cariotipo.getCromossomo(i).getTipo()+1)+"";
            }
            imgNova.drawString(tipo, (imgNova.getWidth()/2)-7, imgNova.getHeight()-6);
            img.set(i,imgNova);
        }
        int width = 0, widthLinha = 0;
        for (i = 0; i < img.size(); i++) {
            widthLinha += img.get(i).getWidth();
            if (i > 0 && (i % numColunas == 0 || i == img.size() - 1)) {
                if (widthLinha > width) {
                    width = widthLinha;
                }
                widthLinha = 0;
            }
        }

        int height = 0, heightLinha = 0;
        int vetHeight[] = new int[(int) (img.size() / numColunas) + 1];
        for (i = 0, j = 0; i < img.size(); i++) {
            if (img.get(i).getHeight() > heightLinha) {
                heightLinha = img.get(i).getHeight();
            }
            if (i > 0 && (i % numColunas == 0 || i == img.size() - 1)) {
                height += heightLinha;
                vetHeight[j] = heightLinha;
                j++;
                heightLinha = 0;
            }
        }

        ImageProcessor imagemFinal = new ByteProcessor(width, height);
        imagemFinal.invert();
        int accX = 0, accY = 0;
        for (i = 0, j = 0; i < img.size(); i++) {
            imagemFinal.insert(img.get(i), 0 + accX, 0 + accY);
            if (i > 0 && i % numColunas == 0) {
                accX = 0;
                accY += vetHeight[j];
                j++;
            } else {
                accX += img.get(i).getWidth();
            }
        }

        try {
            ImageIO.write((BufferedImage) imagemFinal.createImage(), "JPG", new File(onde + ".png"));
        } catch (Exception e) {
        }
    }

}
