package com.unesp.nonograma;

import javax.swing.*;
import java.awt.*;

public class TelaJogo extends JFrame{
    private Tabuleiro tb;
    private JButton[][] botoes;
    private JPanel painelLateral;

    public TelaJogo(Tabuleiro tb){
        this.tb = tb;

        setTitle("Nonograma - " + tb.qualNome());
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        painelLateral = new JPanel();
        painelLateral.setPreferredSize(new Dimension(200, 0));
        painelLateral.setBackground(Color.DARK_GRAY);

        inicializarTabuleiro();

        setLocationRelativeTo(null);

    }

    public void inicializarTabuleiro(){
        int linhas = tb.getLinhas();
        int colunas = tb.getColunas();

        JPanel painel = new JPanel();
        painel.setLayout(new GridLayout(linhas, colunas));

        botoes = new JButton[linhas][colunas];

        for(int l = 0; l < linhas; l++){
            for(int c = 0; c < colunas; c++){
                botoes[l][c] = new JButton();
                botoes[l][c].setBackground(Color.LIGHT_GRAY);
                painel.add(botoes[l][c]);
            }
        }

        add(painel, BorderLayout.CENTER);

    }
}
