package com.unesp.nonograma;


public class Main {
    public static void main(String[] args) {
        Tabuleiro tb = new Tabuleiro("Paiola", 10, 10);
        tb.setDificuldade(2);
        tb.gerarTabuleiroAleatorio();

        TelaJogo tela = new TelaJogo(tb);
        tela.setVisible(true);
    }
}
