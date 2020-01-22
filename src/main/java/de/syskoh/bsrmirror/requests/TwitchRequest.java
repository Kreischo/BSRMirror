package de.syskoh.bsrmirror.requests;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class TwitchRequest {

    private URL url;
    private HttpsURLConnection con;
    private JSONObject job;
    private String channel;

    public TwitchRequest(String channel) {
        this.channel = channel;
        try {
            url = new URL("https://api.twitch.tv/helix/streams?user_login=" + channel);
            con = (HttpsURLConnection) url.openConnection();
            con.addRequestProperty("Client-ID", "jxc01q2hzy9a9al7nvcyhomrvqjno4");

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

    public boolean isLive(){
        return !job.getJSONArray("data").isEmpty() && job.getJSONArray("data").getJSONObject(0).getString("type").equals("live");
    }
}
