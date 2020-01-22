package de.syskoh.bsrmirror.listener;

import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import de.syskoh.bsrmirror.BSRMirrorMain;
import de.syskoh.bsrmirror.requests.TwitchRequest;
import de.syskoh.bsrmirror.requests.BeatSaverAPIRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.LinkedList;
import java.util.Objects;

public class TwitchListener implements TwirkListener {

    private TwitchRequest twitchRequest;
    private String channel = BSRMirrorMain.getInstance().getConfig().discordChannelID;
    private LinkedList<String> requestedKeys = new LinkedList<>();

    @Override
    public void onPrivMsg(TwitchUser sender, TwitchMessage message) {
        BSRMirrorMain.log(sender.getDisplayName()+ ": " + message.getContent());

        if(twitchRequest == null || !twitchRequest.isLive())
            return;

        if(!message.getContent().startsWith("!bsr"))
          return;

        String key = message.getContent().substring(5);
        if(key.length() > 5)
            return;

        if(requestedKeys.contains(key))
            return;

        requestedKeys.add(key);

        BeatSaverAPIRequest bsar = new BeatSaverAPIRequest(key);

        String thumbnail = bsar.getBeatsaverThumbnail();
        EmbedBuilder eb = new EmbedBuilder();


        eb.setTitle("Beat Saber Request");
        eb.addField("Titel ",  bsar.getBeatsaverTitle(), true);
        eb.addField("Bewertung", bsar.getPercentage(2), true);
        eb.addField("Request von", sender.getDisplayName(), true);
        eb.addField("Download",
                "[BeatSaver](https://beatsaver.com/beatmap/" + key + ") - " +
                        "[BeastSaber](https://bsaber.com/songs/" + key + "/) - " +
                        "[Direkter Download](" + bsar.getDirectDownloadLink() + ") - " +
                        "[One Click Installer](" + bsar.getOneClickInstall() + ")" , false);
        eb.setColor(Color.cyan);
        eb.setThumbnail(thumbnail);
        discordMessage(eb.build());
    }

    @Override
    public void onConnect() {
        BSRMirrorMain.log("Twitch is connected");
    }

    private void discordMessage(MessageEmbed msg){
        Objects.requireNonNull(BSRMirrorMain.getInstance().getJda().getTextChannelById(channel)).sendMessage(msg).queue();
    }

    private void discordMessage(String msg){
        Objects.requireNonNull(BSRMirrorMain.getInstance().getJda().getTextChannelById(channel)).sendMessage(msg).queue();
    }

    public void setTwitchClientQuery(TwitchRequest tcq){
        if(this.twitchRequest != null && this.twitchRequest.isLive() && !tcq.isLive()){
            BSRMirrorMain.log(BSRMirrorMain.getInstance().getConfig().channel + " went offline! Deleted request keys.");
            clearKeys();
        }
            this.twitchRequest = tcq;
    }

    public void clearKeys(){
        this.requestedKeys.clear();
    }

}
