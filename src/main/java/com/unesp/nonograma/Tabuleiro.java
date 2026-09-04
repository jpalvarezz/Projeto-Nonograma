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

    /**
     * Gera um puzzle totalmente aleatório (ruído 50/50), como antes.
     * Mantido por compatibilidade / testes rápidos — para puzzles "de
     * verdade" com forma reconhecível, use carregarPuzzle(...) com uma
     * solução vinda de BancoDePuzzles.
     */
    public void gerarTabuleiroAleatorio() {

        Estado[][] solucaoInicial = new Estado[linhas][colunas];

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                solucaoInicial[l][c] = Math.random() < 0.5 ? Estado.MARCADA : Estado.VAZIO;
            }
        }

        carregarPuzzle(solucaoInicial);
    }
    /**
     * Carrega um puzzle a partir de uma solução pronta (por exemplo, gerada
     * a partir de uma imagem via GeradorImagem + BancoDePuzzles). Calcula
     * as pistas e recalcula todos os gabaritos possíveis compatíveis com
     * elas — o ideal é que essa solução já tenha sido validada como única
     * antes de chegar aqui (é isso que BancoDePuzzles faz na geração).
     */
    public void carregarPuzzle(Estado[][] solucao) {

        erros = 0;

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) celulas[l][c] = Estado.INTOCADA;
        }

        pistasLinha = SolverNonograma.calcularPistasLinha(solucao, linhas, colunas);
        pistasColuna = SolverNonograma.calcularPistasColuna(solucao, linhas, colunas);

        possiveisGabaritos = SolverNonograma.gerarTodasSolucoes(pistasLinha, pistasColuna, linhas, colunas);
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
