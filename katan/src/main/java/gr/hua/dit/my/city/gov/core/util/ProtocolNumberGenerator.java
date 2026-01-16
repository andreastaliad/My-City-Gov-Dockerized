package gr.hua.dit.my.city.gov.core.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

//Utility υπεύθυνο για την δημιουργία μοναδικού αριθμού πρωτοκόλλου για τα αιτήματα των πολιτών
//Είναι ορατό και στους πολίτες και στους υπαλλήλους
//Μορφής YYYYMMDD-XXXXXX π.χ. 20260115-90FQ3A
public class ProtocolNumberGenerator {
    private ProtocolNumberGenerator() {}

    //Η γεννήτρια του αριθμού, συνδυάζει την τωρινή ημερομηνία με μια τυχαία αλφαριθμητική ακολουθία για πραγματική μοναδικότητα
    public static String newProtocol() {
        //Τωρινή ημερομηνία σε μορφή YYYYMMDD
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        //Τυχαία αλφαριθμητική ακολουθία
        String rnd = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return date + "-" + rnd; // 20260111-9F3A1C
    }
}
