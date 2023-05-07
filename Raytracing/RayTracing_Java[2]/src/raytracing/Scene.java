package raytracing;

/**
 * <p>Title: Scene</p>
 * <p>Description: </p>
 * Klasa predstvlja scenu kod modela crtanja slike pomocu ray tracinga. Sastoji
 * se od izvora svjetlosti i konacnog broja objekata.
 * <p>Copyright: Copyright (c) 2003</p>
 * @author Milenka Gadze, Miran Mosmondor
 * @version 1.1
 */

import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;

public class Scene {

  final int MAXDEPTH=5; //maksimalna dubina rekurzije
  private int numberOfObjects;
  private Sphere[] sphere;
  private Point lightPosition;
  private ColorVector backroundColors=new ColorVector(0, 0, 0);
  private ColorVector light=new ColorVector((float)1 , (float)1,(float)1);
  private ColorVector ambientLight=new ColorVector((float)0.5, (float)0.5, (float)0.5);

  /**
   * Inicijalni konstruktor koji postavlja poziciju svijetla i parametre svih
   * objekata u sceni.
   *
   * @param lightPosition pozicija svijetla
   * @param numberOfObjects broj objekata u sceni
   * @param sphereParameters parametri svih kugli
   */
  public Scene(Point lightPosition, int numberOfObjects, SphereParameters[] sphereParameters) {
    this.lightPosition=lightPosition;
    this.numberOfObjects=numberOfObjects;
    sphere=new Sphere[numberOfObjects];
    for (int i=0; i<numberOfObjects; i++) {
      sphere[i]=new Sphere(sphereParameters[i]);
    }
  }

  /**
   * Metoda provjerava da li postoji sjena na tocki presjeka. Vraca true ako
   * se zraka od mjesta presjeka prema izvoru svjetlosti sjece s nekim objektom.
   *
   * @param intersection tocka presjeka
   * @return true ako postoji sjena u tocki presjeka, false ako ne postoji
   */
  private boolean shadow(Point intersection) {
    Ray returning = new Ray(intersection, this.lightPosition);
    for (Sphere sp : sphere){
      if (sp.intersection(returning))
        return true;
    }
    return false;
  }

  /**
   * Metoda koja pomocu pracenja zrake racuna boju u tocki presjeka. Racuna se
   * osvjetljenje u tocki presjeka te se zbraja s doprinosima osvjetljenja koje
   * donosi reflektirana i refraktirana zraka.
   *
   * @param ray pracena zraka
   * @param depth dubina rekurzije
   * @return vektor boje u tocki presjeka
   */
  public ColorVector traceRay(Ray ray, int depth) {
    if (depth > MAXDEPTH)
      return new ColorVector(0,0,0);
    Sphere closest = null;
    for (Sphere sp : sphere){
      if (sp.intersection(ray)) {
        if (closest == null) {
          closest = sp;
        } else {
          if (ray.getStartingPoint().getDistanceFrom(closest.getIntersectionPoint()) > ray.getStartingPoint().getDistanceFrom(sp.getIntersectionPoint()))
            closest = sp;
        }
      }

    }
    if (closest == null)
      return this.backroundColors;

    Vector N = closest.getNormal(closest.getIntersectionPoint());
    Vector L = new Vector(closest.getIntersectionPoint(), this.lightPosition);
    Vector V = ray.direction.multiple(-1);
    Vector R = L.getReflectedVector(N);
    N.normalize();
    L.normalize();
    V.normalize();
    R.normalize();

    ColorVector lighting = new ColorVector(ambientLight.getRed(),ambientLight.getGreen(),ambientLight.getBlue()).multiple(closest.getKa());
    double ni = closest.getNi();	 //ako sam unutar kugle, stavljam reciproènu vrijednost za index loma, da bude suprotan smjer
    double vn = V.dotProduct( N );
    if( vn < 0 ) {
      //N = N.multiple( -1 );
      ni = 1./ni;
    }
    if (!this.shadow(closest.getIntersectionPoint())) {
      if (L.dotProduct(N) > 0)
        lighting = lighting.add(this.light.multiple(closest.getKd()).multiple(L.dotProduct(N)));
      if (R.dotProduct(V) > 0){
        //ako je umnozak RxV veæi od 0 i ako nije u sjeni racunan spekularno osvjetljenje
          lighting = lighting.add(light.multiple(closest.getKs()).multiple(Math.pow(R.dotProduct(V), closest.getN()))); //dodajen spekularnu komponentu na color vektor

      }
        //lighting = lighting.add(this.light.multiple(closest.getKs()).multiple(Math.pow(R.dotProduct(V), closest.getN())));
    }
    Vector ReflektiraniV = V.getReflectedVector(N);
    ReflektiraniV.normalize();
    ColorVector Crefl = traceRay(new Ray(closest.getIntersectionPoint(), ReflektiraniV), depth + 1);
    lighting =lighting.add(Crefl.multiple(closest.getReflectionFactor()));
    lighting.correct();
    return lighting;
  }
}