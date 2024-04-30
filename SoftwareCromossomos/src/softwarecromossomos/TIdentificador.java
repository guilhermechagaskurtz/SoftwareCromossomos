package softwarecromossomos;

import ij.process.*;
import java.awt.image.*;
import java.io.File;
import java.util.*;
import mestradocromossomos3.Cariotipo;
import mestradocromossomos3.Cromossomo;
import mestradocromossomos3.DetectorCentromeroGeral;
import mestradocromossomos3.IdentificadorCromossomo;
import mestradocromossomos3.ParametrosIdentificador;
import mestradocromossomos3.TPonto;
import mestradocromossomos3.UtilJuntas;

public class TIdentificador {

    int w, h;
    public final int BRANCO = 0, PRETO = 1;
    public final int METACENTRICO = 0, SUBMETACENTRICO = 1, ACROCENTRICO = -1;
    public final double VARIACAOTAMANHO = 12;
    private ImageProcessor ipMatrizCromossomosOriginal[];
    private ImageProcessor ipMatrizCromossomosMascara[];
    private ImageProcessor ipMatrizCromossomosOriginalTortos[];
    private ImageProcessor ipMatrizCromossomosMascaraTortos[];
    private ImageProcessor ipMatrizCromossomosOriginalRed[];
    private ImageProcessor ipMatrizCromossomosMascaraRed[];
    private ImageProcessor ipMatrizCromossomosOriginalRedInv[];
    private ImageProcessor ipMatrizCromossomosMascaraRedInv[];
    private int centromeros[];
    private int classificacaoCentromeros[];
    private double r1[];
    private double classificacaoBracoCurto[];
    private double classificacaoBracoLongo[];
    private double classificacaoTamanho[];
    private double assinatura[];

    //private double tamanhoRelativo[] = {100,92,77,71,70,66,61,53,52,50,49,41,40,39,33,31,29,28,23,20,21,55,21};
    public TIdentificador() {
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
     * retira as linhas e colunas pretas envolta do cromossomo
     *
     */
    public void retiraPartesPretas() {
        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            ipMatrizCromossomosMascara[i].threshold(127);
            int xIni = numeroLinhasBordaEsquerda(ipMatrizCromossomosMascara[i], 255);
            int xFim = numeroLinhasBordaDireita(ipMatrizCromossomosMascara[i], 255);
            xFim = ipMatrizCromossomosMascara[i].getWidth() - xFim;
            xFim--;
            int yIni = numeroLinhasBordaCima(ipMatrizCromossomosMascara[i], 255);
            int yFim = numeroLinhasBordaBaixo(ipMatrizCromossomosMascara[i], 255);
            yFim = ipMatrizCromossomosMascara[i].getHeight() - yFim;
            yFim--;
            ipMatrizCromossomosOriginal[i] = copia(ipMatrizCromossomosOriginal[i], xIni, yIni, xFim, yFim);
            ipMatrizCromossomosMascara[i] = copia(ipMatrizCromossomosMascara[i], xIni, yIni, xFim, yFim);
        }
    }

    /**
     * retorna a cor que predomina na imagem faz uma copia de uma parte
     * retangular da imagem
     *
     * @param xIni	valor x do ponto inicial
     * @param yIni	valor y do ponto inicial
     * @param xFim	valor x do ponto final
     * @param yFim	valor y do ponto final
     * @return	copia da imagem delimitada pelos pontos
     */
    public ImageProcessor copia(ImageProcessor ip, int xIni, int yIni, int xFim, int yFim) {
        int width = xFim - xIni;
        int heigth = yFim - yIni;
        ImageProcessor ipNova = new ByteProcessor(width + 1, heigth + 1);
        for (int v = yIni; v <= yFim; v++) {
            for (int u = xIni; u <= xFim; u++) {
                int a = (int) (ip.get(u, v));
                ipNova.set(u - xIni, v - yIni, a);
            }
        }
        return ipNova;
    }

    /**
     * cria o vetor de assinaturas das imagens
     *
     */
    public void criaAssinatura() {
        double countOriginal, countMascara;
        assinatura = new double[ipMatrizCromossomosOriginal.length];
        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            countOriginal = 0;
            countMascara = 0;
            for (int l = 0; l < ipMatrizCromossomosOriginal[i].getHeight(); l++) {
                for (int c = 0; c < ipMatrizCromossomosOriginal[i].getWidth(); c++) {
                    countOriginal += (int) ipMatrizCromossomosOriginal[i].get(c, l);
                    countMascara += (int) ipMatrizCromossomosMascara[i].get(c, l);
                }
            }
            assinatura[i] = countOriginal / countMascara;
        }
    }

    /**
     * chama os metodos responsaveis pelo desentortamento dos cromossomos
     *
     */
    public void desentorta() {
        TDesentortador2 desentortador = new TDesentortador2();
        int SOBRA = 10;
        int countSobrepostos = 0;
        Vector<ImageProcessor> retornoDesentortado = new Vector<ImageProcessor>();
        Vector<ImageProcessor> retornoDesentortadoMask = new Vector<ImageProcessor>();
        Vector<ImageProcessor> retornoSobra = new Vector<ImageProcessor>();
        Vector<ImageProcessor> retornoSobraMask = new Vector<ImageProcessor>();

        double menor = 100000;
        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            if (ipMatrizCromossomosOriginal[i].getWidth() < menor) {
                menor = ipMatrizCromossomosOriginal[i].getWidth();
            }
        }
        for (int i = 0; i < ipMatrizCromossomosOriginal.length - countSobrepostos; i++) {
            //if(ipMatrizCromossomosOriginal[i].getWidth()>menor*1.4){
            retornoDesentortado = desentortador.vai(ipMatrizCromossomosMascara[i], ipMatrizCromossomosOriginal[i]);
            retornoDesentortadoMask = desentortador.vai(ipMatrizCromossomosMascara[i], ipMatrizCromossomosMascara[i]);
            if (retornoDesentortado.size() == 1) {
                ImageProcessor ipDesentortada = (ImageProcessor) retornoDesentortado.get(0);
                ImageProcessor ipMaskDesentortada = (ImageProcessor) retornoDesentortadoMask.get(0);
                ImageProcessor ipAux = new ByteProcessor(ipDesentortada.getHeight() + SOBRA, ipDesentortada.getWidth() + SOBRA);
                for (int p = 0; p < ipDesentortada.getHeight(); p++) {
                    for (int q = 0; q < ipDesentortada.getWidth(); q++) {
                        ipAux.set(ipAux.getWidth() - 1 - p - SOBRA / 2, q + SOBRA / 2, (int) ipDesentortada.get(q, p));
                    }
                }
                ImageProcessor ipMaskAux = new ByteProcessor(ipMaskDesentortada.getHeight() + SOBRA, ipMaskDesentortada.getWidth() + SOBRA);
                for (int p = 0; p < ipMaskDesentortada.getHeight(); p++) {
                    for (int q = 0; q < ipMaskDesentortada.getWidth(); q++) {
                        ipMaskAux.set(ipMaskAux.getWidth() - 1 - p - SOBRA / 2, q + SOBRA / 2, (int) ipMaskDesentortada.get(q, p));
                    }
                }
                ipMatrizCromossomosOriginal[i] = ipAux;
                ipMatrizCromossomosMascara[i] = ipMaskAux;
            } else {
                TSobrepostos sobrepostos = new TSobrepostos();
                sobrepostos.vai(ipMatrizCromossomosMascara[i], ipMatrizCromossomosOriginal[i], true);
                retornoDesentortado = (Vector<ImageProcessor>) sobrepostos.getRetorno().clone();
                if (retornoDesentortado.size() > 0) {
                    sobrepostos.vai(ipMatrizCromossomosMascara[i], ipMatrizCromossomosMascara[i], false);
                    retornoDesentortadoMask = (Vector<ImageProcessor>) sobrepostos.getRetorno().clone();
                    countSobrepostos++;
                    //joga os sobrepostos para o final dos vetores, pois depois eles serao descartados
                    for (int k = i; k < ipMatrizCromossomosOriginal.length - 1; k++) {
                        swapIP(ipMatrizCromossomosOriginal, k, k + 1);
                        swapIP(ipMatrizCromossomosMascara, k, k + 1);
                    }
                    //deixa eles de pe
                    for (int k = 0; k < retornoDesentortado.size(); k++) {
                        ImageProcessor ipDesentortada = (ImageProcessor) retornoDesentortado.get(k);
                        ImageProcessor ipMaskDesentortada = (ImageProcessor) retornoDesentortadoMask.get(k);
                        ImageProcessor ipAux = new ByteProcessor(ipDesentortada.getHeight() + SOBRA, ipDesentortada.getWidth() + SOBRA);
                        for (int p = 0; p < ipDesentortada.getHeight(); p++) {
                            for (int q = 0; q < ipDesentortada.getWidth(); q++) {
                                ipAux.set(ipAux.getWidth() - 1 - p - SOBRA / 2, q + SOBRA / 2, (int) ipDesentortada.get(q, p));
                            }
                        }
                        ImageProcessor ipMaskAux = new ByteProcessor(ipMaskDesentortada.getHeight() + SOBRA, ipMaskDesentortada.getWidth() + SOBRA);
                        for (int p = 0; p < ipMaskDesentortada.getHeight(); p++) {
                            for (int q = 0; q < ipMaskDesentortada.getWidth(); q++) {
                                ipMaskAux.set(ipMaskAux.getWidth() - 1 - p - SOBRA / 2, q + SOBRA / 2, (int) ipMaskDesentortada.get(q, p));
                            }
                        }
                        //e joga no vetor dos que nao tao mais sobrepostos
                        retornoSobra.addElement(ipAux.duplicate());
                        retornoSobraMask.addElement(ipMaskAux.duplicate());
                    }
                    i--;
                }
            }
            //}
        }
        ImageProcessor ipOriginalMaisRetorno[] = new ImageProcessor[ipMatrizCromossomosOriginal.length - countSobrepostos + retornoSobra.size()];
        ImageProcessor ipMascaraMaisRetorno[] = new ImageProcessor[ipMatrizCromossomosMascara.length - countSobrepostos + retornoSobraMask.size()];
        int i;
        for (i = 0; i < ipMatrizCromossomosOriginal.length - countSobrepostos; i++) {
            ipOriginalMaisRetorno[i] = ipMatrizCromossomosOriginal[i];
            ipMascaraMaisRetorno[i] = ipMatrizCromossomosMascara[i];
        }
        for (int k = 0; k < retornoSobra.size(); k++) {
            ipOriginalMaisRetorno[i] = (ImageProcessor) retornoSobra.get(k);
            ipMascaraMaisRetorno[i] = (ImageProcessor) retornoSobraMask.get(k);
            i++;
        }
        ipMatrizCromossomosOriginal = ipOriginalMaisRetorno;
        ipMatrizCromossomosMascara = ipMascaraMaisRetorno;

    }

    /**
     * ordena as imagens dos cromossomos pela altura dos mesmos
     *
     */
    public void ordena() {
        for (int i = 0; i < ipMatrizCromossomosOriginal.length - 1; i++) {
            for (int j = 0; j < ipMatrizCromossomosOriginal.length - i - 1; j++) {
                if (ipMatrizCromossomosOriginal[j].getHeight() < ipMatrizCromossomosOriginal[j + 1].getHeight()) {
                    swapIP(ipMatrizCromossomosOriginal, j, j + 1);
                    swapIP(ipMatrizCromossomosMascara, j, j + 1);
                    swapDouble(assinatura, j, j + 1);
                }
            }
        }
    }

    /**
     * acha a posicao do centromero do cromossomo da imagem ip
     *
     * @param ip	imagem original
     * @param ipMascara	mascara da imagem
     * @return	linha da posicao do centromero
     */
    public int verificaPosicaoCentromero(ImageProcessor ip, ImageProcessor ipMascara) {
        int linha = 0;
        double largura = 0, menorLargura = 32000;
        int inicio = (int) Math.round(ip.getHeight() * 0.1) * 2;
        for (int i = inicio; i < ipMascara.getHeight() - inicio; i++) {
            largura = numeroCoresLinha(ipMascara, i, 255);
            if (largura < menorLargura) {
                linha = i;
                menorLargura = largura;
            }
        }
        if (linha > (ipMascara.getHeight() / 2)) {;
            ip.rotate(180);
            ipMascara.rotate(180);
            linha = ip.getHeight() - linha - 1;
        }
        return linha;
    }

    /**
     * cria o vetor de posicoes do centromero de cada imagem
     *
     */
    public void defineCentromero() {
        centromeros = new int[ipMatrizCromossomosOriginal.length];
        classificacaoCentromeros = new int[ipMatrizCromossomosOriginal.length];
        r1 = new double[ipMatrizCromossomosOriginal.length];
        classificacaoBracoCurto = new double[ipMatrizCromossomosOriginal.length];
        classificacaoBracoLongo = new double[ipMatrizCromossomosOriginal.length];
        classificacaoTamanho = new double[ipMatrizCromossomosOriginal.length];
        //Pega a posicao do centromero de cada cromossomo e verifica se eh sub,meta ou acro
        for (int i = 0; i < ipMatrizCromossomosMascara.length; i++) {
            centromeros[i] = verificaPosicaoCentromero(ipMatrizCromossomosOriginal[i], ipMatrizCromossomosMascara[i]);
            classificacaoBracoLongo[i] = ipMatrizCromossomosMascara[i].getHeight() - centromeros[i] - 2;
            classificacaoBracoCurto[i] = centromeros[i] - 2;
            classificacaoTamanho[i] = ipMatrizCromossomosMascara[i].getHeight();
            r1[i] = (double) (classificacaoBracoLongo[i] / classificacaoBracoCurto[i]);
            if (r1[i] >= 1 && r1[i] <= 1.49) {
                classificacaoCentromeros[i] = METACENTRICO;
            }
            if (r1[i] > 1.49 && r1[i] <= 3.25) {
                classificacaoCentromeros[i] = SUBMETACENTRICO;
            }
            if (r1[i] > 3.25) {
                classificacaoCentromeros[i] = ACROCENTRICO;
            }
        }
    }

    /**
     * acha a posicao do centromero do cromossomo da imagem ipErodida
     *
     * @param ipMascara	mascara da imagem
     * @return	linha da posicao do centromero
     */
    public int verificaPosicaoCentromeroErodida(ImageProcessor ipMascara) {
        int linha = 0;
        double largura = 0, menorLargura = 32000;
        int inicio = (int) Math.round(ipMascara.getHeight() * 0.1) * 2;
        for (int i = inicio; i < ipMascara.getHeight() - inicio; i++) {
            largura = numeroCoresLinha(ipMascara, i, 255);
            if (largura < menorLargura) {
                linha = i;
                menorLargura = largura;
            }
        }
        if (linha > (ipMascara.getHeight() / 2)) {;
            linha = ipMascara.getHeight() - linha - 1;
        }
        return linha;
    }

    /**
     * acrescenda DIF linhas e colunas envolta da imagem
     *
     * @param DIF	numero de linhas e colunas a ser acrescentada
     * @param cor	cor das linhas e colunas
     */
    public void expandIP(int DIF, int cor) {
        for (int k = 0; k < ipMatrizCromossomosOriginal.length; k++) {
            ImageProcessor ipNova = new ByteProcessor(ipMatrizCromossomosOriginal[k].getWidth() + DIF, ipMatrizCromossomosOriginal[k].getHeight() + DIF);
            ImageProcessor ipMask = new ByteProcessor(ipMatrizCromossomosMascara[k].getWidth() + DIF, ipMatrizCromossomosMascara[k].getHeight() + DIF);
            if (cor == PRETO) {
                ipNova.setValue(0);
                ipMask.setValue(0);
            } else if (cor == BRANCO) {
                ipNova.invert();
                ipMask.invert();
            }
            for (int i = 0; i < ipMatrizCromossomosOriginal[k].getHeight(); i++) {
                for (int j = 0; j < ipMatrizCromossomosOriginal[k].getWidth(); j++) {
                    ipNova.set(j + DIF / 2, i + DIF / 2, (int) ipMatrizCromossomosOriginal[k].get(j, i));
                    ipMask.set(j + DIF / 2, i + DIF / 2, (int) ipMatrizCromossomosMascara[k].get(j, i));
                }
            }
            ipMatrizCromossomosMascara[k] = ipMask;
            ipMatrizCromossomosOriginal[k] = ipNova;
        }
    }

    /**
     * faz um backup dos cromossomso tortos antes de desentorta-los
     *
     */
    public void salvaTortos() {
        ipMatrizCromossomosOriginalTortos = new ImageProcessor[ipMatrizCromossomosOriginal.length];
        ipMatrizCromossomosMascaraTortos = new ImageProcessor[ipMatrizCromossomosMascara.length];
        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            //salva os tortos
            ipMatrizCromossomosOriginalTortos[i] = ipMatrizCromossomosOriginal[i];
            ipMatrizCromossomosMascaraTortos[i] = ipMatrizCromossomosMascara[i];
        }
    }

    /**
     * aplica um thershold as mascaras das imagens
     *
     */
    public void binariza() {
        for (int i = 0; i < ipMatrizCromossomosMascara.length; i++) {
            ipMatrizCromossomosMascara[i].threshold(127);
        }
    }

    /**
     * inverte a cor do background das imagens
     *
     */
    public void inverteFundo() {
        int l, c;
        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            ipMatrizCromossomosMascara[i].invert();
            for (l = 0; l < ipMatrizCromossomosOriginal[i].getHeight(); l++) {
                for (c = 0; c < ipMatrizCromossomosOriginal[i].getWidth(); c++) {
                    ipMatrizCromossomosOriginal[i].set(c, l, ipMatrizCromossomosMascara[i].get(c, l) + ipMatrizCromossomosOriginal[i].get(c, l));
                }
            }
        }
    }

    /**
     * inverte a cor do background das imagens dos tortos
     *
     */
    public void inverteFundoTortos() {
        int l, c;
        for (int i = 0; i < ipMatrizCromossomosOriginalTortos.length; i++) {
            ipMatrizCromossomosMascaraTortos[i].invert();
            for (l = 0; l < ipMatrizCromossomosOriginalTortos[i].getHeight(); l++) {
                for (c = 0; c < ipMatrizCromossomosOriginalTortos[i].getWidth(); c++) {
                    ipMatrizCromossomosOriginalTortos[i].set(c, l, ipMatrizCromossomosMascaraTortos[i].get(c, l) + ipMatrizCromossomosOriginalTortos[i].get(c, l));
                }
            }
        }
    }

    /**
     * cria um vetor de imagens redimensionadas que terao uma altura do tamanho
     * da maior altura entre os cromossomos
     *
     */
    public void criaImagemRedimensionada() {
        int maiorHeight = ipMatrizCromossomosOriginal[0].getHeight();
        ipMatrizCromossomosOriginalRed = new ImageProcessor[ipMatrizCromossomosOriginal.length];
        ipMatrizCromossomosMascaraRed = new ImageProcessor[ipMatrizCromossomosMascara.length];
        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            ipMatrizCromossomosOriginalRed[i] = ipMatrizCromossomosOriginal[i].resize(ipMatrizCromossomosOriginal[i].getWidth(), maiorHeight);
            ipMatrizCromossomosMascaraRed[i] = ipMatrizCromossomosMascara[i].resize(ipMatrizCromossomosMascara[i].getWidth(), maiorHeight);
        }
    }

    /**
     * cria um vetor com as imagens redimensionadas invertidas
     *
     */
    public void criaImagemRedimensionadaInv() {
        ipMatrizCromossomosOriginalRedInv = new ImageProcessor[ipMatrizCromossomosOriginalRed.length];
        ipMatrizCromossomosMascaraRedInv = new ImageProcessor[ipMatrizCromossomosMascaraRed.length];
        for (int i = 0; i < ipMatrizCromossomosOriginalRed.length; i++) {
            ipMatrizCromossomosOriginalRedInv[i] = ipMatrizCromossomosOriginalRed[i].duplicate();
            ipMatrizCromossomosOriginalRedInv[i].rotate(180);
            ipMatrizCromossomosMascaraRedInv[i] = ipMatrizCromossomosMascaraRed[i].duplicate();
            ipMatrizCromossomosMascaraRedInv[i].rotate(180);
        }
    }

    /**
     * ordena os cromossomos considerando a menor diferen�a de assinaturas,
     * comparando um cromossomo com o outro mas somente aqueles em que a
     * variacao de tamanho seja menor que 25%
     *
     */
    public void ordenaPorAssinaturas() {
        double melhorAss, ass, ass1, ass1Inv;
        int melhorAssIndice;
        //vai ate a penultima posicao pois deve-se achar quem eh o par dele e que vai ficar "do lado"
        for (int i = 0; i < ipMatrizCromossomosOriginalRed.length - 1; i += 2) {
            melhorAss = 10000000;
            melhorAssIndice = 0;
            for (int j = i + 1; j < ipMatrizCromossomosOriginalRed.length; j++) {
                if (i != j && desvioPercentual((int) classificacaoTamanho[i], (int) classificacaoTamanho[j]) < 0.25) {
                    ass1 = getAssinatura(ipMatrizCromossomosOriginalRed[i], ipMatrizCromossomosOriginalRed[j]);
                    ass1Inv = getAssinatura(ipMatrizCromossomosOriginalRedInv[i], ipMatrizCromossomosOriginalRed[j]);
                    if (ass1 < ass1Inv) {
                        ass = ass1;
                    } else {
                        ass = ass1Inv;
                    }

                    if (ass < melhorAss) {
                        melhorAss = ass;
                        melhorAssIndice = j;
                    }
                }
            }
            swapIP(ipMatrizCromossomosOriginal, i + 1, melhorAssIndice);
            swapIP(ipMatrizCromossomosMascara, i + 1, melhorAssIndice);
            swapIP(ipMatrizCromossomosOriginalRed, i + 1, melhorAssIndice);
            swapIP(ipMatrizCromossomosMascaraRed, i + 1, melhorAssIndice);
            swapIP(ipMatrizCromossomosOriginalRedInv, i + 1, melhorAssIndice);
            swapIP(ipMatrizCromossomosMascaraRedInv, i + 1, melhorAssIndice);
            swapInt(centromeros, i + 1, melhorAssIndice);
            swapInt(classificacaoCentromeros, i + 1, melhorAssIndice);
            swapDouble(r1, i + 1, melhorAssIndice);
            swapDouble(classificacaoBracoCurto, i + 1, melhorAssIndice);
            swapDouble(classificacaoBracoLongo, i + 1, melhorAssIndice);
            swapDouble(assinatura, i + 1, melhorAssIndice);
        }
    }

    /**
     * calcula o desvio percentual entre dois valores
     *
     */
    public double desvioPercentual(int x1, int x2) {
        double desvio = (double) (Math.abs(x1 - x2)) / ((x1 + x2) / 2);
        return desvio;
    }

    /**
     * retorna a assinatura da diferenca entre duas imagens
     *
     * @param ip1	imagem original
     * @param ip2	imagem que sera comparada
     * @return	assinatura da diferenca
     */
    public double getAssinatura(ImageProcessor ip1, ImageProcessor ip2) {
        double ass = 0;
        ImageProcessor ip2Resized = ip2.resize(ip1.getWidth(), ip2.getHeight());
        try {
            for (int l = 0; l < ip1.getHeight(); l++) {
                for (int c = 0; c < ip1.getWidth(); c++) {
                    int x = (int) ip1.get(c, l);
                    int y = (int) ip2Resized.get(c, l);
                    ass += (double) Math.abs(x - y);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ass = ass / (ip1.getHeight() * ip1.getWidth());
        return ass;
    }

    /**
     * realiza a identificacao dos cromossomos da imagem buffer
     *
     * @param buffer	imagem original
     * @param bufferMask	mascara da imagem
     * @return	conjunto de imagens dos cromossomos, posicao dos centromeros,
     * assinaturas, etc.
     */
    public Collection Identifica(BufferedImage buffer, BufferedImage bufferMask) {

        w = buffer.getWidth();
        h = buffer.getHeight();
        ImageProcessor ip = new ByteProcessor(buffer);
        ImageProcessor ipNova = new ByteProcessor(bufferMask);

        Segmentacao(ip, ipNova);

        //PARTE QUE TIRA AS BORDAS PRETAS, DEIXANDO SO O CROMOSSOMO NA IMAGEM
        retiraPartesPretas();

        //PARTE QUE EXPANDE A IMAGEM ORIGINAL
        expandIP(10, PRETO);

        //PARTE QUE SALVA OS TORTOS
        salvaTortos();

        //PARTE QUE DESENTORTA OS CROMOSSOMOS
        desentorta();

        //PARTE QUE BINARIZA A MASCARA
        binariza();

        //PARTE QUE PEGA A ASSINATURA DA IMAGEM
        criaAssinatura();

        //PARTE QUE TIRA AS BORDAS PRETAS, DEIXANDO SO O CROMOSSOMO NA IMAGEM
        retiraPartesPretas();

        //PARTE QUE ORDENA OS CROMOSSOMOS POR TAMANHO
        ordena();

        //PARTE PARA VERIFICAR A POSICAO DO CENTROMERO E DE DEIXAR O CROMOSSOMO EM PE
        defineCentromero();

        criaImagemRedimensionada();
        criaImagemRedimensionadaInv();
        ordenaPorAssinaturas();
        //PARTE QUE EXPANDE A IMAGEM ORIGINAL
        expandIP(30, PRETO);

        //PARTE QUE DEIXA O FUNDO BRANCO
        //inverteFundo();
        //inverteFundoTortos();
        //PARTE QUE CRIA O BUFFEREDIMAGE QUE VAI RETORNAR PARA O TMENUPRINCIPAL
        BufferedImage retBuffer[] = new BufferedImage[ipMatrizCromossomosOriginal.length];
        BufferedImage retBufferMask[] = new BufferedImage[ipMatrizCromossomosMascara.length];
        BufferedImage retBufferTortos[] = new BufferedImage[ipMatrizCromossomosOriginalTortos.length];
        BufferedImage retBufferTortosMask[] = new BufferedImage[ipMatrizCromossomosMascaraTortos.length];
        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            retBuffer[i] = (BufferedImage) ipMatrizCromossomosOriginal[i].createImage();
            retBufferMask[i] = (BufferedImage) ipMatrizCromossomosMascara[i].createImage();
        }
        for (int i = 0; i < ipMatrizCromossomosOriginalTortos.length; i++) {
            retBufferTortos[i] = (BufferedImage) ipMatrizCromossomosOriginalTortos[i].createImage();
            retBufferTortosMask[i] = (BufferedImage) ipMatrizCromossomosMascaraTortos[i].createImage();
        }
        Collection colecao = new ArrayList();
        colecao.add(retBuffer);
        colecao.add(retBufferMask);
        colecao.add(retBufferTortos);
        colecao.add(retBufferTortosMask);
        /*colecao.add(r1);
         colecao.add(assinatura);
         colecao.add(classificacaoBracoCurto);
         colecao.add(classificacaoTamanho);
         colecao.add(classificacaoCentromeros);*/
        return colecao;
    }

    /**
     * realiza a identificacao dos cromossomos da imagem buffer
     *
     * @param buffer	imagem original
     * @param bufferMask	mascara da imagem
     * @return	conjunto de imagens dos cromossomos, posicao dos centromeros,
     * assinaturas, etc.
     */
    public Cariotipo IdentificaNovo(BufferedImage buffer, BufferedImage bufferMask) {

        w = buffer.getWidth();
        h = buffer.getHeight();
        ImageProcessor ip = new ByteProcessor(buffer);
        ImageProcessor ipNova = new ByteProcessor(bufferMask);

        Segmentacao(ip, ipNova);

        ArrayList<Cromossomo> cromossomos = new ArrayList<Cromossomo>();

        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            System.out.println("Verificando Sobreposição imagem "+i);
            Cromossomo c = new Cromossomo(ipMatrizCromossomosOriginal[i]);
            //se for um cromossomo com outro sobreposto, dessobrepõe
            if(c.getLimites().size()> 2){
                TSobrepostosNew sobrepostos = new TSobrepostosNew(c);
                sobrepostos.vai();
                ArrayList<ImageProcessor> retornoSobrepostos = sobrepostos.getRetorno();
                for(int j=0; j<retornoSobrepostos.size(); j++){
                    new UtilJuntas().mostraMascara(retornoSobrepostos.get(j));
                    ImageProcessor ipExpandida = new UtilJuntas().expandeDiagonal(retornoSobrepostos.get(j), 255);
                    new UtilJuntas().mostraMascara(ipExpandida);
                    Cromossomo c2 = new Cromossomo(ipExpandida, false);//instancia um cromossomo, forçando que não seja sobreposto
                    cromossomos.add(c2);
                }
            }
            //se não, só adiciona na lista
            else{
                cromossomos.add(c);
            }
        }
        
        Cariotipo cariotipo = new Cariotipo(cromossomos);
        for (int i = 0; i < cromossomos.size(); i++) {
            Cromossomo c = cariotipo.getCromossomo(i);
            DetectorCentromeroGeral detector = new DetectorCentromeroGeral(c);
            String classificacao = detector.detecta();
            c.setCentromero(classificacao);
        }
        String path ="/resources/parametros.xml";
        ParametrosIdentificador param = new ParametrosIdentificador(path);
        IdentificadorCromossomo identificador = new IdentificadorCromossomo(param);
        for (int i = 0; i < cromossomos.size(); i++) {
            Cromossomo c = cariotipo.getCromossomo(i);
            c.setTipo(identificador.identifica_TMA_POR_TAMANHO(c));
            c.setChances(identificador.chances_TMA_POR_TAMANHO2(c));
        }
        cariotipo.ordenaPorTamanho();

        return cariotipo;
    }

    /**
     * retorna uma nova imagem com ip2 do lado de ip1
     *
     * @param ip1	imagem 1
     * @param ip2	imagem 2
     * @return	imagem unida
     */
    public ImageProcessor mergeIP(ImageProcessor ip1, ImageProcessor ip2) {
        int width, height, i, j;
        if (ip1.getHeight() > ip2.getHeight()) {
            height = ip1.getHeight();
        } else {
            height = ip2.getHeight();
        }
        width = ip1.getWidth() + ip2.getWidth();
        ImageProcessor ipMerge = new ByteProcessor(width, height);
        for (i = 0; i < ip1.getHeight(); i++) {
            for (j = 0; j < ip1.getWidth(); j++) {
                ipMerge.set(j, i, (int) ip1.get(j, i));
            }
        }
        for (i = 0; i < ip2.getHeight(); i++) {
            for (j = ip1.getWidth(); j < width; j++) {
                ipMerge.set(j, i, (int) ip2.get(j - ip1.getWidth(), i));
            }
        }
        return ipMerge;
    }

    /**
     * troca de posicao 2 imagens do vetor de imagem
     *
     * @param ip	vetor de imagens
     * @param menor	posicao 1
     * @param indice	posicao 2
     */
    public void swapIP(ImageProcessor[] ip, int menor, int indice) {
        ImageProcessor auxIp = ip[menor].duplicate();
        ip[menor] = ip[indice];
        ip[indice] = auxIp;
    }

    /**
     * troca de posicao 2 valores de um vetor de inteiros
     *
     * @param vetor	vetor de inteiros
     * @param menor	posicao 1
     * @param indice	posicao 2
     */
    public void swapInt(int vetor[], int menor, int indice) {
        int aux = vetor[menor];
        vetor[menor] = vetor[indice];
        vetor[indice] = aux;
    }

    /**
     * troca de posicao 2 valores de um vetor de doubles
     *
     * @param vetor	vetor de doubles
     * @param menor	posicao 1
     * @param indice	posicao 2
     */
    public void swapDouble(double vetor[], int menor, int indice) {
        double aux = vetor[menor];
        vetor[menor] = vetor[indice];
        vetor[indice] = aux;
    }

    /**
     * calcula quantos pixeis da respectiva cor tem na linha
     *
     * @param ip	imagem a ser verificada
     * @param linha	linha da imagem a ser verificada
     * @param cor	cor buscada
     * @return	numero de pixeis da cor "cor" na linha
     */
    public int numeroCoresLinha(ImageProcessor ip, int linha, int cor) {
        int count = 0;
        for (int i = 0; i < ip.getWidth(); i++) {
            int corAux = (int) ip.get(i, linha);
            if (corAux == cor) {
                count++;
            }
        }
        return count;
    }

    /**
     * Metodo que retorna a coluna que contem o primeiro pixel da cor "cor" da
     * linha passada como parametro
     *
     * @param ip	imagem a ser verificada
     * @param linha	linha da imagem a ser verificada
     * @param cor	cor buscada
     * @return	coluna da primeira ocorrencia de um pixel da cor "cor"
     */
    public int colunaPrimeiroPixelCor(ImageProcessor ip, int linha, int cor) {
        int i;
        for (i = 0; i < ip.getWidth(); i++) {
            int corAux = (int) ip.get(i, linha);
            if (corAux == cor) {
                break;
            }
        }
        return i;
    }

    /**
     * Metodo que retorna a coluna que contem o ultimo pixel da cor "cor" da
     * linha passada como parametro
     *
     * @param ip	imagem a ser verificada
     * @param linha	linha da imagem a ser verificada
     * @param cor	cor buscada
     * @return	coluna da ultima ocorrencia de um pixel da cor "cor"
     */
    public int colunaUltimoPixelCor(ImageProcessor ip, int linha, int cor) {
        int i;
        for (i = ip.getWidth() - 1; i >= 0; i--) {
            int corAux = (int) ip.get(i, linha);
            if (corAux == cor) {
                break;
            }
        }
        return i;
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
     * faz a segmentacao da imagem ip
     *
     * @param ip	imagem original
     * @param ipNova	mascara da imagem
     */
    public void Segmentacao(ImageProcessor ip, ImageProcessor ipNova) {
        int cor;
        int corPredominante = corQuePredomina(ipNova);
        int corCromossomos = 255 - corPredominante;
        Vector<TPonto> vetor = new Vector<TPonto>();
        TDna dna = new TDna();
        TPonto ponto;

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                cor = (int) (ipNova.get(j, i));
                //se achou um pixel da cor dos cromossomos
                if (cor == corCromossomos) {
                    //declara um cromossomo
                    TCromossomo cromossomo = new TCromossomo();
                    //e poe no array do vetor o ponto atual
                    vetor.addElement(new TPonto(i, j));
                    while (vetor.size() > 0) {
                        //salva o primeiro
                        ponto = (TPonto) vetor.firstElement();
                        //retira o primeiro
                        vetor.removeElementAt(0);
                        //poe os outros
                        if (ponto.getLinha() - 1 >= 0 && corCromossomos == (int) (ipNova.get(ponto.getColuna(), ponto.getLinha() - 1)) && jaNaoFoi(ponto.getColuna(), ponto.getLinha() - 1, vetor)) {
                            vetor.addElement(new TPonto(ponto.getLinha() - 1, ponto.getColuna()));
                        }
                        if (ponto.getLinha() + 1 < h && corCromossomos == (int) (ipNova.get(ponto.getColuna(), ponto.getLinha() + 1)) && jaNaoFoi(ponto.getColuna(), ponto.getLinha() + 1, vetor)) {
                            vetor.addElement(new TPonto(ponto.getLinha() + 1, ponto.getColuna()));
                        }
                        if (ponto.getColuna() - 1 >= 0 && corCromossomos == (int) (ipNova.get(ponto.getColuna() - 1, ponto.getLinha())) && jaNaoFoi(ponto.getColuna() - 1, ponto.getLinha(), vetor)) {
                            vetor.addElement(new TPonto(ponto.getLinha(), ponto.getColuna() - 1));
                        }
                        if (ponto.getColuna() + 1 < w && corCromossomos == (int) (ipNova.get(ponto.getColuna() + 1, ponto.getLinha())) && jaNaoFoi(ponto.getColuna() + 1, ponto.getLinha(), vetor)) {
                            vetor.addElement(new TPonto(ponto.getLinha(), ponto.getColuna() + 1));
                        }
                        //adiciona o ponto ao cromossomo
                        cromossomo.adicionaPixels(ponto.getLinha(), ponto.getColuna());
                        //e pinta de preto
                        ipNova.set(ponto.getColuna(), ponto.getLinha(), corPredominante);
                    }
                    //ao fim do while, todos os pixels da corCromossomo foram pintados de corPredominante
                    //e todos os pontos foram jogados em cromossomo, agora adiciona esse cromossomo ao
                    //dna
                    cromossomo.encontraOsMais();
                    dna.adicionaCromossomo(cromossomo);
                }
            }
        }
        //cria um array para guardar os ImageProcessor dos cromossomos originais
        //e dos cromossomos com a mascara, cada cromossomo com seu imageProcessor
        int largura = 0, altura = 0, l, c;
        //Declaro os vetores que vao guardar as imagens com uma posicao a mais para ter um criterio de parada
        //logo, a ultima posicao ira conter NULL
        ImageProcessor ipMatrizCromossomosOriginalTemp[] = new ImageProcessor[dna.getCromossomoSize()];
        ImageProcessor ipMatrizCromossomosMascaraTemp[] = new ImageProcessor[dna.getCromossomoSize()];
        for (int i = 0; i < dna.getCromossomoSize(); i++) {
            //System.out.println("Imagem "+i);
            TCromossomo cromo = new TCromossomo();
            cromo = dna.getCromossomoAt(i);
            largura = (cromo.getDireita() - cromo.getEsquerda()) + 1;
            altura = (cromo.getAbaixo() - cromo.getAcima()) + 1;
            //precisa fazer um teste pra ver se nao eh muito pequena a imagem
            if (altura != 0 && largura != 0) {
                int tamanho = (int) Math.sqrt(((largura + 1) * (largura + 1)) + ((altura + 1) * (altura + 1)));
                int difAltura = (tamanho - altura) / 2;
                int difLargura = (tamanho - largura) / 2;
                //System.out.println("Imagem com altura e largura de:"+altura+"-"+largura+" e tamanho de "+tamanho);
                //System.out.println("Com diferenca de altura de e largura de:"+difAltura+"-"+difLargura);
                ipMatrizCromossomosOriginalTemp[i] = new ByteProcessor(tamanho, tamanho);
                ipMatrizCromossomosOriginalTemp[i].setValue(255);
                ipMatrizCromossomosOriginalTemp[i].fill();
                ipMatrizCromossomosMascaraTemp[i] = new ByteProcessor(tamanho, tamanho);
                ipMatrizCromossomosMascaraTemp[i].setValue(0);
                //joga os pixels do respectivo cromossomo do DNA em seu respectivo ImageProcessor
                for (int k = 0; k < cromo.getPixelsSize(); k++) {
                    TPonto ponto2 = new TPonto();
                    ponto2 = cromo.getPixelsAt(k);
                    l = ponto2.getLinha() - cromo.getAcima();
                    c = ponto2.getColuna() - cromo.getEsquerda();
                    cor = (int) (ip.get(ponto2.getColuna(), ponto2.getLinha()));
                    ipMatrizCromossomosOriginalTemp[i].set(c + difLargura, l + difAltura, cor);
                    ipMatrizCromossomosMascaraTemp[i].set(c + difLargura, l + difAltura, 255);
                }
            }
        }

        int tamMaior = 0, invalidos = 0;
        for (int i = 0; i < ipMatrizCromossomosOriginalTemp.length; i++) {
            if (ipMatrizCromossomosOriginalTemp[i].getHeight() > tamMaior) {
                tamMaior = ipMatrizCromossomosOriginalTemp[i].getHeight();
            }
        }
        //tira os muito pequenos, aumentando o numero de invalidos
        for (int i = 0; i < ipMatrizCromossomosOriginalTemp.length - invalidos; i++) {
            if (ipMatrizCromossomosOriginalTemp[i].getHeight() < tamMaior * 0.1) {
                for (int j = i + 1; j < ipMatrizCromossomosOriginalTemp.length - invalidos; j++) {
                    ipMatrizCromossomosOriginalTemp[j - 1] = ipMatrizCromossomosOriginalTemp[j];
                    ipMatrizCromossomosMascaraTemp[j - 1] = ipMatrizCromossomosMascaraTemp[j];
                }
                invalidos++;
                i--;
            }
        }
        //declara os vetores que irao guardar os cromossomos MESMO
        ipMatrizCromossomosOriginal = new ImageProcessor[ipMatrizCromossomosOriginalTemp.length - invalidos];
        ipMatrizCromossomosMascara = new ImageProcessor[ipMatrizCromossomosMascaraTemp.length - invalidos];
        //e copia para eles, execto os invalidos
        for (int i = 0; i < ipMatrizCromossomosOriginal.length; i++) {
            ipMatrizCromossomosOriginal[i] = ipMatrizCromossomosOriginalTemp[i];
            ipMatrizCromossomosMascara[i] = ipMatrizCromossomosMascaraTemp[i];
        }
    }

    /**
     * verifica se a coluna e a linha ja nao estao no vector
     *
     * @param coluna	coluna verificada
     * @param linha	linha verificada
     * @param vetor	vetor verificado
     * @return	false se estiver no vetor
     */
    public boolean jaNaoFoi(int coluna, int linha, Vector vetor) {
        TPonto p = new TPonto();
        for (int k = 0; k < vetor.size(); k++) {
            p = (TPonto) vetor.get(k);
            if (p.getColuna() == coluna && p.getLinha() == linha) {
                return false;
            }
        }
        return true;
    }

    /**
     * rotaciona uma imagem em angle graus
     *
     * @param ip	imagem a ser rotacionada
     * @param angle	angulo de rotacao
     */
    public void rotaciona(ImageProcessor ip, double angle) {
        ip.setInterpolate(true);
        ip.setBackgroundValue(0);
        ip.rotate(angle);
    }

    /**
     * retorna o numero de linhas que tem somente pixeis da cor "cor"
     *
     * @param ip	imagem a ser verificada
     * @param cor	cor verificada
     * @return	numero de linhas acima e abaixo
     */
    public int numeroLinhasCimaBaixo(ImageProcessor ip, int cor) {
        return (numeroLinhasBordaCima(ip, cor) + numeroLinhasBordaBaixo(ip, cor));
    }

    /**
     * retorna o numero de linhas acima que tem somente pixeis da cor "cor"
     *
     * @param ip	imagem a ser verificada
     * @param cor	cor verificada
     * @return	numero de linhas acima
     */
    public int numeroLinhasBordaCima(ImageProcessor ip, int cor) {
        int w = ip.getWidth();
        int h = ip.getHeight();
        int count = 0;
        boolean flag = true;

        for (int v = 0; v < h; v++) {
            for (int u = 0; u < w; u++) {
                int a = (int) (ip.get(u, v));
                if (a == cor) {
                    flag = false;
                    break;
                }
            }
            if (flag == true) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * retorna o numero de linhas abaixo que tem somente pixeis da cor "cor"
     *
     * @param ip	imagem a ser verificada
     * @param cor	cor verificada
     * @return	numero de linhas abaixo
     */
    public int numeroLinhasBordaBaixo(ImageProcessor ip, int cor) {
        int w = ip.getWidth();
        int h = ip.getHeight();
        int count = 0;
        boolean flag = true;

        for (int v = h - 1; v > 0; v--) {
            for (int u = 0; u < w; u++) {
                int a = (int) (ip.get(u, v));
                if (a == cor) {
                    flag = false;
                    break;
                }
            }
            if (flag == true) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * retorna o numero de colunas que tem somente pixeis da cor "cor"
     *
     * @param ip	imagem a ser verificada
     * @param cor	cor verificada
     * @return	numero de colunas a esquerda e a direita
     */
    public int numeroLinhasApartirDasBordas(ImageProcessor ip, int cor) {
        return (numeroLinhasBordaDireita(ip, cor) + numeroLinhasBordaEsquerda(ip, cor));
    }

    /**
     * retorna o numero de colunas a direita que tem somente pixeis da cor "cor"
     *
     * @param ip	imagem a ser verificada
     * @param cor	cor verificada
     * @return	numero de colunas a direita
     */
    public int numeroLinhasBordaDireita(ImageProcessor ip, int cor) {
        int w = ip.getWidth();
        int h = ip.getHeight();
        int count = 0;
        boolean flag = true;

        //da direita para esquerda
        for (int u = w - 1; u > 0; u--) {
            for (int v = 0; v < h; v++) {
                int a = (int) (ip.get(u, v));
                if (a == cor) {
                    flag = false;
                    break;
                }
            }
            if (flag == true) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * retorna o numero de colunas a esquerda que tem somente pixeis da cor
     * "cor"
     *
     * @param ip	imagem a ser verificada
     * @param cor	cor verificada
     * @return	numero de colunas a esquerda
     */
    public int numeroLinhasBordaEsquerda(ImageProcessor ip, int cor) {
        int w = ip.getWidth();
        int h = ip.getHeight();
        int count = 0;
        boolean flag = true;

        //da esquerda para direita
        for (int u = 0; u < w; u++) {
            for (int v = 0; v < h; v++) {
                int a = (int) (ip.get(u, v));
                if (a == cor) {
                    flag = false;
                    break;
                }
            }
            if (flag == true) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}
