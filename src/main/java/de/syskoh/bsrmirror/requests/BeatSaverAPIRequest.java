package de.syskoh.bsrmirror.requests;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class BeatSaverAPIRequest {

    private JSONObject job;
    private String key;

    public BeatSaverAPIRequest(String key) {
        this.key = key;
        try {
            URL url = new URL("https://beatsaver.com/api/maps/detail/" + key);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", "FireFox");

            con.connect();

            InputStreamReader isr = new InputStreamReader(con.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            job = new JSONObject(reader.readLine());

            reader.close();
            isr.close();
            con.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBeatsaverTitle() {
        return job.getString("name");
    }

    public String getBeatsaverThumbnail() {
        return "http://beatsaver.com" + job.getString("coverURL");
    }

    public String getDirectDownloadLink() {
        return "http://beatsaver.com" + job.getString("directDownload");
    }

    //Ja, ich hab extra ne kleine Seite geschrieben, damit man das ganze mit einem Klick installieren kann.
    public String getOneClickInstall(){
        return "http://overlay.stefftek.de/installbeatsaver?" + key;
    }

    public String getUploader(){
        return job.getJSONObject("uploader").getString("username");
    }

    public String getPercentage(int decimals){
        return String.format("%." + decimals + "f", job.getJSONObject("stats").getDouble("rating")*100) + "%";
    }
}
