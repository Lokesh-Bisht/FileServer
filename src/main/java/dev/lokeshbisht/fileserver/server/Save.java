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
import java.util.concurrent.ConcurrentHashMap;


/**
 * A class for saving the current state and fetching the last saved state of the server.
 *
 * @author Lokesh Bisht
 */
public class Save {

    /**
     * Serialize the current state of the server stored in the {@code nameOrIdentifier}
     * {@code ConcurrentHashMap} to the file specified by the {@code filename}.
     *
     * @param nameOrIdIdentifier {@code ConcurrentHashMap} containing the current state
     *                                                    of the server.
     * @param fileName name of the file on the server in which we store the current
     *                 state of the server.
     */
    public static void serialize(ConcurrentHashMap<String, String> nameOrIdIdentifier,
                                 String fileName) {
        try (
                FileOutputStream fos = new FileOutputStream(fileName);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            oos.writeObject(nameOrIdIdentifier);
        } catch (IOException e) {
            System.out.println("Failed to serialize data!");
            e.printStackTrace();
        }
    }


    /**
     * Deserialize the current state of the server stored in the file {@code fileName}
     * and save it to the {@code ConcurrentHashMap} map.
     *
     * @param fileName name of the file on the server from which we retrieve the
     *                 last saved state of the server.
     * @return the last saved state of the server in the form a {@code ConcurrentHashMap}.
     */
    public static ConcurrentHashMap<String, String> deserialize(String fileName) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        try (FileInputStream fis = new FileInputStream(fileName);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            map = (ConcurrentHashMap<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Failed to deserialize data!");
            e.printStackTrace();
        }
        return map;
    }
}