package interfacesRMI;

import estruturasRMI.Carta;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PartidaRMI extends Remote {
    
    public void declararVitoria(int numeroJogador) throws RemoteException;
    public void declararSaiuUno(int numeroJogador) throws RemoteException;
    public void declararUno(int numeroJogador) throws RemoteException;
    public void pularJogada() throws RemoteException;
    public void jogarCarta(Carta c) throws RemoteException;
    public void comprarCarta() throws RemoteException;
}
