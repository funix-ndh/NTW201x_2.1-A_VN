import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

final class Utils {
    public static final String USER_CMD = "USER";
    public static final String PASS_CMD = "PASS";
    public static final String ECHO_CMD = "ECHO";
    public static final String LOGOUT_CMD = "LOGOUT";

    public static final String USER_CMD_TEMPLATE = USER_CMD + " %s\n";
    public static final String PASS_CMD_TEMPLATE = PASS_CMD + " %s\n";
    public static final String ECHO_CMD_TEMPLATE = ECHO_CMD + " %s\n";

    public static final String OK_MSG = "+OK";
    public static final String ERROR_MSG = "-ERR";

    public static final String ERROR_LOGIN_ATTEMPT_MAX_EXCEED_MSG = ERROR_MSG + " LOGIN FAIL EXCEED";
    public static final String ERROR_LOGIN_ALREADY_MSG = ERROR_MSG + " THIS USER ALREADY LOGIN";

    public static PrintWriter getWriterFromSocket(final Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

    public static BufferedReader getReaderFromSocket(final Socket socket) throws IOException {
        return new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    private Utils() {}
}
