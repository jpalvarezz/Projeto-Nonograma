package com.unesp.nonograma;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TelaSelecao extends JFrame {

    private final BancoDePuzzles banco;

    private JRadioButton rbFacil, rbMedio, rbDificil;

    public TelaSelecao(BancoDePuzzles banco) {
        this.banco = banco;

        setTitle("Nonograma - Configuração do Jogo");
        setSize(480, 420);
        setMinimumSize(new Dimension(420, 380));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(TemaVisual.FUNDO_JANELA);

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(new EmptyBorder(36, 40, 36, 40));
        painel.setBackground(TemaVisual.FUNDO_JANELA);

        JLabel titulo = new JLabel("NONOGRAMA", SwingConstants.CENTER);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setFont(TemaVisual.fonteTitulo(32));
        titulo.setForeground(TemaVisual.ACCENT);
        painel.add(titulo);

        JLabel subtitulo = new JLabel("Configure sua partida", SwingConstants.CENTER);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitulo.setFont(TemaVisual.fonteTexto(14));
        subtitulo.setForeground(TemaVisual.TEXTO_SUAVE);
        painel.add(subtitulo);

        painel.add(Box.createVerticalStrut(30));

        JLabel labelDificuldade = new JLabel("DIFICULDADE");
        labelDificuldade.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelDificuldade.setFont(TemaVisual.fonteTextoNegrito(13));
        labelDificuldade.setForeground(TemaVisual.TEXTO_SUAVE);
        painel.add(labelDificuldade);
        painel.add(Box.createVerticalStrut(10));

        JPanel painelDificuldade = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        painelDificuldade.setOpaque(false);
        painelDificuldade.setAlignmentX(Component.CENTER_ALIGNMENT);

        rbFacil = criarRadio("Fácil");
        rbMedio = criarRadio("Médio");
        rbDificil = criarRadio("Difícil");
        rbMedio.setSelected(true);

        ButtonGroup grupoDificuldade = new ButtonGroup();
        grupoDificuldade.add(rbFacil);
        grupoDificuldade.add(rbMedio);
        grupoDificuldade.add(rbDificil);

        painelDificuldade.add(rbFacil);
        painelDificuldade.add(rbMedio);
        painelDificuldade.add(rbDificil);
        painel.add(painelDificuldade);

        painel.add(Box.createVerticalStrut(34));

        JLabel labelModo = new JLabel("MODO DE JOGO");
        labelModo.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelModo.setFont(TemaVisual.fonteTextoNegrito(13));
        labelModo.setForeground(TemaVisual.TEXTO_SUAVE);
        painel.add(labelModo);
        painel.add(Box.createVerticalStrut(12));

        JButtonEstilizado btnAleatorio = TemaVisual.criarBotao("GERAR ALEATÓRIO", new Color(70, 74, 84));
        btnAleatorio.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAleatorio.setMaximumSize(new Dimension(280, 46));
        btnAleatorio.addActionListener(e -> iniciarJogo(true));
        painel.add(btnAleatorio);

        painel.add(Box.createVerticalStrut(12));

        JButtonEstilizado btnDesenho = TemaVisual.criarBotao("USAR DESENHO (IMAGENS)", TemaVisual.ACCENT_ESCURO);
        btnDesenho.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDesenho.setMaximumSize(new Dimension(280, 46));
        btnDesenho.addActionListener(e -> iniciarJogo(false));
        painel.add(btnDesenho);

        add(painel);
    }

    private JRadioButton criarRadio(String texto) {
        JRadioButton radio = new JRadioButton(texto);
        radio.setOpaque(false);
        radio.setForeground(TemaVisual.TEXTO_CLARO);
        radio.setFont(TemaVisual.fonteTexto(14));
        radio.setFocusPainted(false);
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return radio;
    }

    private int obterDificuldadeSelecionada() {
        if (rbFacil.isSelected()) return 1;
        if (rbDificil.isSelected()) return 3;
        return 2;
    }

    private void iniciarJogo(boolean aleatorio) {
        int dificuldadeEscolhida = obterDificuldadeSelecionada();
        CalculadoraDificuldade.Nivel nivel = mapearNivel(dificuldadeEscolhida);

        Tabuleiro tb = new Tabuleiro(aleatorio ? "Aleatório" : "Desenho", 10, 10);
        tb.setDificuldade(dificuldadeEscolhida);

        if (aleatorio) {
            tb.gerarTabuleiroAleatorio();
        } else {
            try {
                BancoDePuzzles.PuzzleGerado puzzle = banco.sortear(nivel);
                tb.carregarPuzzle(puzzle.solucao);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                        "Não há imagens suficientes cadastradas no nível " + nivel + ".\nAdicione mais imagens na pasta ou tente outro nível.",
                        "Falta de Imagens",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        TelaJogo tela = new TelaJogo(tb, banco, nivel);
        tela.setVisible(true);
        this.dispose();
    }

    private CalculadoraDificuldade.Nivel mapearNivel(int dificuldade) {
        switch (dificuldade) {
            case 1: return CalculadoraDificuldade.Nivel.FACIL;
            case 3: return CalculadoraDificuldade.Nivel.DIFICIL;
            default: return CalculadoraDificuldade.Nivel.MEDIO;
        }
    }
}