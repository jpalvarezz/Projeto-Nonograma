package com.unesp.nonograma;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Banco de puzzles com soluções desenhadas à mão.
 * Cada desenho tem um padrão MARCADA/VAZIO definido em DEFINICOES e uma
 * imagem correspondente (carregada da pasta de recursos) usada só pra
 * "revelar" ao jogador depois que ele resolve o nonograma.
 */
public class BancoDePuzzles {

    private static final int LINHAS = 10;
    private static final int COLUNAS = 10;

    /** Um desenho: nome, arquivo de imagem pra reveal, e o padrão 10x10 ('X' = marcada, '.' = vazio). */
    private static final class DefinicaoPuzzle {
        final String nome;
        final String arquivoImagem;
        final String[] padrao;

        DefinicaoPuzzle(String nome, String arquivoImagem, String[] padrao) {
            this.nome = nome;
            this.arquivoImagem = arquivoImagem;
            this.padrao = padrao;
        }
    }

    // ---- Desenhos cadastrados ----
    private static final List<DefinicaoPuzzle> DEFINICOES = List.of(

            new DefinicaoPuzzle("flor", "flor.jpg", new String[] {
                    "..X.X.X...",
                    ".XX.X.XX..",
                    ".XXXXXXX..",
                    "..XXXXX...",
                    "..XXXXX.X.",
                    ".X.XXX.XX.",
                    ".XX.X.XX..",
                    "..X.X.XX..",
                    "..XXXXX...",
                    "...XXX...."
            }),

            new DefinicaoPuzzle("pintinho", "pintinho.jpg", new String[] {
                    "...XXXX...",
                    "..XXXXXX..",
                    ".X..XX..X.",
                    ".X.XXXX.X.",
                    "XXXXXXXXXX",
                    "XXXXXXXXXX",
                    "XXXXXXXXXX",
                    ".XXXXXXXX.",
                    "...XXXX...",
                    ".XXX..XXX."
            }),

            new DefinicaoPuzzle("raposa", "raposa.jpg", new String[] {
                    "..........",
                    ".X.......X",
                    ".XX.....XX",
                    ".XXX...XXX",
                    ".XXXXXXXXX",
                    ".XXXXXXXXX",
                    ".XXXXXXXXX",
                    "..XXXXXXX.",
                    "...XXXXX..",
                    "....XXX..."
            })
    );

    private final List<PuzzleGerado> banco = new ArrayList<>();
    private final Random random = new Random();

    public static class PuzzleGerado {

        public final String nome;
        public final Tabuleiro.Estado[][] solucao;
        public final int[][] pistasLinha;
        public final int[][] pistasColuna;
        public final double slackMedio;
        public final BufferedImage imagem;

        PuzzleGerado(String nome, Tabuleiro.Estado[][] solucao,
                     int[][] pistasLinha, int[][] pistasColuna,
                     double slackMedio, BufferedImage imagem) {
            this.nome = nome;
            this.solucao = solucao;
            this.pistasLinha = pistasLinha;
            this.pistasColuna = pistasColuna;
            this.slackMedio = slackMedio;
            this.imagem = imagem;
        }
    }

    public BancoDePuzzles(File pastaImagens) {
        for (DefinicaoPuzzle def : DEFINICOES) {
            processarDefinicao(def, pastaImagens);
        }
    }

    private void processarDefinicao(DefinicaoPuzzle def, File pastaImagens) {

        Tabuleiro.Estado[][] solucao = converterPadrao(def.padrao, LINHAS, COLUNAS);

        int[][] pistasLinha = SolverNonograma.calcularPistasLinha(solucao, LINHAS, COLUNAS);
        int[][] pistasColuna = SolverNonograma.calcularPistasColuna(solucao, LINHAS, COLUNAS);

        // Garantia de sanidade: mesmo desenhando à mão, confere se o
        // conjunto de pistas resultante não é ambíguo.
        List<Tabuleiro.Estado[][]> todasSolucoes =
                SolverNonograma.gerarTodasSolucoes(pistasLinha, pistasColuna, LINHAS, COLUNAS);

        if (todasSolucoes.size() != 1) {
            System.err.println("Aviso: '" + def.nome + "' tem " + todasSolucoes.size()
                    + " soluções possíveis (não é único) — revise o padrão.");
        }

        // Slack médio calculado só como informação/log.
        double slack = CalculadoraDificuldade.slackMedio(pistasLinha, pistasColuna, LINHAS, COLUNAS);

        BufferedImage imagem = carregarImagem(pastaImagens, def.arquivoImagem);

        banco.add(new PuzzleGerado(def.nome, solucao, pistasLinha, pistasColuna, slack, imagem));

        System.out.printf("%s carregado (slack médio %.2f)%n", def.nome, slack);
    }

    private static Tabuleiro.Estado[][] converterPadrao(String[] padrao, int linhas, int colunas) {

        if (padrao.length != linhas) {
            throw new IllegalArgumentException("Padrão precisa ter " + linhas + " linhas.");
        }

        Tabuleiro.Estado[][] grade = new Tabuleiro.Estado[linhas][colunas];

        for (int l = 0; l < linhas; l++) {
            String linha = padrao[l];
            if (linha.length() != colunas) {
                throw new IllegalArgumentException(
                        "Linha " + l + " precisa ter " + colunas + " caracteres (tem " + linha.length() + ").");
            }
            for (int c = 0; c < colunas; c++) {
                char ch = linha.charAt(c);
                grade[l][c] = (ch == 'X' || ch == 'x') ? Tabuleiro.Estado.MARCADA : Tabuleiro.Estado.VAZIO;
            }
        }

        return grade;
    }

    private BufferedImage carregarImagem(File pasta, String nomeArquivo) {
        if (pasta == null || nomeArquivo == null) return null;
        File arquivo = new File(pasta, nomeArquivo);
        try {
            return GeradorImagem.carregar(arquivo);
        } catch (IOException e) {
            System.err.println("Não foi possível carregar '" + nomeArquivo + "': " + e.getMessage());
            return null;
        }
    }

    /** Sorteia um puzzle qualquer entre todos os desenhos cadastrados. */
    public PuzzleGerado sortear() {
        if (banco.isEmpty()) {
            throw new IllegalStateException("Nenhum puzzle cadastrado em DEFINICOES.");
        }
        return banco.get(random.nextInt(banco.size()));
    }

    public int quantidade() {
        return banco.size();
    }
}