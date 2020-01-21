package de.syskoh.discordbot;

import de.syskoh.discordbot.listener.TwitchListener;
import de.syskoh.discordbot.requests.TwitchRequest;

public class CheckIsLiveThread extends Thread {

    boolean running;
    int interval;
    TwitchListener twitchListener;

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
                TwitchRequest twitchRequest = new TwitchRequest(DiscordBotMain.getInstance().getChannel());
                twitchListener.setTwitchClientQuery(twitchRequest);
                DiscordBotMain.log(DiscordBotMain.getInstance().getChannel() + " is " + (twitchRequest.isLive() ? "" : "not ") + "live!");
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}