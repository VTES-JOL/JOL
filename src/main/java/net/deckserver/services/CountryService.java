package net.deckserver.services;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.*;

public class CountryService {

    private final static BiMap<String, String> COUNTRY_MAP = HashBiMap.create();

    static {
        Arrays.stream(Locale.getISOCountries())
                .forEach(code -> {
                    String country = new Locale.Builder().setRegion(code).build().getDisplayName();
                    COUNTRY_MAP.put(code, country);
                });
    }

    public static List<String> getCountries() {
        return COUNTRY_MAP.values().stream().sorted().toList();
    }

    public static String getCountry(String code) {
        return COUNTRY_MAP.get(code);
    }

    public static String getCode(String country) {
        return COUNTRY_MAP.inverse().get(country);
    }
}
