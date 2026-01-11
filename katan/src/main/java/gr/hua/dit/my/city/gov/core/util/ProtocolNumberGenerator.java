package gr.hua.dit.my.city.gov.core.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ProtocolNumberGenerator {
    private ProtocolNumberGenerator() {}

    public static String newProtocol() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // 20260111
        String rnd = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return date + "-" + rnd; // 20260111-9F3A1C
    }
}
