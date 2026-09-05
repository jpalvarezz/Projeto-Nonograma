package com.unesp.nonograma;

import javax.swing.*;
import java.awt.*;

/**
 * Janela de fim de jogo (vitória ou derrota), estilizada para combinar
 * com o resto do jogo — em vez do JOptionPane padrão do Swing.
 */
public class TelaFimDeJogo extends JDialog {

    public interface AcoesFimDeJogo {
        void aoClicarNovoJogo();
        void aoClicarFechar();
    }

    public TelaFimDeJogo(JFrame dono, boolean vitoria, String mensagem, AcoesFimDeJogo acoes) {
        super(dono, true);
        setUndecorated(true);
        setSize(400, 300);
        setLocationRelativeTo(dono);

        Color corDestaque = vitoria ? TemaVisual.SUCESSO : TemaVisual.ERRO;

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(TemaVisual.FUNDO_PAINEL);
        raiz.setBorder(BorderFactory.createLineBorder(corDestaque, 3));

        JLabel titulo = new JLabel(vitoria ? "VOCÊ VENCEU!" : "FIM DE JOGO", SwingConstants.CENTER);
        titulo.setFont(TemaVisual.fonteTitulo(30));
        titulo.setForeground(corDestaque);
        titulo.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));
        raiz.add(titulo, BorderLayout.NORTH);

        JLabel texto = new JLabel("<html><div style='text-align:center;'>" + mensagem + "</div></html>", SwingConstants.CENTER);
        texto.setFont(TemaVisual.fonteTexto(15));
        texto.setForeground(TemaVisual.TEXTO_SUAVE);
        texto.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        raiz.add(texto, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JButtonEstilizado btnNovoJogo = TemaVisual.criarBotao("NOVO JOGO", TemaVisual.ACCENT_ESCURO);
        btnNovoJogo.addActionListener(e -> {
            dispose();
            acoes.aoClicarNovoJogo();
        });

        JButtonEstilizado btnFechar = TemaVisual.criarBotao("FECHAR", new Color(70, 74, 84));
        btnFechar.addActionListener(e -> {
            dispose();
            acoes.aoClicarFechar();
        });

        painelBotoes.add(btnNovoJogo);
        painelBotoes.add(btnFechar);
        raiz.add(painelBotoes, BorderLayout.SOUTH);

        setContentPane(raiz);
    }
}