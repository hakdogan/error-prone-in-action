package org.jugistanbul;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Names daily report files. {@code YYYY} is the week-based year, not the calendar year, so in the
 * last days of December the name jumps a year ahead: 2025-12-29 becomes {@code
 * report-2026-12-29.csv}. Error Prone's MisusedWeekYear check fails the build; {@code yyyy} fixes it.
 *
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 7.10.2026
 ***/
public class ReportDate {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd");

    String reportName(LocalDate date) {
        return "report-" + FORMATTER.format(date) + ".csv";
    }
}
