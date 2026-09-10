package com.unesp.nonograma;

import java.util.ArrayList;
import java.util.List;

 //Calcula pistas a partir de uma solução e enumera todas as soluções compatíveis com um conjunto de pistas. 
 //Não depende de UI nem de estado de jogo, por isso pode ser
 //usado tanto pelo Tabuleiro (durante a partida) quanto pelo BancoDePuzzles para validar se um puzzle gerado de imagem tem solução única
public class SolverNonograma {

    public static int[][] calcularPistasLinha(Tabuleiro.Estado[][] solucao, int linhas, int colunas) {

        int[][] pistas = new int[linhas][];

        for (int l = 0; l < linhas; l++) {
            pistas[l] = calcularPistasDeUmaSequencia(solucao, l, colunas, true);
        }

        return pistas;
    }

    public static int[][] calcularPistasColuna(Tabuleiro.Estado[][] solucao, int linhas, int colunas) {

        int[][] pistas = new int[colunas][];

        for (int c = 0; c < colunas; c++) {
            pistas[c] = calcularPistasDeUmaSequencia(solucao, c, linhas, false);
        }

        return pistas;
    }

    // linha=true -> lê a linha 'indice' percorrendo colunas; linha=false -> lê a coluna 'indice' percorrendo linhas
    private static int[] calcularPistasDeUmaSequencia(Tabuleiro.Estado[][] solucao, int indice, int tamanho, boolean linha) {

        List<Integer> dicas = new ArrayList<>();
        int bloco = 0;

        for (int i = 0; i < tamanho; i++) {

            Tabuleiro.Estado estado = linha ? solucao[indice][i] : solucao[i][indice];

            if (estado == Tabuleiro.Estado.MARCADA) {
                bloco++;
            } else if (bloco > 0) {
                dicas.add(bloco);
                bloco = 0;
            }
        }

        if (bloco > 0) dicas.add(bloco);
        if (dicas.isEmpty()) dicas.add(0);

        int[] resultado = new int[dicas.size()];
        for (int i = 0; i < dicas.size(); i++) resultado[i] = dicas.get(i);

        return resultado;
    }

    //Gera todas as soluções compatíveis com as pistas
    public static List<Tabuleiro.Estado[][]> gerarTodasSolucoes(
            int[][] pistasLinha, int[][] pistasColuna, int linhas, int colunas) {

        List<List<boolean[]>> possibilidadesLinhas = new ArrayList<>();

        for (int l = 0; l < linhas; l++) {
            List<boolean[]> possibilidades = new ArrayList<>();
            gerarLinhasPossiveis(pistasLinha[l], 0, new boolean[colunas], possibilidades);
            possibilidadesLinhas.add(possibilidades);
        }

        List<Tabuleiro.Estado[][]> solucoes = new ArrayList<>();
        Tabuleiro.Estado[][] tabuleiro = new Tabuleiro.Estado[linhas][colunas];
        buscarSolucoes(0, possibilidadesLinhas, tabuleiro, pistasColuna, linhas, colunas, solucoes);

        return solucoes;
    }

    private static void gerarLinhasPossiveis(int[] pistas, int indicePista, boolean[] linha, List<boolean[]> resultado) {

        if (pistas.length == 1 && pistas[0] == 0) {
            resultado.add(linha.clone());
            return;
        }

        gerarBlocos(pistas, indicePista, 0, linha, resultado);
    }

    private static void gerarBlocos(int[] pistas, int indicePista, int posicao, boolean[] linha, List<boolean[]> resultado) {

        int colunas = linha.length;

        if (indicePista >= pistas.length) {
            resultado.add(linha.clone());
            return;
        }

        int tamanhoBloco = pistas[indicePista];
        int espacoRestante = 0;

        for (int i = indicePista + 1; i < pistas.length; i++) espacoRestante += pistas[i];

        int blocosRestantes = pistas.length - indicePista - 1;
        espacoRestante += blocosRestantes;

        int maxInicio = colunas - tamanhoBloco - espacoRestante;

        for (int inicio = posicao; inicio <= maxInicio; inicio++) {

            for (int i = 0; i < tamanhoBloco; i++) linha[inicio + i] = true;

            int proximaPosicao = inicio + tamanhoBloco;
            if (indicePista < pistas.length - 1) proximaPosicao++;

            gerarBlocos(pistas, indicePista + 1, proximaPosicao, linha, resultado);

            for (int i = 0; i < tamanhoBloco; i++) linha[inicio + i] = false;
        }
    }

    private static void buscarSolucoes(
            int linhaAtual,
            List<List<boolean[]>> possibilidadesLinhas,
            Tabuleiro.Estado[][] tabuleiro,
            int[][] pistasColuna,
            int linhas, int colunas,
            List<Tabuleiro.Estado[][]> solucoes) {

        if (linhaAtual == linhas) {
            solucoes.add(copiar(tabuleiro, linhas, colunas));
            return;
        }

        for (boolean[] possibilidade : possibilidadesLinhas.get(linhaAtual)) {

            for (int c = 0; c < colunas; c++) {
                tabuleiro[linhaAtual][c] = possibilidade[c] ? Tabuleiro.Estado.MARCADA : Tabuleiro.Estado.VAZIO;
            }

            if (colunasAindaPossiveis(tabuleiro, linhaAtual, pistasColuna, linhas, colunas)) {
                buscarSolucoes(linhaAtual + 1, possibilidadesLinhas, tabuleiro, pistasColuna, linhas, colunas, solucoes);
            }
        }
    }

    private static boolean colunasAindaPossiveis(
            Tabuleiro.Estado[][] tabuleiro, int ultimaLinha, int[][] pistasColuna, int linhas, int colunas) {

        for (int c = 0; c < colunas; c++) {

            int[] pista = pistasColuna[c];
            int indicePista = 0;
            int blocoAtual = 0;

            for (int l = 0; l <= ultimaLinha; l++) {

                if (tabuleiro[l][c] == Tabuleiro.Estado.MARCADA) {
                    blocoAtual++;
                } else if (blocoAtual > 0) {

                    if (indicePista >= pista.length) return false;
                    if (blocoAtual != pista[indicePista]) return false;

                    indicePista++;
                    blocoAtual = 0;
                }
            }

            if (blocoAtual > 0) {
                if (indicePista >= pista.length) return false;
                if (blocoAtual > pista[indicePista]) return false;
            }

            int restantes = linhas - ultimaLinha - 1;
            int necessario;

            if (blocoAtual > 0) {

                necessario = pista[indicePista] - blocoAtual;
                int proximasPistas = pista.length - indicePista - 1;
                if (proximasPistas > 0) necessario += proximasPistas;

            } else {

                int proximasPistas = pista.length - indicePista;
                necessario = 0;

                if (proximasPistas > 0) {
                    for (int i = indicePista; i < pista.length; i++) necessario += pista[i];
                    necessario += proximasPistas - 1;
                }
            }

            if (necessario > restantes) return false;
        }

        return true;
    }

    private static Tabuleiro.Estado[][] copiar(Tabuleiro.Estado[][] original, int linhas, int colunas) {

        Tabuleiro.Estado[][] copia = new Tabuleiro.Estado[linhas][colunas];

        for (int l = 0; l < linhas; l++) {
            System.arraycopy(original[l], 0, copia[l], 0, colunas);
        }

        return copia;
    }
}
