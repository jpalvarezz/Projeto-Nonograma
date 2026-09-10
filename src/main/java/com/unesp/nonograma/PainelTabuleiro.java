package com.unesp.nonograma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Componente que desenha o tabuleiro inteiro (pistas de linha, pistas de
 * coluna e a grade de células) em tempo real, num único Graphics2D, em
 * vez de usar uma grade de JButton dentro de um GridLayout.
 */
public class PainelTabuleiro extends JPanel {

    /** Recebe os eventos de interação, traduzidos para coordenadas (linha, coluna) do tabuleiro. */
    public interface OuvinteTabuleiro {
        void aoPressionar(int linha, int coluna, int botao);
        void aoArrastarPara(int linha, int coluna);
        void aoSoltar();
    }

    private final Tabuleiro tabuleiro;
    private final OuvinteTabuleiro ouvinte;

    // Geometria calculada a cada paintComponent (guardada para o mouse usar a mesma conta)
    private double cellSize;
    private double origemX, origemY;   // canto superior-esquerdo da GRADE (depois das pistas)
    private double larguraPistas, alturaPistas;

    private int hoverLinha = -1, hoverColuna = -1;

    // Quando true, a grade inteira é substituída pela imagem original
    // (ver Tabuleiro.getImagem), em vez do padrão de jogo — usado para
    // "revelar" a imagem completa no fim da partida.
    private boolean revelarCores = false;

    public PainelTabuleiro(Tabuleiro tabuleiro, OuvinteTabuleiro ouvinte) {
        this.tabuleiro = tabuleiro;
        this.ouvinte = ouvinte;

        setBackground(TemaVisual.FUNDO_PAINEL);
        setOpaque(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int[] cel = celulaEm(e.getX(), e.getY());
                if (cel != null) ouvinte.aoPressionar(cel[0], cel[1], e.getButton());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int[] cel = celulaEm(e.getX(), e.getY());
                if (cel != null) {
                    ouvinte.aoArrastarPara(cel[0], cel[1]);
                    atualizarHover(cel[0], cel[1]);
                } else {
                    atualizarHover(-1, -1);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ouvinte.aoSoltar();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int[] cel = celulaEm(e.getX(), e.getY());
                if (cel != null) atualizarHover(cel[0], cel[1]);
                else atualizarHover(-1, -1);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                atualizarHover(-1, -1);
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    /** Ativa/desativa a revelação da imagem real (fim de jogo). */
    public void setRevelarCores(boolean revelar) {
        this.revelarCores = revelar;
        repaint();
    }

    private void atualizarHover(int linha, int coluna) {
        if (linha != hoverLinha || coluna != hoverColuna) {
            hoverLinha = linha;
            hoverColuna = coluna;
            repaint();
        }
    }

    /** Converte um ponto em pixels para (linha, coluna) do tabuleiro, ou null se estiver fora da grade. */
    private int[] celulaEm(int x, int y) {
        if (cellSize <= 0) return null;

        double relX = x - origemX;
        double relY = y - origemY;

        if (relX < 0 || relY < 0) return null;

        int coluna = (int) (relX / cellSize);
        int linha = (int) (relY / cellSize);

        if (linha < 0 || linha >= tabuleiro.getLinhas() || coluna < 0 || coluna >= tabuleiro.getColunas()) {
            return null;
        }

        return new int[]{linha, coluna};
    }

    /** Recalcula a geometria (tamanho de célula, origem da grade) para o tamanho atual do painel. */
    private void calcularGeometria() {
        int linhas = tabuleiro.getLinhas();
        int colunas = tabuleiro.getColunas();

        int[][] pistasLinha = tabuleiro.getPistasLinha();
        int[][] pistasColuna = tabuleiro.getPistasColuna();

        int maxBlocosLinha = 1;
        for (int[] p : pistasLinha) maxBlocosLinha = Math.max(maxBlocosLinha, p.length);

        int maxBlocosColuna = 1;
        for (int[] p : pistasColuna) maxBlocosColuna = Math.max(maxBlocosColuna, p.length);

        double unidadesLargura = clamp(maxBlocosLinha * 0.85, 2.2, 4.5);
        double unidadesAltura = clamp(maxBlocosColuna * 0.85, 2.0, 4.0);

        int largura = getWidth();
        int altura = getHeight();

        double candidatoPorLargura = largura / (colunas + unidadesLargura);
        double candidatoPorAltura = altura / (linhas + unidadesAltura);

        cellSize = Math.max(4, Math.min(candidatoPorLargura, candidatoPorAltura));

        larguraPistas = unidadesLargura * cellSize;
        alturaPistas = unidadesAltura * cellSize;

        double larguraTotal = larguraPistas + colunas * cellSize;
        double alturaTotal = alturaPistas + linhas * cellSize;

        origemX = (largura - larguraTotal) / 2.0 + larguraPistas;
        origemY = (altura - alturaTotal) / 2.0 + alturaPistas;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static Color clarearLeve(Color cor) {
        int r = Math.min(255, cor.getRed() + 28);
        int g = Math.min(255, cor.getGreen() + 28);
        int b = Math.min(255, cor.getBlue() + 28);
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        calcularGeometria();

        desenharFaixaDestaque(g2);
        desenharCelulas(g2);
        desenharGrade(g2);
        desenharPistasLinha(g2);
        desenharPistasColuna(g2);

        g2.dispose();
    }

    private void desenharFaixaDestaque(Graphics2D g2) {
        if (hoverLinha < 0 || hoverColuna < 0) return;

        int linhas = tabuleiro.getLinhas();
        int colunas = tabuleiro.getColunas();

        g2.setColor(TemaVisual.FAIXA_DESTAQUE);

        g2.fill(new Rectangle2DDouble(origemX - larguraPistas, origemY + hoverLinha * cellSize, larguraPistas + colunas * cellSize, cellSize));
        g2.fill(new Rectangle2DDouble(origemX + hoverColuna * cellSize, origemY - alturaPistas, cellSize, alturaPistas + linhas * cellSize));
    }

    private void desenharCelulas(Graphics2D g2) {
        int linhas = tabuleiro.getLinhas();
        int colunas = tabuleiro.getColunas();

        // --- REVELAÇÃO DA IMAGEM NA VITÓRIA ---
        // Se revelarCores for true, desenha a imagem original inteira sobre
        // a área da grade, em vez de pintar célula por célula.
        if (revelarCores) {
            BufferedImage imagem = tabuleiro.getImagem();
            if (imagem != null) {
                g2.drawImage(imagem, (int) Math.round(origemX), (int) Math.round(origemY),
                        (int) Math.round(colunas * cellSize), (int) Math.round(linhas * cellSize), null);
                return; // Pula o desenho normal das células
            }
        }

        double margem = Math.max(1.5, cellSize * 0.07);

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {

                double x = origemX + c * cellSize;
                double y = origemY + l * cellSize;

                boolean emDestaque = (l == hoverLinha || c == hoverColuna);

                // --- MODO NORMAL DE JOGO ---
                Tabuleiro.Estado estado = tabuleiro.getEstadoCelula(l, c);

                // Célula com erro: a jogada já foi desfeita (estado voltou pra
                // INTOCADA), mas mostramos o "X" de erro sobre o fundo OPOSTO
                // ao que foi tentado — já que a tentativa estava errada, o
                // fundo mostrado é o outro: tentou MARCAR (botão esquerdo) e
                // errou -> mostra fundo VAZIO; tentou marcar como VAZIO
                // (botão direito) e errou -> mostra fundo PREENCHIDO.
                if (tabuleiro.isEmErro(l, c)) {
                    Tabuleiro.Estado tentativa = tabuleiro.getEstadoTentadoErro(l, c);

                    if (tentativa == Tabuleiro.Estado.MARCADA) {
                        g2.setColor(TemaVisual.CELULA_VAZIA);
                        g2.fill(new Rectangle2DDouble(x, y, cellSize, cellSize));
                    } else {
                        g2.setColor(emDestaque ? TemaVisual.CELULA_MARCADA_BRILHO : TemaVisual.CELULA_MARCADA);
                        g2.fill(new RoundRectangle2D.Double(x + margem, y + margem,
                                cellSize - margem * 2, cellSize - margem * 2, cellSize * 0.18, cellSize * 0.18));
                    }

                    desenharX(g2, x, y, cellSize);
                    continue;
                }

                switch (estado) {
                    case MARCADA -> {
                        g2.setColor(emDestaque ? TemaVisual.CELULA_MARCADA_BRILHO : TemaVisual.CELULA_MARCADA);
                        g2.fill(new RoundRectangle2D.Double(x + margem, y + margem,
                                cellSize - margem * 2, cellSize - margem * 2, cellSize * 0.18, cellSize * 0.18));
                    }
                    case VAZIO -> {
                        g2.setColor(TemaVisual.CELULA_VAZIA);
                        g2.fill(new Rectangle2DDouble(x, y, cellSize, cellSize));

                        double raioPonto = cellSize * 0.10;
                        g2.setColor(TemaVisual.CELULA_VAZIA_PONTO);
                        g2.fill(new Ellipse2D.Double(
                                x + cellSize / 2.0 - raioPonto, y + cellSize / 2.0 - raioPonto,
                                raioPonto * 2, raioPonto * 2));
                    }
                    default -> { // INTOCADA
                        g2.setColor(emDestaque ? TemaVisual.CELULA_INTOCADA_HOVER : TemaVisual.CELULA_INTOCADA);
                        g2.fill(new Rectangle2DDouble(x, y, cellSize, cellSize));
                    }
                }
            }
        }
    }

    /** Desenha o "X" vermelho de erro, centralizado na célula em (x, y). */
    private void desenharX(Graphics2D g2, double x, double y, double tamanho) {
        double margemX = tamanho * 0.26;

        g2.setColor(TemaVisual.ERRO);
        g2.setStroke(new BasicStroke((float) Math.max(2, tamanho * 0.11), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.draw(new Line2DDouble(x + margemX, y + margemX, x + tamanho - margemX, y + tamanho - margemX));
        g2.draw(new Line2DDouble(x + tamanho - margemX, y + margemX, x + margemX, y + tamanho - margemX));
    }

    private void desenharGrade(Graphics2D g2) {
        int linhas = tabuleiro.getLinhas();
        int colunas = tabuleiro.getColunas();

        double xFim = origemX + colunas * cellSize;
        double yFim = origemY + linhas * cellSize;

        for (int c = 0; c <= colunas; c++) {
            double x = origemX + c * cellSize;
            g2.setColor(c % 5 == 0 ? TemaVisual.LINHA_GRADE_FORTE : TemaVisual.LINHA_GRADE);
            g2.setStroke(new BasicStroke(c % 5 == 0 ? 2.2f : 1f));
            g2.draw(new Line2DDouble(x, origemY, x, yFim));
        }

        for (int l = 0; l <= linhas; l++) {
            double y = origemY + l * cellSize;
            g2.setColor(l % 5 == 0 ? TemaVisual.LINHA_GRADE_FORTE : TemaVisual.LINHA_GRADE);
            g2.setStroke(new BasicStroke(l % 5 == 0 ? 2.2f : 1f));
            g2.draw(new Line2DDouble(origemX, y, xFim, y));
        }
    }

    private void desenharPistasLinha(Graphics2D g2) {
        int[][] pistasLinha = tabuleiro.getPistasLinha();
        int linhas = tabuleiro.getLinhas();

        Font fonte = TemaVisual.fonteTextoNegrito((int) clamp(cellSize * 0.34, 9, 22));
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();

        for (int l = 0; l < linhas; l++) {
            int[] pista = pistasLinha[l];
            String texto = formatarPista(pista, "  ");

            boolean emDestaque = (l == hoverLinha);
            g2.setColor(emDestaque ? TemaVisual.ACCENT_CLARO : TemaVisual.TEXTO_CLARO);

            double centroY = origemY + l * cellSize + cellSize / 2.0;
            double xTexto = origemX - larguraPistas + (larguraPistas - fm.stringWidth(texto)) - cellSize * 0.25;

            g2.drawString(texto, (float) Math.max(4, xTexto), (float) (centroY + fm.getAscent() / 2.0 - fm.getDescent() / 2.0));
        }
    }

    private void desenharPistasColuna(Graphics2D g2) {
        int[][] pistasColuna = tabuleiro.getPistasColuna();
        int colunas = tabuleiro.getColunas();

        Font fonte = TemaVisual.fonteTextoNegrito((int) clamp(cellSize * 0.32, 9, 20));
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();
        double alturaLinhaTexto = fm.getHeight() * 0.95;

        for (int c = 0; c < colunas; c++) {
            int[] pista = pistasColuna[c];

            boolean emDestaque = (c == hoverColuna);
            g2.setColor(emDestaque ? TemaVisual.ACCENT_CLARO : TemaVisual.TEXTO_CLARO);

            double centroX = origemX + c * cellSize + cellSize / 2.0;

            double yBase = origemY - cellSize * 0.18;

            for (int i = pista.length - 1; i >= 0; i--) {
                String texto = String.valueOf(pista[i]);
                double xTexto = centroX - fm.stringWidth(texto) / 2.0;
                g2.drawString(texto, (float) xTexto, (float) yBase);
                yBase -= alturaLinhaTexto;
            }
        }
    }

    private static String formatarPista(int[] pista, String separador) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pista.length; i++) {
            sb.append(pista[i]);
            if (i < pista.length - 1) sb.append(separador);
        }
        return sb.toString();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(560, 560);
    }

    private static class Rectangle2DDouble extends java.awt.geom.Rectangle2D.Double {
        Rectangle2DDouble(double x, double y, double w, double h) { super(x, y, w, h); }
    }

    private static class Line2DDouble extends java.awt.geom.Line2D.Double {
        Line2DDouble(double x1, double y1, double x2, double y2) { super(x1, y1, x2, y2); }
    }
}