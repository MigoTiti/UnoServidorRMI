package unoservidorrmi.rede;

import interfacesRMI.GerenciadorPartidasRMI;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import unoservidorrmi.estruturas.Partida;
import estruturasRMI.PartidaListagem;
import interfacesRMI.ComunicadorJogadorRMI;
import interfacesRMI.PartidaRMI;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import static unoservidorrmi.UnoServidorRMI.PORTA;
import unoservidorrmi.util.Utilitarios;

public class GerenciadorPartidas extends UnicastRemoteObject implements GerenciadorPartidasRMI, Serializable{

    private final Map<Integer, Partida> partidas;
    private final Registry registro;
    private static int ID_INCREMENTAL = 0;
    private static int ID_INCREMENTAL_JOGADORES = 0;

    private static GerenciadorPartidas instance = null;

    @Override
    public PartidaRMI entrarEmPartida(int idPartida, int idJogador) throws RemoteException {
        try {
            Partida p = partidas.get(idPartida);
            
            ComunicadorJogadorRMI jogador = (ComunicadorJogadorRMI) registro.lookup(Utilitarios.getNomeJogador(idJogador));
            
            if (p.getJogadoresConectados() == p.getnJogadores()) {
                jogador.informarPartidaCheia();
                return null;
            }
            
            p.adicionarJogador(jogador);
            
            if (p.getJogadoresConectados() == p.getnJogadores())
                p.todosJogadoresConectados();
            
            return (PartidaRMI) registro.lookup(Utilitarios.getNomePartida(idPartida));
        } catch (RemoteException | NotBoundException ex) {
            unoservidorrmi.UnoServidorRMI.exibirException(ex);
            return null;
        }
    }
    
    @Override
    public PartidaRMI criarPartida(String nome, int nJogadores, int idPrimeiroJogador) throws RemoteException {
        try {
            ComunicadorJogadorRMI comunicadorPrimeiroJogador = (ComunicadorJogadorRMI) registro.lookup(Utilitarios.getNomeJogador(idPrimeiroJogador));
            
            int id = ID_INCREMENTAL++;
            Partida p = new Partida(nJogadores, id, nome, comunicadorPrimeiroJogador);
            partidas.put(id, p);
            
            registro.bind(Utilitarios.getNomePartida(id), p);
            
            unoservidorrmi.UnoServidorRMI.appendMensagem("Partida '" + nome + "' criada, capacidade: " + p.getnJogadores() + " jogadores");
            
            return (PartidaRMI) registro.lookup(Utilitarios.getNomePartida(id));
        } catch (NotBoundException | AccessException | AlreadyBoundException ex) {
            unoservidorrmi.UnoServidorRMI.exibirException(ex);
            return null;
        }
    }

    @Override
    public int conectarJogador() throws RemoteException{
        return ID_INCREMENTAL_JOGADORES++;
    }
    
    @Override
    public List<PartidaListagem> listarPartidas() throws RemoteException {
        List<PartidaListagem> partidasLista = new ArrayList<>();

        partidas.entrySet().stream().map((entry) -> entry.getValue()).forEach((partida) -> {
            partidasLista.add(new PartidaListagem(partida.getnJogadores(), partida.getJogadoresConectados(), partida.getId(), partida.getNome()));
        });
        
        return partidasLista;
    }

    private GerenciadorPartidas() throws RemoteException{
        registro = LocateRegistry.getRegistry(PORTA);
        partidas = new ConcurrentHashMap<>();
    }

    public static GerenciadorPartidas getInstance() {
        if (instance == null) {
            try {
                instance = new GerenciadorPartidas();
            } catch (RemoteException ex) {
                unoservidorrmi.UnoServidorRMI.exibirException(ex);
            }
        }

        return instance;
    }
}
