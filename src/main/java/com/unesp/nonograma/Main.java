package com.unesp.nonograma;

import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tabuleiro tb = new Tabuleiro("TP", 10, 10);
            tb.setDificuldade(2);
            tb.gerarTabuleiroAleatorio();
            TelaJogo tela = new TelaJogo(tb);
            tela.setVisible(true);
        });
    }
}
