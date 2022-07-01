package net.dahanne.nelligan.auto.renew;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import static net.dahanne.nelligan.auto.renew.ParseUtils.DATE_TIME_FORMATTER;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseUtilsTest {

    @Test
    void parsePatronInfo() {
        InputStream inputStream = ParseUtilsTest.class.getResourceAsStream("/patroninfo.html");

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            String text = scanner.useDelimiter("\\A").next();
            PatronInfo patronInfo = ParseUtils.parsePatronInfoPage(text, "location");
            assertEquals("location", patronInfo.location());
            assertEquals(25, patronInfo.items().size());

            assertEquals(
                    new Item(
                            "Happy jazz / une sélection de Misja Fitzgerald Michel ; illustrations, Ilya Green ; texte, Carl Norac.",
                            "32777077401607",
                            LocalDate.parse("22-06-30", DATE_TIME_FORMATTER),
                            "789.51 H P",
                            "i9583901",
                            "b2752656",
                            0,
                            "")
                    , patronInfo.items().stream().findFirst().get());


            Item renewedItem = new Item(
                    "L'amnésie des Dalton / dessins de Morris ; scénario de X. Fauche et J. Léturgie.",
                    "32777071055516",
                    LocalDate.parse("22-07-18", DATE_TIME_FORMATTER),
                    "LUCKY_LUK",
                    "i8750499",
                    "b1078634",
                    1,
                    "");
            assertEquals(renewedItem, patronInfo.items().get(23));
        }
    }


    @Test
    void parseRenewedPage() {
        InputStream inputStream = ParseUtilsTest.class.getResourceAsStream("/itemRenewed.html");

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            String text = scanner.useDelimiter("\\A").next();
            Item itemToRenew = new Item(
                    "L'amnésie des Dalton / dessins de Morris ; scénario de X. Fauche et J. Léturgie.",
                    "32777071055516",
                    LocalDate.parse("22-07-16", DATE_TIME_FORMATTER),
                    "LUCKY_LUK",
                    "i8750499",
                    "b1078634",
                    0,
                    "");

            Item renewedItem = ParseUtils.parseRenewedItem(itemToRenew.rValue(), text);
            Item itemToRenewUpdated = new Item(itemToRenew.title(), itemToRenew.barcode(), LocalDate.parse("22-07-18", DATE_TIME_FORMATTER), itemToRenew.callNumber(), itemToRenew.rValue(), itemToRenew.record(), 1, "");
            assertEquals(itemToRenewUpdated, renewedItem);
        }
    }

    @Test
    void parseRenewedFailed() {
        InputStream inputStream = ParseUtilsTest.class.getResourceAsStream("/renew-failed.html");

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            String text = scanner.useDelimiter("\\A").next();
            Item itemToRenew = new Item(
                    "À la poursuite de Maître Moustache / Marta Palazzesi ; traduit de l'italien par Emma Troude-Beheregaray.",
                    "32777083847827",
                    LocalDate.parse("22-05-24", DATE_TIME_FORMATTER),
                    "No Call Number",
                    "i10193789",
                    "b2834010",
                    0,
                    "");

            Item renewedItem = ParseUtils.parseRenewedItem(itemToRenew.rValue(), text);
            Item itemToRenewUpdated = new Item(itemToRenew.title(), itemToRenew.barcode(), LocalDate.parse("22-05-24", DATE_TIME_FORMATTER), itemToRenew.callNumber(), itemToRenew.rValue(), itemToRenew.record(), 0, "BILLED RENEW NOT ALLOWED");
            assertEquals(itemToRenewUpdated, renewedItem);
        }
    }
}
