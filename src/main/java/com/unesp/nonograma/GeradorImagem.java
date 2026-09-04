package com.unesp.nonograma;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Converte uma imagem (ícone/silhueta simples) em uma matriz de Estado
 * (MARCADA/VAZIO) do tamanho do tabuleiro, para ser usada como a
 * "solução secreta" de um puzzle.
 *
 * Recomendações para as imagens do banco:
 *  - Silhueta simples, sem gradientes nem cenário de fundo — o ideal é
 *    um desenho de 1-2 cores só (tipo um ícone/emoji), sem sombreado.
 *  - Sujeito ocupando a MAIOR parte possível do quadro (recorte antes de
 *    adicionar ao banco; espaço vazio ao redor "desperdiça" resolução,
 *    já que o tabuleiro é só 10x10).
 *  - Fundo transparente OU bem uniforme e claro, desenho escuro.
 *  - PNG é preferível a JPG por preservar transparência e bordas nítidas.
 *
 * Importante: reduzir uma imagem grande para 10x10 é uma redução muito
 * agressiva (proporção de ~100:1 numa imagem 1024x1024). Por isso este
 * conversor NÃO usa o redimensionamento padrão do Java2D (que, em
 * reduções tão grandes, não faz média de todos os pixels — só olha uma
 * vizinhança pequena e pode virar ruído). Em vez disso, cada célula do
 * tabuleiro é calculada como a MÉDIA de luminância de todos os pixels
 * da região correspondente na imagem original (filtro de área/box).
 */
public class GeradorImagem {

    /**
     * @param imagem  imagem de origem
     * @param linhas  altura do tabuleiro
     * @param colunas largura do tabuleiro
     * @param limiar  limiar de luminância (0-255): células com média mais
     *                escura que isso viram MARCADA, mais claras viram VAZIO.
     */
    public static Tabuleiro.Estado[][] converter(BufferedImage imagem, int linhas, int colunas, int limiar) {

        double[][] mediaLuminancia = new double[linhas][colunas];
        double[][] mediaAlpha = new double[linhas][colunas];

        calcularMedias(imagem, linhas, colunas, mediaLuminancia, mediaAlpha);

        Tabuleiro.Estado[][] grade = new Tabuleiro.Estado[linhas][colunas];

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {

                // Célula majoritariamente transparente conta como fundo
                if (mediaAlpha[l][c] < 128) {
                    grade[l][c] = Tabuleiro.Estado.VAZIO;
                    continue;
                }

                grade[l][c] = mediaLuminancia[l][c] < limiar
                        ? Tabuleiro.Estado.MARCADA
                        : Tabuleiro.Estado.VAZIO;
            }
        }

        return grade;
    }

    /**
     * Calcula, para cada célula do tabuleiro, a média de luminância e de
     * alpha de todos os pixels da imagem original que caem naquela região
     * (mapeamento proporcional simples — filtro de área/box, uma
     * passagem só pela imagem).
     */
    private static void calcularMedias(
            BufferedImage imagem, int linhas, int colunas,
            double[][] saidaMediaLuminancia, double[][] saidaMediaAlpha) {

        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        double[][] somaLuminancia = new double[linhas][colunas];
        double[][] somaAlpha = new double[linhas][colunas];
        int[][] contagem = new int[linhas][colunas];

        for (int y = 0; y < altura; y++) {

            int l = Math.min(linhas - 1, y * linhas / altura);

            for (int x = 0; x < largura; x++) {

                int c = Math.min(colunas - 1, x * colunas / largura);

                int rgb = imagem.getRGB(x, y);

                int alpha = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                double luminancia = 0.299 * r + 0.587 * g + 0.114 * b;

                somaLuminancia[l][c] += luminancia;
                somaAlpha[l][c] += alpha;
                contagem[l][c]++;
            }
        }

        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {

                int n = Math.max(1, contagem[l][c]);

                saidaMediaLuminancia[l][c] = somaLuminancia[l][c] / n;
                saidaMediaAlpha[l][c] = somaAlpha[l][c] / n;
            }
        }
    }

    /**
     * Calcula automaticamente um bom limiar de binarização usando o
     * método de Otsu: acha o ponto de corte no histograma de luminância
     * que melhor separa a imagem em dois grupos (claro/escuro), maximizando
     * a variância entre eles. Pixels transparentes (fundo) são ignorados
     * no cálculo, pra não distorcer o histograma.
     */
    public static int calcularLimiarOtsu(BufferedImage imagem) {

        int[] histograma = new int[256];
        int total = 0;

        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {

                int rgb = imagem.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                if (alpha < 128) continue; // ignora fundo transparente

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int luminancia = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
                luminancia = Math.max(0, Math.min(255, luminancia));

                histograma[luminancia]++;
                total++;
            }
        }

        if (total == 0) return 128;

        double somaTotal = 0;
        for (int i = 0; i < 256; i++) somaTotal += (double) i * histograma[i];

        double somaFundo = 0;
        int pesoFundo = 0;

        double melhorVariancia = -1;
        int melhorLimiar = 128;

        for (int t = 0; t < 256; t++) {

            pesoFundo += histograma[t];
            if (pesoFundo == 0) continue;

            int pesoPrimeiroPlano = total - pesoFundo;
            if (pesoPrimeiroPlano == 0) break;

            somaFundo += (double) t * histograma[t];

            double mediaFundo = somaFundo / pesoFundo;
            double mediaPrimeiroPlano = (somaTotal - somaFundo) / pesoPrimeiroPlano;

            double variancaEntreClasses = (double) pesoFundo * pesoPrimeiroPlano
                    * (mediaFundo - mediaPrimeiroPlano) * (mediaFundo - mediaPrimeiroPlano);

            if (variancaEntreClasses > melhorVariancia) {
                melhorVariancia = variancaEntreClasses;
                melhorLimiar = t;
            }
        }

        return melhorLimiar;
    }

    public static BufferedImage carregar(File arquivo) throws IOException {
        return ImageIO.read(arquivo);
    }
}