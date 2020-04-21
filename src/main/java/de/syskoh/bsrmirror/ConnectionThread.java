package de.syskoh.bsrmirror;

import com.gikk.twirk.Twirk;
import net.dv8tion.jda.api.JDA;

import java.io.IOException;

public class ConnectionThread extends Thread {

    JDA jda;
    Twirk twirk;

    public ConnectionThread(JDA jda, Twirk twirk) {
        this.jda = jda;
        this.twirk = twirk;
    }

    @Override
    public void run() {
        try {
            if(!twirk.isConnected()){
                BSRMirrorMain.log("Twirk was disconnected, Reconnecting...");
                twirk.connect();
            }

            Thread.sleep(30000);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
