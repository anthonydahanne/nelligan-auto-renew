package net.dahanne.nelligan.auto.renew;

import java.util.List;

public record PatronInfo(String name, String emailOnfile, int numberOfCheckouts, int numberOfHOlds, String finesAmount, List<Item> items) {
}
