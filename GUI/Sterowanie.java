/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ja
 */
public class Sterowanie {
    public int silnik =0;
    public int stopnie =0;
    public int obroty = 0;
    public int unlimited =0;
    public int moc =0;

    public static String zmien(int x){
        String engine ="";
        switch(x){
            case 1: engine = "A"; break;
            case 2: engine = "B"; break;
            case 3: engine = "C"; break;            
        }
        return engine;
    }

    public static void makeObrot(int x, int y, int z, int q){
        String tryb ="";
        switch(z){
            case 0: tryb = "stopni"; CommandLn.CommandLn(zmien(x), y, 0, 0, q); break;
            case 1: tryb = "obrotów"; CommandLn.CommandLn(zmien(x), 0, y, 0, q);break;
            //case 2: tryb = "unlimited"; CommandLn.CommandLn(zmien(x), 0, 0, y, q); break;
        }
        if (y>0 & q>0) System.out.println("Obrót silnika "+zmien(x)+" w prawo o " + y+" "+tryb+" z mocą "+q);
        if (y>0 & q<0) System.out.println("Obrót silnika "+zmien(x)+" w lewo o " + y +" "+tryb+" z mocą "+q*(-1));
    }

    public static void leftTurn(int x, int q){
        if (q<0) q=q*(-1);
        System.out.println("Obrót silnika "+zmien(x)+" w lewo z mocą "+q);
        CommandLn.CommandLn(zmien(x), 0, 0, -1, q);
    }

     public static void rightTurn(int x, int q){
        if (q<0) q=q*(-1);
        System.out.println("Obrót silnika "+zmien(x)+" w prawo z mocą "+q);
        CommandLn.CommandLn(zmien(x), 0, 0, 1, q);
     }

     public static void engineStop(int x){
         int q=0;
         CommandLn.CommandLn(zmien(x), 0, 0, 0, q);
         System.out.println("Silnik zatrzymany "+x);         
     }    
}
