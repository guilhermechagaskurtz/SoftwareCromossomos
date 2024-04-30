package softwarecromossomos;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import mestradocromossomos3.Cariotipo;
import mestradocromossomos3.Cromossomo;

public class Software_Cromossomos extends JFrame implements ActionListener, MouseListener, MouseMotionListener {

    Cariotipo cariotipo;
    //BufferedImage retBufferedImage[], retBufferedImageMask[], retBufferedImageTortos[], retBufferedImageMaskTortos[];
    Vector<BufferedImage> historicoOriginal = new Vector<BufferedImage>();
    Vector<BufferedImage> historicoMascara = new Vector<BufferedImage>();
    //1 - original, 2 - maascara, 3 - cariotipo'
    public final int ORIGINAL = 1, MASCARA = 2, CARIOTIPO = 3, NADA = -1;
    public final int M = 0, S = 1, A = -1;
    int status = NADA;
    double r1[], assinatura[], classificacaoBracoCurto[], classificacaoTamanho[];
    int classificacaoCentromeros[];
    int centromerosCorreta[] = {M, M, S, S, M, M, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, S, A, A, A, A, A, A, M, M, S, S, S, S, M, M, M, M, A, A, A, A, S, S, A, A};
    String[] comboTipos = {"n/a", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
    JComboBox comboCromossomo[];
    JLabel lblCromossomo[], lblCromossomoTortos[]/*, lblInformacoes[]*/;
    ImageIcon imgCromossomo[], imgCromossomoTortos[];
    int click1 = -1;

    int imagemCorrenteOriginal = -1, imagemCorrenteMascara = -1;

    int xIni, xFim, numVizinhosParaPintar = 1, corParaPintar = 0;
    JPanel menuBarToolBarPanel = new JPanel();
    JPanel imagemPanel = new JPanel();
    JPanel cariotipoPanel = new JPanel();
    JPanel cariotipoNumPanel = new JPanel();

    JScrollPane imgScroll;
    JScrollPane cariotipoScroll;

    JPopupMenu popupMenuTamanho = new JPopupMenu();
    JMenuItem menuItemTamanho1, menuItemTamanho2, menuItemTamanho3, menuItemTamanho4;

    JToolBar toolBar = new JToolBar("Still draggable");

    JButton botaoContraste = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/contraste.jpg")));
    JButton botaoAcentuar = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/acentuar.jpg")));
    JButton botaoSmooth = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/borrar.JPG")));
    //JButton botaoRefresh = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/refresh.JPG")));
    JButton botaoErodir = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/erodir.JPG")));
    JButton botaoDilatar = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/dilatar.JPG")));
    JButton botaoVoltar = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/voltar.JPG")));
    JButton botaoAvancar = new JButton(new ImageIcon(getClass().getClassLoader().getResource("resources/avancar.JPG")));

    JToggleButton botaoMascara = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/cromo_m.JPG")));
    JToggleButton botaoOriginal = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/cromo.JPG")));
    JToggleButton botaoLapis = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/lapis.JPG")));
    JToggleButton botaoLinha = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/linha.JPG")));
    JToggleButton botaoBranco = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/branco.JPG")));
    JToggleButton botaoPreto = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/preto.JPG")));
    JToggleButton botaoTrocar = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/trocar.JPG")));
    JToggleButton botaoInverter = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/inverter.JPG")));
    JToggleButton botaoZoomIn = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/zoomin.jpg")));
    JToggleButton botaoZoomOut = new JToggleButton(new ImageIcon(getClass().getClassLoader().getResource("resources/zoomout.jpg")));

    JMenuBar menuBar;
    JMenu menuArquivo, menuFerramentas;
    JMenuItem menuItemAbrir, menuItemSalvar, menuItemSair, menuItemIdentificarTamanho;

    ImageIcon imagemPrincipal, imagemPrincipalMascara;
    JLabel lblImagemPrincipal = new JLabel("");

    int flag = 0;

    private Image getImage(String pathName) {
        URL url = getClass().getResource(pathName);
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        return image;
    }

    public Software_Cromossomos() {

        super("Software Identificador de Cromossomos");
        lblImagemPrincipal.addMouseListener(this);
        lblImagemPrincipal.addMouseMotionListener(this);
        imgScroll = new JScrollPane(lblImagemPrincipal);
        imgScroll.setPreferredSize(new Dimension(640, 480));

        defineActionListener();
        defineHints();
        habilitaBotoes("Nenhum");
        montaToolBar(toolBar);

        //Cria um menu bar.
        menuBar = new JMenuBar();

        //Constroi o menu Arquivo.
        menuArquivo = new JMenu("Arquivo");
        menuArquivo.setMnemonic(KeyEvent.VK_A);
        menuBar.add(menuArquivo);

        //um grupo de JMenuItems
        menuItemAbrir = new JMenuItem("Abrir", KeyEvent.VK_T);
        menuItemAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK)); //define um atalho (ALT + 1);
        menuItemAbrir.addActionListener(this);
        menuArquivo.add(menuItemAbrir);

        menuItemSalvar = new JMenuItem("Salvar Cariótipo", KeyEvent.VK_S);
        menuItemSalvar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK)); //define um atalho (ALT + 3);
        menuItemSalvar.addActionListener(this);
        menuArquivo.add(menuItemSalvar);

        menuItemSair = new JMenuItem("Sair", new ImageIcon("images/middle.gif"));
        menuItemSair.setMnemonic(KeyEvent.VK_B);
        menuItemSair.addActionListener(this);
        menuArquivo.add(menuItemSair);

        //Constroi o menu Ferramentas.
        menuFerramentas = new JMenu("Ferramentas");
        menuFerramentas.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menuFerramentas);

        //um grupo de JMenuItems
        menuItemIdentificarTamanho = new JMenuItem("Identificar", KeyEvent.VK_I);
        menuItemIdentificarTamanho.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK)); //define um atalho (ALT + 1);
        menuItemIdentificarTamanho.addActionListener(this);
        menuFerramentas.add(menuItemIdentificarTamanho);

        //define o popup meno para o tamanho das linhas/pontos que vao ser desenhados
        menuItemTamanho1 = new JMenuItem("T1");
        menuItemTamanho1.addActionListener(this);
        menuItemTamanho2 = new JMenuItem("T2");
        menuItemTamanho2.addActionListener(this);
        menuItemTamanho3 = new JMenuItem("T3");
        menuItemTamanho3.addActionListener(this);
        menuItemTamanho4 = new JMenuItem("T4");
        menuItemTamanho4.addActionListener(this);
        popupMenuTamanho.add(menuItemTamanho1);
        popupMenuTamanho.add(menuItemTamanho2);
        popupMenuTamanho.add(menuItemTamanho3);
        popupMenuTamanho.add(menuItemTamanho4);
        botaoLapis.addMouseListener(this);
        botaoLinha.addMouseListener(this);

        menuBarToolBarPanel.setLayout(new BorderLayout());
        menuBarToolBarPanel.add(menuBar, BorderLayout.NORTH);
        menuBarToolBarPanel.add(toolBar, BorderLayout.CENTER);
        getContentPane().add(menuBarToolBarPanel, BorderLayout.NORTH);
        getContentPane().add(imgScroll, BorderLayout.CENTER);
        //getContentPane().add(lblImagemPrincipal, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cariotipoPanel.setLayout(new GridLayout(7, 7));
        cariotipoScroll = new JScrollPane(cariotipoPanel);

        pack();
        setVisible(true);
    }

    /**
     * Adiciona os componentes ao toolBar passado como parametro
     *
     * @param toolBar JToolBar a ser montado
     */
    protected void montaToolBar(JToolBar toolBar) {
        toolBar.add(botaoContraste);
        toolBar.add(botaoAcentuar);
        toolBar.add(botaoSmooth);
        //toolBar.add(botaoRefresh);
        toolBar.add(botaoErodir);
        toolBar.add(botaoDilatar);
        toolBar.add(botaoLapis);
        toolBar.add(botaoLinha);
        toolBar.add(botaoVoltar);
        toolBar.add(botaoAvancar);
        toolBar.add(botaoOriginal);
        toolBar.add(botaoMascara);
        toolBar.add(botaoInverter);
        toolBar.add(botaoTrocar);
        toolBar.add(botaoZoomIn);
        toolBar.add(botaoZoomOut);
        toolBar.add(botaoBranco);
        toolBar.add(botaoPreto);
    }

    /**
     * Define os hints dos bot�es
     *
     */
    protected void defineHints() {
        botaoDilatar.setToolTipText("Dilatar");
        botaoErodir.setToolTipText("Erodir");
        botaoMascara.setToolTipText("Mascara");
        botaoOriginal.setToolTipText("Original");
        botaoContraste.setToolTipText("Aumentar Contraste");
        botaoAcentuar.setToolTipText("Acentuar");
        botaoSmooth.setToolTipText("Borrar");
        //botaoRefresh.setToolTipText("Atualizar Máscara");
        botaoLapis.setToolTipText("Lapis");
        botaoLinha.setToolTipText("Linha");
        botaoTrocar.setToolTipText("Trocar");
        botaoInverter.setToolTipText("Inverter");
        botaoZoomIn.setToolTipText("Zoom +");
        botaoZoomOut.setToolTipText("Zoom -");
        botaoVoltar.setToolTipText("Voltar");
        botaoAvancar.setToolTipText("Avancar");
        botaoBranco.setToolTipText("Cor branca");
        botaoPreto.setToolTipText("Cor preta");
    }

    /**
     * Define as a��es dos bot�es
     *
     */
    protected void defineActionListener() {
        botaoDilatar.addActionListener(this);
        botaoErodir.addActionListener(this);
        botaoContraste.addActionListener(this);
        botaoAcentuar.addActionListener(this);
        botaoSmooth.addActionListener(this);
        //botaoRefresh.addActionListener(this);
        botaoMascara.addActionListener(this);
        botaoOriginal.addActionListener(this);
        botaoLapis.addActionListener(this);
        botaoLinha.addActionListener(this);
        botaoTrocar.addActionListener(this);
        botaoInverter.addActionListener(this);
        botaoZoomIn.addActionListener(this);
        botaoZoomOut.addActionListener(this);
        botaoVoltar.addActionListener(this);
        botaoAvancar.addActionListener(this);
        botaoBranco.addActionListener(this);
        botaoPreto.addActionListener(this);
        botaoPreto.setSelected(true);
    }

    /**
     * Habilita ou n�o os bot�es
     *
     * @param tipo define quais bot�es ser�o habilitados
     */
    protected void habilitaBotoes(String tipo) {
        if (tipo.equals("Nenhum")) {
            botaoDilatar.setEnabled(false);
            botaoErodir.setEnabled(false);
            botaoContraste.setEnabled(false);
            botaoAcentuar.setEnabled(false);
            botaoSmooth.setEnabled(false);
            //botaoRefresh.setEnabled(false);
            botaoOriginal.setEnabled(false);
            botaoMascara.setEnabled(false);
            botaoInverter.setEnabled(false);
            botaoTrocar.setEnabled(false);
            botaoLapis.setEnabled(false);
            botaoLinha.setEnabled(false);
            botaoZoomIn.setEnabled(false);
            botaoZoomOut.setEnabled(false);
            botaoVoltar.setEnabled(false);
            botaoAvancar.setEnabled(false);
            botaoPreto.setEnabled(false);
            botaoBranco.setEnabled(false);
        }
        if (tipo.equals("Mascara")) {
            botaoDilatar.setEnabled(true);
            botaoErodir.setEnabled(true);
            botaoContraste.setEnabled(false);
            botaoAcentuar.setEnabled(false);
            botaoSmooth.setEnabled(false);
            //botaoRefresh.setEnabled(false);
            botaoOriginal.setEnabled(true);
            botaoMascara.setEnabled(false);
            botaoInverter.setEnabled(false);
            botaoTrocar.setEnabled(false);
            botaoZoomIn.setEnabled(false);
            botaoZoomOut.setEnabled(false);
            botaoLapis.setEnabled(true);
            botaoLinha.setEnabled(true);
            botaoVoltar.setEnabled(true);
            botaoAvancar.setEnabled(true);
            botaoPreto.setEnabled(true);
            botaoBranco.setEnabled(true);
        } else if (tipo.equals("Original")) {
            botaoDilatar.setEnabled(false);
            botaoErodir.setEnabled(false);
            botaoContraste.setEnabled(true);
            botaoAcentuar.setEnabled(true);
            botaoSmooth.setEnabled(true);
            //botaoRefresh.setEnabled(true);
            botaoOriginal.setEnabled(false);
            botaoMascara.setEnabled(true);
            botaoInverter.setEnabled(false);
            botaoTrocar.setEnabled(false);
            botaoZoomIn.setEnabled(false);
            botaoZoomOut.setEnabled(false);
            botaoLapis.setEnabled(true);
            botaoLinha.setEnabled(true);
            botaoVoltar.setEnabled(true);
            botaoAvancar.setEnabled(true);
            botaoPreto.setEnabled(true);
            botaoBranco.setEnabled(true);
        } else if (tipo.equals("Cariotipo")) {
            botaoDilatar.setEnabled(false);
            botaoErodir.setEnabled(false);
            botaoContraste.setEnabled(false);
            botaoAcentuar.setEnabled(false);
            botaoSmooth.setEnabled(false);
            //botaoRefresh.setEnabled(false);
            botaoOriginal.setEnabled(false);
            botaoMascara.setEnabled(false);
            botaoInverter.setEnabled(true);
            botaoZoomIn.setEnabled(true);
            botaoZoomOut.setEnabled(true);
            botaoTrocar.setEnabled(true);
            botaoLapis.setEnabled(false);
            botaoLinha.setEnabled(false);
            botaoVoltar.setEnabled(false);
            botaoAvancar.setEnabled(false);
        }
    }

    /**
     * joga a imagem passada como parametro na tela (no label)
     *
     * @param lbl label no qual vai ser colocada a imagem
     * @param imagem imagem a ser colocada no label
     */
    public void atualizaImagem(JLabel lbl, ImageIcon imagem) {
        lbl.setIcon(imagem);
        lbl.setVisible(true);
        lbl.setBounds(0, 0, imagem.getIconWidth(), imagem.getIconHeight());
        getContentPane().repaint();
        pack();
    }

    /**
     * troca as imagens de um label para o outro
     *
     * @param lbl1 label origem
     * @param lbl2	label destino
     */
    public void trocaImagem(JLabel lbl1, JLabel lbl2) {
        ImageIcon img = (ImageIcon) lbl1.getIcon();
        atualizaImagem(lbl1, (ImageIcon) lbl2.getIcon());
        atualizaImagem(lbl2, img);
    }

    /**
     * troca o texto de um label para o outro
     *
     * @param lbl1 label origem
     * @param lbl2	label destino
     */
    public void trocaLabel(JLabel lbl1, JLabel lbl2) {
        String txt = lbl1.getText();
        lbl1.setText(lbl2.getText());
        lbl2.setText(txt);
    }

    /**
     * recebe um ImageIcon como parametro e retorna o BufferedImage desta imagem
     *
     * @param img ImageIcon no qual se quer o BufferedImage
     * @return	BufferedImage do ImageIcon
     */
    private BufferedImage getBufferedImage(ImageIcon img) {
        BufferedImage bufferedImage = new BufferedImage(img.getIconWidth(), img.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(img.getImage(), null, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * recebe a imgem original como parametro e retorna a imagem como Mascara
     *
     * @param img Imagem no qual se quer a mascara
     * @return	ImageIcon mascara da imagem original
     */
    private ImageIcon getImagemMascara(ImageIcon img) {
        BufferedImage bufferedImage = new BufferedImage(imagemPrincipal.getIconWidth(), imagemPrincipal.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);
        bufferedImage = getBufferedImage(img);
        BufferedImage retBufferedImage;
        TMascarador mascarador = new TMascarador();
        retBufferedImage = mascarador.mascara(bufferedImage);
        adicionaHistorico("Original", bufferedImage); //salva no historico as duas primeiras imagens original e mascara
        adicionaHistorico("Mascara", retBufferedImage);
        return new ImageIcon(retBufferedImage);
    }

    /**
     * limpa o historico de imagens
     *
     */
    private void limpaHistorico() {
        historicoMascara.clear();
        historicoOriginal.clear();
        imagemCorrenteOriginal = -1;
        imagemCorrenteMascara = -1;
    }

    /**
     * adiciona uma imagem ao historico
     *
     * @param tipo Tipo de imagem (mascara ou original)
     * @param img	Imagem a ser adicionada
     */
    private void adicionaHistorico(String tipo, BufferedImage img) {
        BufferedImage imgHist = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        imgHist = img.getSubimage(0, 0, img.getWidth(), img.getHeight());
        if (tipo.equals("Original")) {
            for (int i = historicoOriginal.size() - 1; i > imagemCorrenteOriginal; i--) {
                historicoOriginal.remove(i);
            }
            if (historicoOriginal.size() == 10) {
                historicoOriginal.remove(0); //se estiver cheio, remove o primeiro
            }
            historicoOriginal.add(imgHist);
            if (imagemCorrenteOriginal < 9) {
                imagemCorrenteOriginal++;
            }
            System.out.println("Adicionou no historico - Original");
        } else if (tipo.equals("Mascara")) {
            for (int i = historicoMascara.size() - 1; i > imagemCorrenteMascara; i--) {
                historicoMascara.remove(i);
            }
            if (historicoMascara.size() == 10) {
                historicoMascara.remove(0); //se estiver cheio, remove o primeiro
            }
            historicoMascara.add(imgHist);
            if (imagemCorrenteMascara < 9) {
                imagemCorrenteMascara++;
            }
            System.out.println("Adicionou no historico - Mascara");
            System.out.println("Imagem corrente: " + imagemCorrenteMascara);
        }
    }

    /**
     * volta uma imagem no historico
     *
     * @param tipo Tipo de imagem (mascara ou original)
     */
    private void voltarHistorico(String tipo) {
        if (tipo.equals("Original")) {
            if (imagemCorrenteOriginal - 1 >= 0) {
                imagemCorrenteOriginal--;
                imagemPrincipal.setImage(historicoOriginal.get(imagemCorrenteOriginal));
                repaint();
            }
        }
        if (tipo.equals("Mascara")) {
            if (imagemCorrenteMascara - 1 >= 0) {
                imagemCorrenteMascara--;
                imagemPrincipalMascara.setImage(historicoMascara.get(imagemCorrenteMascara));
                System.out.println("voltei");
                System.out.println("Imagem corrente: " + imagemCorrenteMascara);
                repaint();
            }
        }
    }

    /**
     * avanca uma imagem no historico
     *
     * @param tipo Tipo de imagem (mascara ou original)
     */
    private void avancarHistorico(String tipo) {
        System.out.println("Avancar tipo: " + tipo);
        if (tipo.equals("Original")) {
            if (imagemCorrenteOriginal + 1 < historicoOriginal.size()) {
                imagemCorrenteOriginal++;
                imagemPrincipal.setImage(historicoOriginal.get(imagemCorrenteOriginal));
                repaint();
            }
        }
        if (tipo.equals("Mascara")) {
            System.out.println("A mascara esta em " + imagemCorrenteMascara + " e o tamanho do historico eh :" + historicoMascara.size());
            if (imagemCorrenteMascara + 1 < historicoMascara.size()) {
                imagemCorrenteMascara++;
                imagemPrincipalMascara.setImage(historicoMascara.get(imagemCorrenteMascara));
                System.out.println("AVANCAR: Imagem corrente: " + imagemCorrenteMascara);
                repaint();
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
        if (e.getSource() == menuItemAbrir) {
            File iconFile;
            JFileChooser arquivo = new JFileChooser();
            FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imagens", "gif", "jpg", "png");
            arquivo.setFileFilter(filtro);
            if (arquivo.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                iconFile = arquivo.getSelectedFile();
                if (iconFile.exists()) {
                    limpaHistorico();
                    habilitaBotoes("Original");
                    botaoOriginal.setSelected(true);
                    getContentPane().remove(cariotipoScroll);
                    imagemPrincipal = new ImageIcon(iconFile.getAbsolutePath());
                    imagemPrincipalMascara = getImagemMascara(imagemPrincipal);
                    getContentPane().add(imgScroll, BorderLayout.CENTER);
                    atualizaImagem(lblImagemPrincipal, imagemPrincipal);
                    status = ORIGINAL;
                }
            }
        }

        if (e.getSource() == menuItemSalvar) {
            if (status == CARIOTIPO) {
                File iconFile;
                JFileChooser arquivo = new JFileChooser();
                FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imagem PNG", "png");
                arquivo.setFileFilter(filtro);
                if (arquivo.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    iconFile = arquivo.getSelectedFile();
                    SaveImage saver = new SaveImage();
                    saver.salvar(cariotipo, iconFile.getAbsolutePath());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Você deve ter um Cariótipo para salvar");
            }
        }

        if (e.getSource() == menuItemSair) {
            System.exit(0);
        }
        //se clicar para identificar por tamanho
        if (e.getSource() == menuItemIdentificarTamanho) {
            //cria um buffered image mara a principal e outro para a mascara
            BufferedImage bufferedImage = new BufferedImage(imagemPrincipal.getIconWidth(), imagemPrincipal.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);
            BufferedImage bufferedImageMascara = new BufferedImage(imagemPrincipalMascara.getIconWidth(), imagemPrincipalMascara.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);
            //e chama a funcao que vai retornar para ele o buffer da imagem que esta no panel(principal e mascara)
            bufferedImage = getBufferedImage(imagemPrincipal);
            bufferedImageMascara = getBufferedImage(imagemPrincipalMascara);
            //cria um array de bufferedimage que vai receber as imagens do plugin do imagej
            TIdentificador mascara = new TIdentificador();
            //chama o metodo Identifica da classe Mascara que ira retornar um Cariotipo com os cromossomos
            //passando como parametro a mascara e a imagem original
            cariotipo = mascara.IdentificaNovo(bufferedImage, bufferedImageMascara);

            lblCromossomo = new JLabel[cariotipo.getCromossomos().size()];
            //lblInformacoes = new JLabel[cariotipo.getCromossomos().size()];
            comboCromossomo = new JComboBox[cariotipo.getCromossomos().size()];
            imgCromossomo = new ImageIcon[cariotipo.getCromossomos().size()];
            cariotipoPanel.removeAll();
            for (int i = 0, j = 1; i < cariotipo.getCromossomos().size(); i++) {
                imgCromossomo[i] = new ImageIcon(cariotipo.getCromossomo(i).getOriginal().createImage());//imagem do cromossomo
                lblCromossomo[i] = new JLabel(imgCromossomo[i]);//informações sobre o cromossomo
                lblCromossomo[i].addMouseListener(this);
                comboCromossomo[i] = new JComboBox<String>(comboTipos);//combo para mudar o tipo
                comboCromossomo[i].addItemListener(new ItemListener() {

                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            for (int i = 0; i < comboCromossomo.length; i++) {
                                if (e.getSource() == comboCromossomo[i]) {
                                    int numTipo = comboCromossomo[i].getSelectedIndex();
                                    cariotipo.getCromossomo(i).setTipo(numTipo-1);
                                    break;
                                }
                            }
                        }
                    }
                });

                lblCromossomo[i].setBounds(0, 0, imgCromossomo[i].getIconWidth(), imgCromossomo[i].getIconHeight());//
                lblCromossomo[i].setHorizontalAlignment(JLabel.CENTER);
                JPanel pnlteste = new JPanel();
                pnlteste.setLayout(new BorderLayout());
                pnlteste.add(lblCromossomo[i], BorderLayout.NORTH);
                comboCromossomo[i].setSelectedIndex(cariotipo.getCromossomo(i).getTipo() + 1);
                comboCromossomo[i].setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));
                pnlteste.add(comboCromossomo[i], BorderLayout.SOUTH);
                pnlteste.setSize(10, 10);
                pnlteste.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                pnlteste.setBackground(Color.WHITE);
                cariotipoPanel.add(pnlteste);
                if (i % 2 != 0) {
                    j++;
                }
            }

            habilitaBotoes("Cariotipo");
            status = CARIOTIPO;
            getContentPane().remove(imgScroll);
            getContentPane().add(cariotipoScroll, BorderLayout.CENTER);
            getContentPane().repaint();
            pack();
        }
        if (e.getSource() == botaoOriginal) {
            atualizaImagem(lblImagemPrincipal, imagemPrincipal);
            botaoMascara.setSelected(false);
            habilitaBotoes("Original");
            status = ORIGINAL;
        }
        if (e.getSource() == botaoMascara) {
            imagemPrincipalMascara = getImagemMascara(imagemPrincipal);
            atualizaImagem(lblImagemPrincipal, imagemPrincipalMascara);
            botaoOriginal.setSelected(false);
            habilitaBotoes("Mascara");
            status = MASCARA;
        }
        if (e.getSource() == botaoLapis) {
            if (botaoLapis.isSelected()) {
                lblImagemPrincipal.setCursor(new Cursor(Cursor.HAND_CURSOR));
                botaoLinha.setSelected(false);
            } else {
                lblImagemPrincipal.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
        if (e.getSource() == botaoLinha) {
            if (botaoLinha.isSelected()) {
                lblImagemPrincipal.setCursor(new Cursor(Cursor.HAND_CURSOR));
                botaoLapis.setSelected(false);
            } else {
                lblImagemPrincipal.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
        if (e.getSource() == botaoErodir) {
            aplicaFiltro(imagemPrincipalMascara, "Dilatar", "Mascara"); //eh invertido pq... inverteu la no imagej    	        	
        }
        if (e.getSource() == botaoDilatar) {
            aplicaFiltro(imagemPrincipalMascara, "Erodir", "Mascara");  //eh invertido pq... inverteu la no imagej      	        	
        }
        if (e.getSource() == botaoContraste) {
            aplicaFiltro(imagemPrincipal, "Contraste", "Original");
        }
        if (e.getSource() == botaoAcentuar) {
            aplicaFiltro(imagemPrincipal, "Acentuar", "Original");
        }
        if (e.getSource() == botaoSmooth) {
            aplicaFiltro(imagemPrincipal, "Borrar", "Original");
        }
        /*if (e.getSource() == botaoRefresh) {
            imagemPrincipalMascara = getImagemMascara(imagemPrincipal);
        }*/
        if (e.getSource() == botaoVoltar) {
            if (botaoMascara.isSelected()) {
                voltarHistorico("Mascara");
            } else if (botaoOriginal.isSelected()) {
                voltarHistorico("Original");
            }
        }
        if (e.getSource() == botaoAvancar) {
            System.out.println("Clicou no avancar");
            if (botaoMascara.isSelected()) {
                avancarHistorico("Mascara");
            } else if (botaoOriginal.isSelected()) {
                avancarHistorico("Original");
            }
        }
        if (e.getSource() == botaoInverter) {
            botaoTrocar.setSelected(false);
            botaoZoomIn.setSelected(false);
            botaoZoomOut.setSelected(false);
        }
        if (e.getSource() == botaoTrocar) {
            botaoInverter.setSelected(false);
            botaoZoomIn.setSelected(false);
            botaoZoomOut.setSelected(false);
        }
        if (e.getSource() == botaoZoomIn) {
            botaoInverter.setSelected(false);
            botaoTrocar.setSelected(false);
            botaoZoomOut.setSelected(false);
        }
        if (e.getSource() == botaoZoomOut) {
            botaoInverter.setSelected(false);
            botaoTrocar.setSelected(false);
            botaoZoomIn.setSelected(false);
        }
        if (e.getSource() == menuItemTamanho1) {
            numVizinhosParaPintar = 1;
        }
        if (e.getSource() == menuItemTamanho2) {
            numVizinhosParaPintar = 2;
        }
        if (e.getSource() == menuItemTamanho3) {
            numVizinhosParaPintar = 3;
        }
        if (e.getSource() == menuItemTamanho4) {
            numVizinhosParaPintar = 4;
        }
        if (e.getSource() == botaoBranco) {
            botaoPreto.setSelected(false);
            corParaPintar = 255;
        }
        if (e.getSource() == botaoPreto) {
            botaoBranco.setSelected(false);
            corParaPintar = 0;
        }
    }

    /**
     * aplica um filtro a uma imagem
     *
     * @param imagem imagem na qual vai ser aplicado o filtro
     * @param Filtro	filtro que sera aplicado
     * @param tipo	tipo de imagem (original ou mascara)
     */
    public void aplicaFiltro(ImageIcon imagem, String Filtro, String tipo) {
        BufferedImage bufferedImage = new BufferedImage(imagem.getIconWidth(), imagem.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);
        bufferedImage = getBufferedImage(imagem);
        BufferedImage retBufferedImage;
        TFiltro filtro = new TFiltro();
        retBufferedImage = filtro.Filtrar(bufferedImage, Filtro);
        if (tipo == "Original") {
            imagemPrincipal.setImage(retBufferedImage);
            adicionaHistorico("Original", retBufferedImage);
        } else if (tipo == "Mascara") {
            imagemPrincipalMascara.setImage(retBufferedImage);
            adicionaHistorico("Mascara", retBufferedImage);
        }
        repaint();
    }

    public void mouseClicked(MouseEvent arg0) {
        int i;
        if ((arg0.getSource().equals(botaoLapis) || arg0.getSource().equals(botaoLinha)) && arg0.getButton() == MouseEvent.BUTTON3) {
            popupMenuTamanho.show(botaoLapis, 0, botaoLapis.getY() + botaoLapis.getHeight() + 2);
        } else if (!(arg0.getSource().equals(botaoLapis)) && !(arg0.getSource().equals(botaoLinha)) && status == CARIOTIPO) {
            //se clicou com o botaoTrocar ativo
            if (botaoTrocar.isSelected()) {
                for (i = 0; i < lblCromossomo.length; i++) {
                    //se clicou em algum label de imagem
                    if (arg0.getSource() == lblCromossomo[i]) {
                        //se nao clicou em nada ainda, somente seleciona
                        if (click1 == -1) {
                            click1 = i;
                            lblCromossomo[i].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                        } //se ja clicou em algum e esse algum eh o mesmo, somente desmarca
                        else if (click1 == i) {
                            click1 = -1;
                            lblCromossomo[i].setBorder(null);
                        } //se clicou em algum e esse algum eh outro, troca entao e tira as bordas amarelas
                        else {
                            //troca a imagem
                            trocaImagem(lblCromossomo[i], lblCromossomo[click1]);
                            ImageIcon imgAux = imgCromossomo[i];
                            imgCromossomo[i] = imgCromossomo[click1];
                            imgCromossomo[click1] = imgAux;
                            lblCromossomo[click1].setBorder(null);
                            //troca o jcombobox
                            int indexAux = comboCromossomo[i].getSelectedIndex();
                            comboCromossomo[i].setSelectedIndex(comboCromossomo[click1].getSelectedIndex());
                            comboCromossomo[click1].setSelectedIndex(indexAux);
                            //troca o cromossomo
                            Cromossomo caux = cariotipo.getCromossomo(i);
                            cariotipo.getCromossomos().set(i, cariotipo.getCromossomo(click1));
                            cariotipo.getCromossomos().set(click1, caux);
                            click1 = -1;
                            revalidate();
                        }
                    }
                }
            } //se clicou com o botaoInverter ativo
            else if (botaoInverter.isSelected()) {
                for (i = 0; i < lblCromossomo.length; i++) {
                    //se clicou em algum cromossomo
                    if (arg0.getSource().equals(lblCromossomo[i])) {
                        TInversor inversor = new TInversor();
                        BufferedImage image = getBufferedImage((ImageIcon) lblCromossomo[i].getIcon());
                        image = inversor.Inverte(image);
                        atualizaImagem(lblCromossomo[i], new ImageIcon(image));
                        cariotipo.getCromossomo(i).setInvertido(!cariotipo.getCromossomo(i).isInvertido());
                    }
                }
            } //se clicou com o botaoZoomIn ativo 
            else if (botaoZoomIn.isSelected()) {
                for (i = 0; i < lblCromossomo.length; i++) {
                    if (arg0.getSource().equals(lblCromossomo[i])) {
                        TZoom zoom = new TZoom(2);
                        BufferedImage image = getBufferedImage((ImageIcon) lblCromossomo[i].getIcon());
                        image = zoom.zoomIn(image);
                        atualizaImagem(lblCromossomo[i], new ImageIcon(image));
                    }
                }
            } //se clicou com o botaoZoomOut ativo 
            else if (botaoZoomOut.isSelected()) {
                for (i = 0; i < lblCromossomo.length; i++) {
                    if (arg0.getSource().equals(lblCromossomo[i])) {
                        TZoom zoom = new TZoom(2);
                        BufferedImage image = getBufferedImage((ImageIcon) lblCromossomo[i].getIcon());
                        image = zoom.zoomOut(image);
                        atualizaImagem(lblCromossomo[i], new ImageIcon(image));
                    }
                }
            } // se clicou sem nenhum botão ativo, mostra informações adicionais sobre o cromossomo
            else {
                for (i = 0; i < lblCromossomo.length; i++) {
                    if (arg0.getSource().equals(lblCromossomo[i])) {
                        JDialog dialog = new JDialog();
                        JPanel pnlEsquerda = new JPanel();
                        JPanel pnlDireita = new JPanel();
                        JPanel pnlPrincipal = new JPanel();
                        JLabel lblImagem = new JLabel(imgCromossomo[i]);
                        pnlEsquerda.add(lblImagem);

                        JLabel lblInfo = new JLabel("");
                        String centromero = cariotipo.getCromossomo(i).getCentromero();
                        if (centromero.equals("M")) {
                            centromero = "Metacêntrico";
                        } else if (centromero.equals("S")) {
                            centromero = "Submetacêntrico";
                        } else if (centromero.equals("A")) {
                            centromero = "Acrocêntrico";
                        }
                        String chances = "";
                        DecimalFormat df = new DecimalFormat("#.00");
                        for (int j = 0; j < cariotipo.getCromossomo(i).getChances().length; j++) {
                            double chance = cariotipo.getCromossomo(i).getChances()[j];
                            if (chance > 0) {
                                if (j == 23) {
                                    chances += "X: " + df.format(chance * 100) + "%";
                                } else if (j == 24) {
                                    chances += "Y: " + df.format(chance * 100) + "%";
                                } else {
                                    chances += (j + 1) + ": " + df.format(chance * 100) + "%";
                                }
                                chances += "<BR>";
                            }
                        }
                        lblInfo.setText("<html><h4>Chances de cada tipo:</h4>" + chances + "<br>Centromero: " + centromero + "</html>");
                        pnlEsquerda.add(lblImagem);
                        pnlDireita.add(lblInfo);
                        pnlPrincipal.setLayout(new GridLayout(1, 2));
                        pnlPrincipal.add(pnlEsquerda);
                        pnlPrincipal.add(pnlDireita);
                        dialog.getContentPane().add(pnlPrincipal);
                        dialog.setModal(true);
                        dialog.pack();
                        dialog.setVisible(true);
                        dialog.setTitle("Informações adicionais");
                    }
                }
            }
        }
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
        if (!(arg0.getSource().equals(botaoLapis)) && !(arg0.getSource().equals(botaoLinha)) && (status == ORIGINAL || status == MASCARA)) {
            System.out.println("Mouse pressionado");
            //PINTA NA MASCARA
            if (botaoLapis.isSelected() && botaoMascara.isSelected()) {
                BufferedImage imagem = getBufferedImage(imagemPrincipalMascara);
                WritableRaster raster = imagem.getRaster();
                //pintaPonto(raster, arg0.getX(), arg0.getY());
                pintaPonto(raster, arg0.getX(), getMouseYImagem(arg0.getY(), imagemPrincipalMascara));
                imagemPrincipalMascara.setImage(imagem);
                repaint();
            } else if (botaoLinha.isSelected() && botaoMascara.isSelected()) {
                xIni = arg0.getX();
                //xFim = arg0.getY();
                xFim = getMouseYImagem(arg0.getY(), imagemPrincipalMascara);
            } //PINTA NA IMAGEM ORIGINAL
            else if (botaoLapis.isSelected() && botaoOriginal.isSelected()) {
                BufferedImage imagem = getBufferedImage(imagemPrincipal);
                WritableRaster raster = imagem.getRaster();
                //pintaPonto(raster, arg0.getX(), arg0.getY());
                pintaPonto(raster, arg0.getX(), getMouseYImagem(arg0.getY(), imagemPrincipal));
                imagemPrincipal.setImage(imagem);
                repaint();
            } else if (botaoLinha.isSelected() && botaoOriginal.isSelected()) {
                xIni = arg0.getX();
                //xFim = arg0.getY();
                xFim = getMouseYImagem(arg0.getY(), imagemPrincipal);
            }
        }
    }

    public void mouseReleased(MouseEvent arg0) {
        if (!(arg0.getSource().equals(botaoLapis)) && !(arg0.getSource().equals(botaoLinha)) && (status == ORIGINAL || status == MASCARA)) {
            System.out.println("Mouse levantado");
            //PINTA NA MASCARA
            if (botaoLapis.isSelected() && botaoMascara.isSelected()) {
                BufferedImage imagem = getBufferedImage(imagemPrincipalMascara);
                adicionaHistorico("Mascara", imagem);
            } else if (botaoLinha.isSelected() && botaoMascara.isSelected()) {
                BufferedImage imagem = getBufferedImage(imagemPrincipalMascara);
                //pintaLinha(imagem, xIni, xFim, arg0.getX(), arg0.getY());
                pintaLinha(imagem, xIni, xFim, arg0.getX(), getMouseYImagem(arg0.getY(), imagemPrincipalMascara));
                imagemPrincipalMascara.setImage(imagem);
                adicionaHistorico("Mascara", imagem);
                repaint();
            } //PINTA NA IMAGEM ORIGINAL
            else if (botaoLapis.isSelected() && botaoOriginal.isSelected()) {
                BufferedImage imagem = getBufferedImage(imagemPrincipal);
                adicionaHistorico("Original", imagem);
            } else if (botaoLinha.isSelected() && botaoOriginal.isSelected()) {
                BufferedImage imagem = getBufferedImage(imagemPrincipal);
                //pintaLinha(imagem, xIni, xFim, arg0.getX(), arg0.getY());
                pintaLinha(imagem, xIni, xFim, arg0.getX(), getMouseYImagem(arg0.getY(), imagemPrincipal));
                imagemPrincipal.setImage(imagem);
                adicionaHistorico("Original", imagem);
                repaint();
            }
        }
    }

    public void mouseDragged(MouseEvent arg0) {
        if (!(arg0.getSource().equals(botaoLapis)) && !(arg0.getSource().equals(botaoLinha)) && (status == ORIGINAL || status == MASCARA)) {
            System.out.println("Mouse arrastado");
            if (botaoLapis.isSelected() && botaoMascara.isSelected()) {
                BufferedImage imagem = getBufferedImage(imagemPrincipalMascara);
                WritableRaster raster = imagem.getRaster();
                //pintaPonto(raster, arg0.getX(), arg0.getY());
                pintaPonto(raster, arg0.getX(), getMouseYImagem(arg0.getY(), imagemPrincipalMascara));
                imagemPrincipalMascara.setImage(imagem);
                repaint();
            } //PINTA NA IMAGEM ORIGINAL
            else if (botaoLapis.isSelected() && botaoOriginal.isSelected()) {
                BufferedImage imagem = getBufferedImage(imagemPrincipal);
                WritableRaster raster = imagem.getRaster();
                //pintaPonto(raster, arg0.getX(), arg0.getY());
                pintaPonto(raster, arg0.getX(), getMouseYImagem(arg0.getY(), imagemPrincipal));
                imagemPrincipal.setImage(imagem);
                repaint();
            }
        }
    }

    public void mouseMoved(MouseEvent arg0) {

    }

    /**
     * calcula a posição correta que o mouse clicou em y
     *
     * @param argY posicao do clique do mouse na tela
     * @param img imagem clicada
     */
    public int getMouseYImagem(int argY, ImageIcon img) {
        //calcula a altura de onde está o mouse com base no tamanho da tela
        int y = 0;
        //se a altura do scroll for menor que o tamanho da imagem, retorna o próprio local y onde clicou
        if (imgScroll.getHeight() < img.getIconHeight()) {
            y = argY;
            //se não, calcula a diferença
        } else {
            y = argY - (imgScroll.getHeight() - img.getIconHeight()) / 2;
        }
        return y;
    }

    /**
     * desenha um ponto no raster
     *
     * @param raster	raster no qual sera desenhado o ponto
     * @param x	posicao x do ponto
     * @param y posicao y do ponto
     */
    public void pintaPonto(WritableRaster raster, int x, int y) {
        int col, lin;
        col = x - numVizinhosParaPintar;
        lin = y - numVizinhosParaPintar;
        for (int i = lin; i < y + numVizinhosParaPintar; i++) {
            for (int j = col; j < x + numVizinhosParaPintar; j++) {
                if (i < imagemPrincipalMascara.getIconHeight() && j < imagemPrincipalMascara.getIconWidth() && i > 0 && j > 0) {
                    raster.setSample(j, i, 0, corParaPintar);
                }
            }
        }
    }

    /**
     * desenha uma linha na imagem
     *
     * @param xIni	x inicial da reta
     * @param xFim	x final da reta
     * @param yIni y inicial da reta
     * @param yFim	y final da reta
     */
    public void pintaLinha(BufferedImage image, int xIni, int yIni, int xFim, int yFim) {
        Graphics2D graphics = image.createGraphics(); //cria um graphics 2D que vai pegar a imagem do icon e passar para o buffer
        graphics.setColor(new Color(corParaPintar, corParaPintar, corParaPintar)); //define a cor em rgb, como eh tons de cinza fica r=g=b
        graphics.setStroke(new BasicStroke(numVizinhosParaPintar + 2)); //define a largura da linha
        graphics.drawLine(xIni, yIni, xFim, yFim);
        graphics.dispose();
    }

    public static void main(String[] args) {

        new Software_Cromossomos();
    }

}
