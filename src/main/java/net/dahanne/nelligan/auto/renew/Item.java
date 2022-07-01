package net.dahanne.nelligan.auto.renew;

import java.time.LocalDate;

public record Item(String title, String barcode, LocalDate dueDate, String callNumber, String rValue, String record, Integer renewed, String error) {
}

