package net.dahanne.nelligan.auto.renew;

import com.microsoft.playwright.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

@ApplicationScoped
public class NelliganClient {

    private final String NELLIGAN_DECOUVERTE_BASE_URL = "https://nelligandecouverte.ville.montreal.qc.ca";
    // Not needed for now - but could be necessary in the future
    private static final String NELLIGAN_BASE_URL = "https://nelligan.ville.montreal.qc.ca";

    private String baseUrl = NELLIGAN_DECOUVERTE_BASE_URL;

    public NelliganClient() {
    }

    public PatronInfo authenticateAndRenew(String username, String password, int daysUntilRenewing) {
        try (Playwright playwright = Playwright.create();
             var chromiumBrowser = playwright.chromium().launch(new BrowserType.LaunchOptions().setSlowMo(500))) {
//                .setHeadless(false)
            Page page = chromiumBrowser.newPage();
            page.navigate(baseUrl + "/iii/encore/?lang=eng");
            // one more time, since the first time was 405, probably expecting a cookie
            page.navigate(baseUrl + "/iii/encore/?lang=eng");
            page.locator("#GenericLink_0").click();
            page.getByLabel("Library card number").fill(username);
            page.getByLabel("PIN").fill(password);
            page.locator("a:has-text('Submit')").click();
            page.navigate(baseUrl + "/iii/encore/myaccount?lang=eng&suite=cobalt");
            String name = page.locator(".accountSummary h2").textContent().trim();
            String emailRaw = page.locator(".accountSummaryColumn:has(h4:has-text('Email:'))").textContent();
            String email = emailRaw.replace("Email:", "").trim();
            String checkoutsText = page.locator(".currentAccountFunction").textContent().trim();
            int checkoutsCount = parseInt(checkoutsText.replaceAll(".*\\((\\d+)\\).*", "$1"));

            String holdsText = page.locator("#webpacFuncDirectLinkComponent").textContent().trim();
            int holdsCount = parseInt(holdsText.replaceAll(".*\\((\\d+)\\).*", "$1"));

            String finesText = page.locator("#webpacFuncDirectLinkComponent_0").textContent();
            String finesAmount = "";

            Matcher matcher = Pattern.compile("\\(([^)]+)\\)").matcher(finesText);
            if (matcher.find()) {
                finesAmount = matcher.group(1).trim(); // This gives you "0.00$"
            }
            if (checkoutsCount == 0) {
                return new PatronInfo(name, email, checkoutsCount, holdsCount, finesAmount, Collections.emptyList());
            } else {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd", Locale.ENGLISH);
                LocalDate now = LocalDate.now();

                Frame frame = page.frame("accountContentIframe");
                Locator rows = frame.locator("tr.patFuncEntry");
                int count = rows.count();

                var items = new ArrayList<Item>();
                for (int i = 0; i < count; i++) {
                    Locator row = rows.nth(i);

                    String title = row.locator("th.patFuncBibTitle > a > span.patFuncTitleMain").innerText().trim();
                    String barcode = row.locator("td.patFuncBarcode").innerText().trim();
                    String callNumber = row.locator("td.patFuncCallNo").innerText().trim();
                    var patFuncRenewCount = row.locator("span.patFuncRenewCount");
                    var renewed = patFuncRenewCount.count() == 0 ? 0 : Integer.parseInt(patFuncRenewCount.innerText().trim().split(" ")[1]);

                    var dueTextCell = row.locator("td.patFuncStatus");
                    String dueText = (String) dueTextCell.evaluate("node => node.childNodes[0].textContent.trim()");
                    String error = null;
                    if (!dueText.startsWith("DUE")) {
                        error = (dueText.contains("NOT") || dueText.contains("ON HOLD") || dueText.contains("TOO SOON") || dueText.contains("TOO MANY")) ? dueText : "";
                    }
                    String datePart = dueText.replace("DUE", "").trim();
                    LocalDate dueDate = LocalDate.parse(datePart, formatter);

                    if (now.isAfter(dueDate.minusDays(daysUntilRenewing))) {
                        // Click the renew checkbox for this row
                        row.locator("input[type=checkbox]").click();
                        frame.locator("span.buttonSpriteSpan2", new Frame.LocatorOptions().setHasText("Renew Marked"))
                                .first() // In case multiple exist
                                .locator("xpath=ancestor::a")
                                .click();
                        frame.locator("input[name='renewsome'][value='YES']").first().click();
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        Locator errorMessage = row.locator("td.patFuncStatus").locator("div[style='color:red']");
                        if (errorMessage.count() > 0) {
                            error = errorMessage.innerText().trim();
                        }
                    }
                    items.add(new Item(title, barcode, dueDate, callNumber, renewed, error));
                }
                return new PatronInfo(name, email, checkoutsCount, holdsCount, finesAmount, items);
            }
        }
    }

    void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
