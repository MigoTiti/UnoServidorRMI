package estruturasRMI;

import java.io.Serializable;

public class PartidaListagem implements Serializable{

    private final int numeroJogadores;
    private final int jogadoresConectados;
    private final int id;
    private final String nome;

    public PartidaListagem(int nJogadores, int jogadoresConectados, int id, String nome) {
        this.numeroJogadores = nJogadores;
        this.jogadoresConectados = jogadoresConectados;
        this.id = id;
        this.nome = nome;
    }

    public int getNumeroJogadores() {
        return numeroJogadores;
    }

    public int getJogadoresConectados() {
        return jogadoresConectados;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
