package de.syskoh.bsrmirror;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import de.syskoh.bsrmirror.listener.DiscordEventListener;
import de.syskoh.bsrmirror.listener.TwitchListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class BSRMirrorMain {

    public static final Logger LOGGER = Logger.getLogger("MainLogger");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static File logFile = new File("logs" + System.getProperty("file.separator") + sdf.format(System.currentTimeMillis()) + ".log");
    private static PrintWriter logWriter;
    public final String configFileName = "botconfig.yml";
    public final File configFile = new File(configFileName);
    private Config config;
    private JDA jda;
    private Twirk twirk;
    private static BSRMirrorMain instance;
    private CheckIsLiveThread checkIsLiveThread;
    private TwitchListener tl;


    public static void main(String[] args) throws LoginException, InterruptedException, IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        new BSRMirrorMain();
    }

    private BSRMirrorMain() throws InterruptedException, LoginException, IOException {
        instance = this;


        if (!logFile.exists()){
            if(!logFile.getParentFile().exists()){
                logFile.getParentFile().mkdirs();
            }
            logFile.createNewFile();
        }

        logWriter = new PrintWriter(logFile);

        try {
            generateBotConfig();
            loadConfig();
        } catch (FileNotFoundException ex) {
            log("Could not find the file " + configFileName + ". Does it exist?\n\n");
            ex.printStackTrace();
            System.exit(-1);
        } catch (YamlException e) {
            log("The config file is malformed. Please make sure there are no spaces at the beginning of each line.\n\n");
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            log("Couldn't generate default config. Do you have write permissions?\n\n");
            e.printStackTrace();
        }

        //TODO: Check if config fields are not null

        try {
            initTwitch();
            initJDA();
            checkIsLiveThread = new CheckIsLiveThread(config.isLiveCheckInterval, tl);
            checkIsLiveThread.start();
            new ConnectionThread(jda,twirk);
        } catch (IOException ex) {
            log("Error on initialisation. Is Discord or Twitch down?");
            ex.printStackTrace();
        }

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        } catch (Exception ex) {
            log("Couldn't add a shutdown-hook!\n\n");
            ex.printStackTrace();
        }
    }

    private void generateBotConfig() throws IOException {
        if (configFile.exists())
            return;

        log("Generating default config");

        PrintWriter printWriter = new PrintWriter(configFile);
        InputStreamReader isr = new InputStreamReader(this.getClass().getResourceAsStream("/defaultConfig.yml"));

        int content;

        while ((content = isr.read()) != -1) {
            printWriter.print((char) content);
        }
        isr.close();
        printWriter.close();

        log("Done generating. Exiting, please setup the config!");
        System.exit(0);
    }

    private void loadConfig() throws FileNotFoundException, YamlException {
        config = new YamlReader(new FileReader(configFileName)).read(Config.class);
    }

    private void initTwitch() throws IOException, InterruptedException {
        twirk = new TwirkBuilder("#" + config.channel, config.botChannel, config.botOAuthToken).build();
        tl = new TwitchListener();
        twirk.addIrcListener(tl);
        twirk.connect();
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(3000);
                if(!twirk.isConnected()){
                    log("Reconnecting to Twitch");
                    twirk.connect();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();

    }

    private void initJDA() throws InterruptedException, LoginException {
        jda = new JDABuilder(config.discordToken).build();
        jda.addEventListener(new DiscordEventListener());
        jda.setAutoReconnect(true);
        jda.awaitReady();
    }

    public void shutdown() {
        log("Shutting down...");
        checkIsLiveThread.stopThread();
        jda.shutdownNow();
        twirk.disconnect();
        logWriter.close();
    }


    public static void log(String l) {
        logWriter.println("[" + sdf.format(System.currentTimeMillis()) + "] " + l);
        logWriter.flush();
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

    public static BSRMirrorMain getInstance() {
        return instance;
    }
}
