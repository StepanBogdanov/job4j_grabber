package ru.job4j.grabber.utils;

import java.time.LocalDateTime;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        String dateTime = parse.split("\\+")[0];
        return LocalDateTime.parse(dateTime);
    }

}