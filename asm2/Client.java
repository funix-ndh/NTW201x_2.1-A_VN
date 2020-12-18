import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

final class Client {
    private static final Integer port = 5555;
    private static final Scanner sc = new Scanner(System.in);

    public static void main(final String[] args) throws IOException {
        final InetAddress inetAddress = InetAddress.getLocalHost();
        try (final Socket socket = new Socket(inetAddress.getHostAddress(), port)) {
            new Chat(socket).run();
        }
    }

    static class Chat {

        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;

        Chat(final Socket socket) throws IOException {
            this.socket = socket;
            in = Utils.getReaderFromSocket(socket);
            out = Utils.getWriterFromSocket(socket);
        }

        public void run() throws IOException {
            try (socket) {
                login();
                echo();
            }
        }

        private void login() throws IOException {
            do {
                while (!isUsernameValid()) {
                    continue;
                }
            } while (!isPasswordCorrect());
            System.out.println("Login successful");
        }

        private boolean isUsernameValid() throws IOException {
            System.out.print("Please enter login username: ");
            out.printf(Utils.USER_CMD_TEMPLATE, sc.nextLine());
            if (isResponseSuccess()) {
                return true;
            }
            System.out.println("Username not found");
            return checkLoginAttemptExceed();
        }

        private boolean isPasswordCorrect() throws IOException {
            System.out.print("Please enter login password: ");
            out.printf(Utils.PASS_CMD_TEMPLATE, sc.nextLine());
            if (isResponseSuccess()) {
                if (isUserLoginAlready()) {
                    System.out.println("This user already login");
                    return checkLoginAttemptExceed();
                }
                return true;
            }
            System.out.println("Incorrect Username & Password");
            return checkLoginAttemptExceed();
        }

        private void echo() throws IOException {
            while (true) {
                System.out.print("Send message to server(enter 'exit' to halt the program): ");
                final String input = sc.nextLine();
                if ("exit".equals(input)) {
                    out.printf(Utils.LOGOUT_CMD);
                    System.out.println("Logout successful, client stopped");
                    return;
                }
                out.printf(Utils.ECHO_CMD_TEMPLATE, input);
                System.out.printf("Reply from server: %s\n", in.readLine());
            }
        }

        private boolean isResponseSuccess() throws IOException {
            return in.readLine().startsWith(Utils.OK_MSG);
        }

        private boolean isUserLoginAlready() throws IOException {
            return in.ready() && in.readLine().equals(Utils.ERROR_LOGIN_ALREADY_MSG);
        }

        private boolean checkLoginAttemptExceed() throws IOException {
            if (in.ready() && Utils.ERROR_LOGIN_ATTEMPT_MAX_EXCEED_MSG.equals(in.readLine())) {
                System.out.println("Login attempt max exceed");
                System.exit(0);
            }
            return false;
        }
    }
}
