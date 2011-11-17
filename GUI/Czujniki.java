/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kamil Kranc
 */
public class Czujniki {
    static int numerSensora = -1;
    //static int modeSensora = -1;
    static int typeSensora = -1;

    //poczebne
    public static void setSensor(int i){
        numerSensora = i;
        System.out.println("Ustawiono sensor "+i);
    }

    public static int getSensor(){
        return(numerSensora);
    }
    //poczebne
    public static String readSensor(){
        if(getSensor()>0){
        String wynik = CommandLn.CommandSensor(getSensor(), getSensorType());
        System.out.println("Otrzymany wynik: "+wynik);
        return(wynik);
        }
        else return("Wybierz czujnik");
    }        
    //poczebne
    public static void setSensorType(int i){
        switch(i){
            case 1: System.out.println("ustawiono typ touch");
                    typeSensora =1;
                    break;
            case 2: System.out.println("ustawiono typ sound");
                    typeSensora =2;
                    break;
            case 3: System.out.println("ustawiono typ light");
                    typeSensora =3;
                    break;
            case 4: System.out.println("ustawiono typ ultrasonic");
                    typeSensora =4;
                    break;
        }
        CommandLn.SetSensor(getSensor(), i);
    }

    public static int getSensorType(){
        return(typeSensora);
    }

    /*niepoczebne
    public static void setSensorMode(int i){
        switch(i){
            case 1: System.out.println("ustawiono tryb bool");
                    modeSensora = i;
                    break;
            case 2: System.out.println("ustawiono tryb switch");
                    modeSensora = i;
                    break;
            case 3: System.out.println("ustawiono tryb periodic");
                    modeSensora = i;
                    break;
        }
    }

    public static int getSensorMode(){
        return(modeSensora);
    }*/

    public static void resetSensor(int i){
        CommandLn.CommandSensor(i, -10);
        System.out.println("Sensor nr. "+i+" zresetowany");
    }
}
