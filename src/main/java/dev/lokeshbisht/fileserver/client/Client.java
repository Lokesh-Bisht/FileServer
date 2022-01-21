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


package dev.lokeshbisht.fileserver.client;

import dev.lokeshbisht.fileserver.Configurations;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


/**
 * Client program for sending request to the server. These request may include
 * <ul>
 *     <li>Uploading file on the server</li>
 *     <li>Fetching file from the server</li>
 *     <li>Deleting file from the server</li>
 *     <li>Closing the client program</li>
 *     <li>And shutting down the server (only for testing environment)</li>
 * </ul>
 *
 * @author Lokesh Bisht
 */

public class Client {

    private static final String SERVER_ADDRESS = Configurations.ADDRESS;
    private static final int PORT = Configurations.PORT;
    private static final Scanner scanner = new Scanner(System.in);

    private static final String clientDirPath = System.getProperty("user.dir") +
            File.separator + "client" + File.separator + "data" + File.separator;

    private static final String GET = "1";
    private static final String PUT = "2";
    private static final String DELETE = "3";
    private static final String CLOSE_CLIENT = "4";
    private static final String EXIT = "exit";


    /** Create a TCP/IP socket connection to connect the client to the server. */
    @SuppressWarnings("InfiniteLoopStatement")
    public void connectToServer() {
        instructions();
        while (true) {
            try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
                try (DataInputStream input = new DataInputStream(socket.getInputStream());
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
                    sendRequestToServer(input, output);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void instructions() {
        System.out.printf("This is a client program for sending different " +
                "types of requests to the server. In this program you can" +
                " perform the following operations: %n%n");
        System.out.println("  1. You can upload a file on the server.");
        System.out.println("  2. You can fetch a file from the server.");
        System.out.println("  3. You can delete a file from the server.");
        System.out.println("  4. You can close the client program.");
        System.out.println("  5. You can also shutdown the server.");
        System.out.printf("  Fetch and delete requests requires filename/fileID.%n%n");
    }

    /**
     * Sends the client request (read, write, and delete) to the server.
     *
     * @param input input stream for reading the response sent by the server.
     * @param output output stream for writing the response to the server.
     * @throws IOException if an I/O error occurs while reading/writing
     * from/to the stream
     */
    private void sendRequestToServer(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.printf("%nEnter action (1 - get a file, 2 - save a file, 3 - delete a file, " +
                    "4 - close the client connection, exit - to quit the server): ");

        String action = scanner.nextLine();
        String nameOrId = "";
        String localFilename = "";
        String message = "";

        if (action.equals(GET) || action.equals(DELETE)) {
            System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
            nameOrId = scanner.nextLine();
            if (nameOrId.equals("1")) {
                nameOrId = "BY_NAME";
                System.out.print("Enter filename: ");
            } else {
                nameOrId = "BY_ID";
                System.out.print("Enter id: ");
            }
            localFilename = scanner.nextLine();
        }

        switch (action) {
            case PUT:
                System.out.print("Enter the local file name (present inside the " +
                        "/client/data folder) that you want to upload on the server: ");
                localFilename = scanner.nextLine();
                System.out.print("Enter the file name to be saved on server: ");
                String serverFilename = scanner.nextLine();
                action = "PUT";
                message = action +  " " + serverFilename;
                break;
            case GET:
                action = "GET";
                message = action + " " + nameOrId + " " + localFilename;
                break;
            case DELETE:
                action = "DELETE";
                message = action + " " + nameOrId + " " + localFilename;
                break;
            case CLOSE_CLIENT:
                output.writeUTF("Close Client connection");
                System.exit(0);
            case EXIT:
                output.writeUTF("EXIT");
                System.exit(0);
            default:
                output.writeUTF("INVALID REQUEST");
                System.out.println("Invalid action!");
                return;
        }

        // Send message to the server
        output.writeUTF(message);
        output.flush();
        if (action.equals("PUT")) {
            if (!transmitFile(localFilename, output)) {
                return;
            }
        }
        System.out.println("The request was sent.");
        readAndDisplayServerResponse(action, input);
    }


    /**
     * Reads the response sent by the server. And displays an appropriate message to the client.
     *
     * @param action type of client request to the server.
     * @param input input stream for reading the response sent by the server.
     * @throws IOException if an I/O error occurs while reading from the stream
     */
    private void readAndDisplayServerResponse(String action, DataInputStream input) throws IOException {
        String serverResponse = input.readUTF();
        String[] responseTokens = serverResponse.split(" ");
        int statusCode = Integer.parseInt(responseTokens[0]);

        switch (statusCode) {
            case 200:
                switch (action) {
                    case "GET":
                        receiveFile(input);
                        break;
                    case "PUT":
                        String fileID = responseTokens[1];
                        System.out.println("Response says that file is saved! ID = " + fileID);
                        break;
                    case "DELETE":
                        System.out.println("The response says that this file was deleted successfully!");
                        break;
                }
                break;
            case 404:
                System.out.println("The response says that this file is not found!");
                break;
            case 403:
                System.out.println("Ok, the response says that creating the file was forbidden!");
                break;
            case 400:
                System.out.println("Something strange! Hmm...");
                break;
            default:
                System.out.println("Invalid response!");
                break;
        }
    }


    /**
     * Uploads the local file (inside the File Server/client/data directory) on the server.
     *
     * @param filename the locale filename to be uploaded on the server.
     * @param output output stream for writing the response to the server.
     */
    private boolean transmitFile(String filename, DataOutputStream output) {
        String path = clientDirPath + filename;
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(Paths.get(path));
            try {
                output.writeUTF(String.valueOf(fileContent.length));
                output.write(fileContent);
                output.flush();
            } catch (IOException e) {
                System.out.println("Cannot read the file " + filename);
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            System.out.println("File \"" + filename + "\" does not exist at the location "
                    + path);
            return false;
        }
        return true;
    }


    /**
     * Receives the file (file that the client requested) sent by the server.
     * And saves it in the local directory (File Server/client/data).
     *
     * @param input input stream for reading the response sent by the server.
     * @throws IOException if an I/O error occurs while reading from the stream
     */
    private void receiveFile(DataInputStream input) throws IOException {
        int size = input.readInt();
        byte[] fileContent = new byte[size];
        input.readFully(fileContent, 0, fileContent.length);

        System.out.print("The file was downloaded! Specify a name for it: ");
        String saveName = scanner.nextLine();

        String path = clientDirPath + saveName;
        Files.write(Paths.get(path), fileContent);
        System.out.println("File saved on the hard drive in the client/data/" +
                " folder inside File Server directory");
    }
}