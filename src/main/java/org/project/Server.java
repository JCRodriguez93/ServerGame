package org.project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servidor al que se conectan los clientes concurrentemente.
 *
 * @author Jorge Campos Rodriguez
 */
public class Server extends Thread {

    /*
     * Atributos para el servidor.
     */
    Socket cliente; //socket para el cliente
    static final int PUERTO = 4000; //puerto de conexión

    public static String palabra; //palabra que usaré para adivinar
    public static int numclientes = 0; //numero de clientes que incrementa o decrementan

    /**
     * Atributos para colorear mensajes de consola
     */
    public static String black = "\033[30m";
    public static String cyan = "\033[36m";
    public static String red = "\033[31m";
    public static String green = "\033[32m";
    public static String blue = "\033[34m";
    public static String purple = "\033[35m";
    public static String reset = "\u001B[0m";

    // lista para añadir la palabra en un ArrayList de caracteres.
    private static List<Character> letras = new ArrayList<Character>();

    //objeto para la entrada de datos por consola
    Scanner scanner;

    /**
     * Input Stream para llevar el control de los flujos de entrada
     */
    private DataInputStream flujoEntrada = null;
    /**
     * Input Stream para llevar el control de los flujos de salida
     */
    private DataOutputStream flujoSalida = null;

    /**
     * Constructor de la clase Servidor. le pasamos por parámetro un objeto
     * socket y lo inicializamos así como el nombre para el hilo.
     *
     * @param cli
     * @param nombre
     */
    public Server(Socket cli, String nombre) { //constructor para pasarle un socket cliente al servidor
        //Conectamos al cliente por el puerto
        super(nombre);
        this.cliente = cli;

    }

    @Override
    public void run() {

        int estado = 1;
        boolean fin = false;

        while (!fin) {

            switch (estado) {
                case (1)://logueo
                    estado = this.logueo();
                    break;
                case (2)://configuración
                    //Configuramos la palabra
                    estado = this.configuracion();
                    break;
                case (3)://jugamos
                    estado = this.jugar();
                    break;
                case (4): //acertado
                    System.out.println(this.green + "\tEl " + Thread.currentThread().getName() + " ha acertado");
                    System.out.println(this.green + Thread.currentThread().getName() + " desconectado");

                    fin = true;
                    break;
            }
        }  //FIN

    }

    /**
     * Método jugar. Llama al método ocultar letras, da una pista de cuantas
     * letras tiene la palabra y pide letras al cliente para ir descubriendo la
     * palabra. Si resuelve correctamente pasa al estado 4 y gana, si no, se
     * sigue en el estado 3 jugando.
     *
     * @return int con el estado del diagrama.
     */
    private int jugar() {

        int estado = 3;
        String msj, respuesta, dichas;
        do {
            this.setMensajeClientes("La palabra tiene " + this.palabra.length() + " letras\n"
                    + "di una letra: ");
            msj = this.getMensajeClientes(); //obtengo la letra

            Pattern patron = Pattern.compile("[a-zA-Z]{1}");
            Matcher coincidencia = patron.matcher(msj);

            if (coincidencia.matches()) {

                dichas = this.getMensajeClientes(); //obtengo el array dicho

                //llamo al método devolver palabra para enviar al cliente las que ha acertado
                char descubiertas[] = devolverPalabra(dichas);

                this.setMensajeClientes(this.black + "Has descubierto las siguientes letras: \n"
                        + this.blue + Arrays.toString(descubiertas) + this.black + "\n¿quieres resolver?[si/no]");

                //muestro lo que lleva descubierto el hilo
                System.out.println("El " + Thread.currentThread().getName() + " ha dicho las siguientes letras: " + dichas);
                System.out.println("El " + Thread.currentThread().getName() + " ha descubierto las siguientes posiciones: " + Arrays.toString(descubiertas));

                msj = this.getMensajeClientes(); //recibo el "si" o "no"

                if (msj.equalsIgnoreCase("si")) { //si el cliente quiere resolver...

                    this.setMensajeClientes("Resuelva la palabra:");
                    respuesta = this.getMensajeClientes(); //obtengo la palabra

                    if (respuesta.equalsIgnoreCase(this.palabra)) { //si es la palabra, estado 4, win
                        estado = 4;
                        this.setMensajeClientes("¡GANASTE!");
                        --Server.numclientes;
                    } else { //si no lo es, seguimos probando
                        estado = 3;
                        this.setMensajeClientes("fallaste");

                    }
                } else if (msj.equalsIgnoreCase("no")) { //si no quiere resolver, seguimos probando
                    estado = 3;
                }

            } else { //esta parte creo que fallará y si no falla es milagro
                this.setMensajeClientes("no has indicado una letra como se te pidió");
                estado = 3;
            }
        } while (estado == 3);
        return estado;
    }

    /**
     * Método devolverPalabra. recoge la letra escrita por el usuario y si
     * coincide con alguna de la lista la descubre.
     *
     * @param descubiertas
     * @return devuelve un char array[] con las letras descubiertas
     */
    private char[] devolverPalabra(String descubiertas) {
        char temp;
        char descu[] = new char[palabra.length()]; //creo un array del tamaño de la palabra

        for (int i = 0; i < palabra.length(); i++) {

            for (int j = 0; j < descubiertas.length(); j++) {

                temp = descubiertas.charAt(j); //añado en una variable temporal el primer elemento de las descubiertas
                if (temp == palabra.charAt(i)) { //si ese elemento está en la palabra
                    descu[i] = temp; //añado ese elemento al array de char
                    break;
                } else if (temp != palabra.charAt(i)) { //si no coincide, pone asteriscos en las letras
                    descu[i] = '*';
                }
            }
        }
        return descu;
    }

    /**
     * Método configuracion. Obtiene la palabra dicha por el cliente y añade
     * cada letra a un ArrayList.
     *
     * @return int con el estado del diagrama.
     */
    private int configuracion() {

        Server.letras.clear(); //añadido, para borrar la lista de letras y volver a configurar

        int estado = 1;
        this.palabra = this.getMensajeClientes();
        this.palabra.chars().boxed().forEach(i -> this.letras.add(Character.toChars(i)[0]));
        System.out.println(this.green + "Juego configurado por " + Thread.currentThread().getName() + " la palabra es: " + palabra);
        return estado;
    }

    /**
     * Método logueo. Requiere al cliente un nombre de usuario. Si cumple el
     * patrón empezará a jugar. En el caso de que sea un usuario inválido
     * enviará advertencias, o si la palabra no está configurada.
     *
     * @return int con el estado del diagrama.
     */
    private int logueo() {
        int estado = 1;
        String msj;

        do {
            System.out.println(Thread.currentThread().getName() + " esperando autentificación");
            this.setMensajeClientes(this.blue + "Introduce el usuario (debe ser entre 8 y 15 caracteres alfanuméricos)" + this.reset);
            msj = this.getMensajeClientes();

            Pattern patron = Pattern.compile("[a-zA-Z0-9]{8,15}"); //creo el patrón
            Matcher coincide = patron.matcher(msj);

            if (coincide.matches()) {

                if (msj.equals("Administrador")) {
                    this.setMensajeClientes("Bienvenido " + msj + "!");
                    estado = 2;
                    this.setMensajeClientes(String.valueOf(estado));

                    System.out.println(this.cyan + "\tEl " + Thread.currentThread().getName() + " a entrado como: " + msj); //quitable

                } else if (this.palabra == null) {
                    this.setMensajeClientes("La palabra está sin actualizar");
                    estado = 1;
                    this.setMensajeClientes(String.valueOf(estado));

                } else {
                    this.setMensajeClientes(this.green + "usuario válido");
                    estado = 3;
                    this.setMensajeClientes(String.valueOf(estado));

                }
            } else if (msj.length() > 15 || msj.length() < 8) {
                estado = 1;
                this.setMensajeClientes(this.red + "usuario no válido");
                this.setMensajeClientes(String.valueOf(estado));
            }

        } while (estado == 1);

        return estado;
    }

    public static void main(String[] args) {

        new Boot();

    }

    /**
     * Método para recibir mensajes del cliente.
     *
     * @return devuelve la respuesta del cliente sobre lo que se le requiere.
     */
    public String getMensajeClientes() {
        String respuesta = "";
        try {
            this.flujoEntrada = new DataInputStream(this.cliente.getInputStream());
            // El servidor me envía un mensaje
            respuesta = this.flujoEntrada.readUTF();
        } catch (IOException e) {
            System.out.println("setFlujoEntrada " + e.getMessage());
        }
        return respuesta;
    }

    /**
     * Método para enviar mensajes al cliente.
     *
     * @param msj es el mensaje que se le envía al cliente
     */
    public void setMensajeClientes(String msj) {
        try {
            this.flujoSalida = new DataOutputStream(this.cliente.getOutputStream());
            this.flujoSalida.writeUTF(msj);
        } catch (IOException e) {
            System.out.println("setFlujoSalida " + e.getMessage());
        }
    }

}
