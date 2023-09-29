package juegoNave;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import juegoNave.ElementoBasico;
import juegoNave.Enemigo;
import juegoNave.EnemigoCuadrado;
import juegoNave.EnemigoRedondo;
import juegoNave.Nave;
import juegoNave.PantallaImagen;
import juegoNave.PantallaPerdedor;
//import juegoNave.Pelota;
import juegoNave.Puntaje;
import juegoNave.Sonidos;
import juegoNave.Vidas;

public class Juego extends JPanel implements KeyListener, Runnable {
	
    private final static int PANTALLA_INICIO = 1;
    private final static int PANTALLA_JUEGO = 2;
    private final static int PANTALLA_PERDEDOR = 3;
    private final static int PANTALLA_GANADOR = 4;
    private static final long serialVersionUID = 1L;
    private int anchoJuego;
    private int largoJuego;
    private int tiempoDeEsperaEntreActualizaciones;
    private List<ElementoBasico> disparos;
    private ElementoBasico nave;
    private Puntaje puntaje;
    private Vidas vidas;
    private List<Enemigo> enemigos;
    private Sonidos sonidos;
    private int pantallaActual;
    private int enemigosPorLinea;
    private int filasDeEnemigos;
    private int cantidadVidas;
    private long contadorVelocidadEnemigos = 0;
    private PantallaImagen portada;
    private PantallaImagen pantallaGanador;
    private PantallaImagen pantallaEsperar;
    private PantallaPerdedor pantallaPerdedor;

    public Juego(int anchoJuego, int largoJuego, int tiempoDeEsperaEntreActualizaciones, int enemigosPorLinea,
            int filasDeEnemigos, int vidas) {
        this.pantallaActual = PANTALLA_INICIO;
        this.anchoJuego = anchoJuego;
        this.largoJuego = largoJuego;
        createDisparo();
        this.nave = new Nave(30, largoJuego - 20, 0, 0, 80, 20, Color.GRAY);
        this.enemigos = new ArrayList<Enemigo>();
        this.vidas = new Vidas(10, 45, new Font("Arial", 8, 20), Color.white, vidas);
        this.tiempoDeEsperaEntreActualizaciones = tiempoDeEsperaEntreActualizaciones;
        this.enemigosPorLinea = enemigosPorLinea;
        this.filasDeEnemigos = filasDeEnemigos;
        this.cantidadVidas = vidas;
        this.portada = new PantallaImagen(anchoJuego, largoJuego, "imagenes/portada.png");
        this.pantallaGanador = new PantallaImagen(anchoJuego, largoJuego, "imagenes/ganaste.png");
        this.pantallaEsperar = new PantallaImagen(anchoJuego, largoJuego, "imagenes/esperar.png");
        cargarSonidos();
        this.sonidos.repetirSonido("background");
        inicializarJuego();
    }

    private void inicializarJuego() {
        this.enemigos.clear();
        this.pantallaPerdedor = null;
        this.vidas = new Vidas(10, 45, new Font("Arial", 8, 20), Color.white, cantidadVidas);
        this.puntaje = new Puntaje(10, 20, new Font("Arial", 8, 20), Color.white);
        agregarEnemigos(enemigosPorLinea, filasDeEnemigos);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(anchoJuego, largoJuego);
    }

    /*
     * Actualizar la actualizacion y el dibujado del juego de esta forma no es
     * recomendable dado que tendra distintas velocidades en distinto hardware. Se
     * hizo asi por simplicidad para facilitar el aprendizaje dado que lo
     * recomendado es separar la parte de dibujado de la de actualizacion y usar
     * interpolation
     */
    
    //@Override
    public void run() {
        while (true) {
            if (pantallaActual == PANTALLA_JUEGO) {
                actualizarJuego();
                incrementarVelocidadEnemigos();     
            }
            dibujarJuego();
            esperar(tiempoDeEsperaEntreActualizaciones);
        }
    }

   // @Override
    public void keyPressed(KeyEvent arg0) {

        if (pantallaActual == PANTALLA_INICIO) {
            inicializarJuego();
            pantallaActual = PANTALLA_JUEGO;
        }

        if (pantallaActual == PANTALLA_PERDEDOR || pantallaActual == PANTALLA_GANADOR) {
            pantallaActual = PANTALLA_INICIO;
        }

        if (pantallaActual == PANTALLA_JUEGO) {

            // si mantengo apretada la tecla de la derecha se asigna velocidad 1 a la nave
            if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
                nave.setVelocidadX(1);
            }

            // si mantengo apretada la tecla de la izquierda se asigna velocidad -1 a la
            // nave
            if (arg0.getKeyCode() == KeyEvent.VK_LEFT) {
                nave.setVelocidadX(-1);
            }
        }
    }

   // @Override
    public void keyReleased(KeyEvent arg0) {
        // si suelto la tecla 39 o la 37 se asigna velocidad 0 a la nave
        if (arg0.getKeyCode() == KeyEvent.VK_RIGHT || arg0.getKeyCode() == KeyEvent.VK_LEFT) {
            nave.setVelocidadX(0);
        }
          //Al presionar la tecla espaciadora, se crean 3 proyectiles que se disparan de  la nave.
        if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
        	disparos.add(new Disparo((nave.getPosicionX())+16, largoJuego - 50,0,-5,6,12,Color.orange));
        	disparos.add(new Disparo((nave.getPosicionX())+36, largoJuego - 50,0,-5,6,12,Color.orange));
        	disparos.add(new Disparo((nave.getPosicionX())+52, largoJuego - 50,0,-5,6,12,Color.orange));
        }
    }

    //@Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    // Este metodo se llama cuando se hace un this.repaint() automaticamente
    // Aca se dibujan a todos los elementos, para ello cada elemento implementa el
    // metodo dibujarse
    protected void paintComponent(Graphics g) {
        this.limpiarPantalla(g);
        if (pantallaActual == PANTALLA_INICIO) {
            dibujarInicioJuego(g);
        }
        if (pantallaActual == PANTALLA_PERDEDOR) {
            if (this.pantallaPerdedor == null) {
                this.pantallaPerdedor = new PantallaPerdedor(anchoJuego, largoJuego, "imagenes/perdiste.png", this.puntaje.getPuntaje());
            }
            pantallaPerdedor.dibujarse(g);
        }
        if (pantallaActual == PANTALLA_GANADOR) {
            pantallaGanador.dibujarse(g);
        }
        if (pantallaActual == PANTALLA_JUEGO) {
        	dibujarDisparos(g);	
            nave.dibujarse(g);
            puntaje.dibujarse(g);
            vidas.dibujarse(g);
            //disparo.dibujarse(g);
            dibujarEnemigos(g);
        }
    }

    // En este metodo se actualiza el estado de todos los elementos del juego
    private void actualizarJuego() {
        verificarEstadoAmbiente();
        nave.moverse();
        for(ElementoBasico disparo : disparos) {
        	disparo.moverse();
        }
        moverEnemigos();
    }

    private void dibujarJuego() {
        this.repaint();
    }

    public void agregarEnemigo(Enemigo enemigo) {
        this.enemigos.add(enemigo);
    }

    private ElementoBasico createDisparo() {
        disparos = new ArrayList<ElementoBasico>();
        return nave;
    } 
    
    private void dibujarDisparos(Graphics graphics) {
    	for (ElementoBasico disparo : disparos) {
    		disparo.dibujarse(graphics);
    	}
    }
    // En ese metodo se cargan los sonidos que estan es src/main/resources
    private void cargarSonidos() {
        try {
            sonidos = new Sonidos();
            sonidos.agregarSonido("toc", "sonidos/toc.wav");
            sonidos.agregarSonido("tic", "sonidos/tic.wav");
            sonidos.agregarSonido("muerte", "sonidos/muerte.wav");
            sonidos.agregarSonido("background", "sonidos/background.wav");
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private void dibujarInicioJuego(Graphics g) {
        portada.dibujarse(g);
    }

    // se hace una iteracion de todos los enemigos cargados en la lista de enemigos
    // y se le dice a cada uno que ejecute el metodo moverse().
    // moverse() actualiza la posicionX y posicionY del elemento en base a la
    // direccion/velocidad que tenia para X e Y
    private void moverEnemigos() {
        for (Enemigo enemigo : enemigos) {
            enemigo.moverse();
        }
    }

    // Se hace una iteracion en la lista de enemigos y se ejecuta el metodo
    // dibujarse()
    private void dibujarEnemigos(Graphics g) {
        for (Enemigo enemigo : enemigos) {
            enemigo.dibujarse(g);
        }
    }

    // En este metodo verifico las colisiones, los rebotes de la pelota contra las
    // paredes, la colision entre enemigos y el fin de juego
    private void verificarEstadoAmbiente() {
    	verificarReboteEnemigosContraParedesLaterales();
        verificarReboteEntreEnemigos();
        verificarColisionEntreEnemigoYdisparos();
        verificarColisionEntreNaveYenemigos();
        verificarFinDeJuego();
    }

    // Se iteran todos los enemigos y se verifica para cada enemigo si hay colision
    // con cada enemigo. Si hay colision se ejecuta el metodo rebotarEnEjeX() del
    // enemigo esto hace que el enemigo cambie de direccion en el eje X
    private void verificarReboteEntreEnemigos() {
        for (Enemigo enemigo1 : enemigos) {
            for (Enemigo enemigo2 : enemigos) {
                if (enemigo1 != enemigo2 && enemigo1.hayColision(enemigo2)) {
                    enemigo1.rebotarEnEjeX();
                }
            }
        }
    }
    
    private void incrementarVelocidadEnemigos() {
    	contadorVelocidadEnemigos++;
        if (contadorVelocidadEnemigos >= 1000) {
        	System.out.println("actualizando velocidad enemigos");
        	contadorVelocidadEnemigos = 0;
        	for (Enemigo enemigo : enemigos) {
        		enemigo.setVelocidadY(enemigo.getVelocidadY()+0.01);
        	}
        }
        
    }
    
    // Este método verifica que si hay colisión entre un disparo y un enemigo
    // suma 1 punto
    private void verificarColisionEntreEnemigoYdisparos() {
    	for (ElementoBasico disparo : disparos) {
    		Iterator<Enemigo> iterador = enemigos.iterator();
    		while (iterador.hasNext()) {
    			Enemigo enemigo = iterador.next();
    			if(enemigo.hayColision(disparo)) {
    				iterador.remove();
    				disparo.setPosicionY(largoJuego-50);
    				puntaje.sumarPunto();
    				sonidos.tocarSonido("tic");
    			}
    		}
    	}
    }
    

    private void verificarColisionEntreNaveYenemigos() {
    	for (Enemigo enemigo : enemigos) {
    			if(enemigo.hayColision(nave)) {
    				vidas.perderVida();
    				sonidos.tocarSonido("muerte");  			
    		}
    	}
    }
    

    // se verifica si hay colision de cada enemigo contra las paredes laterales, si
    // hay colision se cambia la direccion del enemigo en el eje X
    private void verificarReboteEnemigosContraParedesLaterales() {
        for (Enemigo enemigo : enemigos) {
            if (enemigo.getPosicionX() <= 0 || enemigo.getPosicionX() + enemigo.getAncho() >= anchoJuego) {
                enemigo.rebotarEnEjeX();
            }
        }
    }
    

    // Se verifica si la cantidad de enemigos es 0 o si la cantidad de vidas es 0
    // para parar el juego
    private void verificarFinDeJuego() {

        if (vidas.getVidas() == 0) {
            pantallaActual = PANTALLA_PERDEDOR;
        }

        if (enemigos.size() == 0) {
            pantallaActual = PANTALLA_GANADOR;
        }
    }

    // Se verifica si la pelota toca el piso, si es asi se pierde una vida, se crea
    // una nueva pelota, se toca el sonido muerte y se muestra la pantalla perdiste
    // y se esperan 5 segundos
    /*
    private void verificarSiPelotaTocaElPiso() {
        if (pelota.getPosicionY() + pelota.getLargo() >= largoJuego) {
            vidas.perderVida();
            pelota = createPelota();
            sonidos.tocarSonido("muerte");
            pantallaEsperar.dibujarse(this.getGraphics());
            esperar(5000);
        }
    }*/

    
    /*private void verificarSiEnemigoTocaElPiso() {
    	for (Enemigo enemigo : enemigos) {
    		if (enemigo.getPosicionY() <= 0 || enemigo.getPosicionX() + enemigo.getAncho() >= anchoJuego) {
    			enemigo.rebotarEnEjeY();
    	     }
    	}
    		 	 
    }*/
    
    /*private void verificarSiEnemigoTocaElPiso() {
    	for (Enemigo enemigo : enemigos) {
    		if (enemigo.getPosicionY() <= 0 || enemigo.getPosicionY() + enemigo.getAncho() >= largoJuego) {
    			enemigo.rebotarEnEjeY();
    			vidas.perderVida();
    			disparo = createDisparo();
    		    //enemigo.dibujarse(getGraphics());
    			//agregarEnemigos(enemigosPorLinea, filasDeEnemigos);
    			//enemigos.add(enemigo);
    			//agregarEnemigo(enemigo);
    			//enemigo = agregarEnemigos(enemigosPorLinea, filasDeEnemigos);
    			sonidos.tocarSonido("muerte");
                pantallaEsperar.dibujarse(this.getGraphics());
                esperar(1000);      
                //enemigo.dibujarse(getGraphics());
    	     }
    	}
    		 	 
    }*/
   

    // metodo para limpiar la pantalla
    private void limpiarPantalla(Graphics graphics) {
        graphics.setColor(Color.blue);
        graphics.fillRect(0, 0, anchoJuego, largoJuego);
    }

    // metodo para esperar una cantidad de milisegundos
    private void esperar(int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private void agregarEnemigos(int enemigosPorLinea, int filasDeEnemigos) {
        for (int x = 1; x < enemigosPorLinea; x++) {
            for (int y = 1; y < filasDeEnemigos; y++) {
                Color color = new Color(new Random().nextInt(255), new Random().nextInt(255),
                        new Random().nextInt(255));
                // Si x es multiplo de 2 agrega un enemigo redondo
                if (x % 2 == 0) {
                    agregarEnemigo(new EnemigoRedondo(50 + x * 60, 60 + y * 30, 0.1, 0.05, 20, 20, color));
                    // si x es multiplo de 3 agrega un enemigo cuadrado
                } else if (x % 1 == 0) {
                    agregarEnemigo(new EnemigoCuadrado(50 + x * 60, 60 + y * 30, -0.1, 0.05, 20, 20, color));
                    // de lo contrario se agrega un enemigo imagen
                } 

            }
        }
    }
	

}
