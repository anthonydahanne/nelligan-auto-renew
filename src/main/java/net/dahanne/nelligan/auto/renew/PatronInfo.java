package net.dahanne.nelligan.auto.renew;

import java.util.List;

public record PatronInfo(String name, String location, List<Item> items) {
}
