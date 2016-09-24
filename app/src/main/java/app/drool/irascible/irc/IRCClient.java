package app.drool.irascible.irc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class IRCClient {
    private final String TAG = this.getClass().getSimpleName();
    private IRCServerData serverData;
    private IRCClientListener listener;

    private Socket socket;
    private BufferedWriter streamWriter;
    private BufferedReader streamReader;

    private boolean shouldStopThreads = false;
    private final Thread readThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: Starting read thread");

            while (!shouldStopThreads) {
                try {
                    String line;
                    while (!shouldStopThreads && (line = streamReader.readLine()) != null) {
                        if (line.toUpperCase().startsWith("PING ")) {
                            Log.d(TAG, "run: Responded to ping");
                            writeToBuffer("PONG " + line.substring(5) + "\r\n");
                        } else
                            listener.receiveMessageFromClient(line);
                    }
                } catch (IOException e) {
                    listener.receiveErrorFromClient(e.getMessage());
                }
            }

            Log.d(TAG, "run: Stopping read thread");
        }
    });

    public void writeToBuffer(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (streamWriter == null) return;
                    streamWriter.write(message + "\r\n");
                    streamWriter.flush();
                } catch (IOException e) {
                    listener.receiveErrorFromClient(e.getMessage());
                }
            }
        }).start();
    }

    public IRCClient(IRCClientListener listener, IRCServerData data) {
        this.listener = listener;
        this.serverData = data;
    }

    public boolean connect() {
        try {
            socket = new Socket(serverData.getServerAddress(), serverData.getServerPort());
            streamWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            streamReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            listener.receiveErrorFromClient("Could not connect to " + serverData.getServerAddress());
            return false;
        }
        return true;
    }

    public boolean login() {
        try {
            streamWriter.write("NICK " + serverData.getNickName() + "\r\n");
            streamWriter.write("USER " + serverData.getIdent() + " 8 * : " + serverData.getNickName() +"\r\n");
            streamWriter.flush();
        } catch (IOException e) {
            listener.receiveErrorFromClient("Could not communicate with " + serverData.getServerAddress());
            return false;
        }
        return true;
    }

    public void startReadThread() {
        shouldStopThreads = false;
        readThread.start();
    }

    public void destroy() {
        try {
            shouldStopThreads = true;
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
            streamReader.close();
            streamWriter.close();
            readThread.interrupt();
        } catch (IOException e) { }
    }

    public interface IRCClientListener {
        void receiveMessageFromClient(String message);
        void receiveErrorFromClient(String error);
    }

    public IRCClientListener getListener() {
        return listener;
    }
}
