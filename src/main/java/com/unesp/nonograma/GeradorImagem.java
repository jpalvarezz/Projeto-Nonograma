package com.unesp.nonograma;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Converte uma imagem (ícone/silhueta simples) em uma matriz de Estado
 * (MARCADA/VAZIO) do tamanho do tabuleiro, para ser usada como a
 * "solução secreta" de um puzzle.
 *
 * Recomendações para as imagens do banco:
 *  - Silhueta simples, sem muitos detalhes finos (o tabuleiro é 10x10,
 *    detalhes pequenos se perdem no redimensionamento).
 *  - Fundo transparente OU bem claro, desenho escuro — funciona melhor
 *    com o threshold de luminância usado aqui.
 *  - PNG é preferível a JPG por preservar transparência e bordas nítidas.
 */

public class GeradorImagem {
    /**
     * @param imagem  imagem de origem
     * @param linhas  altura do tabuleiro
     * @param colunas largura do tabuleiro
     * @param limiar  limiar de luminância (0-255): pixels mais escuros que
     *                isso viram MARCADA, mais claros viram VAZIO. Controla
     *                a densidade de preenchimento do resultado.
     */
    public static Tabuleiro.Estado[][] converter(BufferedImage imagem, int linhas, int colunas, int limiar){
        BufferedImage redimensionada = redimensionar(imagem, colunas, linhas);

        Tabuleiro.Estado[][] grade = new Tabuleiro.Estado[linhas][colunas];

        for(int l = 0; l < linhas; l++){
            for(int c = 0; c < colunas; c++){
                int rgb = redimensionada.getRGB(c, l);
                int alpha = (rgb >> 24) & 0xFF;

                //Pixel majoritariamente transparente conta como fundo
                if(alpha < 128){
                    grade[l][c] = Tabuleiro.Estado.VAZIO;
                    continue;
                }

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                double luminancia = 0.299 * r + 0.587 * g + 0.114 * b;

                grade[l][c] = luminancia < limiar
                        ? Tabuleiro.Estado.MARCADA
                        : Tabuleiro.Estado.VAZIO;
            }
        }
        return grade;
    }

    // Redimensiona com interpolação bilinear, essencial pra imagem grande
    // virar uma grade pequena sem virar ruído sem forma nenhuma.
    public static BufferedImage redimensionar(BufferedImage original, int largura, int altura){
        BufferedImage redimensionada = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = redimensionada.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, largura, altura, null);
        g2d.dispose();

        return redimensionada;
    }

    public static BufferedImage carregar(File arquivo) throws IOException{
        return ImageIO.read(arquivo);
    }

}
