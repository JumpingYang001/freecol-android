/**
 *  Copyright (C) 2002-2012   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */


package net.sf.freecol.server.networking;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import net.sf.freecol.FreeCol;
import net.sf.freecol.common.networking.Connection;
import net.sf.freecol.common.networking.MessageHandler;
import net.sf.freecol.server.FreeColServer;

import org.w3c.dom.Element;

import android.util.Log;



/**
 * The networking server in which new clients can connect and methods
 * like <code>sendToAll</code> are kept.
 *
 * <br><br>
 *
 * When a new client connects to the server a new {@link Connection}
 * is made, with {@link net.sf.freecol.server.control.UserConnectionHandler}
 * as the control object.
 *
 * @see net.sf.freecol.common.networking
 */
public final class Server extends Thread {

    private static Logger logger = Logger.getLogger(Server.class.getName());

    /** The public "well-known" socket to which clients may connect. */
    private ServerSocket serverSocket;

    /** A hash of Connection objects, keyed by the Socket they relate to. */
    private HashMap<Socket, Connection> connections = new HashMap<Socket, Connection>();

    /**
     * Whether to keep running the main loop that is awaiting new
     * client connections.
     */
    private boolean running = true;

    /** The owner of this <code>Server</code>. */
    private FreeColServer freeColServer;

    /** The TCP port that is beeing used for the public socket. */
    private int port;

    /** For information about this variable see the run method. */
    private final Object shutdownLock = new Object();

    /**
     * Creates a new network server. Use {@link #run server.start()} to start
     * listening for new connections.
     *
     * @param freeColServer The owner of this <code>Server</code>.
     * @param port The TCP port to use for the public socket.
     * @throws IOException if the public socket cannot be created.
     */
    public Server(FreeColServer freeColServer, int port) throws IOException {
        super(FreeCol.SERVER_THREAD+"Server");
        this.freeColServer = freeColServer;
        this.port = port;
        //serverSocket = new ServerSocket(port, freeColServer.getMaximumPlayers());
        serverSocket = new ServerSocket(port);
    }

    /**
     * Starts the thread's processing.  Contains the loop that is
     * waiting for new connections to the public socket.  When a new
     * client connects to the server a new {@link Connection} is made,
     * with {@link net.sf.freecol.server.control.UserConnectionHandler}
     * as the control object.
     */
    public void run() {
        // This method's entire body is synchronized to shutdownLock.
        // The reason why this is done is to prevent the shutdown
        // method from finishing before this thread is finished
        // working.  We have to do this because the
        // ServerSocket::close method keeps the server alive for
        // several milliseconds EVEN AFTER THE CLOSE METHOD IS
        // FINISHED. And because of this a new server can't be created
        // on the same port as this server right after closing this
        // server.
        //
        // Now that the shutdown method 'hangs' until the entire
        // server thread is finished you can be certain that the
        // ServerSocket is REALLY closed after execution of shutdown.
        synchronized (shutdownLock) {
            while (running) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();

                    logger.info("Got client connection from "
                                + clientSocket.getInetAddress().toString());
					Log.d("Server", "Client connected: "
							+ clientSocket.getInetAddress().toString());
                    //Connection connection =
                        new Connection(clientSocket, freeColServer.getUserConnectionHandler(),
                                       FreeCol.SERVER_THREAD);
                    //connections.put(clientSocket, connection);
                } catch (IOException e) {
                    if (running) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        logger.warning(sw.toString());
                    }
                }
            }
        }
    }

    /**
     * Sends a network message to all connections except
     * <code>exceptConnection</code> (if the argument is non-null).
     *
     * @param element The root element of the message to send.
     * @param exceptConnection If non-null, the
     *        <code>Connection</code> not to send to.
     */
    public void sendToAll(Element element, Connection exceptConnection) {
        Iterator<Connection> connectionIterator = getConnectionIterator();

        while (connectionIterator.hasNext()) {
            Connection connection = connectionIterator.next();
            if (connection != exceptConnection) {
                try {
                    connection.sendAndWait(element);
                } catch (IOException e) {
                    logger.warning("Exception while attempting to send to "
                                   + connection);
                }
            }
        }
    }

    /**
     * Sends a network message to all connections.
     *
     * @param element The root element of the message to send.
     */
    public void sendToAll(Element element) {
        sendToAll(element, null);
    }

    /**
     * Gets the TCP port that is beeing used for the public socket.
     *
     * @return The TCP port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the specified <code>MessageHandler</code> to all connections.
     *
     * @param messageHandler The <code>MessageHandler</code>.
     */
    public void setMessageHandlerToAllConnections(MessageHandler messageHandler) {
        Iterator<Connection> connectionIterator = getConnectionIterator();

        while (connectionIterator.hasNext()) {
            Connection connection = connectionIterator.next();
            connection.setMessageHandler(messageHandler);
        }
    }

    /**
     * Gets an iterator of every connection to this server.
     *
     * @return The <code>Iterator</code>.
     * @see Connection
     */
    public Iterator<Connection> getConnectionIterator() {
        return connections.values().iterator();
    }

    /**
     * Shuts down the server thread.
     */
    public void shutdown() {
        running = false;

        try {
            serverSocket.close();
            logger.fine("Closed server socket.");
        } catch (IOException e) {
            logger.warning("Could not close the server socket!");
        }

        synchronized (shutdownLock) {
            // Nothing to do here... just waiting for the server
            // thread to finish.  For more info see the run() method
        }

        Iterator<Connection> connectionsIterator = getConnectionIterator();
        while (connectionsIterator.hasNext()) {
            Connection c = connectionsIterator.next();

            try {
                if (c != null) {
                    //c.reallyClose();
                    c.close();
                }
            } catch (IOException e) {
                logger.warning("Could not close the connection.");
            }
        }

        freeColServer.removeFromMetaServer();

        logger.fine("Server shutdown.");
    }

    /**
     * Gets a <code>Connection</code> identified by a <code>Socket</code>.
     *
     * @param socket The <code>Socket</code> that identifies the
     *               <code>Connection</code>
     * @return The <code>Connection</code>.
     */
    public Connection getConnection(Socket socket) {
        return connections.get(socket);
    }

    /**
     * Adds a (usually Dummy)Connection into the hashmap.
     *
     * @param connection The connection to add.
     */
    public void addDummyConnection(Connection connection) {
        connections.put(new Socket(), connection);
    }

    /**
     * Adds a Connection into the hashmap.
     *
     * @param connection The connection to add.
     */
    public void addConnection(Connection connection) {
        connections.put(connection.getSocket(), connection);
    }

    /**
     * Removes the given connection.
     *
     * @param connection The connection that should be removed.
     */
    public void removeConnection(Connection connection) {
        connections.remove(connection.getSocket());
    }
}
