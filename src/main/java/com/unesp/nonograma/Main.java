package com.unesp.nonograma;

import javax.swing.SwingUtilities;
import java.io.File;
public class Main {
    // Pasta com as imagens do banco fixo. Ajuste se o caminho de recursos
    // do projeto for outro (ex: usando classpath em vez de caminho relativo).
    private static final String PASTA_IMAGENS = "src/main/resources/imagens";


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BancoDePuzzles banco = new BancoDePuzzles(new File(PASTA_IMAGENS));

            // Dificuldade escolhida pelo jogador (1 = fácil, 2 = médio, 3 = difícil).
            // Por enquanto fixo em 2; dá pra trocar por uma tela de seleção depois.
            int dificuldadeEscolhida = 2;
            CalculadoraDificuldade.Nivel nivel = mapearNivel(dificuldadeEscolhida);

            BancoDePuzzles.PuzzleGerado puzzle = banco.sortear(nivel);

            Tabuleiro tb = new Tabuleiro("TP", 10, 10);
            tb.setDificuldade(dificuldadeEscolhida);
            tb.carregarPuzzle(puzzle.solucao);

            TelaJogo tela = new TelaJogo(tb, banco, nivel);
            tela.setVisible(true);
        });
    }

    private static CalculadoraDificuldade.Nivel mapearNivel(int dificuldade){
        switch (dificuldade){
            case 1: return CalculadoraDificuldade.Nivel.FACIL;
            case 3: return CalculadoraDificuldade.Nivel.DIFICIL;
            default: return CalculadoraDificuldade.Nivel.MEDIO;
        }
    }
}
