/*
 * Copyright 2023 Ilya Shishkov (https://github.com/shishkovilja)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ignitop;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import dev.ignitop.ignite.IgniteHelper;
import dev.ignitop.ui.TerminalProvider;
import dev.ignitop.ui.TerminalUi;
import dev.ignitop.ui.keyhandler.KeyPressHandler;
import dev.ignitop.ui.updater.impl.TopologyInformationUpdater;

/**
 *
 */
public class IgniTop {
    /** Default update interval in seconds. */
    public static final int DEFAULT_UPDATE_INTERVAL = 1;

    /** Default addresses. */
    public static final String[] DEFAULT_ADDRESSES = {"127.0.0.1:10800"};

    /** Terminal provider. */
    private final TerminalProvider termProv;

    /** Terminal UI. */
    private final TerminalUi terminalUi;

    /** Ignite helper. */
    private IgniteHelper igniteHelper;

    /** Screen updater executor. */
    private final ScheduledExecutorService screenUpdaterExec;

    /** Key press handler executor. */
    private final ExecutorService keyPressExec;

    /** Screen updater future. */
    private ScheduledFuture<?> screenUpdaterFut;

    /** Key press future. */
    private Future<?> keyPressFut;

    /**
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        IgniTop igniTop = new IgniTop();

        try {
            igniTop.start(args);
        }
        catch (CancellationException ignore) {
            // No-op.
        }
        catch (Throwable e) {
            igniTop.shutdown();

            throw new RuntimeException(e);
        }
    }

    /**
     * Default constructor.
     */
    public IgniTop() {
        termProv = new TerminalProvider();

        terminalUi = new TerminalUi(termProv);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();

            System.out.println("DEBUG: After shutdown hook");
        }));

        screenUpdaterExec = Executors.newScheduledThreadPool(1);

        keyPressExec = Executors.newFixedThreadPool(1);
    }

    /**
     * @param args Command line arguments.
     */
    private void start(String[] args) throws Exception {
        String[] addrs = processArguments(args);

        igniteHelper = new IgniteHelper(addrs);

        terminalUi.updater(new TopologyInformationUpdater(igniteHelper));

        screenUpdaterFut = screenUpdaterExec.scheduleAtFixedRate(terminalUi::refresh, 0, DEFAULT_UPDATE_INTERVAL,
            TimeUnit.SECONDS);

        KeyPressHandler keyPressHnd = new KeyPressHandler();

        keyPressHnd.addKeyHandler('t', () -> terminalUi.updater(new TopologyInformationUpdater(igniteHelper)));

        keyPressFut = keyPressExec.submit(() -> {
            try {
                while (!Thread.interrupted()) {
                    int ch = termProv.reader().read();

                    if (ch == -1)
                        return;
                    else if (ch != -2) // Read timeout.
                        keyPressHnd.handle((char)ch);
                }
            }
            catch (IOException e) {
                // Ignore interruptions during read.
                if (e instanceof InterruptedIOException)
                    return;

                e.printStackTrace();

                screenUpdaterFut.cancel(true);
            }
        });

        screenUpdaterFut.get();
        keyPressFut.cancel(true);
    }

    /**
     *
     */
    public void shutdown() {
        if (screenUpdaterFut != null)
            screenUpdaterFut.cancel(true);

        if (keyPressFut != null)
            keyPressFut.cancel(true);

        if (igniteHelper != null)
            igniteHelper.close();

        if (termProv != null)
            termProv.close();

        screenUpdaterExec.shutdown();
        keyPressExec.shutdown();
    }

    /**
     * Get addresses from arguments.
     *
     * @param args Args.
     */
    private String[] processArguments(String[] args) {
        if (args.length == 0) {
            System.err.println("No addresses was specified. Using default addresses: " +
                Arrays.toString(DEFAULT_ADDRESSES));
        }

        return Arrays.stream(args)
            .map(s -> s.split(","))
            .findFirst()
            .orElse(DEFAULT_ADDRESSES);
    }
}
