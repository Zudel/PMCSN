package utils;

public class fasciaOraria {
private String fasciaOraria;
    private int frequenza;
    private double proporzione;
    private int index;
    private double meanPoisson;
    private int estremoInferiore;
    private int estremoSuperiore;


    public fasciaOraria(String fasciaOraria, int frequenza, double proporzione, int index, double meanPoisson, int estremoInferiore, int estremoSuperiore) {
        this.fasciaOraria = fasciaOraria;
        this.frequenza = frequenza;
        this.proporzione = proporzione * 100; // moltiplica per 100 per avere la percentuale
        this.index = index;
        this.estremoInferiore = estremoInferiore;
        this.estremoSuperiore = estremoSuperiore;
        this.meanPoisson = meanPoisson;

    }

    public
    public double getMeanPoisson() {
        return meanPoisson;
    }
    public void setMeanPoisson(double meanPoisson) {
        this.meanPoisson = meanPoisson;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
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
