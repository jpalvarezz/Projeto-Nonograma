package com.unesp.nonograma;

import java.util.ArrayList;
import java.util.List;

public class Tabuleiro {

    private int linhas;
    private int colunas;

    public enum Estado {
        INTOCADA,
        MARCADA,
        VAZIO
    }

    private int erros = 0;

    private String nome;
    private int dificuldade;
    private int limErros;

    // Estado atual escolhido pelo jogador
    private Estado[][] celulas;

    // Pistas
    private int[][] pistasLinha;
    private int[][] pistasColuna;

    // Cada Estado[][] representa uma possivel solução
    private List<Estado[][]> possiveisGabaritos;

    public Tabuleiro(String nome, int linhas, int colunas) {

        this.nome = nome;
        this.linhas = linhas;
        this.colunas = colunas;

        this.celulas = new Estado[linhas][colunas];

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) celulas[l][c] = Estado.INTOCADA;
        }

        possiveisGabaritos = new ArrayList<>();
    }

    // DIFICULDADE

    public void setDificuldade(int dificuldade) {

        this.dificuldade = dificuldade;

        switch (dificuldade) {

            case 1:
                limErros = 999;
                break;

            case 2:
                limErros = 3;
                break;

            case 3:
                limErros = 2;
                break;

            default:
                limErros = 3;
        }
    }

    public void gerarTabuleiroAleatorio() {

        erros = 0;

        // Limpa o tabuleiro
        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) celulas[l][c] = Estado.INTOCADA;
        }

        // Cria solucao aleatoria
        // Usada para criar as pistas.
        Estado[][] solucaoInicial = new Estado[linhas][colunas];

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                solucaoInicial[l][c] = Math.random() < 0.5 ? Estado.MARCADA : Estado.VAZIO;
                // True se < 0.5, False se >= 0.5
            }
        }

        calcularPistas(solucaoInicial);

        // Gera outras possiveis solucoes
        gerarPossiveisGabaritos();
    }

    private void calcularPistas(Estado[][] estado) {

        pistasLinha = new int[linhas][];
        pistasColuna = new int[colunas][];

        // LINHAS
        for (int l = 0; l < linhas; l++) {

            List<Integer> dicas = new ArrayList<>();
            int bloco = 0;

            for (int c = 0; c < colunas; c++) {

                if (estado[l][c] == Estado.MARCADA) {
                    bloco++;
                } else {
                    if (bloco > 0) {
                        dicas.add(bloco);
                        bloco = 0;
                    }
                }
            }

            if (bloco > 0) dicas.add(bloco);
            if (dicas.isEmpty()) dicas.add(0);

            pistasLinha[l] = new int[dicas.size()];

            for (int i = 0; i < dicas.size(); i++) pistasLinha[l][i] = dicas.get(i);
        }

        // COLUNAS
        for (int c = 0; c < colunas; c++) {

            List<Integer> dicas = new ArrayList<>();
            int bloco = 0;

            for (int l = 0; l < linhas; l++) {

                if (estado[l][c] == Estado.MARCADA) {
                    bloco++;
                } else {
                    if (bloco > 0) {
                        dicas.add(bloco);
                        bloco = 0;
                    }
                }
            }

            if (bloco > 0) dicas.add(bloco);
            if (dicas.isEmpty()) dicas.add(0);

            pistasColuna[c] = new int[dicas.size()];

            for (int i = 0; i < dicas.size(); i++) pistasColuna[c][i] = dicas.get(i);
        }
    }

    private void gerarPossiveisGabaritos() {

        possiveisGabaritos.clear();

        // Possibilidades de cada linha
        List<List<boolean[]>> possibilidadesLinhas = new ArrayList<>();

        for (int l = 0; l < linhas; l++) {

            List<boolean[]> possibilidades = new ArrayList<>();

            gerarLinhasPossiveis(
                    pistasLinha[l],
                    0,
                    new boolean[colunas],
                    possibilidades
            );

            possibilidadesLinhas.add(possibilidades);
        }

        /*
         * Agora combinamos as possibilidades das linhas.
         *
         * A poda das colunas evita gerar combinações
         * impossíveis.
         */
        Estado[][] tabuleiro = new Estado[linhas][colunas];

        buscarSolucoes(
                0,
                possibilidadesLinhas,
                tabuleiro
        );
    }

    // Possiveis solucoes da linha
    private void gerarLinhasPossiveis(
            int[] pistas,
            int indicePista,
            boolean[] linha,
            List<boolean[]> resultado
    ) {

        // Linha vazia
        if (pistas.length == 1 && pistas[0] == 0) {
            resultado.add(linha.clone());
            return;
        }

        gerarBlocos(
                pistas,
                indicePista,
                0,
                linha,
                resultado
        );
    }

    private void gerarBlocos(
            int[] pistas,
            int indicePista,
            int posicao,
            boolean[] linha,
            List<boolean[]> resultado
    ) {

        // Todos os blocos foram colocados
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

            // Insere bloco
            for (int i = 0; i < tamanhoBloco; i++) linha[inicio + i] = true;

            int proximaPosicao = inicio + tamanhoBloco;

            // Precisa de pelo menos 1 de espacamento
            if (indicePista < pistas.length - 1) proximaPosicao++;

            gerarBlocos(
                    pistas,
                    indicePista + 1,
                    proximaPosicao,
                    linha,
                    resultado
            );

            // Desfaz bloco
            for (int i = 0; i < tamanhoBloco; i++) linha[inicio + i] = false;
        }
    }

    private void buscarSolucoes(
            int linhaAtual,
            List<List<boolean[]>> possibilidadesLinhas,
            Estado[][] tabuleiro
    ) {

        // Todas as linhas foram preenchidas
        if (linhaAtual == linhas) {
            Estado[][] solucao = copiarTabuleiro(tabuleiro);
            possiveisGabaritos.add(solucao);
            return;
        }

        for (boolean[] possibilidade : possibilidadesLinhas.get(linhaAtual)) {

            // Coloca a linha no tabuleiro
            for (int c = 0; c < colunas; c++) {
                tabuleiro[linhaAtual][c] = possibilidade[c] ? Estado.MARCADA : Estado.VAZIO;
            }

            if (colunasAindaPossiveis(tabuleiro, linhaAtual)) {
                buscarSolucoes(
                        linhaAtual + 1,
                        possibilidadesLinhas,
                        tabuleiro
                );
            }
        }
    }

    // Tratamento das colunas
    private boolean colunasAindaPossiveis(
            Estado[][] tabuleiro,
            int ultimaLinha
    ) {

        for (int c = 0; c < colunas; c++) {

            int[] pista = pistasColuna[c];
            int indicePista = 0;
            int blocoAtual = 0;

            for (int l = 0; l <= ultimaLinha; l++) {

                if (tabuleiro[l][c] == Estado.MARCADA) {
                    blocoAtual++;
                } else {

                    if (blocoAtual > 0) {

                        // Bloco ultrapassou pista
                        if (indicePista >= pista.length) return false;

                        if (blocoAtual != pista[indicePista]) return false;

                        indicePista++;
                        blocoAtual = 0;
                    }
                }
            }

            // Bloco pode continuar na proxima linha
            if (blocoAtual > 0) {

                if (indicePista >= pista.length) return false;

                if (blocoAtual > pista[indicePista]) return false;
            }

            int restantes = linhas - ultimaLinha - 1;
            int necessario = 0;

            if (blocoAtual > 0) {

                necessario = pista[indicePista] - blocoAtual;

                int proximasPistas = pista.length - indicePista - 1;

                if (proximasPistas > 0) necessario += proximasPistas;

            } else {

                int proximasPistas = pista.length - indicePista;

                if (proximasPistas > 0) {

                    necessario = 0;

                    for (int i = indicePista; i < pista.length; i++) necessario += pista[i];

                    necessario += proximasPistas - 1;
                }
            }

            if (necessario > restantes) return false;
        }

        return true;
    }

    // Filtra gabaritos
    public boolean verificarCelula(int l, int c) {

        Estado escolhido = celulas[l][c];

        if (escolhido == Estado.INTOCADA) return true;

        // Remove incompativeis
        List<Estado[][]> restantes = new ArrayList<>();

        for (Estado[][] gabarito : possiveisGabaritos) {
            if (gabarito[l][c] == escolhido) restantes.add(gabarito);
        }

        // Jogada impossivel
        if (restantes.isEmpty()) {

            erros++;

            // Desfaz a jogada
            celulas[l][c] = Estado.INTOCADA;

            return false;
        }

        possiveisGabaritos = restantes;

        return true;
    }

    // Desmarca
    public void desmarcar(int l, int c) {
        celulas[l][c] = Estado.INTOCADA;
    }

    // Vitoria
    public boolean isVitoria() {

        // Todas foram preenchidas
        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                if (celulas[l][c] == Estado.INTOCADA) return false;
            }
        }

        return !possiveisGabaritos.isEmpty();
    }

    // Getter

    public int[][] getPistasLinha() {
        return pistasLinha;
    }

    public int[][] getPistasColuna() {
        return pistasColuna;
    }

    public Estado getEstadoCelula(int l, int c) {
        return celulas[l][c];
    }

    public int getLinhas() {
        return linhas;
    }

    public int getColunas() {
        return colunas;
    }

    public int qtosErros() {
        return erros;
    }

    public String qualNome() {
        return nome;
    }

    public int getLimiteErros() {
        return limErros;
    }

    public int getQuantidadeGabaritos() {
        return possiveisGabaritos.size();
    }

    // Copia tabuleiro
    private Estado[][] copiarTabuleiro(Estado[][] original) {

        Estado[][] copia = new Estado[linhas][colunas];

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) copia[l][c] = original[l][c];
        }

        return copia;
    }

    public void marcar(int l, int c) {
        celulas[l][c] = Estado.MARCADA;
    }

    public void vazio(int l, int c) {
        celulas[l][c] = Estado.VAZIO;
    }

    public boolean isGameOver() {
        return erros >= limErros;
    }
}
