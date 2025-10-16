package net.deckserver.services;

import java.util.Locale;

public class CountryService {

    public static String[] getCountryCodes() {
        return Locale.getISOCountries();
    }

    public static String getCountry(String code) {
        return new Locale.Builder().setRegion(code).build().getDisplayName();
    }
}
