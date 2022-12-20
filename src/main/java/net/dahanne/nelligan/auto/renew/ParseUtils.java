package net.dahanne.nelligan.auto.renew;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParseUtils {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd");

    static Function<Element, Item> htmlPageToItems = element -> {
        var title = element.getElementsByClass("patFuncTitleMain").first().text();
        var barcode = element.getElementsByClass("patFuncBarcode").first().text().trim();
        String trimmedPatFuncStatus = element.getElementsByClass("patFuncStatus").first().text().trim();
        String patFuncStatusDateOnly = trimmedPatFuncStatus.substring(4, 12);
        String extraInfo = trimmedPatFuncStatus.length() > 13 ? trimmedPatFuncStatus.substring(13) : "";
        var dueDate = LocalDate.parse(patFuncStatusDateOnly, DATE_TIME_FORMATTER);
        var callNumber = element.getElementsByClass("patFuncCallNo").first().text().trim();
        var rValue = element.getElementsByAttribute("value").first().attr("value");
        String hrefValue = element.getElementsByAttribute("href").first().attr("href");
        var record = hrefValue.substring(hrefValue.indexOf("=") + 1, hrefValue.indexOf("~"));
        var patFuncRenewCount = element.getElementsByClass("patFuncRenewCount").first();
        var renewed = patFuncRenewCount == null ? 0 : Integer.parseInt(patFuncRenewCount.text().trim().split(" ")[1]);
        String error = (extraInfo.contains("NOT") || extraInfo.contains("ON HOLD") || extraInfo.contains("TOO SOON")|| extraInfo.contains("TOO MANY")) ? extraInfo : "";
        dueDate = extraInfo.contains("Now due") ? LocalDate.parse(extraInfo.substring(extraInfo.indexOf("Now due") + 8, extraInfo.indexOf("Now due") + 16), DATE_TIME_FORMATTER) : dueDate;
        return new Item(title, barcode, dueDate, callNumber, rValue, record, renewed, error);
    };

    public static PatronInfo parsePatronInfoPage(String pageContent, String location) {
        Document parse = Jsoup.parse(pageContent);
        String name = parse.getElementsByClass("pat-titre").first().getElementsByTag("strong").first().text();

        List<Item> items = parse.getElementsByClass("patFuncEntry").stream().map(htmlPageToItems).collect(Collectors.toList());

        return new PatronInfo(name, location, items);
    }

    public static Item parseRenewedItem(String itemToRenewRValue, String pageContent) {
        Document parse = Jsoup.parse(pageContent);

        Optional<Item> renewedItem = parse.getElementsByClass("patFuncEntry")
                .stream()
                .map(htmlPageToItems)
                .filter(item -> item.rValue().equals(itemToRenewRValue))
                .findFirst();

        return renewedItem.orElseThrow();
    }
}
