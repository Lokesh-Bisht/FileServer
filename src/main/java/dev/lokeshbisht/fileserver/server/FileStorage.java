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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for processing different client request (create, fetch and delete) and
 * returning an appropriate response to the <b>Server</b> class which ultimately
 * send the response back to the client.
 *
 * This class also implements the Serializable interface for storing the current
 * state and fetching the last saved state of the server
 *
 * @author Lokesh Bisht
 */
public class FileStorage implements Serializable {

    private ConcurrentHashMap<String, String> filenameToIdMap;
    private ConcurrentHashMap<String, String> idToFilenameMap;

    private File nameIdentifierFile;
    private File idIdentifierFile;
    private final String serverDirPath = System.getProperty("user.dir") +
            File.separator + "server" + File.separator + "data" + File.separator;


    /**
     * FileStorage constructor initializes two {@code ConcurrentHashMap}
     * {@code filenameToIdMap} and {@code idToFilenameMap}. And fetches the last saved
     * state of the server.
     */
    public FileStorage() {
        filenameToIdMap = new ConcurrentHashMap<>();
        idToFilenameMap = new ConcurrentHashMap<>();
        fetchSerializeData();
    }

    /**
     * <p>Restores the last saved state of the server before processing any client requests.</p>
     * <br/>
     * <p>If there are no {@code nameIdentifier} and {@code idIdentifierFile} files to
     * restore the server state. Then the method creates nameIdentifierFile.txt and
     * idIdentifierFile.txt for storing the {@code filenameToIdMap} and {@code idToFilenameMap}
     * ConcurrentHashMaps respectively.</p>
     */
    private synchronized void fetchSerializeData() {
        String path = serverDirPath + "nameIdentifierFile.txt";
        nameIdentifierFile = new File(path);
        path = serverDirPath + "idIdentifierFile.txt";
        idIdentifierFile = new File(path);

        if (!nameIdentifierFile.exists() && !nameIdentifierFile.isDirectory()) {
            try {
                boolean fileCreated = nameIdentifierFile.createNewFile();
                if (!fileCreated) {
                    System.out.println("Failed to create file to store serialized data.");
                    System.exit(0);
                }
            } catch (IOException e) {
                System.out.println("Failed to create file to store serialized data.");
                e.printStackTrace();
            }
        }

        if (!idIdentifierFile.exists() && !idIdentifierFile.isDirectory()) {
            try {
                boolean fileCreated = idIdentifierFile.createNewFile();
                if (!fileCreated) {
                    System.out.println("Failed to create file to store serialized data.");
                }
            } catch (IOException e) {
                System.out.println("Failed to create file to store serialized data.");
                e.printStackTrace();
            }
        }

        if (nameIdentifierFile.length() != 0 && idIdentifierFile.length() != 0) {
            filenameToIdMap = Save.deserialize(String.valueOf(nameIdentifierFile));
            idToFilenameMap = Save.deserialize(String.valueOf(idIdentifierFile));
        }
    }


    /**
     * Saves the current state of the server in a local file on the server
     * before shutting down the server.
     */
    public synchronized void updateStoredSerializedData() {
        Save.serialize(filenameToIdMap, String.valueOf(nameIdentifierFile));
        Save.serialize(idToFilenameMap, String.valueOf(idIdentifierFile));
    }

    /** Shows the current info of all the files on the server. */
    public synchronized void showServerCurrentFilesInfo() {
        System.out.printf("%nFiles available on the server: %n");
        // Do not directly use the filenameToIdMap in the foreach loop you will
        // get old values of filenameToIdMap and not the current state/values.
        ConcurrentHashMap<String, String> temp = filenameToIdMap;

        for (Map.Entry<String, String> entry : temp.entrySet()) {
            System.out.println("File ID = " + entry.getValue() + ", FileName = " + entry.getKey());
        }
        System.out.println();
    }


    /**
     * Saves a file with the name {@code serverFilename} on the server.
     *
     * @param serverFilename the file is saved on the server by this name.
     * @param fileContent a {@code byte[]} array containing the contents
     *                    of the file to be saved on the server.
     * @return {@code fileID} if the file is successfully created on the server,
     * or -1 otherwise.
     */
    public long addFile(String serverFilename, byte[] fileContent) {
        long fileID = -1;
        try {
            String path = serverDirPath + serverFilename;
            File file = new File(path);

            if (!file.exists() && !file.isDirectory()) {
                Files.write(Paths.get(path), fileContent);

                synchronized (this) {
                    fileID = filenameToIdMap.size();
                    if (idToFilenameMap.get(fileID) != null) {
                        long start = fileID;
                        long end = 1000_000_000_000_000_000L;
                        while (start <= end) {
                            long mid = (start + end) / 2;
                            if (idToFilenameMap.get(String.valueOf(mid)) == null) {
                                end = mid - 1;
                            } else {
                                start = mid + 1;
                            }
                        }
                        fileID = start;
                    }
                    idToFilenameMap.put(String.valueOf(fileID), serverFilename);
                    filenameToIdMap.put(serverFilename, String.valueOf(fileID));
                }
            } else {
                System.out.println("The file already exists on the server.");
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return fileID;
    }


    /**
     * Retrieve the file with id {@code fileID} from the server.
     *
     * @param fileID id of the file to be fetched from the server.
     * @return {@code fileContent} a {@code byte[]} array containing the content
     * of the file if the file is present on the server, or
     * {@code null} if this file is not present on the server.
     *
     */
    public byte[] getFile(long fileID) {
        byte[] fileContent = null;
        String filename = idToFilenameMap.get(String.valueOf(fileID));
        if (filename == null) {
            return null;
        }
        String path = serverDirPath + filename;
        try {
            fileContent = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.out.println("Can't fetch file " + filename + ". File does not " +
                    "exist at path: " + path);
        }
        return fileContent;
    }

    /**
     * Retrieve the file {@code filename} from the server.
     *
     * @param filename name of the file to be fetched from the server.
     * @return {@code fileContent} a {@code byte[]} array containing the contents
     * of the file if the file is present on the server, or {@code null} if this
     * file is not present on the server.
     */
    public byte[] getFile(String filename) {
        byte[] fileContent = null;
        if (filenameToIdMap.get(filename) == null) {
            return null;
        }
        String path = serverDirPath + filename;
        try {
            fileContent = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.out.println("Can't fetch file " + filename + ". File does not " +
                    "exist at path: " + path);
        }
        return fileContent;
    }


    /**
     * Delete the file with id {@code fileID} from the server.
     *
     * @param fileID ID of the file to be deleted from the server.
     * @return {@code true} if the file is deleted from the server, otherwise
     * {@code false} if the file is not present on the server.
     */
    public boolean deleteFile(long fileID) {
        String filename = idToFilenameMap.get(String.valueOf(fileID));
        if (filename == null) {
            return false;
        }
        String path = serverDirPath + filename;
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            boolean isFileDeleted = file.delete();
            if (!isFileDeleted) {
                System.out.println("Failed in deleting file \"" + filename + " \" from the server");
                return false;
            }
            synchronized (this) {
                idToFilenameMap.remove(fileID);
                filenameToIdMap.remove(filename);
            }
            return true;
        }
        return false;
    }

    /**
     * Delete the file {@code fileName} from the server.
     *
     * @param filename name of the file to be deleted from the server.
     * @return {@code true} if the file is deleted from the server, otherwise
     * {@code false} if the file is not present on the server.
     */

    public boolean deleteFile(String filename) {
        String fileID = filenameToIdMap.get(filename);
        if (fileID == null) {
            return false;
        }
        String path = serverDirPath + filename;
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            boolean isFileDeleted = file.delete();
            if (!isFileDeleted) {
                System.out.println("Failed in deleting file \"" + filename + " \" from the server");
                return false;
            }
            synchronized (this) {
                idToFilenameMap.remove(fileID);
                filenameToIdMap.remove(filename);
            }
            return true;
        }
        return false;
    }
}