package interfacesRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import estruturasRMI.PartidaListagem;

public interface GerenciadorPartidasRMI extends Remote {
    
    public List<PartidaListagem> listarPartidas() throws RemoteException;
    public PartidaRMI criarPartida(String nome, int nJogadores, int idPrimeiroJogador) throws RemoteException;
    public int conectarJogador() throws RemoteException;
    public PartidaRMI entrarEmPartida(int id, int jogador) throws RemoteException;
}
