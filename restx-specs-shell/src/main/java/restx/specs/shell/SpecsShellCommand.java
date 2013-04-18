package restx.specs.shell;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import restx.factory.Component;
import restx.server.simple.simple.SimpleWebServer;
import restx.shell.RestxShell;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 4/10/13
 * Time: 4:27 PM
 */
@Component
public class SpecsShellCommand extends StdShellCommand {
    protected SpecsShellCommand() {
        super(ImmutableList.of("spec"), "restx spec commands: list, server, ... ");
    }

    @Override
    protected Optional<ShellCommandRunner> doMatch(String line) {
        final List<String> args = splitArgs(line);
        if (args.size() < 2) {
            return Optional.absent();
        }

        switch (args.get(1)) {
            case "server":
                return Optional.<ShellCommandRunner>of(new SpecServerCommandRunner(args));
        }

        return Optional.absent();
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(
                new ArgumentCompleter(new StringsCompleter("spec"), new StringsCompleter("server")));
    }



    private class SpecServerCommandRunner implements ShellCommandRunner {
        private final List<String> args;

        public SpecServerCommandRunner(List<String> args) {
            this.args = args;
        }

        @Override
        public void run(final RestxShell shell) throws Exception {
            final String routerPath;
            if (args.size() > 2) {
                routerPath = args.get(2);
            } else {
                routerPath = "/api";
            }
            int port = 8888;
            if (args.size() > 3) {
                port = Integer.parseInt(args.get(3));
            }

            shell.printIn("LAUNCHING SPECS SERVER on port " + port + "...\n", RestxShell.AnsiCodes.ANSI_GREEN);
            shell.println("type `stop` to stop the server, `help` to get help on available commands");

            System.setProperty("restx.factory.load", "onrequest");
            final SimpleWebServer webServer = new SimpleWebServer(routerPath, ".", port);
            webServer.startAndAwait();

            shell.getConsoleReader().setPrompt("spec-server> ");
            shell.getConsoleReader().addCompleter(new StringsCompleter("stop", "open", "help"));

            boolean exit = false;
            while (!exit) {
                String line = shell.getConsoleReader().readLine().trim();
                switch (line) {
                    case "stop":
                        exit = stop(shell, webServer);
                        break;
                    case "open":
                        openInBrowser(shell, webServer.baseUrl() + routerPath);
                        break;
                    case "help":
                        help(shell);
                        break;
                    default:
                        shell.println(
                                "command not found. use `help` to get the list of commands.");
                }
            }
        }

        private void openInBrowser(RestxShell shell, String uri) throws IOException {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (UnsupportedOperationException e) {
                shell.printIn("can't open browser: " + e.getMessage(), RestxShell.AnsiCodes.ANSI_RED);
            } catch (IOException e) {
                shell.printIn("can't open browser: " + e.getMessage(), RestxShell.AnsiCodes.ANSI_RED);
            } catch (URISyntaxException e) {
                shell.printIn("can't open browser: " + e.getMessage(), RestxShell.AnsiCodes.ANSI_RED);
            }
        }

        private boolean stop(RestxShell consoleReader, SimpleWebServer webServer) {
            boolean exit;
            try {
                consoleReader.println("stopping server...");
                webServer.stop();
                exit = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return exit;
        }

        private void help(RestxShell shell) throws IOException {
            shell.printIn("stop", RestxShell.AnsiCodes.ANSI_GREEN);
            shell.println(" - to stop the server");

            shell.printIn("open", RestxShell.AnsiCodes.ANSI_GREEN);
            shell.println(" - open a browser on the spec server");

            shell.printIn("help", RestxShell.AnsiCodes.ANSI_GREEN);
            shell.println(" - this help");

            shell.println("");
            shell.println("to add new routes simply create/edit .spec.yaml file in\n" +
                    "current directory or subdirectories.");
        }
    }
}
