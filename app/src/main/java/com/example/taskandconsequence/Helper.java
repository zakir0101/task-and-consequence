package com.example.taskandconsequence;

import java.util.Calendar;
import java.util.Date;

public class Helper {
    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    public static int getNumOfDays(String frequency) {
        int numOfDays;
        switch (frequency) {
            case "daily":
                numOfDays = 1;
                break;
            case "weekly":
                numOfDays = 7;
                break;
            case "monthly":
                numOfDays = 30;
                break;
            default:
                numOfDays = 1;
                break;
        }
        return numOfDays;
    }
}
