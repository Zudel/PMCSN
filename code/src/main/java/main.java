import model.Ssq3;
import model.Ssq3Area;
import utils.*;
import java.util.List;
import static model.SimulatorParameters.*;

public class main {
    static List<fasciaOraria> listaFasciaOraria = utils.LeggiCSV("C:\\Users\\Roberto\\Documents\\GitHub\\PMCSN\\code\\src\\main\\resources\\distribuzioneOrdiniGiornalieri.csv");
    public static void main(String[] args) {

        int numberJobsServerOrder = 0;
        int numberJobsPickingCenter = 0;
        int numberJobsrPackingCenter = 0;
        int numberJobsPackingCenter = 0;
        int numberJobsQualityCenter = 0;
        int numberJobsShippingCenter = 0;
        Ssq3Area ServerOrderArea = new Ssq3Area(); // time integrated number in the node, queue and service of ServerOrder



        for (int i = 0; i < listaFasciaOraria.size(); i++) {
            System.out.println(listaFasciaOraria.get(i).getFasciaOraria() + " " + listaFasciaOraria.get(i).getFrequenza() + " " + listaFasciaOraria.get(i).getProporzione()+"%");
        }






    }
}
