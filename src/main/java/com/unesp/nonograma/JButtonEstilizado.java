package com.unesp.nonograma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Botão com cantos arredondados e efeito de hover/pressionado, pintado
 * manualmente (em vez de usar o visual padrão do Swing, que foge do
 * resto da estética escura do jogo).
 */
public class JButtonEstilizado extends JButton {

    private final Color corBase;
    private Color corAtual;

    public JButtonEstilizado(String texto, Color corBase) {
        super(texto);
        this.corBase = corBase;
        this.corAtual = corBase;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(TemaVisual.TEXTO_CLARO);
        setFont(TemaVisual.fonteTextoNegrito(15));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                corAtual = clarear(corBase, 0.15f);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                corAtual = corBase;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                corAtual = escurecer(corBase, 0.15f);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                corAtual = getModel().isRollover() ? clarear(corBase, 0.15f) : corBase;
                repaint();
            }
        });
    }

    private static Color clarear(Color cor, float fator) {
        int r = (int) Math.min(255, cor.getRed() + 255 * fator);
        int g = (int) Math.min(255, cor.getGreen() + 255 * fator);
        int b = (int) Math.min(255, cor.getBlue() + 255 * fator);
        return new Color(r, g, b);
    }

    private static Color escurecer(Color cor, float fator) {
        int r = (int) Math.max(0, cor.getRed() * (1 - fator));
        int g = (int) Math.max(0, cor.getGreen() * (1 - fator));
        int b = (int) Math.max(0, cor.getBlue() * (1 - fator));
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arco = 14;
        g2.setColor(isEnabled() ? corAtual : corAtual.darker());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arco, arco));

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 140), Math.max(d.height, 40));
    }
}