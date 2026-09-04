package com.unesp.nonograma;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Carrega as imagens de um banco fixo (pasta de recursos), converte cada
 * uma em um puzzle válido (com solução única) e classifica a dificuldade
 * real de cada uma via CalculadoraDificuldade, agrupando por nível.
 *
 * O tamanho do tabuleiro é fixo (10x10) em todas as dificuldades — só o
 * padrão da imagem (e o limiar de conversão que acabou funcionando) muda.
 *
 * Uso esperado:
 *   BancoDePuzzles banco = new BancoDePuzzles(new File("src/main/resources/imagens"));
 *   BancoDePuzzles.PuzzleGerado puzzle = banco.sortear(CalculadoraDificuldade.Nivel.FACIL);
 *   Tabuleiro tb = new Tabuleiro("Nonograma", 10, 10);
 *   tb.carregarPuzzle(puzzle.solucao);
 */
public class BancoDePuzzles {

    private static final int LINHAS = 10;
    private static final int COLUNAS = 10;

    // Candidatos de limiar testados por imagem, do mais restritivo ao mais permissivo.
    // Fica com o primeiro que resultar em solução única.
    private static final int[] LIMIARES_CANDIDATOS = {100, 128, 150, 180};

    private final Map<CalculadoraDificuldade.Nivel, List<PuzzleGerado>> banco =
            new EnumMap<>(CalculadoraDificuldade.Nivel.class);

    private final Random random = new Random();

    public static class PuzzleGerado {

        public final String nomeArquivo;
        public final Tabuleiro.Estado[][] solucao;
        public final int[][] pistasLinha;
        public final int[][] pistasColuna;
        public final CalculadoraDificuldade.Nivel nivel;
        public final double slackMedio;

        PuzzleGerado(String nomeArquivo, Tabuleiro.Estado[][] solucao,
                     int[][] pistasLinha, int[][] pistasColuna,
                     CalculadoraDificuldade.Nivel nivel, double slackMedio) {
            this.nomeArquivo = nomeArquivo;
            this.solucao = solucao;
            this.pistasLinha = pistasLinha;
            this.pistasColuna = pistasColuna;
            this.nivel = nivel;
            this.slackMedio = slackMedio;
        }
    }

    public BancoDePuzzles(File pastaImagens) {

        for (CalculadoraDificuldade.Nivel nivel : CalculadoraDificuldade.Nivel.values()) {
            banco.put(nivel, new ArrayList<>());
        }

        if (pastaImagens == null || !pastaImagens.isDirectory()) {
            System.err.println("Pasta de imagens não encontrada: " + pastaImagens);
            return;
        }

        File[] arquivos = pastaImagens.listFiles((dir, nome) -> {
            String n = nome.toLowerCase();
            return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
        });

        if (arquivos == null) return;

        Arrays.sort(arquivos, Comparator.comparing(File::getName));

        for (File arquivo : arquivos) {
            processarImagem(arquivo);
        }
    }

    private void processarImagem(File arquivo) {

        BufferedImage imagem;

        try {
            imagem = ImageIO.read(arquivo);
        } catch (IOException e) {
            System.err.println("Não foi possível ler " + arquivo.getName() + ": " + e.getMessage());
            return;
        }

        if (imagem == null) {
            System.err.println("Formato não suportado: " + arquivo.getName());
            return;
        }

        // Tenta primeiro o limiar calculado automaticamente pra essa imagem
        // (método de Otsu); se não der solução única, cai nos candidatos fixos.
        List<Integer> candidatos = new ArrayList<>();
        candidatos.add(GeradorImagem.calcularLimiarOtsu(imagem));

        for (int fixo : LIMIARES_CANDIDATOS) {
            if (!candidatos.contains(fixo)) candidatos.add(fixo);
        }

        for (int limiar : candidatos) {

            Tabuleiro.Estado[][] solucao = GeradorImagem.converter(imagem, LINHAS, COLUNAS, limiar);

            int[][] pistasLinha = SolverNonograma.calcularPistasLinha(solucao, LINHAS, COLUNAS);
            int[][] pistasColuna = SolverNonograma.calcularPistasColuna(solucao, LINHAS, COLUNAS);

            List<Tabuleiro.Estado[][]> todasSolucoes =
                    SolverNonograma.gerarTodasSolucoes(pistasLinha, pistasColuna, LINHAS, COLUNAS);

            // Só aceita se a solução for única — senão o puzzle é ambíguo
            if (todasSolucoes.size() != 1) continue;

            double slack = CalculadoraDificuldade.slackMedio(pistasLinha, pistasColuna, LINHAS, COLUNAS);
            CalculadoraDificuldade.Nivel nivel = CalculadoraDificuldade.classificar(slack, COLUNAS);

            PuzzleGerado puzzle = new PuzzleGerado(arquivo.getName(), solucao, pistasLinha, pistasColuna, nivel, slack);

            banco.get(nivel).add(puzzle);
            System.out.printf("%s -> nível %s (slack médio %.2f, limiar %d)%n",
                    arquivo.getName(), nivel, slack, limiar);
            return;
        }

        System.err.println("Nenhum limiar gerou solução única para " + arquivo.getName() + " — pulando.");
    }

    public PuzzleGerado sortear(CalculadoraDificuldade.Nivel nivel) {

        List<PuzzleGerado> lista = banco.get(nivel);

        if (lista == null || lista.isEmpty()) {
            throw new IllegalStateException(
                    "Nenhum puzzle disponível para o nível " + nivel +
                            ". Adicione mais imagens em src/main/resources/imagens, " +
                            "ou ajuste os cortes em CalculadoraDificuldade.classificar()."
            );
        }

        return lista.get(random.nextInt(lista.size()));
    }

    public int quantidade(CalculadoraDificuldade.Nivel nivel) {
        return banco.get(nivel).size();
    }
}