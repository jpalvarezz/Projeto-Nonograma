package com.unesp.nonograma;

import java.awt.*;

/**
 * Paleta de cores e fontes usada em todas as telas do jogo, num lugar só.
 * Isso evita ficar espalhando "new Color(43,43,43)" mágico em cada tela
 * e garante que tudo (menu, tabuleiro, tela de vitória) combine visualmente.
 */
public final class TemaVisual {

    private TemaVisual() { }

    // Fundos
    public static final Color FUNDO_JANELA   = new Color(24, 26, 32);
    public static final Color FUNDO_PAINEL   = new Color(32, 35, 43);
    public static final Color FUNDO_MENU     = new Color(20, 22, 28);

    // Destaque (accent) — usado em botões, bordas ativas, título
    public static final Color ACCENT         = new Color(94, 156, 255);
    public static final Color ACCENT_ESCURO  = new Color(64, 116, 209);
    public static final Color ACCENT_CLARO   = new Color(150, 190, 255);

    // Estados de jogo
    public static final Color SUCESSO        = new Color(76, 201, 130);
    public static final Color ERRO           = new Color(235, 87, 87);
    public static final Color AVISO          = new Color(240, 173, 78);

    // Células do tabuleiro
    public static final Color CELULA_INTOCADA        = new Color(223, 226, 232);
    public static final Color CELULA_INTOCADA_HOVER  = new Color(238, 240, 244);
    public static final Color CELULA_MARCADA         = new Color(41, 44, 54);
    public static final Color CELULA_MARCADA_BRILHO  = new Color(70, 74, 88);
    public static final Color CELULA_VAZIA           = new Color(255, 255, 255);
    public static final Color CELULA_VAZIA_PONTO     = new Color(150, 155, 165);
    public static final Color LINHA_GRADE            = new Color(60, 64, 74);
    public static final Color LINHA_GRADE_FORTE      = new Color(15, 16, 20);
    public static final Color FAIXA_DESTAQUE         = new Color(94, 156, 255, 35);

    // Textos
    public static final Color TEXTO_CLARO    = new Color(235, 237, 240);
    public static final Color TEXTO_SUAVE    = new Color(170, 175, 185);

    public static final String FAMILIA_FONTE = "Segoe UI";

    public static Font fonteTitulo(int tamanho) {
        return new Font(FAMILIA_FONTE, Font.BOLD, tamanho);
    }

    public static Font fonteTexto(int tamanho) {
        return new Font(FAMILIA_FONTE, Font.PLAIN, tamanho);
    }

    public static Font fonteTextoNegrito(int tamanho) {
        return new Font(FAMILIA_FONTE, Font.BOLD, tamanho);
    }

    /** Botão estilizado padrão (usado no menu lateral e nas telas de fim/seleção). */
    public static JButtonEstilizado criarBotao(String texto, Color corFundo) {
        return new JButtonEstilizado(texto, corFundo);
    }
}