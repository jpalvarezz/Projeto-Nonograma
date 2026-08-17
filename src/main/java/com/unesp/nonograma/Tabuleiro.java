package com.unesp.nonograma;

import java.util.ArrayList;
import java.util.List;

public class Tabuleiro {

    private int linhas;   //nao vamos fazer mais que 10x10 pelo jeito
    private int colunas;

    public enum Estado {
        INTOCADA, MARCADA, VAZIO  //definicao de casas, para fazer a matriz enumerador
    }

    private int erros = 0; //quantidade de erros atua do jogo
    private String nome; //nome do tabuleiro
    private int dificuldade;
    private int limErros;
    private Estado[][] celulas; //matriz de enumerador de celulas (jogo atual)
    private Estado[][] estadoCorreto; //gabarito
    private int[][] pistasLinha;  //pistas (voce que vai calcular joao)
    private int[][] pistasColuna; //pistas (voce que vai calcular joao)

    //construtor
    public Tabuleiro(String nome, int linhas, int colunas) {
        this.nome = nome;
        this.linhas = linhas;
        this.colunas = colunas;
        this.celulas = new Estado[linhas][colunas];
        this.estadoCorreto = new Estado[linhas][colunas];

        for (int l = 0; l < linhas; l++){
            for (int c = 0; c < colunas; c++){
                this.celulas[l][c] = Estado.INTOCADA;
            }
        }

    }

    public void setDificuldade(int dificuldade){
        this.dificuldade = dificuldade;

        switch(dificuldade){
            case 1:
                this.limErros = 999;
                break;
            case 2:
                this.limErros = 3;
                break;
            case 3:
                this.limErros = 2;
                break;
                default:
                    this.limErros = 3;
        }
    }

    //gera um tabuleiro aleatorio, talvez mudar as chances de entrar marcado
    public void gerarTabuleiroAleatorio() {
        for (int l = 0; l < linhas; l++)
            for (int c = 0; c < colunas; c++)
                this.estadoCorreto[l][c] = Math.random() < 0.5 ? Estado.MARCADA : Estado.VAZIO;

        //Quando criar o gabarito, ja calcula as pistas
        calcularPistas();
    }

    //aqui compara o tabuleiro atual com o correto, para ver se marcou erro e corrigir
    public void compararEstado() {

        for (int l = 0; l < linhas; l++)
            for (int c = 0; c < colunas; c++) {

                if((estadoCorreto[l][c] != celulas[l][c]) && celulas[l][c] != Estado.INTOCADA){
                    erros++;
                    celulas[l][c] = estadoCorreto[l][c];
                }


            }
    }
    // Retorna true se o jogador perdeu (estourou o limite de erros)
    public boolean isGameOver() {
        return this.erros >= this.limErros;
    }

    // Confere se o jogador venceu (retorna boolean em vez de int)
    public boolean isVitoria() {
        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                // Se a célula deveria ser marcada e não foi
                if (estadoCorreto[l][c] == Estado.MARCADA && celulas[l][c] != Estado.MARCADA) {
                    return false; // Não venceu ainda
                }
                // Se a célula deveria ser vazia, mas o jogador marcou (erro)
                if (estadoCorreto[l][c] == Estado.VAZIO && celulas[l][c] == Estado.MARCADA) {
                    return false; // Não venceu ainda
                }
            }
        }
        return true; // Se passou por tudo sem problemas, venceu!
    }

    //marca uma casa
    public void marcar(int l, int c){
        celulas[l][c] = Estado.MARCADA;
    }
    //marca uma casa como vazio
    public void vazio(int l, int c){
        celulas[l][c] = Estado.VAZIO;
    }
    //retorna a quantidade atual de erros
    public int qtosErros() {
        return erros;
    }
    //retorna nome do tabuleiro
    public String qualNome() {
        return nome;
    }

    public void calcularPistas(){
        //Inicializar as matrizes
        this.pistasLinha = new int[linhas][];
        this.pistasColuna = new int[colunas][];

        //Calculo das pistas das LINHAS
        for (int l = 0; l < linhas; l++){
            List<Integer> dicasLinha = new ArrayList<>();
            int blocosSeguidos = 0;

            for(int c = 0; c < colunas; c++){
                if(estadoCorreto[l][c] ==  Estado.MARCADA){
                    blocosSeguidos++;
                } else {
                    if(blocosSeguidos > 0){
                        dicasLinha.add(blocosSeguidos);
                        blocosSeguidos = 0;
                    }
                }
            }
            // Adiciona o ultimo bloco se a linha terminar com marcação
            if(blocosSeguidos > 0){
                dicasLinha.add(blocosSeguidos);
            }

            //Se a linha inteira for vazia, a dica padrão é 0
            if(dicasLinha.isEmpty()){
                dicasLinha.add(0);
            }

            //Converte a lista para o array da classe
            pistasLinha[l] = new int[dicasLinha.size()];
            for(int i = 0; i < dicasLinha.size(); i++){
                pistasLinha[l][i] = dicasLinha.get(i);
            }
        }

        ////Calculo das pistas das COLUNAS
        for(int c = 0; c < colunas; c++){
            List<Integer> dicasColuna = new ArrayList<>();
            int blocosSeguidos = 0;

            for(int l = 0; l < linhas; l++){
                if(estadoCorreto[l][c] == Estado.MARCADA){
                    blocosSeguidos++;
                } else{
                    if(blocosSeguidos > 0){
                        dicasColuna.add(blocosSeguidos);
                        blocosSeguidos = 0;
                    }
                }
            }
            if(blocosSeguidos > 0){
                dicasColuna.add(blocosSeguidos);
            }
            if(dicasColuna.isEmpty()){
                dicasColuna.add(0);
            }

            pistasColuna[c] = new int[dicasColuna.size()];
            for(int i = 0; i < dicasColuna.size(); i++){
                pistasColuna[c][i] = dicasColuna.get(i);
            }
        }
    }

    //Getters para o GUI
    public int[][] getPistasLinha(){
        return pistasLinha;
    }

    public int[][] getPistasColuna(){
        return pistasColuna;
    }

    // Método para o Front-end ler o estado de uma célula específica
    public Estado getEstadoCelula(int l, int c) {
        return this.celulas[l][c];
    }

    // Útil para o Front-end saber o tamanho da grade na hora de criar os botões
    public int getLinhas() { return linhas; }
    public int getColunas() { return colunas; }

}