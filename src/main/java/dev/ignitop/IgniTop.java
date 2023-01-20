package dev.ignitop;

import java.util.Arrays;
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

    /** Default addresses. */
    public static final String[] DEFAULT_ADDRESSES = {"127.0.0.1:10800"};

    /**
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No addresses was specified. Using default addresses: " +
                Arrays.toString(DEFAULT_ADDRESSES));
        }

        String[] addrs = Arrays.stream(args)
            .map(s -> s.split(","))
            .findFirst()
            .orElse(DEFAULT_ADDRESSES);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        try (Terminal terminal = new Terminal();
             IgniteClient client = Ignition.startClient(new ClientConfiguration().setAddresses(addrs))) {
            TopologyInformationUpdater topUpdater = new TopologyInformationUpdater(client, new TerminalUI(terminal));

            ScheduledFuture<?> fut = executor.scheduleAtFixedRate(topUpdater::body, 0, DEFAULT_UPDATE_INTERVAL,
                TimeUnit.SECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                fut.cancel(true);

                terminal.close();
            }));

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
