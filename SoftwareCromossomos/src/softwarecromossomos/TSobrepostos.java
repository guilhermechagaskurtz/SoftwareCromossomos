package softwarecromossomos;

import java.awt.image.*;
import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.awt.event.*;
import ij.process.*;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.Straightener;
import ij.gui.Roi;
import ij.gui.PolygonRoi;
import ij.*;
import mestradocromossomos3.TPonto;

public class TSobrepostos implements MouseListener, ActionListener {

    public final int LIMITE = 5, DIVISAO = 6, PONTO = 7;
    public final int CORREDOR = 1, PAREDE = 0, SOLUCAO = 2, ERRADO = 3, PERCORRIDO = 4;
    ImageIcon imagemPrincipal;
    JLabel lblImagemPrincipal;
    JButton botaoProximo = new JButton("Adicionar");
    JButton botaoFinalizar = new JButton("Finalizar");
    JLabel lblPontos = new JLabel("");
    JPanel pnlPrincipal = new JPanel();
    JPanel pnlDireita = new JPanel();
    JPanel pnlEsquerda = new JPanel();
    JPanel pnlBotoes = new JPanel();
    JDialog dialog = new JDialog();
    Vector<TPonto> limites = new Vector<TPonto>();
    Vector<TPonto> divisoes = new Vector<TPonto>();
    Vector<TPonto> pontos = new Vector<TPonto>();
    Vector<TPonto> limitesCaminho = new Vector<TPonto>();
    Vector<TPonto> caminho = new Vector<TPonto>();
    Vector<Vector<TPonto>> caminhos = new Vector<Vector<TPonto>>();
    Vector<ImageProcessor> ipRetorno = new Vector<ImageProcessor>();

    //ImageProcessor ip;
    ImageProcessor ipMask, ipOriginal, ipSkeleton;
    int mat[][];
    TPonto inicio, fim;
    BufferedImage buffer;

    public TSobrepostos() {
    }

    /**
     * cria uma copia da imagem e retorna
     *
     * @param origem	imagem a ser copiada
     * @return copia da imagem origem
     */
    public BufferedImage criaCopiaDaImagem(BufferedImage origem) {
        BufferedImage destino = new BufferedImage(origem.getWidth(), origem.getHeight(), origem.getType());
        Graphics2D g = destino.createGraphics();
        g.drawImage(origem, 0, 0, origem.getWidth(), origem.getHeight(), 0, 0, destino.getWidth(), destino.getHeight(), null);
        g.dispose();
        return destino;
    }

    /**
     * inicia a criacao da interface
     *
     */
    public void Inicia() {
        buffer = criaCopiaDaImagem((BufferedImage) ipOriginal.createImage());
        imagemPrincipal = new ImageIcon(buffer);
        lblImagemPrincipal = new JLabel(imagemPrincipal);
        lblImagemPrincipal.addMouseListener(this);
        pnlEsquerda.add(lblImagemPrincipal);

        botaoProximo.addActionListener(this);
        botaoFinalizar.addActionListener(this);
        pnlBotoes.setLayout(new GridLayout(2, 1));
        pnlBotoes.add(botaoProximo);
        pnlBotoes.add(botaoFinalizar);

        pnlDireita.setLayout(new GridLayout(2, 1));
        pnlDireita.add(lblPontos);
        pnlDireita.add(pnlBotoes);

        pnlPrincipal.setLayout(new GridLayout(1, 2));
        pnlPrincipal.add(pnlEsquerda);
        pnlPrincipal.add(pnlDireita);

        dialog.getContentPane().add(pnlPrincipal);
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
        dialog.setTitle("Tratar Sobrepostos");
    }

    /**
     * calcula a distancia entre dois pontos
     *
     * @param xIni	valor x do ponto inicial
     * @param yIni	valor y do ponto inicial
     * @param xFim	valor x do ponto final
     * @param yFim	valor y do ponto final
     * @return distancia entre os pontos
     */
    public double distancia(int xIni, int yIni, int xFim, int yFim) {
        double dist;
        double xAoQuadrado = (xFim - xIni) * (xFim - xIni);
        double yAoQuadrado = (yFim - yIni) * (yFim - yIni);
        dist = Math.sqrt(xAoQuadrado + yAoQuadrado);
        return dist;
    }

    /**
     * verifica se os dois pontos sao iguais
     *
     * @param p1	ponto 1
     * @param p2	ponto 2
     * @return verdadeiro se os pontos sao iguais
     */
    public boolean igual(TPonto p1, TPonto p2) {
        if (p1.getColuna() == p2.getColuna() && p1.getLinha() == p2.getLinha()) {
            return true;
        }
        return false;
    }

    /**
     * define o caminho das retas dos cromossomos da imagem atraves de um
     * algoritmo de saida do labirinto
     *
     */
    public void defineCaminho() {
        int aux, i, j, menor, linha, coluna;
        double menorDist;
        TPonto p, q;
        caminho.add(new TPonto(inicio.getLinha(), inicio.getColuna()));
        for (i = 0; i < ipSkeleton.getHeight(); i++) {
            for (j = 0; j < ipSkeleton.getWidth(); j++) {
                aux = mat[i][j];
                System.out.print(aux);
                if ((aux == SOLUCAO) && (i != inicio.getLinha() || j != inicio.getColuna()) && (i != fim.getLinha() || j != fim.getColuna())) {
                    caminho.add(new TPonto(i, j));
                }
            }
            System.out.println();
        }
        caminho.add(new TPonto(fim.getLinha(), fim.getColuna()));
        for (i = 0; i < caminho.size() - 1; i++) {
            p = (TPonto) caminho.get(i);
            menor = i + 1;
            menorDist = 100000;
            for (j = i + 1; j < caminho.size() - 1; j++) {
                q = (TPonto) caminho.get(j);
                if (distancia(p.getColuna(), p.getLinha(), q.getColuna(), q.getLinha()) < menorDist) {
                    menorDist = distancia(p.getColuna(), p.getLinha(), q.getColuna(), q.getLinha());
                    menor = j;
                }
            }
            if (menorDist < 2) {
                p = (TPonto) caminho.get(i + 1);
                q = (TPonto) caminho.get(menor);
                linha = p.getLinha();
                coluna = p.getColuna();
                p.setLinha(q.getLinha());
                p.setColuna(q.getColuna());
                q.setLinha(linha);
                q.setColuna(coluna);
            } else {
                caminho.removeElementAt(menor);
            }
        }
        System.out.println("CAMINHO");
        for (i = 0; i < caminho.size(); i++) {
            p = (TPonto) caminho.get(i);
            System.out.println("(" + p.getLinha() + "," + p.getColuna() + ")");
            if (i < caminho.size() - 1) {
                q = (TPonto) caminho.get(i + 1);
                System.out.println(distancia(p.getColuna(), p.getLinha(), q.getColuna(), q.getLinha()));
            }
        }
    }

    /**
     * metodo recursivo que acha a saida do labirinto
     *
     * @param lin	linha do ponto
     * @param col	coluna do ponto
     * @return verdadeiro se achou a saida
     */
    public boolean achaSaida(int lin, int col) {
        boolean achou;
        if (lin == fim.getLinha() && col == fim.getColuna()) {
            return true;
        } else {
            mat[lin][col] = PERCORRIDO;
            achou = false;

            if (col + 1 < ipSkeleton.getWidth() && mat[lin][col + 1] == CORREDOR && achou == false) { //a direita esta livre
                achou = achaSaida(lin, col + 1);
            }
            if (col - 1 > 0 && mat[lin][col - 1] == CORREDOR && achou == false) { //a esquerda esta livre
                achou = achaSaida(lin, col - 1);
            }
            if (lin - 1 > 0 && mat[lin - 1][col] == CORREDOR && achou == false) { //acima esta livre
                achou = achaSaida(lin - 1, col);
            }
            if (lin + 1 < ipSkeleton.getHeight() && mat[lin + 1][col] == CORREDOR && achou == false) { //a direita esta livre
                achou = achaSaida(lin + 1, col);
            }
            if (lin - 1 >= 0 && col + 1 < ipSkeleton.getWidth() && mat[lin - 1][col + 1] == CORREDOR && achou == false) { //a direita-acima esta livre
                achou = achaSaida(lin - 1, col + 1);
            }
            if (lin - 1 >= 0 && col - 1 >= 0 && mat[lin - 1][col - 1] == CORREDOR && achou == false) { //a esquerda-acima esta livre
                achou = achaSaida(lin - 1, col - 1);
            }
            if (lin + 1 < ipSkeleton.getHeight() && col + 1 < ipSkeleton.getWidth() && mat[lin + 1][col + 1] == CORREDOR && achou == false) { //a direita-abaixo esta livre
                achou = achaSaida(lin + 1, col + 1);
            }
            if (lin + 1 < ipSkeleton.getHeight() && col - 1 >= 0 && mat[lin + 1][col - 1] == CORREDOR && achou == false) { //a esquerda-abaixo esta livre
                achou = achaSaida(lin + 1, col - 1);
            }
            if (achou) {
                mat[lin][col] = SOLUCAO;
            } else {
                mat[lin][col] = ERRADO;
            }
        }
        return achou;
    }

    public void mouseClicked(MouseEvent e) {
        int k, menor = 0, menorDiv = 0;
        double menorDist = 100000, menorDistDiv = 100000;
        int x = e.getX();
        int y = e.getY();
        TPonto p, q;

        if (limitesCaminho.size() < 2) {
            for (k = 0; k < limites.size(); k++) {
                p = (TPonto) limites.get(k);
                if (distancia(x, y, p.getColuna(), p.getLinha()) < menorDist) {
                    menorDist = distancia(x, y, p.getColuna(), p.getLinha());
                    menor = k;
                }
            }

            for (k = 0; k < divisoes.size(); k++) {
                p = (TPonto) divisoes.get(k);
                if (distancia(x, y, p.getColuna(), p.getLinha()) < menorDistDiv) {
                    menorDistDiv = distancia(x, y, p.getColuna(), p.getLinha());
                    menorDiv = k;
                }
            }
            if (menorDist < menorDistDiv) {
                p = (TPonto) limites.get(menor);
            } else {
                p = (TPonto) divisoes.get(menorDiv);
            }

            if (limitesCaminho.size() == 0) {
                pintaCirculo(p.getColuna(), p.getLinha());
                limitesCaminho.addElement(new TPonto(p.getLinha(), p.getColuna()));
                lblPontos.setText(p.getLinha() + "," + p.getColuna());
            }
            if (limitesCaminho.size() == 1) {
                q = (TPonto) limitesCaminho.get(0);
                if (!(igual(p, q))) {
                    pintaCirculo(p.getColuna(), p.getLinha());
                    limitesCaminho.addElement(new TPonto(p.getLinha(), p.getColuna()));
                    lblPontos.setText(lblPontos.getText() + " - " + p.getLinha() + "," + p.getColuna());
                    dialog.pack();
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Defina somente dois limites");
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == botaoFinalizar) {
            for (int k = 0; k < caminhos.size(); k++) {
                caminho = (Vector<TPonto>) caminhos.get(k);
                adicionaPontos(ipMask, ipSkeleton, caminho.size() - 1, caminho.size() - 2, true);
                adicionaPontos(ipMask, ipSkeleton, 0, 1, false);
                ImageProcessor ret = desentortar(true).convertToByte(true);
                ipRetorno.addElement(ret);
            }
            dialog.dispose();
        }
        if (e.getSource() == botaoProximo) {
            if (limitesCaminho.size() == 2) {
                mat = new int[ipSkeleton.getHeight()][ipSkeleton.getWidth()];
                for (int i = 0; i < ipSkeleton.getHeight(); i++) {
                    for (int j = 0; j < ipSkeleton.getWidth(); j++) {
                        if ((int) ipSkeleton.get(j, i) == 0) {
                            mat[i][j] = CORREDOR;
                        } else if ((int) ipSkeleton.get(j, i) == 255) {
                            mat[i][j] = PAREDE;
                        }
                    }
                }
                caminho.clear();
                inicio = (TPonto) limitesCaminho.get(0);
                fim = (TPonto) limitesCaminho.get(1);
                if (achaSaida(inicio.getLinha(), inicio.getColuna())) {
                    defineCaminho();
                    caminhos.addElement(new Vector<TPonto>(caminho));
                    limitesCaminho.clear();
                }
                lblPontos.setText("");
            }

        }
    }

    /**
     * desentorta a imagem
     *
     * @param flag	se eh para remover ou nao os pontos do meio do caminho
     * @return imagem desentortada
     */
    public ImageProcessor desentortar(boolean flag) {
        TPonto ponto;
        int j;
        if (flag == true) {
            for (int i = 1; i < caminho.size(); i++) {
                for (j = 0; j < 6 && i < caminho.size() - 1; j++) {
                    caminho.removeElementAt(i);
                }
            }
        }
        //removePontosDesnecessarios(ipSkeleton);
        int xPoints[] = new int[caminho.size()];
        int yPoints[] = new int[caminho.size()];
        int nPoints = 0;
        for (int i = 0; i < caminho.size(); i++) {
            ponto = (TPonto) caminho.get(i);
            xPoints[i] = ponto.getColuna();
            yPoints[i] = ponto.getLinha();
            nPoints++;
        }
        //tira as bordas da imagem
        for (int i = 0; i < ipOriginal.getHeight(); i++) {
            ipOriginal.set(0, i, 0);
            ipOriginal.set(ipOriginal.getWidth() - 1, i, 0);
        }
        for (int i = 0; i < ipOriginal.getWidth(); i++) {
            ipOriginal.set(i, 0, 0);
            ipOriginal.set(i, ipOriginal.getHeight() - 1, 0);
        }

        /*for(int i=0; i<ipOriginal.getHeight();i++){
         for(j=0; j<ipOriginal.getWidth();j++){
         System.out.print((int)ipOriginal.get(j,i)+"\t");
         }
         System.out.println();
         }
         System.out.println("---------------------------------");*/
        ImagePlus imp = new ImagePlus("", ipOriginal);

        imp.setRoi(new PolygonRoi(xPoints, yPoints, nPoints, Roi.POLYLINE));
        
        Straightener straightener = new Straightener();
        
        ImageProcessor ip2 = straightener.straightenLine(imp, 30);
        
        /*for(int i=0; i<ip2.getHeight();i++){
         for(j=0; j<ip2.getWidth();j++){
         System.out.print((int)ip2.get(j,i)+"-");
         }
         System.out.println();
         }*/
        System.out.println("---------------------------------");
        return ip2;
    }

    /**
     * calcula o numero de pontos limites e divisoes das retas presentes na
     * imagem
     *
     */
    public void contaLimitesEdivisoes() {
        int i, j, m, n, count;
        limites.clear();
        divisoes.clear();
        pontos.clear();
        for (i = 0; i < ipSkeleton.getHeight(); i++) {
            for (j = 0; j < ipSkeleton.getWidth(); j++) {
                count = 0;
                if ((int) ipSkeleton.get(j, i) == 0) {
                    for (m = -1; m <= 1; m++) {
                        for (n = -1; n <= 1; n++) {
                            if (!(n == 0 && m == 0)) {
                                if (j + n >= 0 && i + m >= 0 && j + n < ipSkeleton.getWidth() && i + m < ipSkeleton.getHeight()
                                        && (int) ipSkeleton.get(j + n, i + m) == 0) {
                                    count++;
                                }
                            }
                        }
                    }
                }
                if (count == 1) {
                    limites.addElement(new TPonto(i, j));
                }
                if (count >= 3) {
                    divisoes.addElement(new TPonto(i, j));
                }
                if (count == 2) {
                    pontos.addElement(new TPonto(i, j));
                }
            }
        }
    }

    /**
     * verifica se o ponto p pertence a reta formada pelos pontos p1 e p2
     *
     * @param p1	ponto 1 formador da reta
     * @param p2	ponto 2 formador da reta
     * @param p	ponto a ser verificado
     * @return	verdadeiro ou falso dependendo se o ponto pertence ou nao a reta
     */
    public boolean pertenceAreta(TPonto p1, TPonto p2, TPonto p) {
        int a, b, x, y;
        if (p1.getColuna() == p2.getColuna() && p1.getColuna() == p.getColuna()) {
            return true;
        }
        if (p1.getColuna() == p2.getColuna() && p1.getColuna() != p.getColuna()) {
            return false;
        }
        if (p1.getLinha() == p2.getLinha() && p1.getLinha() == p.getLinha()) {
            return true;
        }
        if (p1.getLinha() == p2.getLinha() && p1.getLinha() != p.getLinha()) {
            return false;
        }
        a = (p1.getLinha() - p2.getLinha()) / (p1.getColuna() - p2.getColuna());
        b = p2.getLinha() - ((p1.getLinha() - p2.getLinha()) / (p1.getColuna() - p2.getColuna())) * p2.getColuna();
        x = p.getColuna();
        y = a * x + b;
        if (y == p.getLinha()) {
            return true;
        }
        return false;
    }

    /**
     * adiciona pontos na reta da imagem de forma que ela aumente seu tamanho
     * nas pontas
     *
     * @param ipMask	mascara da imagem original
     * @param ip	imagem original
     * @param p1	posicao 1 (antepenultima ou segunda) do caminho
     * @param p2	posicao 2 (ultima ou primeira) do caminho
     * @param ehParaAdicionarNoFim	verifica se eh para adicionar no fim ou no
     * inicio da reta
     */
    public void adicionaPontos(ImageProcessor ipMask, ImageProcessor ip, int p1, int p2, boolean ehParaAdicionarNoFim) {
        TPonto p, q;
        int a, b, x, y;
        int count = 0;
        //Adiciona outros pontos na linha
        try {
            p = caminho.get(p1);
            q = caminho.get(p2);
            //se for uma reta paralela a y
            if (p.getColuna() == q.getColuna()) {
                x = p.getColuna();
                //em direcao pra baixo
                if (p.getLinha() > q.getLinha()) {
                    y = p.getLinha() + 1;
                    //SEMPRE VERIFICA SE NAO SAIU DA PARTE BRANCA DA MASCARA TAMBEM
                    while ((y <= ip.getHeight() - 1) && ((int) ipMask.get(x, y) == 255)) {
                        if (ehParaAdicionarNoFim) {
                            caminho.addElement(new TPonto(y, x));
                        } else {
                            caminho.add(0, new TPonto(y, x));
                        }
                        ip.set(x, y, 0);
                        y++;
                        count++;
                    }
                }
                //em direcao pra cima
                if (q.getLinha() > p.getLinha()) {
                    y = p.getLinha() - 1;
                    //SEMPRE VERIFICA SE NAO SAIU DA PARTE BRANCA DA MASCARA TAMBEM
                    while ((y >= 0) && ((int) ipMask.get(x, y) == 255)) {
                        if (ehParaAdicionarNoFim) {
                            caminho.addElement(new TPonto(y, x));
                        } else {
                            caminho.add(0, new TPonto(y, x));
                        }
                        ip.set(x, y, 0);
                        y--;
                        count++;
                    }
                }
            } //se nao for, entao pode calcular a equacao
            else {
                a = (p.getLinha() - q.getLinha()) / (p.getColuna() - q.getColuna());
                b = q.getLinha() - ((p.getLinha() - q.getLinha()) / (p.getColuna() - q.getColuna())) * q.getColuna();
                //se for para direita
                if (p.getColuna() > q.getColuna()) {
                    x = p.getColuna() + 1;
                    while (x <= ip.getWidth() - 1) {
                        y = a * x + b;
                        //SEMPRE VERIFICA SE NAO SAIU DA PARTE BRANCA DA MASCARA TAMBEM
                        if ((y >= 0 && y <= ip.getHeight() - 1) && ((int) ipMask.get(x, y) == 255)) {
                            if (ehParaAdicionarNoFim) {
                                caminho.addElement(new TPonto(y, x));
                            } else {
                                caminho.add(0, new TPonto(y, x));
                            }
                            ip.set(x, y, 0);
                            x++;
                            count++;
                        } else {
                            break;
                        }
                    }
                } //se for para a esquerda
                else {
                    x = p.getColuna() - 1;
                    while (x >= 0) {
                        y = a * x + b;
                        //SEMPRE VERIFICA SE NAO SAIU DA PARTE BRANCA DA MASCARA TAMBEM
                        if ((y >= 0 && y <= ip.getHeight() - 1) && ((int) ipMask.get(x, y) == 255)) {
                            if (ehParaAdicionarNoFim) {
                                caminho.addElement(new TPonto(y, x));
                            } else {
                                caminho.add(0, new TPonto(y, x));
                            }
                            ip.set(x, y, 0);
                            x--;
                            count++;
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        System.out.println("CAMINHO AUMENTADO");
        for (int i = 0; i < caminho.size(); i++) {
            p = (TPonto) caminho.get(i);
            System.out.println("(" + p.getLinha() + "," + p.getColuna() + ")");
        }
    }

    /**
     * desenha um circulo na imagem principal
     *
     * @param x	posicao x
     * @param y posicao y
     */
    public void pintaCirculo(int x, int y) {
        Graphics2D graphics = buffer.createGraphics();
        graphics.setColor(new Color(255, 255, 255));
        graphics.drawOval(x - 10, y - 10, 20, 20);
        graphics.dispose();
        imagemPrincipal.setImage(buffer);
        lblImagemPrincipal.setIcon(imagemPrincipal);
        lblImagemPrincipal.repaint();
    }

    /**
     * metodo principal que aplica os filtros antes de desentortar e tratar os
     * sobrepostos
     *
     * @param ipMaskf	mascara da imagem
     * @param ipOriginalf	imagem original
     * @param flag	verifica se eh para mostrar a interface novamente ou se esta
     * somente desentortando a mascara
     */
    public void vai(ImageProcessor ipMaskf, ImageProcessor ipOriginalf, boolean flag) {
        int i, j;
        ipRetorno.clear();
        ipMask = ipMaskf;
        ipOriginal = ipOriginalf;
        ipSkeleton = ipMaskf.duplicate();
        //tira as partes brancas que encostam nas bordas
        for (i = 0; i < ipSkeleton.getHeight(); i++) {
            ipSkeleton.set(0, i, 0);
            ipSkeleton.set(ipMask.getWidth() - 1, i, 0);
        }
        for (j = 0; j < ipSkeleton.getWidth(); j++) {
            ipSkeleton.set(j, 0, 0);
            ipSkeleton.set(j, ipMask.getHeight() - 1, 0);
        }
        //faz isso para focar a aparecer 2 limites
        for (i = 0; i < 10; i++) {
            ipSkeleton.smooth();
            ipSkeleton.threshold(127);
        }
        ipSkeleton.dilate();
        ipSkeleton.erode();
        GaussianBlur g = new GaussianBlur();
        g.blurGaussian(ipSkeleton, 3, 3, 0.002);
        ipSkeleton.threshold(127);
        //Inverte as cores
        ipSkeleton.invert();
        //return ipSkeleton;
        ((ByteProcessor) ipSkeleton).skeletonize();
        ipRetorno.clear();
        //conta o numero de pixeis no fim das linhas e de divisoes
        if (flag == true) {
            contaLimitesEdivisoes();
            Inicia();
        } else {
            for (int k = 0; k < caminhos.size(); k++) {
                caminho = (Vector<TPonto>) caminhos.get(k);
				//adicionaPontos(ipMask,ipSkeleton,caminho.size()-1,caminho.size()-2,true);
                //adicionaPontos(ipMask,ipSkeleton,0,1,false);
                ImageProcessor ret = desentortar(false).convertToByte(true);
                /*for(int p=0; p<ret.getHeight();p++){
                 for(int q=0; q<ret.getWidth();q++){
                 if((int)ret.get(q,p)>0) System.out.print("1");
                 else System.out.print("0"); 
                 }
                 System.out.println();
                 }*/
                ipRetorno.addElement(ret);
            }
        }
    }

    /**
     * reotrna as imagens que estavam sobrepostas ja desentortadas
     *
     * @return	vetor das imagens desentortadas
     */
    public Vector<ImageProcessor> getRetorno() {
        return ipRetorno;
    }
}
