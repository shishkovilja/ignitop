package dev.ignitop;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dev.ignitop.ignite.IgniteManager;
import dev.ignitop.ignite.util.IgniteClientHelper;
import dev.ignitop.ui.Terminal;
import dev.ignitop.ui.TerminalUi;
import dev.ignitop.ui.updater.impl.TopologyInformationUpdater;

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

        try (Terminal terminal = new Terminal(); IgniteClientHelper igniteClientHelper = new IgniteClientHelper(addrs)) {
            TerminalUi terminalUi = new TerminalUi(terminal);
            IgniteManager igniteMgr = new IgniteManager(igniteClientHelper);
            terminalUi.updater(new TopologyInformationUpdater(igniteMgr));

            ScheduledFuture<?> fut = executor.scheduleAtFixedRate(terminalUi::refresh, 0, DEFAULT_UPDATE_INTERVAL,
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
