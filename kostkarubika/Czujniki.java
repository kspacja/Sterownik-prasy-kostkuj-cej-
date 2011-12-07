package kostkarubika;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kamil Kranc
 */
public class Czujniki {
    static int numerSensora = 0;
    //static int modeSensora = -1;    
    static int typSensora[][] = new int[5][5];

    //poczebne
    public static void setSensor(int i){
        numerSensora = i;
        System.out.println("Ustawiono sensor "+i);
    }

    public static int getSensor(){
        return(numerSensora);
    }
    //poczebne
    public static String readSensor(int i){
        setSensor(i);
        if(getSensor()>0){        
        CommandLn.SetSensor(getSensor(), getSensorType(getSensor()));
        String wynik = CommandLn.CommandSensor(getSensor(), getSensorType(getSensor()));
        System.out.println("Otrzymany wynik: "+wynik);
        return(wynik);
        }
        else return("Wybierz czujnik");
    }        
    //poczebne
    /*public static void setSensorType(int c, int i){
        if (c==1){
        switch(i){
            case 1: System.out.println("ustawiono typ touch");
                    typeSensora1 =1;
                    break;
            case 2: System.out.println("ustawiono typ sound");
                    typeSensora1 =2;
                    break;
            case 3: System.out.println("ustawiono typ light");
                    typeSensora1 =3;
                    break;
            case 4: System.out.println("ustawiono typ ultrasonic");
                    typeSensora1 =4;
                    break;
        }
        }
        if (c==2){
            switch(i){
            case 1: System.out.println("ustawiono typ touch");
                    typeSensora2 =1;
                    break;
            case 2: System.out.println("ustawiono typ sound");
                    typeSensora2 =2;
                    break;
            case 3: System.out.println("ustawiono typ light");
                    typeSensora2 =3;
                    break;
            case 4: System.out.println("ustawiono typ ultrasonic");
                    typeSensora2 =4;
                    break;
        }
        }
        if(c==3){
            switch(i){
            case 1: System.out.println("ustawiono typ touch");
                    typeSensora3 =1;
                    break;
            case 2: System.out.println("ustawiono typ sound");
                    typeSensora3 =2;
                    break;
            case 3: System.out.println("ustawiono typ light");
                    typeSensora3 =3;
                    break;
            case 4: System.out.println("ustawiono typ ultrasonic");
                    typeSensora3 =4;
                    break;
        }
        }
        if(c==4){
            switch(i){
            case 1: System.out.println("ustawiono typ touch");
                    typeSensora4 =1;
                    break;
            case 2: System.out.println("ustawiono typ sound");
                    typeSensora4 =2;
                    break;
            case 3: System.out.println("ustawiono typ light");
                    typeSensora4 =3;
                    break;
            case 4: System.out.println("ustawiono typ ultrasonic");
                    typeSensora4 =4;
                    break;
        }
        }
        CommandLn.SetSensor(getSensor(), i);

    }*/

    public static void setSensorType(int c, int i){
        typSensora[c][i] = typSensora[c][1];
        for(int x=1;x<5;x++){
            if(x!=i)
                typSensora[c][x]=typSensora[c][0];
        }
        CommandLn.SetSensor(c, i);
    }

    public static int getSensorType(int i){
        int typeSensora = 0;
        for(int c=1;c<5;c++){
            if (typSensora[i][c]==1) typeSensora=c;
        }
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
