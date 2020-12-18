import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class Server {

    private static final String dbFile = "account.txt";
    private static final Integer port = 5555;
    private static final Map<String, String> db = new HashMap<>();
    private static final Set<String> online = new HashSet<>();

    private static Integer totalConnection = 0;

    public static void main(final String[] args) throws IOException {
        try (final BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(dbFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String[] pair = line.split(" ");
                db.put(pair[0], pair[1]);
            }
        }
        System.out.printf("Server is running on port: %s\n", port);
        System.out.println("Waiting for connection...");
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                new Chat(serverSocket.accept()).start();
            }
        }
    }

    static class Chat extends Thread {

        private final Socket clientSocket;
        private final BufferedReader in;
        private final PrintWriter out;

        private Integer totalFail = 0;
        private String username;
        private final String connectionID;

        Chat(final Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            in = Utils.getReaderFromSocket(clientSocket);
            out = Utils.getWriterFromSocket(clientSocket);
            connectionID = UUID.randomUUID().toString();
        }

        private static synchronized void incrementConnection(final String connectionID) {
            System.out.printf("Accepted new connectionID: %s\n", connectionID);
            totalConnection++;
            System.out.printf("Total connection: %s\n", totalConnection);
        }

        private static synchronized void decrementConnection(final String connectionID) {
            System.out.printf("A connection has finished: %s\n", connectionID);
            totalConnection--;
            System.out.printf("Total connection: %s\n", totalConnection);
        }

        @Override
        public void run() {
            try (clientSocket) {
                incrementConnection(connectionID);
                String line;
                process:
                while ((line = in.readLine()) != null) {
                    final String[] cmd = line.split(" ");
                    switch (cmd[0]) {
                        case Utils.USER_CMD:
                            userCommandProcess(cmd[1]);
                            if (!canContinueLogin()) {
                                break process;
                            }
                            break;
                        case Utils.PASS_CMD:
                            passwordCommandProcess(cmd[1]);
                            if (!canContinueLogin()) {
                                break process;
                            }
                            break;
                        case Utils.ECHO_CMD:
                            out.println(cmd[1]);
                            break;
                        case Utils.LOGOUT_CMD:
                            break process;
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            decrementConnection(connectionID);
            if (username != null) {
                online.remove(username);
            }
        }

        private boolean canContinueLogin() {
            if (totalFail < 5) {
                return true;
            }
            out.println(Utils.ERROR_LOGIN_ATTEMPT_MAX_EXCEED_MSG);
            return false;
        }

        private void userCommandProcess(final String username) {
            if (db.containsKey(username)) {
                this.username = username;
                out.println(Utils.OK_MSG);
            } else {
                responseError(Utils.ERROR_MSG);
            }
        }

        private void passwordCommandProcess(final String password) {
            if (db.get(username).equals(password)) {
                out.println(Utils.OK_MSG);
                if (online.contains(username)) {
                    responseError(Utils.ERROR_LOGIN_ALREADY_MSG);
                } else {
                    online.add(username);
                }
            } else {
                responseError(Utils.ERROR_MSG);
            }
        }

        private void responseError(final String msg) {
            out.println(msg);
            totalFail++;
        }
    }
}
