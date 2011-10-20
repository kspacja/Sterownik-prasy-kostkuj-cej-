/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ja
 */
public class Sterowanie {

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
            case 0: tryb = "stopni"; break;
            case 1: tryb = "obrotów"; break;
            case 2: tryb = "czas"; break;
        }
        if (y>0 & q>0) System.out.println("Obrót silnika "+zmien(x)+" w prawo o " + y+" "+tryb+" z mocą "+q);
        if (y>0 & q<0) System.out.println("Obrót silnika "+zmien(x)+" w lewo o " + y +" "+tryb+" z mocą "+q*(-1));
    }

    public static void leftTurn(int x, int q){
        System.out.println("Obrót silnika "+zmien(x)+" w lewo z mocą"+q);
    }

     public static void rightTurn(int x, int q){
        System.out.println("Obrót silnika "+zmien(x)+" w prawo z mocą"+q);
     }

     public static void engineStop(){
         System.out.println("Silnik zatrzymany");
     }    
}
