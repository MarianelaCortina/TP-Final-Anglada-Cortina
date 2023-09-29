package juegoNave;

import java.awt.Color;
import java.awt.Graphics;

public class Disparo extends ElementoBasico {

    public Disparo(int posicionX, int posicionY, int velocidadX, int velocidadY, int ancho, int largo, Color color) {
        super(posicionX, posicionY, velocidadX, velocidadY, ancho, largo, color);
    }

    public void dibujarse(Graphics graphics) {
        graphics.setColor(getColor());
        graphics.fillRect(getPosicionX(), getPosicionY(), getAncho(), getLargo());
    }

}
