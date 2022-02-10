# <p align="center">Welcome to FileServer👨‍💻 👋</p>

<blockquote>
FileServer is a multithreaded client-server program that uses TCP/IP connection for establshing a connection between a client and the server.
It allows multiple clients to connect to and interact with the server simulataneously. The fileServer is a file storage program that allows 
the client to upload, fetch and delete files on/from the server. The file can be of any type text, image, video, audio, dmg, etc. 



</blockquote>

<br/>



## Table of contents
1. [Getting Started](#getting-started)
    - [The Server](#server)
    - [The Client](#client)
2. [Demo](#demo)
3. [Installation](#installation)
    - [From IDE:](#IDE)
    - [From terminal:](#terminal)
4. [License](#license)
5. [Author](#author)

<br/>

## Getting started <a name="getting-started"></a>

### The Server <a name="server"></a>

FileServer consist of a multithreaded server that listens for incoming clients on the main thread and processes the client request on a separate pool 
of threads. This helps the server from not being occupied by the processing of the long running client request. And server will always be available 
for listening to new client request. Once the server process the client request, the server will generate a response and send it to the client.

and handles each client request on a separate thread.


### The Client <a name="client"></a>

The client sends a request to the server. And waits for the server response. After the server sends back a response the client displays an approriate message realated to the response to the user. 

The client can send the different types of request to the server, which are as following:

1. The client can send a request to upload a file on the servre.
2. Client can also fetch a file from the server.
3. Delete a file from the server.
4. Client can also close its connection to the server.
5. It can also send a request to shutdown server (In real world scenarios we don't do this :P)


<br/>


## ✨Demo <a name="demo"></a>

**Uploading files on the server**

https://user-images.githubusercontent.com/40322896/153406912-f750d8d0-5e85-4755-9b0c-cc246e40adf9.mov

<br/> <br/>


**Retrieving files from the server**

https://user-images.githubusercontent.com/40322896/153407294-825f8bb6-17b5-4d74-9cf6-01dfaa2e0b73.mov

<br/> <br/>

**Deleting files from the server**

https://user-images.githubusercontent.com/40322896/153407354-acf1c426-c011-45ea-b227-5b8de0c131d7.mov

<br/> <br/>

**Invalid Client Request**: 
For example retrieving/deleting (by file name or file ID) a file from the server that does not exist on the server. Or uploading a file on the server that does not exist on the client machine.

https://user-images.githubusercontent.com/40322896/153409073-49da0406-181f-43f0-8410-aaddfcb13a66.mov

<br/> <br/>

**Other user actions**:
1. Invalid user action
2. Closing the client
3. Shutting down the server

https://user-images.githubusercontent.com/40322896/153411122-e4a632da-a706-4e44-8f92-5e504b7d5848.mov

<br/> <br/>

## Installation <a name="installation"></a>

**From IDE:** <a name="IDE"></a>

1. Download the project.
2. Unzip it.
3. Import the project to an IDE like IntelliJ IDEA, Microsoft Visual Studio or Eclipse.
4. Build the project.
5. Run the main file inside server folder to start the server.
6. Run the main file inside client folder to start the client.
7. You can also allow multiple instances of the client program to run.

<br/>

**From terminal:** <a name="terminal"></a>
1. Open the project directory on terminal/command prompt.
2. Move to the server folder and use the **javac Main.java** command to compile the server program.
3. Move to the client folder and use the **javac Main.java** command to compile the client program.
4. Use the **java Main** command to start the server.
5. Use the **java Main** command to start the client.

<br/>

**Note:** You need to have Java on your system to run this project.

<br/>

## License <a name="license"></a>

Read more about the license here: <br/>
[Apache License 2.0](https://github.com/lowkey96/FileServer/blob/master/LICENSE)

<br/>

## Author <a name="author"></a>

**Lokesh Bisht**
