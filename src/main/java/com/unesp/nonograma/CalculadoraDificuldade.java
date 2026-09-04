package com.unesp.nonograma;

/**
 * Calcula uma métrica de dificuldade real para um puzzle de nonograma,
 * baseada na FOLGA (slack) das linhas e colunas — não na quantidade de
 * blocos preenchidos.
 *
 * Slack de uma linha = quanto espaço "livre" sobra além do mínimo
 * necessário para encaixar todos os blocos da pista com 1 espaço entre eles.
 * Quanto menor o slack, menos arranjos possíveis aquela linha tem sozinha
 * (mais fácil de deduzir na lógica). Quanto maior o slack, mais ambíguo
 * fica isoladamente, exigindo cruzar informação com outras linhas/colunas.
 */
public class CalculadoraDificuldade {

    public enum Nivel {
        FACIL, MEDIO, DIFICIL
    }

    private static int calcularSlack(int[] pistas, int tamanho) {

        // Linha totalmente vazia: slack máximo (nenhuma restrição real)
        if (pistas.length == 1 && pistas[0] == 0) return tamanho;

        int somaBlocos = 0;
        for (int p : pistas) somaBlocos += p;

        int espacosObrigatorios = pistas.length - 1;
        int minimoNecessario = somaBlocos + espacosObrigatorios;

        return tamanho - minimoNecessario;
    }

    public static double slackMedio(int[][] pistasLinha, int[][] pistasColuna, int linhas, int colunas) {

        double somaSlack = 0;
        int total = 0;

        for (int[] pista : pistasLinha) {
            somaSlack += calcularSlack(pista, colunas);
            total++;
        }

        for (int[] pista : pistasColuna) {
            somaSlack += calcularSlack(pista, linhas);
            total++;
        }

        return total == 0 ? 0 : somaSlack / total;
    }

    /**
     * Classifica o nível com base no slack médio relativo ao tamanho
     * da linha/coluna (proporção, não valor absoluto — assim funciona
     * independente do tamanho do tabuleiro).
     *
     * Os cortes (0.20 / 0.35) são um ponto de partida razoável para um
     * tabuleiro 10x10; se, na prática, os níveis ficarem desbalanceados
     * (ex: quase tudo caindo em DIFICIL), ajuste esses dois números.
     */
    public static Nivel classificar(double slackMedio, int tamanhoReferencia) {

        double proporcao = slackMedio / tamanhoReferencia;

        if (proporcao < 0.30) return Nivel.FACIL;
        if (proporcao < 0.45) return Nivel.MEDIO;
        return Nivel.DIFICIL;
    }
}
