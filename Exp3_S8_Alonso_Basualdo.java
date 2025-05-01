// Falta agregar que se vean los ID en, eliminar una venta y eliminar una reserva

package exp3_s8_alonso_basualdo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Exp3_S8_Alonso_Basualdo {

    static String[] VIP = new String[5];   // V1-V5 ASIENTOS DISPONIBLES PARA VIP
    static String[] PLATEA = new String[10];
    static String[] GENERAL = new String[15];
    
    static Cliente[] clientes = new Cliente[50]; // arreglo para almacenar hasta 50 clientes
    static Venta[] ventas = new Venta[100]; // arreglo para almacenar hasta 50 ventas
    
    static ArrayList<Descuento> descuentos = new ArrayList<>();
    static ArrayList<Reserva> reservas = new ArrayList<>();

    static int contadorClientes = 0;
    static int contadorVentas = 0;
    static int contadorReservas = 0;
    static double totalIngresos = 0;

    static Scanner scanner = new Scanner(System.in);

    static void inicializadorAsientos() {
        for (int i = 0; i < VIP.length; i++) {
            VIP[i] = "V" + (i + 1);
        }
        for (int i = 0; i < PLATEA.length; i++) {
            PLATEA[i] = "P" + (i + 1);
        }
        for (int i = 0; i < GENERAL.length; i++) {
            GENERAL[i] = "G" + (i + 1);
        }
    }

    static void inicializarDescuentos() {
        descuentos.add(new Descuento("E", 0.10));
        descuentos.add(new Descuento("T", 0.15));
    }

    static void mostrarMenuPrincipal() {
        int op;
        do {
            System.out.println("\n--- TEATRO MORO ---");
            System.out.println("1. Vender entrada");
            System.out.println("2. Reservar entrada");
            System.out.println("3. Modificar reserva");
            System.out.println("4. Imprimir boleta");
            System.out.println("5. Eliminar una venta");
            System.out.println("6. Eliminar una reserva");
            System.out.println("7. Ver resumen");
            System.out.println("0. Salir");
            System.out.print("Seleccione opcion: ");
            while (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("Ingrese un numero valido: ");
            }
            op = scanner.nextInt();
            scanner.nextLine();
            switch (op) {
                case 1 ->venderEntrada();
                case 2 ->reservarEntrada();
                case 3 ->modificarReserva();
                case 4 ->imprimirBoleta();
                case 5 ->eliminarVenta();
                case 6 ->eliminarReserva();
                case 7 -> verResumen();
                case 0 ->
                    System.out.println("Gracias por visitar la pagina de Teatro Moro, hasta pronto!");
                default ->
                    System.out.println("Opcion invalida");
            }
        }while (op != 0);
    }
    static void venderEntrada(){
        System.out.println("Ingrese su nombre a registrar para la compra: ");
        String nombre = scanner.nextLine().trim();
        if (!validarTexto(nombre)){
            System.out.println("Nombre invalido");
            return;
        }
        System.out.println("Tipo Estudiante (E), Adulto Mayor (T) o General (G)?:  ");
        String tipo = scanner.nextLine().trim().toUpperCase();
        if(!tipo.matches("[ETG]")) { // si se ingresa una letra que no hace match con ETG no existe
            System.out.println("Tipo invalido o no existente");
            return;
        }
        Cliente clie = new Cliente(contadorClientes, nombre, tipo);
        clientes[contadorClientes++] = clie;
        
        int sec = seleccionSeccion();
        if (sec<1){
            return;
        }
        mostrarAsientosDisponibles(sec);
        
        System.out.println("Asiento a comprar: ");
        String asiento = scanner.nextLine().trim().toUpperCase();
        if (!validarAsiento(asiento,sec)){
            System.out.println("Asiento no disponible.");
            return;
        }
        Reserva r = buscarReserva(asiento);
        if ( r != null ){
            if (!r.esVigente()){
                reservas.remove(r);
                System.out.println("La reserva expiro.");
                return;                               
            }
            reservas.remove(r);
        }
        double precio = precioPorSeccion(asiento);
        double descuento = obtenerDescuento(tipo);
        double precioFinal = precio * (1 - descuento);
        
        marcarOcupado(asiento);
        Venta v = new Venta(contadorVentas, asiento, precio,descuento,precioFinal,clie.id);
        ventas[contadorVentas] = v;
        contadorVentas++;
        totalIngresos += precioFinal;
        
        System.out.println("Venta exitosa "+ nombre + ("| Asiento: "+ asiento + ("| Total : $"+precioFinal)));
    }
    
    static void reservarEntrada(){
        System.out.println("Ingrese su nombre para la reserva: ");
        String nombre = scanner.nextLine().trim().toUpperCase();
        if (!validarTexto(nombre)){
            System.out.println("Nombre invalido");
            return;
        }
        System.out.println("Tipo Estudiante (E), Adulto Mayor (T) o General (G)?:  ");
        String tipo = scanner.nextLine().trim().toUpperCase();
        if(!tipo.matches("[ETG]")) { // si se ingresa una letra que no hace match con ETG no existe
            System.out.println("Tipo invalido o no existente");
            return;
        }
        Cliente clie =new Cliente(contadorClientes, nombre, tipo);
        clientes[contadorClientes++]= clie;
        
        int sec = seleccionSeccion();
        if (sec < 1){
            return;
        }
        mostrarAsientosDisponibles(sec);
        System.out.println("Eliga el asiento que desea reservar: ");
        String asiento = scanner.nextLine().trim().toUpperCase();
        if (!validarAsiento(asiento, sec)){
            System.out.println("Asiento no disponible");
        }
        marcarOcupado(asiento);
        Reserva r = new Reserva(contadorReservas++, clie.id, asiento);
        reservas.add(r);
        System.out.println("Reserva exitosa (valida por 2 min");
        System.out.println("Id de la reserva: "+ r.idReserva);                                     
    }
    
    static void modificarReserva(){
        System.out.println("Ingrese el ID de la reserva que desea modificar");
        if (!scanner.hasNextInt()){
            System.out.println("ID invalido.");
            return;
        }
        int id = scanner.nextInt();
        scanner.nextLine();
        Reserva r = buscarReserva(id);
        if (r == null){
            System.out.println("Reserva no existe");
            return;
        }
        if (!r.esVigente()){
            System.out.println("Reserva caducada");
            reservas.remove(r);
            return;
        }
        System.out.println("Confirmar la compra?");
        if (scanner.nextLine().trim().equalsIgnoreCase("S")){
            Cliente clie = clientes[r.idCliente];
            double precio = precioPorSeccion(r.asiento);
            double descuento = obtenerDescuento(clie.tipo);
            double precioFinal = precio * ( 1 - descuento);
            Venta v = new Venta(contadorVentas, r.asiento,precio,descuento,precioFinal, clie.id);
            ventas[contadorVentas++]= v;
            totalIngresos+= precioFinal;
            reservas.remove(r);
            System.out.println("Reserva confirmada como venta");
        }
    }
    static void imprimirBoleta(){
        if (contadorVentas==0){
            System.out.println("No hay ventas registradas");
            return;
        }
        System.out.println("\n Ventas registradas");
        for (int i=0; i < contadorVentas; i++){
            Venta v = ventas[i];
            if (v != null){
                Cliente clie = clientes[v.idCliente];
                System.out.println("ID"+ i +"| Asiento: "+ v.asiento + "| Cliente: "+ clie.nombre);
            }
        }
        System.out.println("\n Ingrese ID de venta para imprimir boleta");
        if (!scanner.hasNextInt()){
            System.out.println("ID Invalido");
            scanner.next();
            return;
        }
        int id = scanner.nextInt();
        scanner.nextLine();
        if (id < 0 || id >= contadorVentas || ventas[id] == null){
            System.out.println("Venta no encontrada");
            return;
        }
        
        Venta v = ventas[id];
        Cliente clie = clientes[v.idCliente];
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        System.out.println("\n================== BOLETA DE COMPRA ==================");
        System.out.printf("| %-46s |\n", "TEATRO MORO - ¡Gracias por su compra!");
        System.out.println("------------------------------------------------------");
        System.out.printf("| %-20s: %-22s |\n", "Cliente", clie.nombre);
        System.out.printf("| %-20s: %-22s |\n", "Tipo de cliente", tipoCompleto(clie.tipo));
        System.out.printf("| %-20s: %-22s |\n", "Asiento", v.asiento);
        System.out.printf("| %-20s: $%-21.2f |\n", "Precio base", v.precio);
        System.out.printf("| %-20s: %-21s |\n", "Descuento aplicado", (int) (v.descuento * 100) + "%");
        System.out.printf("| %-20s: $%-21.2f |\n", "Total pagado", v.precioFinal);
        System.out.printf("| %-20s: %-22s |\n", "Fecha", LocalDateTime.now().format(fmt));
        System.out.println("======================================================");
    }
    
    static void eliminarVenta (){
        if (contadorVentas == 0){
            System.out.println("No hay ventas registradas");
            return;
        }
        System.out.println("Ingrese ID de venta para eliminar: ");
        if (!scanner.hasNextInt()){
            System.out.println("ID invalido");
            scanner.next();
            return;
        }
        int buscado = scanner.nextInt();
        scanner.nextLine();
        
        Venta v = null;
        int pos = -1;
        for (int i = 0; i < contadorVentas; i++){
            if (ventas[i] != null && ventas[i].idVenta == buscado){
                v = ventas[i];
                pos = i;
                break;
            }
        }
        if ( v == null){
            System.out.println("Venta no encontrada");
            return;
        }
        
        marcarDisponible(v.asiento);
        totalIngresos -= v.precioFinal;
        
        ventas[pos]=null;
        System.out.println("Venta eliminada");
        
    }
    
    static void eliminarReserva(){
        System.out.println("Ingrese ID de reserva que desea eliminar");
        if (!scanner.hasNextInt()){
            System.out.println("ID invalido");
            scanner.next();
            return;
        }
        int id =scanner.nextInt();
        scanner.nextLine();
        Reserva r = buscarReserva(id);
        if (r == null){
            System.out.println("Reserva no encontrada");
            return;
        }
        marcarDisponible(r.asiento);
        reservas.remove(r);
        System.out.println("Reserva eliminada con exito");
    }
    
    static void verResumen(){
        System.out.println("Total ventas: "+ contadorVentas);
        System.out.println("Reservas vigentes: "+ reservas.stream().filter(r -> r.esVigente()).count());
        System.out.println("Ingresos totales: "+ totalIngresos);
    }
    
    static boolean validarTexto (String txt){
        return txt != null && !txt.isEmpty() && txt.matches("[\\p{L} ]+");
    }
    
    static boolean validarAsiento (String asiento, int seccion){
        String[] zona = seccion == 1 ? VIP : seccion == 2 ? PLATEA : GENERAL;        
        for (String a : zona) {
            if (a.equals(asiento)){
                return true;
            }
        }
        return false;
    }
    
    static int seleccionSeccion(){
        System.out.println("Seccion (1 = VIP || 2 = PLATEA || 3 = GENERAL) : ");
        if (!scanner.hasNextInt()){
            System.out.println("Seccion selecionada invalida");
            scanner.next();
            return -1;
            
        }
        int f = scanner.nextInt();
        scanner.nextLine();
        if ( f < 1 || f > 3){
            System.out.println("Seccion invalida");
        }
        return f;
    }
    
    static void mostrarAsientosDisponibles(int f) {
        String[] zona = ( f == 1 ? VIP: f == 2 ? PLATEA:GENERAL);
        System.out.println("Disponibles: ");
        for (String a : zona){
            if (!a.equals("X")){
                System.out.println(a + " ");
            }
        }
        System.out.println();
    }
    
    static void marcarOcupado (String asiento){
        String[] zona1 = asiento.startsWith("V") ? VIP: asiento.startsWith("P") ? PLATEA:GENERAL;
        int idx = Integer.parseInt(asiento.substring(1)) - 1;
        zona1[idx] = "X";
    }
    static void marcarDisponible (String asiento){
        String[] zona = asiento.startsWith("V") ? VIP: asiento.startsWith("P") ? PLATEA:GENERAL;
        int idx = Integer.parseInt(asiento.substring(1)) - 1;
        zona[idx] = "X";
    }
    static double precioPorSeccion (String asiento){
        if (asiento.startsWith("V")){
            return 35000;
        }
        if (asiento.startsWith("P")){
            return 25000;
        }
        return 15000;
    }
    static double obtenerDescuento (String tipo){
        for (Descuento d : descuentos){
            if (d.tipoCliente.equals(tipo)){
                return d.porcentaje;
            }
        }
        return 0;
    }
    static String tipoCompleto (String tipo){
        return switch (tipo){
            case "E" -> "Estudiante";
            case "T" -> "Tercera edad";
            case "G" -> "General";
            default -> "Desconocido";
                
        };
    }
    static Reserva buscarReserva ( String asiento ){
        return reservas.stream().filter(r -> r.asiento.equals(asiento)).findFirst().orElse(null);
        
    }
    static Reserva buscarReserva ( int id ){
        return reservas.stream().filter(r -> r.idReserva == id).findFirst().orElse(null);
    }
    
    static class Cliente{
        
        int id;
        String nombre;
        String tipo;
        Cliente(int id, String n, String t){
            this.id = id;
            this.nombre = n;
            this.tipo = t;
        }
        
    }
    static class Venta {
        int idVenta;
        String asiento;
        double precio;
        double descuento;
        double precioFinal;
        int idCliente;
        
        Venta(int id, String a, double p, double d, double pf, int cid){
            this.idVenta = id;
            this.asiento = a;
            this.precio = p;
            this.descuento =d;
            this.precioFinal = pf;
            this.idCliente = cid;
        }
    }
    
    static class Descuento {
        String tipoCliente;
        double porcentaje;
        
        Descuento (String t, double p){
            this.tipoCliente = t;
            this.porcentaje = p;
        }
    }
    
    static class Reserva {
        int idReserva, idCliente;
        String asiento;
        LocalDateTime tiempo;
        
        Reserva ( int idRes, int idC, String a){
            this.idReserva = idRes;
            this.idCliente = idC;
            this.asiento = a;
            this.tiempo = LocalDateTime.now();
        }
        boolean esVigente(){
            return Duration.between(tiempo,LocalDateTime.now()).toMinutes()<3;
        }
    }
    
    public static void main(String[] args) {
        inicializadorAsientos();
        inicializarDescuentos();
        mostrarMenuPrincipal();
    }
}
