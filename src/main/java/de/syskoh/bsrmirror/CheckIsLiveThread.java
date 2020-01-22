package de.syskoh.bsrmirror;

import de.syskoh.bsrmirror.listener.TwitchListener;
import de.syskoh.bsrmirror.requests.TwitchRequest;

public class CheckIsLiveThread extends Thread {

    boolean running;
    int interval;
    TwitchListener twitchListener;
    boolean lastState = false;

    public CheckIsLiveThread(int interval, TwitchListener twitchListener) {
        this.running = true;
        this.interval = interval;
        this.twitchListener = twitchListener;
    }

    public void stopThread() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                TwitchRequest twitchRequest = new TwitchRequest(BSRMirrorMain.getInstance().getChannel());
                twitchListener.setTwitchClientQuery(twitchRequest);
                //DiscordBotMain.log(DiscordBotMain.getInstance().getChannel() + " is " + (twitchRequest.isLive() ? "" : "not ") + "live!");
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}