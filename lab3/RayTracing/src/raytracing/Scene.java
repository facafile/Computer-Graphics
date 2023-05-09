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
import javax.naming.NameNotFoundException;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.sql.Ref;

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
    //ako je dubina>max. dubine, prekini funkciju i vrati crnu boju
    if (depth > MAXDEPTH)
      return new ColorVector(0, 0, 0);

    //nađi najbliži presjek zrake R sa scenom
    Sphere closest = null;

    for (Sphere curent : sphere) {
      if (curent.intersection(ray)) {
        if (closest == null || closest.getIntersectionPoint().getDistanceFrom(ray.getStartingPoint()) > curent.getIntersectionPoint().getDistanceFrom(ray.getStartingPoint()))
          closest = curent;
      }
    }

    //ako nema presjeka, prekini funkciju i vrati kao rezultat boju pozadine
    if (closest == null)
      return this.backroundColors;

    //izračunaj boju lokalnog osvjetljenja Clocal u točki presjeka
    Point intersectionPoint = closest.getIntersectionPoint();
    ColorVector local = new ColorVector(0 , 0, 0);
    Vector N = closest.getNormal(intersectionPoint);
    Vector L = new Vector(intersectionPoint, this.lightPosition);
    Vector R = L.getReflectedVector(N);
    Vector V = new Vector(intersectionPoint, ray.getStartingPoint());

    N.normalize();
    L.normalize();
    R.normalize();
    V.normalize();



    //ambientalno
    local = local.add(ambientLight.multiple(closest.getKa()));

    double nI = closest.getNi();
    double vn = V.dotProduct(N);
    if( vn < 0 ) {
      N = N.multiple( -1 );
      nI = 1./nI;
    }

    if(!this.shadow(intersectionPoint)){
      //difuzno i spekularno
      if (L.dotProduct(N) > 0){
        local = local.add(light.multiple(closest.getKd()).multiple(L.dotProduct(N)));
        local = local.add(light.multiple(closest.getKs()).multiple(Math.pow(R.dotProduct(V), closest.getN())));
      }


      //spekularno
      //if (R.dotProduct(V) > 0)
      // local = local.add(light.multiple(closest.getKs()).multiple(Math.pow(R.dotProduct(V), closest.getN())));
    }

    ColorVector lighting = local;

    //izračunaj odbijenu zraku Rrefl
    Ray reflected = new Ray(intersectionPoint, V.getReflectedVector(N));
    ColorVector refl = traceRay(reflected, depth + 1);

    lighting = lighting.add(refl.multiple(closest.getReflectionFactor()));

    //izračunaj refraktiranu zraku Rrefr
    Ray refracted = new Ray(intersectionPoint, V.getRefractedVector(N, nI));
    ColorVector refr = traceRay(refracted, depth + 1);

    lighting = lighting.add(refr.multiple(closest.getRefractionFactor()));

    lighting.correct();

    return lighting;
  }

}