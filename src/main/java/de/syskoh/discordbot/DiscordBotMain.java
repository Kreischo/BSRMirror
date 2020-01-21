package de.syskoh.discordbot;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import de.syskoh.discordbot.listener.DiscordEventListener;
import de.syskoh.discordbot.listener.TwitchListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class DiscordBotMain {

    public static final Logger LOGGER = Logger.getLogger("MainLogger");

    private Config config;
    private JDA jda;
    private Twirk twirk;
    private static DiscordBotMain instance;
    private CheckIsLiveThread checkIsLiveThread;
    private TwitchListener tl;


    public static void main(String[] args) throws LoginException, InterruptedException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        new DiscordBotMain();
    }

    private DiscordBotMain() throws InterruptedException, LoginException {
        instance = this;
        try {
            loadConfig();
        } catch (FileNotFoundException ex){
            log("Could not find the file botconfig.yml. Does it exist?\n\n");
            ex.printStackTrace();
            System.exit(-1);
        } catch (YamlException e) {
            log("The config file is malformed. Please make sure there are no spaces at the beginning of each line.\n\n");
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            initTwitch();
            initJDA();
            checkIsLiveThread = new CheckIsLiveThread(config.isLiveCheckInterval, tl);
            checkIsLiveThread.start();
        } catch (IOException ex) {
            log("Error on initialisation. Is Discord or Twitch down?");
            ex.printStackTrace();
        }
    }

    private void loadConfig() throws FileNotFoundException, YamlException {
        config= new YamlReader(new FileReader("botconfig.yml")).read(Config.class);
        System.out.println(config);
    }

    private void initTwitch() throws IOException, InterruptedException {
        twirk = new TwirkBuilder("#" + config.channel, config.botChannel, config.botOAuthToken).build();
        tl =  new TwitchListener();
        twirk.addIrcListener(tl);
        twirk.connect();
    }

    private void initJDA() throws InterruptedException, LoginException {
        jda = new JDABuilder(config.discordToken).build();
        jda.addEventListener(new DiscordEventListener());
        jda.awaitReady();
    }

    public void shutdown() {
        jda.shutdownNow();
        twirk.disconnect();
    }


    public static void log(String l) {
        LOGGER.info(l);
    }

    public Twirk getTwitchClient() {
        return twirk;
    }

    public Config getConfig() {
        return config;
    }

    public TwitchListener getTwitchListener() {
        return tl;
    }

    public JDA getJda() {
        return jda;
    }

    public String getChannel() {
        return config.channel;
    }

    public static DiscordBotMain getInstance() {
        return instance;
    }
}
