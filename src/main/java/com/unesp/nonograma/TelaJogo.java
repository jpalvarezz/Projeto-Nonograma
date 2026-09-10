package com.unesp.nonograma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TelaJogo extends JFrame {

    private final Tabuleiro tb;
    private PainelTabuleiro painelTabuleiro;

    private JLabel labelErros;
    private JLabel labelStatus;
    private JLabel labelSolucoes;

    // Banco de puzzles e nível usados para sortear um novo puzzle real
    // (a partir de imagem) quando o jogador clica em "NOVO JOGO".
    private final BancoDePuzzles banco;
    private final CalculadoraDificuldade.Nivel nivel;

    // Puzzle específico usado nesta partida (null no modo aleatório) —
    // guardamos pra poder mostrar a imagem original na tela de vitória.
    private final BancoDePuzzles.PuzzleGerado puzzleAtual;

    // Estado da jogada em andamento durante um arraste do mouse (estilo Picross):
    // o "alvo" é o estado que está sendo pintado sobre as células por onde o
    // mouse passa, decidido a partir da célula onde o botão foi pressionado.
    private Tabuleiro.Estado alvoArraste = null;
    private boolean erroDuranteArraste = false;
    private boolean telaFimJaMostrada = false;

    public TelaJogo(Tabuleiro tb, BancoDePuzzles banco, CalculadoraDificuldade.Nivel nivel, BancoDePuzzles.PuzzleGerado puzzleAtual) {
        this.tb = tb;
        this.banco = banco;
        this.nivel = nivel;
        this.puzzleAtual = puzzleAtual;
        configurarJanela();
        criarInterface();
        atualizarInterface();
    }

    private void configurarJanela() {
        setTitle("Nonograma - " + tb.qualNome());
        setSize(950, 720);
        setMinimumSize(new Dimension(560, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(TemaVisual.FUNDO_JANELA);
    }

    private void criarInterface() {
        JPanel principal = new JPanel(new BorderLayout(0, 0));
        principal.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        principal.setBackground(TemaVisual.FUNDO_JANELA);

        principal.add(criarCabecalho(), BorderLayout.NORTH);
        principal.add(criarAreaJogo(), BorderLayout.CENTER);
        principal.add(criarMenu(), BorderLayout.EAST);

        add(principal);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel titulo = new JLabel("NONOGRAMA", SwingConstants.LEFT);
        titulo.setFont(TemaVisual.fonteTitulo(26));
        titulo.setForeground(TemaVisual.ACCENT);
        cabecalho.add(titulo, BorderLayout.WEST);

        JLabel subtitulo = new JLabel(tb.qualNome() + " • " + nomeNivel(nivel), SwingConstants.RIGHT);
        subtitulo.setFont(TemaVisual.fonteTexto(14));
        subtitulo.setForeground(TemaVisual.TEXTO_SUAVE);
        cabecalho.add(subtitulo, BorderLayout.EAST);

        return cabecalho;
    }

    private String nomeNivel(CalculadoraDificuldade.Nivel nivel) {
        return switch (nivel) {
            case FACIL -> "Fácil";
            case MEDIO -> "Médio";
            case DIFICIL -> "Difícil";
        };
    }

    private JPanel criarAreaJogo() {
        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        painelTabuleiro = new PainelTabuleiro(tb, new PainelTabuleiro.OuvinteTabuleiro() {
            @Override
            public void aoPressionar(int linha, int coluna, int botao) {
                iniciarArraste(linha, coluna, botao);
            }

            @Override
            public void aoArrastarPara(int linha, int coluna) {
                continuarArraste(linha, coluna);
            }

            @Override
            public void aoSoltar() {
                finalizarArraste();
            }
        });

        area.add(painelTabuleiro, BorderLayout.CENTER);
        return area;
    }

    // ---------- Lógica de marcação / arrastar (estilo Picross) ----------

    private void iniciarArraste(int linha, int coluna, int botao) {

        if (tb.isGameOver() || tb.isVitoria()) return;

        Tabuleiro.Estado atual = tb.getEstadoCelula(linha, coluna);

        if (botao == MouseEvent.BUTTON1) {
            // Clique esquerdo: alterna MARCADA <-> INTOCADA. Se a célula já
            // está marcada, o alvo do arraste vira "apagar"; senão, "marcar".
            alvoArraste = (atual == Tabuleiro.Estado.MARCADA) ? Tabuleiro.Estado.INTOCADA : Tabuleiro.Estado.MARCADA;
        } else if (botao == MouseEvent.BUTTON3) {
            // Clique direito: alterna VAZIO <-> INTOCADA.
            alvoArraste = (atual == Tabuleiro.Estado.VAZIO) ? Tabuleiro.Estado.INTOCADA : Tabuleiro.Estado.VAZIO;
        } else {
            return;
        }

        erroDuranteArraste = false;
        aplicarEstadoNaCelula(linha, coluna);
    }

    private void continuarArraste(int linha, int coluna) {
        if (alvoArraste == null) return;
        if (tb.isGameOver() || tb.isVitoria()) return;
        aplicarEstadoNaCelula(linha, coluna);
    }

    private void finalizarArraste() {
        if (alvoArraste == null) return;
        alvoArraste = null;
        concluirJogada();
    }

    private void aplicarEstadoNaCelula(int linha, int coluna) {

        Tabuleiro.Estado atual = tb.getEstadoCelula(linha, coluna);

        // Célula já está no estado que estamos "pintando" durante o arraste — nada a fazer.
        if (atual == alvoArraste) return;

        // O mouse pode disparar vários eventos de "arrastar" em cima da
        // MESMA célula (mesmo sem ela mudar), por isso, se essa célula já
        // está marcada com erro pra essa mesma tentativa, não reprocessa —
        // senão a mesma célula errada contaria vários erros de uma vez só.
        // Células diferentes continuam contando um erro cada, normalmente.
        if (tb.isEmErro(linha, coluna) && tb.getEstadoTentadoErro(linha, coluna) == alvoArraste) {
            return;
        }

        if (alvoArraste == Tabuleiro.Estado.INTOCADA) {
            tb.desmarcar(linha, coluna);
        } else {
            if (alvoArraste == Tabuleiro.Estado.MARCADA) {
                tb.marcar(linha, coluna);
            } else {
                tb.vazio(linha, coluna);
            }

            boolean correta = tb.verificarCelula(linha, coluna);
            if (!correta) erroDuranteArraste = true;
        }

        atualizarInterface();
    }

    private void concluirJogada() {

        if (erroDuranteArraste) {
            labelStatus.setForeground(TemaVisual.ERRO);
            labelStatus.setText("Jogada inválida!");
        }

        if (tb.isGameOver()) {
            labelStatus.setForeground(TemaVisual.ERRO);
            labelStatus.setText("VOCÊ PERDEU!");
            painelTabuleiro.setRevelarCores(true);
            mostrarTelaFim(false, "Você atingiu o limite de " + tb.getLimiteErros() + " erro(s).\nQue tal tentar de novo?");
            return;
        }

        if (tb.isVitoria()) {
            labelStatus.setForeground(TemaVisual.SUCESSO);
            labelStatus.setText("VOCÊ VENCEU!");
            painelTabuleiro.setRevelarCores(true);
            mostrarTelaFim(true, "Parabéns! Você completou o nonograma\ncom " + tb.qtosErros() + " erro(s).");
            return;
        }

        if (!erroDuranteArraste) {
            labelStatus.setForeground(TemaVisual.TEXTO_CLARO);
            labelStatus.setText("Continue jogando!");
        }
    }

    private void mostrarTelaFim(boolean vitoria, String mensagem) {

        // Evita abrir a tela de fim de jogo mais de uma vez (ex: se o
        // usuário ainda estiver arrastando o mouse sobre células extras).
        if (telaFimJaMostrada) return;
        telaFimJaMostrada = true;

        TelaFimDeJogo tela = new TelaFimDeJogo(this, vitoria, mensagem, new TelaFimDeJogo.AcoesFimDeJogo() {
            @Override
            public void aoClicarNovoJogo() {
                novoJogo();
            }

            @Override
            public void aoClicarFechar() {
                // Só fecha a janelinha de fim de jogo; o tabuleiro final continua visível.
            }
        });

        tela.setVisible(true);
    }

    // ---------- Menu lateral ----------

    private JPanel criarMenu() {

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(210, 0));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));
        menu.setBackground(TemaVisual.FUNDO_MENU);

        JLabel tituloMenu = new JLabel("PARTIDA");
        tituloMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        tituloMenu.setForeground(TemaVisual.ACCENT);
        tituloMenu.setFont(TemaVisual.fonteTextoNegrito(14));
        menu.add(tituloMenu);
        menu.add(Box.createVerticalStrut(18));

        labelErros = criarLabelInfo();
        menu.add(labelErros);
        menu.add(Box.createVerticalStrut(10));

        labelSolucoes = criarLabelInfo();
        menu.add(labelSolucoes);
        menu.add(Box.createVerticalStrut(20));

        JSeparator separador = new JSeparator();
        separador.setForeground(new Color(60, 64, 74));
        separador.setMaximumSize(new Dimension(170, 1));
        menu.add(separador);
        menu.add(Box.createVerticalStrut(20));

        labelStatus = new JLabel("Jogue!", SwingConstants.CENTER);
        labelStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelStatus.setForeground(TemaVisual.TEXTO_CLARO);
        labelStatus.setFont(TemaVisual.fonteTextoNegrito(16));
        menu.add(labelStatus);

        menu.add(Box.createVerticalGlue());

        JLabel dica = new JLabel("<html><div style='text-align:center;'>Clique e arraste para<br>marcar várias células.<br><br>Botão direito marca<br>como vazio (X).</div></html>");
        dica.setAlignmentX(Component.CENTER_ALIGNMENT);
        dica.setForeground(TemaVisual.TEXTO_SUAVE);
        dica.setFont(TemaVisual.fonteTexto(12));
        dica.setHorizontalAlignment(SwingConstants.CENTER);
        menu.add(dica);
        menu.add(Box.createVerticalStrut(20));

        JButtonEstilizado novoJogo = TemaVisual.criarBotao("NOVO JOGO", TemaVisual.ACCENT_ESCURO);
        novoJogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        novoJogo.addActionListener(e -> novoJogo());
        menu.add(novoJogo);

        return menu;
    }

    private JLabel criarLabelInfo() {
        JLabel label = new JLabel();
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(TemaVisual.TEXTO_CLARO);
        label.setFont(TemaVisual.fonteTexto(14));
        return label;
    }

    // Cria novo jogo voltando para a tela de seleção
    private void novoJogo() {
        this.dispose();
        TelaSelecao tela = new TelaSelecao(banco);
        tela.setVisible(true);
    }

    private void atualizarInterface() {
        painelTabuleiro.repaint();

        labelErros.setText("Erros: " + tb.qtosErros() + " / " + tb.getLimiteErros());
        labelSolucoes.setText("Soluções possíveis: " + tb.getQuantidadeGabaritos());
    }
}