package com.unesp.nonograma;

import java.awt.Color;
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

    // Marca, por célula, se a última tentativa ali deu erro (jogada
    // desfeita) e qual estado o jogador tentou colocar — é isso que
    // permite desenhar o "X" de erro com o fundo certo (vazio ou já
    // pintado) mesmo depois da jogada ter sido desfeita internamente.
    private boolean[][] emErro;
    private Estado[][] estadoTentadoErro;

    // Pistas
    private int[][] pistasLinha;
    private int[][] pistasColuna;

    // Cada Estado[][] representa uma possivel solução
    private List<Estado[][]> possiveisGabaritos;

    // Cor média real de cada célula, vinda da imagem original (null se o
    // puzzle não veio de imagem, ex: modo aleatório — nesse caso o
    // tabuleiro cai de volta na cor monocromática padrão).
    private Color[][] cores;

    public Tabuleiro(String nome, int linhas, int colunas) {

        this.nome = nome;
        this.linhas = linhas;
        this.colunas = colunas;

        this.celulas = new Estado[linhas][colunas];
        this.emErro = new boolean[linhas][colunas];
        this.estadoTentadoErro = new Estado[linhas][colunas];

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
     * Carrega um puzzle a partir de uma solução pronta, sem cores reais
     * (usado pelo modo aleatório). Equivale a carregarPuzzle(solucao, null).
     */
    public void carregarPuzzle(Estado[][] solucao) {
        carregarPuzzle(solucao, null);
    }

    /**
     * Carrega um puzzle a partir de uma solução pronta (por exemplo, gerada
     * a partir de uma imagem via GeradorImagem + BancoDePuzzles). Calcula
     * as pistas e recalcula todos os gabaritos possíveis compatíveis com
     * elas — o ideal é que essa solução já tenha sido validada como única
     * antes de chegar aqui (é isso que BancoDePuzzles faz na geração).
     *
     * @param cores cor média real de cada célula, vinda da imagem original,
     *              usada para "revelar" o tabuleiro colorido na vitória.
     *              Pode ser null (puzzle sem imagem de origem, ex: aleatório).
     */
    public void carregarPuzzle(Estado[][] solucao, Color[][] cores) {

        erros = 0;
        this.cores = cores;

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                celulas[l][c] = Estado.INTOCADA;
                emErro[l][c] = false;
                estadoTentadoErro[l][c] = null;
            }
        }

        pistasLinha = SolverNonograma.calcularPistasLinha(solucao, linhas, colunas);
        pistasColuna = SolverNonograma.calcularPistasColuna(solucao, linhas, colunas);

        possiveisGabaritos = SolverNonograma.gerarTodasSolucoes(pistasLinha, pistasColuna, linhas, colunas);
    }

    /** Cor real da célula (l, c), ou null se não houver imagem de origem / célula de fundo. */
    public Color getCorCelula(int l, int c) {
        return cores != null ? cores[l][c] : null;
    }

    /**
     * Devolve o gabarito já quando a lista de possibilidades convergiu pra
     * um só (mesmo critério usado por isVitoria) — usado pela tela pra
     * "revelar" o desenho completo assim que o jogador vence. Retorna null
     * enquanto ainda houver mais de uma solução possível.
     */
    public Estado[][] getGabaritoSeUnico() {
        return possiveisGabaritos.size() == 1 ? possiveisGabaritos.get(0) : null;
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

            // Guarda o que foi tentado para poder desenhar o "X" de erro
            // com o fundo certo, mesmo desfazendo a jogada logo em seguida.
            emErro[l][c] = true;
            estadoTentadoErro[l][c] = escolhido;

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
        limparErro(l, c);
    }

    // Limpa o "X" de erro de uma célula (chamado sempre que o jogador tenta de novo)
    private void limparErro(int l, int c) {
        emErro[l][c] = false;
        estadoTentadoErro[l][c] = null;
    }

    // A célula está marcada com o "X" de erro (última tentativa ali foi inválida)?
    public boolean isEmErro(int l, int c) {
        return emErro[l][c];
    }

    // Qual estado o jogador tentou colocar na última tentativa errada (MARCADA ou VAZIO)
    public Estado getEstadoTentadoErro(int l, int c) {
        return estadoTentadoErro[l][c];
    }

    /**
     * Vitória: não é mais necessário preencher TODAS as células (inclusive
     * as vazias) — marcar VAZIO é só um apoio visual/lógico pro jogador,
     * que ainda assim conta como erro se for feito no lugar errado (isso
     * já é tratado em verificarCelula).
     *
     * A condição real de vitória é:
     *  1) a lista de gabaritos possíveis já convergiu pra um só (ou seja,
     *     não sobrou ambiguidade sobre qual é a solução — pra puzzles de
     *     imagem isso já é verdade desde o início, já que o BancoDePuzzles
     *     só aceita imagens com solução única; pra puzzles aleatórios pode
     *     ser preciso marcar alguns vazios pra eliminar as outras hipóteses);
     *  2) toda célula que é MARCADA nesse gabarito único já foi marcada
     *     como MARCADA pelo jogador.
     */
    public boolean isVitoria() {

        if (possiveisGabaritos.size() != 1) return false;

        Estado[][] solucao = possiveisGabaritos.get(0);

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                if (solucao[l][c] == Estado.MARCADA && celulas[l][c] != Estado.MARCADA) {
                    return false;
                }
            }
        }

        return true;
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
        limparErro(l, c);
    }

    public void vazio(int l, int c) {
        celulas[l][c] = Estado.VAZIO;
        limparErro(l, c);
    }

    public boolean isGameOver() {
        return erros >= limErros;
    }
}