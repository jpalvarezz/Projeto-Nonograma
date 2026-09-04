package com.unesp.nonograma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TelaJogo extends JFrame {
    private Tabuleiro tb;
    private JButton[][] botoes;
    private JLabel labelErros;
    private JLabel labelStatus;
    private JLabel labelSolucoes;
    private JPanel painelTabuleiro;

    // Banco de puzzles e nível usados para sortear um novo puzzle real
    // (a partir de imagem) quando o jogador clica em "NOVO JOGO".
    // Ficam null se a tela for criada com o construtor antigo — nesse
    // caso "NOVO JOGO" cai de volta no ruído aleatório, como antes.
    private BancoDePuzzles banco;
    private CalculadoraDificuldade.Nivel nivel;


    private static final int LARGURA_PISTAS_LINHAS = 80;
    private static final int ALTURA_PISTAS_COLUNAS = 70;

    public TelaJogo(Tabuleiro tb, BancoDePuzzles banco, CalculadoraDificuldade.Nivel nivel) {
        this.tb = tb;
        this.banco = banco;
        this.nivel = nivel;
        configurarJanela();
        criarInterface();
        atualizarInterface();
    }

    private void configurarJanela() {
        setTitle("Nonograma - " + tb.qualNome());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void criarInterface() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        principal.setBackground(new Color(43, 43, 43));

        JLabel titulo = new JLabel(
                "NONOGRAMA",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        principal.add(titulo, BorderLayout.NORTH);
        principal.add(criarAreaJogo(), BorderLayout.CENTER);
        //Menu
        principal.add(criarMenu(), BorderLayout.EAST);
        add(principal);
    }

    private JPanel criarAreaJogo() {
        int linhas = tb.getLinhas();
        int colunas = tb.getColunas();
        JPanel area = new JPanel(new GridBagLayout());
        area.setBackground(new Color(43, 43, 43));
        GridBagConstraints gbc = new GridBagConstraints();
        //Canto superior esquerdo
        JPanel canto = new JPanel();
        canto.setBackground(new Color(43, 43, 43));

        canto.setPreferredSize(
                new Dimension(
                        LARGURA_PISTAS_LINHAS,
                        ALTURA_PISTAS_COLUNAS
                )
        );
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;

        area.add(canto, gbc);
        //Pistas das colunas
        JPanel painelPistasColunas = new JPanel(
                new GridLayout(1, colunas)
        );
        painelPistasColunas.setBackground(new Color(43, 43, 43));
        int[][] pistasColunas = tb.getPistasColuna();
        for (int c = 0; c < colunas; c++) {
            JLabel label = new JLabel(
                    formatarPistaVertical(pistasColunas[c]),
                    SwingConstants.CENTER
            );
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            painelPistasColunas.add(label);
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        area.add(painelPistasColunas, gbc);

        //Pistas das linhas
        JPanel painelPistasLinhas = new JPanel(
                new GridLayout(linhas, 1)
        );

        painelPistasLinhas.setBackground(new Color(43, 43, 43));
        painelPistasLinhas.setPreferredSize(
                new Dimension(LARGURA_PISTAS_LINHAS, 0)
        );
        int[][] pistasLinhas = tb.getPistasLinha();

        for (int l = 0; l < linhas; l++) {

            JLabel label = new JLabel(
                    formatarPistaHorizontal(pistasLinhas[l]),
                    SwingConstants.RIGHT
            );

            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 14));

            label.setBorder(
                    BorderFactory.createEmptyBorder(0, 0, 0, 8)
            );

            painelPistasLinhas.add(label);
        }
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        area.add(painelPistasLinhas, gbc);
        //Tabuleiro
        painelTabuleiro = criarPainelTabuleiro();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        area.add(painelTabuleiro, gbc);
        return area;
    }

    //Pistas verticais
    private String formatarPistaVertical(int[] pistas) {
        StringBuilder texto = new StringBuilder("<html>");
        for (int i = 0; i < pistas.length; i++) {
            texto.append(pistas[i]);
            if (i < pistas.length - 1) texto.append("<br>");
        }
        texto.append("</html>");
        return texto.toString();
    }

    //Pistas horizontais
    private String formatarPistaHorizontal(int[] pistas) {
        StringBuilder texto = new StringBuilder("<html>");
        for (int i = 0; i < pistas.length; i++) {
            texto.append(pistas[i]);
            if (i < pistas.length - 1) texto.append("&nbsp;&nbsp;");
        }

        texto.append("</html>");
        return texto.toString();
    }

    //Cria tabuleiro
    private JPanel criarPainelTabuleiro() {
        int linhas = tb.getLinhas();
        int colunas = tb.getColunas();
        JPanel painel = new JPanel(
                new GridLayout(linhas, colunas)
        );

        painel.setBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2)
        );
        botoes = new JButton[linhas][colunas];
        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                final int linha = l;
                final int coluna = c;

                JButton botao = new JButton();

                botao.setFocusPainted(false);
                botao.setMargin(new Insets(0, 0, 0, 0));
                botao.setFont(new Font("Arial", Font.BOLD, 20));

                botao.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {

                        if (tb.isGameOver() || tb.isVitoria()) return;

                        if (SwingUtilities.isLeftMouseButton(e)) {
                            clicarEsquerdo(linha, coluna);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            clicarDireito(linha, coluna);
                        }
                    }
                });
                botoes[l][c] = botao;
                painel.add(botao);
            }
        }
        return painel;
    }

    //Clique esquerdo
    private void clicarEsquerdo(int linha, int coluna) {

        Tabuleiro.Estado atual = tb.getEstadoCelula(linha, coluna);

        if (atual == Tabuleiro.Estado.MARCADA) {

            tb.desmarcar(linha, coluna);
            atualizarInterface();

            return;
        }

        tb.marcar(linha, coluna);
        verificarJogada(linha, coluna);
    }

    //Clique direito
    private void clicarDireito(int linha, int coluna) {

        Tabuleiro.Estado atual = tb.getEstadoCelula(linha, coluna);
         //INTOCADA -> VAZIO
         //VAZIO    -> INTOCADA
         //MARCADA  -> VAZIO
        if (atual == Tabuleiro.Estado.VAZIO) {
            tb.desmarcar(linha, coluna);
            atualizarInterface();

            return;
        }
        tb.vazio(linha, coluna);
        verificarJogada(linha, coluna);
    }

    //Verifica jogada
    private void verificarJogada(int linha, int coluna) {
        boolean correta = tb.verificarCelula(linha, coluna);
        atualizarInterface();
        //Erro
        if (!correta) {
            labelStatus.setText("ERRO!");
            JOptionPane.showMessageDialog(
                    this,
                    "Essa seleção não é compatível " +
                    "com nenhuma solução possível.",
                    "Erro",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        //Game over
        if (tb.isGameOver()) {

            labelStatus.setText("VOCÊ PERDEU!");

            JOptionPane.showMessageDialog(
                    this,
                    "Você atingiu o limite de erros!",
                    "Fim de jogo",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        //Vitoria
        if (tb.isVitoria()) {

            labelStatus.setText("VOCÊ VENCEU!");

            JOptionPane.showMessageDialog(
                    this,
                    "Parabéns! Você completou o nonograma!",
                    "Vitória",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        if (correta) labelStatus.setText("Continue jogando!");
    }

    //Menu
    private JPanel criarMenu() {

        JPanel menu = new JPanel();

        menu.setPreferredSize(new Dimension(190, 0));

        menu.setLayout(
                new BoxLayout(
                        menu,
                        BoxLayout.Y_AXIS
                )
        );

        menu.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 15, 20, 15
                )
        );

        menu.setBackground(Color.DARK_GRAY);

        //Erros
        labelErros = new JLabel();

        labelErros.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelErros.setForeground(Color.WHITE);
        labelErros.setFont(new Font("Arial", Font.BOLD, 18));

        menu.add(labelErros);
        menu.add(Box.createVerticalStrut(15));

        //Soluções restantes
        labelSolucoes = new JLabel();

        labelSolucoes.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelSolucoes.setForeground(Color.WHITE);
        labelSolucoes.setFont(new Font("Arial", Font.PLAIN, 14));

        menu.add(labelSolucoes);

        menu.add(Box.createVerticalStrut(15));

        //Status
        labelStatus = new JLabel(
                "Jogue!",
                SwingConstants.CENTER
        );

        labelStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelStatus.setForeground(Color.WHITE);
        labelStatus.setFont(new Font("Arial", Font.BOLD, 16));

        menu.add(labelStatus);
        menu.add(Box.createVerticalGlue());

        JButton novoJogo = new JButton("NOVO JOGO");
        novoJogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        novoJogo.setMaximumSize(
                new Dimension(160, 40)
        );
        novoJogo.addActionListener(e -> novoJogo());
        menu.add(novoJogo);
        return menu;
    }

    // Cria novo jogo voltando para a tela de seleção
    private void novoJogo() {
        this.dispose(); // Fecha a janela do jogo atual
        TelaSelecao tela = new TelaSelecao(banco); // Abre a tela de escolha novamente
        tela.setVisible(true);
    }

    private void atualizarInterface() {
        for (int l = 0; l < tb.getLinhas(); l++) {
            for (int c = 0; c < tb.getColunas(); c++) {
                JButton botao = botoes[l][c];
                Tabuleiro.Estado estado = tb.getEstadoCelula(l, c);
                //Intocada
                if (estado == Tabuleiro.Estado.INTOCADA) {
                    botao.setText("");
                    botao.setBackground(Color.LIGHT_GRAY);
                    botao.setForeground(Color.BLACK);
                }

                //Marcada
                else if (estado == Tabuleiro.Estado.MARCADA) {

                    botao.setText("■");
                    botao.setBackground(Color.BLACK);
                    botao.setForeground(Color.WHITE);
                }
                //Vazio
                else {
                    botao.setText("X");
                    botao.setBackground(Color.WHITE);
                    botao.setForeground(Color.RED);
                }
            }
        }
        // Atualiza erros
        labelErros.setText(
                "Erros: " + tb.qtosErros() +
                " / " + tb.getLimiteErros()
        );
        // Atualiza quantidade de soluções
        labelSolucoes.setText(
                "Soluções possíveis: " +
                tb.getQuantidadeGabaritos()
        );
    }
}
