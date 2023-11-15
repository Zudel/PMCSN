package utils;

public class FasciaOraria {
    private String fasciaOraria;
    private double frequenza;
    private double proporzione;
    private int index;
    private double meanPoisson;
    private int estremoInferiore;
    private int estremoSuperiore;


    public FasciaOraria(String fasciaOraria, double frequenza, double proporzione, int index, int estremoInferiore, int estremoSuperiore) {
        this.fasciaOraria = fasciaOraria;
        this.frequenza = frequenza; // dividi per 3600 per avere la frequenza oraria in secondi
        this.proporzione = proporzione ; // moltiplica per 100 per avere la percentuale
        this.index = index;
        this.estremoInferiore = estremoInferiore;
        this.estremoSuperiore = estremoSuperiore;
        this.meanPoisson = 1/this.frequenza;

    }
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

    public double getFrequenza() {
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

    public double getLowerBound() {
        return estremoInferiore;
    }

    public double getUpperBound() {
        return estremoSuperiore;
    }
}
