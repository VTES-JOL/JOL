package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.model.PlayerModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class StatusCreator implements ViewCreator {

    private long lastModified;
    private String outageString;

    @Override
    public String getFunction() {
        return "callbackStatus";
    }

    @Override
    public Object createData(AdminBean abean, PlayerModel model) {
        File outageFile = new File(System.getenv("JOL_DATA"), "outage.txt");
        // Read file if we haven't read it since it last updated
        long fileModified = outageFile.lastModified();
        if (fileModified != lastModified) {
            try {
                outageString = new BufferedReader(new FileReader(outageFile)).readLine();
                lastModified = fileModified;
            } catch (IOException e) {
                return "not yet";
            }
        }
        OffsetDateTime outageTime = OffsetDateTime.parse(outageString, DateTimeFormatter.ISO_DATE_TIME);
        Duration within = Duration.between(OffsetDateTime.now(), outageTime);
        if (within.toHours() > 0 && within.toHours() < 48) {
            return outageString;
        } else if (within.toHours() < 0) {
            return "outage past";
        }
        return "not yet";
    }
}
