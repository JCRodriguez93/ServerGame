package org.project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.project.Server.*;


/**
 *Clase Arranque.
 * esta clase acepta las peticiones de los clientes que se van conectando.
 * @author Jorge Campos Rodríguez
 */
public class Boot{


    {
        int numcli = 0;
        try {
            ServerSocket skServidor = new ServerSocket(PUERTO);
            System.out.println("Escucho el puerto " + PUERTO);

            while (true) {
                try {
                    Socket cliente = skServidor.accept();
                    System.out.println("cliente " + (++numcli) + " conectado ");
                    ++Server.numclientes;
                    new Server(cliente, "Cliente " + numcli).start();
                    System.out.println("Nº de clientes conectados: " + Server.numclientes);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
