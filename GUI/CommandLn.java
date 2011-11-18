
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kamil Zbigniew Kranc
 */
public class CommandLn {
public static String path="";
public static String fileName="";

    public static void setPath(String p){
        path = p;
    }

    public static void setFile(String f){
        fileName = f;
    }

    public static String createCommand(String silnik, int stopnie, int obroty, int unlimited, int moc){
        String command ="";
        if (stopnie!=0 & moc>0){
        command = "java "+fileName+" motor "+silnik+" direction=forward"+" power=" +Integer.toString(moc)+" degrees="+Integer.toString(stopnie);
        }
        if (stopnie!=0 & moc<0){
        command = "java "+fileName+" motor "+silnik+" direction=backwards"+" power=" +Integer.toString(moc*(-1))+" degrees="+Integer.toString(stopnie);
        }
        if (obroty!=0 & moc>0){
        command = "java "+fileName+" motor "+silnik+" direction=forward"+" power=" +Integer.toString(moc)+" rotations="+Integer.toString(obroty);
        }
        if (obroty!=0 & moc<0){
        command = "java "+fileName+" motor "+silnik+" direction=backwards"+" power=" +Integer.toString(moc*(-1))+" rotations="+Integer.toString(obroty);
        }
        if (unlimited==1){
        command = "java "+fileName+" motor "+silnik+" direction=forward"+" power=" +Integer.toString(moc)+" unlimited";
        }
        if (unlimited==-1){
        command = "java "+fileName+" motor "+silnik+" direction=backwards"+" power=" +Integer.toString(moc)+" unlimited";
        }
        if (moc==0){
        command = "java "+fileName+" motor "+silnik+" direction=stop";
        }
        return(command);

    }

    public static String SetCommand(int nrCzujnika, int rodzCzujnika){
        String command="";
        if(rodzCzujnika==1){
            command = "java "+fileName+" set-sensor "+nrCzujnika+" type=touch";            
        }
        if(rodzCzujnika==2){
            command = "java "+fileName+" set-sensor "+nrCzujnika+" type=sound";
        }
        if(rodzCzujnika==3){
            command = "java "+fileName+" set-sensor "+nrCzujnika+" type=light_ambient";
        }
        if(rodzCzujnika==4){
            command = "java "+fileName+" set-sensor "+nrCzujnika+" type=ultrasonic";
        }
        return(command);
    }

    public static String GetSensor(int nrCzujnika){
        String command="";        
        command = "java "+fileName+" get-sensor "+nrCzujnika+" -t 200";
        //command ="ipconfig";
        return command;
    }

    public static void SetSensor(int nrSensora, int typSensora){
        String wyjscie="";
    try
    {
        Runtime rt = Runtime.getRuntime();

        String commandToSend="";

        if(nrSensora>0)
        commandToSend = SetCommand(nrSensora, typSensora);

        System.out.println(commandToSend);
        //RABNAC CATCHA
        try{
        Process pr = Runtime.getRuntime().exec(commandToSend, null, new File(path));
        }
        catch(Exception e){
            System.out.println("Brak pośrednika");
        }

    }
        catch(Exception x)
        {
            x.printStackTrace();
        }
    }

    public static String CommandSensor(int nrSensora, int typSensora){
        String wyjscie="";
    try
    {
        Runtime rt = Runtime.getRuntime();

        String commandToSend="";        

        if(nrSensora>0)
        commandToSend = GetSensor(nrSensora);

        if(typSensora==-10){
            commandToSend = "java "+fileName+" reset-sensor-scaled "+nrSensora;
        }
        
        System.out.println(commandToSend);
        Process pr = null;
        
        //RABNAC CATCHA
        try{
        pr = Runtime.getRuntime().exec(commandToSend, null, new File(path));
        }
        catch(Exception e){
            System.out.println("Nie udało się nawiązać połączenia z pośrednikiem");
        }
        
        //początek sekcji odbioru odpowiedzi
        BufferedReader processOutput=null;
        try{
        processOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        }
        catch(Exception e){
            System.out.println("Nie udało się zainicjować strumienia zwracającego");
        }
        long startTime = System.currentTimeMillis();
        long current = startTime;
        String line = "";
        StringBuffer tymczasowy = new StringBuffer();
        while(current-startTime<300)
        {
            line = processOutput.readLine();
            if (line != null){
            tymczasowy.append("\n");
            tymczasowy.append(line);
            }
            if(line == null) current = System.currentTimeMillis();
            //if(line == null) break;
            //System.out.println("wynik: " + line);
        }
        wyjscie = tymczasowy.toString();
        processOutput.close();
        //koniec sekcji odbioru
        
    }
        catch(Exception x)
        {
            x.printStackTrace();
        }
        System.out.println("Wynik to: "+wyjscie);
        if(wyjscie.length()>0)
        return(wyjscie);
        else return("Brak wartosci");
    }

    public static void CommandLn(String silnik, int stopnie, int obroty, int unlimited, int moc){
    try
    {
        Runtime rt = Runtime.getRuntime();

        String commandToSend="";       

        
        if(stopnie!=0){
            commandToSend = createCommand(silnik, stopnie, obroty, unlimited, moc);            
        }
        else if(obroty!=0){
            commandToSend = createCommand(silnik, stopnie, obroty, unlimited, moc);            
        }
        //else if(czas!=0){
        //    commandToSend = createCommand(silnik, stopnie, obroty, czas, moc);
        else if(unlimited==1){
            commandToSend = createCommand(silnik, stopnie, obroty, unlimited, moc);
            }
        else if(unlimited==-1){
            commandToSend = createCommand(silnik, stopnie, obroty, unlimited, moc);
        }
        else commandToSend = createCommand(silnik, stopnie, obroty, unlimited, moc);       
        

        System.out.println(commandToSend);
        //RABNAC CATCHA
        try{
        Process pr = Runtime.getRuntime().exec(commandToSend, null, new File(path));        
        }
        catch(Exception e){
            System.out.println("Nie udało się nawiązać połączenia z pośrednikiem");
        }
        
    }
        catch(Exception x)
        {
            x.printStackTrace();
        }
    }    

}
