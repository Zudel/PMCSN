package utils;

public class fasciaOraria {
private String fasciaOraria;
    private int frequenza;
    private double proporzione;

    public fasciaOraria(String fasciaOraria, int frequenza, double proporzione) {
        this.fasciaOraria = fasciaOraria;
        this.frequenza = frequenza;
        this.proporzione = proporzione * 100; // moltiplica per 100 per avere la percentuale
    }

    public String getFasciaOraria() {
        return fasciaOraria;
    }

    public void setFasciaOraria(String fasciaOraria) {
        this.fasciaOraria = fasciaOraria;
    }

    public int getFrequenza() {
        return frequenza;
    }

    public void setFrequenza(int frequenza) {
        this.frequenza = frequenza;
    }

    public double getProporzione() {
        return proporzione;
    }

    public void setProporzione(double proporzione) {
        this.proporzione = proporzione;
    }

}
