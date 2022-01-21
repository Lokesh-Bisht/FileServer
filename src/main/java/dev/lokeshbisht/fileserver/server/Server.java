/*
 * Copyright 2022 Lokesh Bisht. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.lokeshbisht.fileserver.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This class is responsible for starting the web server. And listening to incoming
 * connections or clients. If a client is accepted by the server then a TCP/IP connection
 * is established between the client and the server using sockets.
 *
 * The server process the long-running client request on a separate threads to avoid
 * blocking the server. So that the server is always available to accept new clients.
 * The client requests are queued into a task-queue and executed one by one in FIFO
 * order by a pool of threads.
 *
 * @author Lokesh Bisht
 */

public class Server {

    /** Constants indicating the type of client request. */
    private static final String PUT_REQ = "PUT";
    private static final String GET_REQ = "GET";
    private static final String DELETE_REQ = "DELETE";
    private static final String EXIT_REQ = "EXIT";
    private static final String INVALID_REQUEST = "INVALID REQUEST";

    /** HTTP response status codes. */
    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;

    private ServerSocket serverSocket = null;
    private FileStorage fileStorage;
    private ExecutorService executor;
    private final int port;

    /** Server constructor sets the port for creating the ServerSocket. */
    public Server(int port) {
        this.port = port;
    }

    /** Instantiates a new instance of the file storage class to save a file on the server or
     * fetch/delete a file from the server. And creates a new <b>ExecutorService</b>.
     */
    private void initialize() {
        fileStorage = new FileStorage();
        executor = Executors.newCachedThreadPool();
    }


    /**
     * Start the server and keep the server running indefinitely until
     * an "exit" command is received.
     */
    public void start() {

        initialize();
        openServerSocket();

        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleClientRequest(socket));
            } catch (IOException e) {
                throw new RuntimeException("Error accepting client connection", e);
            }
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.port, e);
        }
    }

    private synchronized void closeServerSocket() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }


    /** The below method does the following:
     * <ul>
     *     <li>Close the executor and free the threads acquired by the executor.</li>
     *     <li>Save the current state of the server to one or more local files on the server.
     *     So that whenever the server restarts it can resume from the last saved state.</li>
     *     <li>Closes the server socket to shutdown the server.</li>
     * </ul>
     */
    private void shutdownServer() {
        executor.shutdown();
        fileStorage.updateStoredSerializedData();
        closeServerSocket();
        System.exit(0);
    }


    /**
     * Handles the request sent by the clients.
     *
     * @param socket A client socket that helps the server and client in communicating
     *               back and forth.
     * @exception  NumberFormatException if {@code fileID} is not a numeric value.
     * */

    private void handleClientRequest(Socket socket) {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {

            // Show the current status of files on the server.
            fileStorage.showServerCurrentFilesInfo();

            final String received = input.readUTF();
            final String[] requestTokens = received.split(" ");


            String request = requestTokens[0];
            String response;
            String serverFilename = "";
            long fileID = -1;
            byte[] fileContent = null;

            if (request.equals(INVALID_REQUEST)) {
                return;
            }

            if (request.equals(EXIT_REQ)) {
                shutdownServer();
            }

            if (request.equals(PUT_REQ)) {
                if (requestTokens[1].equals("")) {
                    serverFilename = System.currentTimeMillis() + "";
                } else {
                    serverFilename = requestTokens[1];
                }
            }

            boolean byID = false;

            if (request.equals(GET_REQ) || request.equals(DELETE_REQ)) {
                if (requestTokens[1].equals("BY_ID")) {
                    byID = true;
                    try {
                        fileID = Long.parseLong(requestTokens[2]);
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid ID. ID can only be a numeric value");
                        return;
                    }
                } else {
                    serverFilename = requestTokens[2];
                }
            }

            switch (request) {
                case PUT_REQ:
                    int size = Integer.parseInt(input.readUTF());
                    fileContent = new byte[size];
                    input.readFully(fileContent, 0, fileContent.length);
                    fileID = fileStorage.addFile(serverFilename, fileContent);
                    response = fileID == -1 ? "" + FORBIDDEN : OK + " " + fileID;
                    break;
                case GET_REQ:
                    fileContent = byID ? fileStorage.getFile(fileID) : fileStorage.getFile(serverFilename);
                    response = "" + (fileContent == null ? NOT_FOUND : OK);
                    break;
                case DELETE_REQ:
                    boolean isDeleted = byID ? fileStorage.deleteFile(fileID) : fileStorage.deleteFile(serverFilename);
                    response = "" + (isDeleted ? OK : NOT_FOUND);
                    break;
                default:
                    response = "" + BAD_REQUEST;
                    break;
            }

            output.writeUTF(response);

            // Send the file requested by the client back to the client for download.
            // Check response.equals("200") to avoid NPE.
            if (request.equals(GET_REQ) && response.equals("200")) {
                output.writeInt(fileContent.length);
                output.write(fileContent);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
