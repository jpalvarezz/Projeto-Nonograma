package com.unesp.nonograma;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Tela de configuração da partida.
 */
public class TelaSelecao extends JFrame {

    private final BancoDePuzzles banco;

    private JRadioButton rbFacil, rbMedio, rbDificil;
    private JRadioButton rbModoAleatorio, rbModoImagem;
    private JLabel labelDificuldade;

    public TelaSelecao(BancoDePuzzles banco) {
        this.banco = banco;

        setTitle("Nonograma - Configuração do Jogo");
        setSize(480, 460);
        setMinimumSize(new Dimension(420, 420));
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

        // ---------- MODO DE JOGO ----------

        JLabel labelModo = new JLabel("MODO DE JOGO");
        labelModo.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelModo.setFont(TemaVisual.fonteTextoNegrito(13));
        labelModo.setForeground(TemaVisual.TEXTO_SUAVE);
        painel.add(labelModo);
        painel.add(Box.createVerticalStrut(10));

        JPanel painelModo = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        painelModo.setOpaque(false);
        painelModo.setAlignmentX(Component.CENTER_ALIGNMENT);

        rbModoAleatorio = criarRadio("Aleatório");
        rbModoImagem = criarRadio("Desenho (imagens)");
        rbModoAleatorio.setSelected(true);

        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rbModoAleatorio);
        grupoModo.add(rbModoImagem);

        painelModo.add(rbModoAleatorio);
        painelModo.add(rbModoImagem);
        painel.add(painelModo);

        painel.add(Box.createVerticalStrut(34));

        // ---------- DIFICULDADE (aplica a ambos os modos) ----------

        labelDificuldade = new JLabel("DIFICULDADE");
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

        JLabel avisoImagem = new JLabel("No modo Desenho, o puzzle é sorteado livremente; a dificuldade só define o limite de erros", SwingConstants.CENTER);
        avisoImagem.setAlignmentX(Component.CENTER_ALIGNMENT);
        avisoImagem.setFont(TemaVisual.fonteTexto(11));
        avisoImagem.setForeground(new Color(120, 124, 134));
        avisoImagem.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        avisoImagem.setVisible(false);
        painel.add(avisoImagem);

        painel.add(Box.createVerticalStrut(30));

        // ---------- BOTÃO ÚNICO ----------

        JButtonEstilizado btnJogar = TemaVisual.criarBotao("JOGAR", TemaVisual.ACCENT_ESCURO);
        btnJogar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnJogar.setMaximumSize(new Dimension(280, 46));
        btnJogar.addActionListener(e -> iniciarJogo(rbModoAleatorio.isSelected()));
        painel.add(btnJogar);

        // Mostra o aviso de sorteio apenas no modo Desenho
        ActionListener atualizarAvisoModo = e -> avisoImagem.setVisible(rbModoImagem.isSelected());
        rbModoAleatorio.addActionListener(atualizarAvisoModo);
        rbModoImagem.addActionListener(atualizarAvisoModo);
        atualizarAvisoModo.actionPerformed(null); // aplica o estado inicial

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

        BancoDePuzzles.PuzzleGerado puzzleEscolhido = null;
        int dificuldadeEscolhida = obterDificuldadeSelecionada();
        CalculadoraDificuldade.Nivel nivel = mapearNivel(dificuldadeEscolhida);

        if (!aleatorio) {

            try {
                puzzleEscolhido = banco.sortear();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                        "Nenhum puzzle cadastrado.\nAdicione desenhos em BancoDePuzzles.DEFINICOES.",
                        "Falta de Imagens",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Tabuleiro tb = new Tabuleiro(aleatorio ? "Aleatório" : "Desenho", 10, 10);
        tb.setDificuldade(dificuldadeEscolhida);

        if (aleatorio) {
            tb.gerarTabuleiroAleatorio();
        } else {
            tb.carregarPuzzle(puzzleEscolhido.solucao, puzzleEscolhido.imagem);
        }

        TelaJogo tela = new TelaJogo(tb, banco, nivel, puzzleEscolhido);
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