package com.unesp.nonograma;

import javax.swing.*;
import java.awt.*;

public class TelaJogo extends JFrame {

    // Atributos
    private Tabuleiro tb;
    private JButton[][] botoes;

    // Construtor
    public TelaJogo(Tabuleiro tb) {
        this.tb = tb;

        //CONFIGURAÇÕES BÁSICAS DA JANELA
        setTitle("Nonograma - " + tb.qualNome());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //O FUNDO FLEXÍVEL pra centralizar tudo
        // O GridBagLayout sem restrições centraliza o conteúdo
        JPanel painelFundo = new JPanel(new GridBagLayout());
        painelFundo.setBackground(new Color(43, 43, 43));

        //Junta Menu e jogo
        // BorderLayout com 30 pixels de espaço horizontal entre as áreas
        JPanel painelAgrupador = new JPanel(new BorderLayout(30, 0));
        painelAgrupador.setOpaque(false); // Transparente para o fundo cinza escuro aparecer

        //O MENU LATERAL
        JPanel painelLateral = new JPanel();
        painelLateral.setPreferredSize(new Dimension(200, 0));
        painelLateral.setBackground(Color.DARK_GRAY);

        //A ÁREA DO JOGO
        JPanel painelAreaJogo = new JPanel(new BorderLayout());
        painelAreaJogo.setPreferredSize(new Dimension(450, 450));
        painelAreaJogo.setBackground(new Color(43, 43, 43));

        // Cria a grade de botões chamando o método auxiliar
        JPanel painelTabuleiro = criarPainelTabuleiro();
        painelAreaJogo.add(painelTabuleiro, BorderLayout.CENTER);

        // Coloca o menu e o Jogo dentro do Agrupador
        painelAgrupador.add(painelLateral, BorderLayout.WEST);
        painelAgrupador.add(painelAreaJogo, BorderLayout.CENTER);

        // Coloca o Agrupador no centro do Fundo
        painelFundo.add(painelAgrupador);

        // Adiciona o Fundo na Janela Principal
        add(painelFundo, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    // Método pra criar a matriz de botões
    private JPanel criarPainelTabuleiro() {
        int linhas = tb.getLinhas();
        int colunas = tb.getColunas();

        JPanel painel = new JPanel(new GridLayout(linhas, colunas));
        botoes = new JButton[linhas][colunas];

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                botoes[l][c] = new JButton();
                botoes[l][c].setBackground(Color.LIGHT_GRAY); //COR INICIAL DA CELULA INTOCADA

                // Remove as margens internas do botão para ele parecer mais um quadrado de grade
                botoes[l][c].setMargin(new Insets(0, 0, 0, 0));

                painel.add(botoes[l][c]);
            }
        }

        return painel;
    }
}