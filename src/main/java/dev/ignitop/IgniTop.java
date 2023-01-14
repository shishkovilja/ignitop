package dev.ignitop;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dev.ignitop.ui.Terminal;
import dev.ignitop.ui.TerminalUI;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import dev.ignitop.ignite.TopologyInformationUpdater;

/**
 *
 */
public class IgniTop {
    /** Default update interval in seconds. */
    public static final int DEFAULT_UPDATE_INTERVAL = 5;

    /**
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        try (Terminal terminal = new Terminal();
             IgniteClient client = Ignition.startClient(new ClientConfiguration().setAddresses("127.0.0.1:10800"))) {
            TopologyInformationUpdater topUpdater = new TopologyInformationUpdater(client, new TerminalUI(terminal));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                executor.shutdown();

                terminal.close();
            }));

            ScheduledFuture<?> fut = executor.scheduleAtFixedRate(topUpdater::body, 0, DEFAULT_UPDATE_INTERVAL,
                TimeUnit.SECONDS);

            fut.get();
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
        finally {
            executor.shutdown();
        }
    }
}
