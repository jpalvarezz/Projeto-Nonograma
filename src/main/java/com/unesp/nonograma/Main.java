package com.unesp.nonograma;

import javax.swing.SwingUtilities;
import java.io.File;

public class Main {
    // Pasta com as imagens do banco fixo.
    private static final String PASTA_IMAGENS = "src/main/resources/imagens";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Inicializa o banco apenas uma vez ao abrir o jogo
            BancoDePuzzles banco = new BancoDePuzzles(new File(PASTA_IMAGENS));

            // Abre a tela inicial de seleção
            TelaSelecao tela = new TelaSelecao(banco);
            tela.setVisible(true);
        });
    }
}