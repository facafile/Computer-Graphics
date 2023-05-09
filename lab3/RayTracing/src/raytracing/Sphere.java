package raytracing;

/**
 * <p>Title: Sphere</p>
 * <p>Description: </p>
 * Klasa predstavlja kuglu u prostoru. Nasljeduje apstraktnu klasu Object. Kugla
 * je odredena svojim polozajem, radijusom, bojom, parametrima materijala i
 * udjelima pojedninih zraka (osnovne, odbijene i lomljene).
 * <p>Copyright: Copyright (c) 2003</p>
 * @author Milenka Gadze, Miran Mosmondor
 * @version 1.1
 */


import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;

public class Sphere extends Object{

  private double radius;
  final double Epsilon = 0.0001;
  private double rayDistanceFromCenter;
  private Point IntersectionPoint;

  /**
   * Inicijalni konstruktor koji postavlja sve parametre kugle. Za prijenos
   * parametara koristi se pomocna klasa SphereParameters.
   *
   * @param sphereParameters parametri kugle
   */
  public Sphere(SphereParameters sphereParameters) {
    super(sphereParameters.getCenterPosition(), sphereParameters.getRaysContributions(), sphereParameters.getMaterialParameters(),sphereParameters.getN(), sphereParameters.getNi());
    this.radius=sphereParameters.getRadius();
  }

  /**
   * Metoda ispituje postojanje presjeka zrake ray s kuglom. Ako postoji presjek
   * postavlja tocku presjeka IntersectionPoint, te
   * vraca logicku vrijednost true.
   *
   * @param ray zraka za koju se ispituje postojanje presjeka sa kuglom
   * @return logicku vrijednost postojanja presjeka zrake s kuglom
   */
  public boolean intersection(Ray ray) {
        Vector pc = new Vector(ray.startingPoint, this.centerPosition);
        Double angle = ray.direction.getAngle(pc);

        if (angle - Math.PI / 2 > Epsilon)
            return  false;

        this.rayDistanceFromCenter = pc.getLength() * Math.sin(angle);

        if (this.rayDistanceFromCenter - this.radius > Epsilon)
            return false;

        Double pdLength = Math.sqrt(Math.pow(pc.getLength(), 2) - Math.pow(this.rayDistanceFromCenter, 2));
        Double closerIntersect = pdLength - Math.sqrt(Math.pow(this.radius, 2) - Math.pow(this.rayDistanceFromCenter, 2));
        this.IntersectionPoint = new Point(ray.startingPoint, ray.direction, closerIntersect);

        if (closerIntersect <= Epsilon) {
            closerIntersect = pdLength + Math.sqrt(Math.pow(this.radius, 2) - Math.pow(this.rayDistanceFromCenter, 2));
            this.IntersectionPoint = new Point(ray.startingPoint, ray.direction, closerIntersect);
        }

        return true;
  }

  /**
   * Vraca tocku presjeka kugle sa zrakom koja je bliza pocetnoj tocki zrake.
   *
   * @return tocka presjeka zrake s kuglom koja je bliza izvoru zrake
   */
  public Point getIntersectionPoint() {
    return IntersectionPoint;
  }

  /**
	* Vraca normalu na kugli u tocki point
	*
	* @param point na kojoj se racuna normala na kugli
	* @return normal vektor normale
	*/
  public Vector getNormal(Point point) {
	Vector norm =  new Vector(this.centerPosition, point);
    norm.normalize();
    return norm;
  }


}