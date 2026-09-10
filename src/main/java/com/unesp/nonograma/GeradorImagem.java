package com.unesp.nonograma;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Carregamento de imagens usadas para "revelar" o desenho por trás de um
 * puzzle já resolvido. As soluções em si não são mais derivadas de imagem
 * (são desenhadas à mão em BancoDePuzzles) — esta classe só cuida de ler
 * o arquivo de imagem correspondente pra exibir no final.
 */
public class GeradorImagem {

    public static BufferedImage carregar(File arquivo) throws IOException {
        return ImageIO.read(arquivo);
    }
}