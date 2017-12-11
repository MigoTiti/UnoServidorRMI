package interfacesRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import estruturasRMI.Carta;

public interface ComunicadorJogadorRMI extends Remote {
 
    public void setTodosJogadoresConectados(boolean todosConectados) throws RemoteException;
    public void informarPartidaCheia() throws RemoteException;
    public void setMaoDoUsuario(List<Carta> maoDoUsuario) throws RemoteException;
    public void setNumeroJogador(int numeroJogador) throws RemoteException;
    public int getId() throws RemoteException;
    public void setPrimeiraCarta(Carta c) throws RemoteException;
    public void setProximoJogador(int jogador) throws RemoteException;
    public void setCartaNaMesa(Carta c) throws RemoteException;
    public void declararUno(int jogador) throws RemoteException;
    public void declararSaiuDeUno(int jogador) throws RemoteException;
    public void jogadorGanhou(int jogador) throws RemoteException;
    public void setContagemCorrente(boolean corrente, int contagem) throws RemoteException;
    public void retornarCompra(List<Carta> cartas) throws RemoteException;
}
