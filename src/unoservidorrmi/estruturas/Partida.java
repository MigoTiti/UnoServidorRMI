package unoservidorrmi.estruturas;

import estruturasRMI.Carta;
import interfacesRMI.ComunicadorJogadorRMI;
import interfacesRMI.PartidaRMI;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Partida extends UnicastRemoteObject implements PartidaRMI, Serializable {

    private Baralho baralho;
    private final Map<Integer, ComunicadorJogadorRMI> jogadores;

    private final int nJogadores;
    private int jogadoresConectados;
    private final int id;
    private final String nome;

    private int jogadorDaVez = 1;
    private boolean sentidoHorario = true;
    private boolean correnteCompra = false;
    private int contagemCorrente = 0;

    public Partida(int nJogadores, int id, String nome, ComunicadorJogadorRMI primeiroJogador) throws RemoteException {
        this.nJogadores = nJogadores;
        this.id = id;
        this.nome = nome;
        this.jogadores = new HashMap<>();
        this.jogadoresConectados = 0;
        this.baralho = null;
        adicionarJogador(primeiroJogador);
    }

    @Override
    public void pularJogada() throws RemoteException {
        incrementarVezDoJogador(1, sentidoHorario);
        
        reportarJogada(false, true);
    }

    @Override
    public void comprarCarta() throws RemoteException {
        List<Carta> cartasCompradas = new ArrayList<>();

        if (correnteCompra) {
            for (int i = 0; i < contagemCorrente; i++) {
                cartasCompradas.add(this.baralho.getCartaNoTopo());
            }

            correnteCompra = false;
            contagemCorrente = 0;
        } else {
            cartasCompradas.add(this.baralho.getCartaNoTopo());
        }
        
        jogadores.get(jogadorDaVez).retornarCompra(cartasCompradas);
        reportarJogada(false, false);
    }

    @Override
    public void jogarCarta(Carta cartaJogada) throws RemoteException {
        this.baralho.jogarCarta(cartaJogada);

        int numero = cartaJogada.getNumero();

        switch (numero) {
            case Carta.MAIS_DOIS:
                contagemCorrente += 2;
                correnteCompra = true;
                incrementarVezDoJogador(1, sentidoHorario);
                break;
            case Carta.MAIS_QUATRO:
                contagemCorrente += 4;
                correnteCompra = true;
                incrementarVezDoJogador(1, sentidoHorario);
                break;
            case Carta.BLOQUEAR:
                incrementarVezDoJogador(2, sentidoHorario);
                break;
            case Carta.REVERTER:
                sentidoHorario = !sentidoHorario;
                incrementarVezDoJogador(1, sentidoHorario);
                break;
            default:
                incrementarVezDoJogador(1, sentidoHorario);
                break;
        }

        reportarJogada(true, true);
    }

    private void reportarJogada(boolean cartaNaMesaMudou, boolean proximoJogadorMudou) {
        jogadores.entrySet().stream().map((entry) -> entry.getValue()).forEach((jogador) -> {
            try {
                if (cartaNaMesaMudou) {
                    jogador.setCartaNaMesa(this.baralho.getCartaNaMesa());
                }

                if (proximoJogadorMudou) {
                    jogador.setProximoJogador(this.jogadorDaVez);
                }

                jogador.setContagemCorrente(this.correnteCompra, this.contagemCorrente);
            } catch (RemoteException ex) {
                unoservidorrmi.UnoServidorRMI.exibirException(ex);
            }
        });
    }

    public void iniciarPartida() {
        distribuirCartas();
        distribuirNumerosDeJogadores();
        setarPrimeiraCartaNaMesa();
    }

    @Override
    public void declararVitoria(int numeroJogador) throws RemoteException {
        jogadores.entrySet().stream().map((entry) -> entry.getValue()).forEach((jogador) -> {
            try {
                jogador.jogadorGanhou(numeroJogador);
            } catch (RemoteException ex) {
                unoservidorrmi.UnoServidorRMI.exibirException(ex);
            }
        });
    }

    @Override
    public void declararSaiuUno(int numeroJogador) throws RemoteException {
        jogadores.entrySet().stream().map((entry) -> entry.getValue()).forEach((jogador) -> {
            try {
                jogador.declararSaiuDeUno(numeroJogador);
            } catch (RemoteException ex) {
                unoservidorrmi.UnoServidorRMI.exibirException(ex);
            }
        });
    }

    @Override
    public void declararUno(int numeroJogador) throws RemoteException {
        jogadores.entrySet().stream().map((entry) -> entry.getValue()).forEach((jogador) -> {
            try {
                jogador.declararUno(numeroJogador);
            } catch (RemoteException ex) {
                unoservidorrmi.UnoServidorRMI.exibirException(ex);
            }
        });
    }

    private void setarPrimeiraCartaNaMesa() {
        baralho.jogarCarta(baralho.getCartaNoTopo());

        Carta primeiraCarta = baralho.getCartaNaMesa();

        jogadores.entrySet().stream().map((entry) -> entry.getValue()).forEach((jogador) -> {
            try {
                jogador.setPrimeiraCarta(primeiraCarta);
            } catch (RemoteException ex) {
                unoservidorrmi.UnoServidorRMI.exibirException(ex);
            }
        });
    }

    private void distribuirNumerosDeJogadores() {
        jogadores.entrySet().stream().forEach((jogador) -> {
            try {
                Integer numeroJogador = jogador.getKey();
                ComunicadorJogadorRMI comunicador = jogador.getValue();
                comunicador.setNumeroJogador(numeroJogador);
            } catch (RemoteException ex) {
                Logger.getLogger(Partida.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void distribuirCartas() {
        this.baralho = new Baralho();

        jogadores.entrySet().stream().map((entry) -> entry.getValue()).forEach((jogador) -> {
            try {
                List<Carta> mao = new ArrayList<>();

                for (int j = 0; j < 7; j++) {
                    Carta c = baralho.getCartaNoTopo();
                    mao.add(c);
                }

                jogador.setMaoDoUsuario(mao);
            } catch (RemoteException ex) {
                unoservidorrmi.UnoServidorRMI.exibirException(ex);
            }
        });
    }

    private void incrementarVezDoJogador(int valor, boolean sentidoHorario) {
        if (sentidoHorario) {
            for (int i = 1; i <= valor; i++) {
                if (jogadorDaVez == nJogadores) {
                    jogadorDaVez = 1;
                } else {
                    jogadorDaVez++;
                }
            }
        } else {
            for (int i = 1; i <= valor; i++) {
                if (jogadorDaVez == 1) {
                    jogadorDaVez = nJogadores;
                } else {
                    jogadorDaVez--;
                }
            }
        }
    }

    public void todosJogadoresConectados() {
        jogadores.entrySet().stream().map((entry) -> entry.getValue()).forEach((jogador) -> {
            try {
                jogador.setTodosJogadoresConectados(true);
            } catch (RemoteException ex) {
                unoservidorrmi.UnoServidorRMI.exibirException(ex);
            }
        });

        iniciarPartida();
    }

    public final void adicionarJogador(ComunicadorJogadorRMI jogador) {
        this.jogadores.put(++jogadoresConectados, jogador);
    }

    public int getId() {
        return id;
    }

    public int getnJogadores() {
        return nJogadores;
    }

    public int getJogadoresConectados() {
        return jogadoresConectados;
    }

    public String getNome() {
        return nome;
    }
}
