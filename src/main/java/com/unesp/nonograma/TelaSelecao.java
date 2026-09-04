package com.unesp.nonograma;

import javax.swing.*;
import java.awt.*;

public class TelaSelecao extends JFrame {
    private BancoDePuzzles banco;

    // Variáveis para os botões de seleção de dificuldade
    private JRadioButton rbFacil, rbMedio, rbDificil;

    public TelaSelecao(BancoDePuzzles banco) {
        this.banco = banco;
        setTitle("Nonograma - Configuração do Jogo");
        setSize(450, 350); // Aumentei um pouco o tamanho para caber a dificuldade
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout principal
        JPanel painel = new JPanel(new GridLayout(5, 1, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel labelModo = new JLabel("Configuração da Partida:", SwingConstants.CENTER);
        labelModo.setFont(new Font("Arial", Font.BOLD, 20));

        // --- PAINEL DE DIFICULDADE ---
        JPanel painelDificuldade = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        rbFacil = new JRadioButton("Fácil");
        rbMedio = new JRadioButton("Médio");
        rbDificil = new JRadioButton("Difícil");

        // Define o Médio como selecionado por padrão
        rbMedio.setSelected(true);

        // Agrupa os botões para que apenas um possa ser selecionado por vez
        ButtonGroup grupoDificuldade = new ButtonGroup();
        grupoDificuldade.add(rbFacil);
        grupoDificuldade.add(rbMedio);
        grupoDificuldade.add(rbDificil);

        painelDificuldade.add(rbFacil);
        painelDificuldade.add(rbMedio);
        painelDificuldade.add(rbDificil);

        // --- BOTÕES DE MODO DE JOGO ---
        JButton btnAleatorio = new JButton("Gerar Aleatório");
        btnAleatorio.setFont(new Font("Arial", Font.BOLD, 16));
        btnAleatorio.addActionListener(e -> iniciarJogo(true));

        JButton btnDesenho = new JButton("Usar Desenho (Imagens)");
        btnDesenho.setFont(new Font("Arial", Font.BOLD, 16));
        btnDesenho.addActionListener(e -> iniciarJogo(false));

        // Adicionando tudo à tela
        painel.add(labelModo);
        painel.add(painelDificuldade); // Adiciona as opções de dificuldade
        painel.add(new JLabel("Escolha o modo para iniciar:", SwingConstants.CENTER)); // Texto instrutivo
        painel.add(btnAleatorio);
        painel.add(btnDesenho);

        add(painel);
    }

    // Método auxiliar para pegar a dificuldade escolhida nos RadioButtons
    private int obterDificuldadeSelecionada() {
        if (rbFacil.isSelected()) return 1;
        if (rbDificil.isSelected()) return 3;
        return 2; // Retorna 2 (Médio) por padrão
    }

    private void iniciarJogo(boolean aleatorio) {
        int dificuldadeEscolhida = obterDificuldadeSelecionada();
        CalculadoraDificuldade.Nivel nivel = mapearNivel(dificuldadeEscolhida);

        Tabuleiro tb = new Tabuleiro(aleatorio ? "Aleatório" : "Desenho", 10, 10);
        tb.setDificuldade(dificuldadeEscolhida);

        if (aleatorio) {
            tb.gerarTabuleiroAleatorio();
        } else {
            // Tratamento de erro caso o banco não tenha imagens para a dificuldade escolhida
            try {
                BancoDePuzzles.PuzzleGerado puzzle = banco.sortear(nivel);
                tb.carregarPuzzle(puzzle.solucao);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                        "Não há imagens suficientes cadastradas no nível " + nivel + ".\nAdicione mais imagens na pasta ou tente outro nível.",
                        "Falta de Imagens",
                        JOptionPane.WARNING_MESSAGE);
                return; // Impede que o jogo inicie quebrado e deixa o usuário na tela de seleção
            }
        }

        // Se tudo deu certo, abre a tela de jogo
        TelaJogo tela = new TelaJogo(tb, banco, nivel);
        tela.setVisible(true);
        this.dispose(); // Fecha a tela de seleção
    }

    private CalculadoraDificuldade.Nivel mapearNivel(int dificuldade) {
        switch (dificuldade) {
            case 1: return CalculadoraDificuldade.Nivel.FACIL;
            case 3: return CalculadoraDificuldade.Nivel.DIFICIL;
            default: return CalculadoraDificuldade.Nivel.MEDIO;
        }
    }
}