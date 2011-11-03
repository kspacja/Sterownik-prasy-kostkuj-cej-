
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ja
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

    public static String createCommand(String silnik, int stopnie, int obroty, int czas, int moc){
        String command ="";
        if (stopnie!=0 & moc>0){
        command = "java -cp \""+path+"\" "+fileName +" motor "+silnik+" direction=forward"+" power=" +Integer.toString(moc)+" degrees="+Integer.toString(stopnie);
        }
        if (stopnie!=0 & moc<0){
        command = "java -cp \""+path+"\" "+fileName +" motor "+silnik+" direction=backward"+" power=" +Integer.toString(moc*(-1))+" degrees="+Integer.toString(stopnie);
        }
        if (obroty!=0 & moc>0){
        command = "java -cp \""+path+"\" "+fileName +" motor "+silnik+" direction=forward"+" power=" +Integer.toString(moc)+" rotations="+Integer.toString(obroty);
        }
        if (obroty!=0 & moc<0){
        command = "java -cp \""+path+"\" "+fileName +" motor "+silnik+" direction=backward"+" power=" +Integer.toString(moc*(-1))+" rotations="+Integer.toString(obroty);
        }        
        return(command);

    }

    
    public static void CommandLn(String silnik, int stopnie, int obroty, int czas, int moc){
    try
    {
        Runtime rt = Runtime.getRuntime();

        String commandToSend;        

        //BufferedReader processOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        //BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
        
        //Decyzja o trybie
        if(stopnie!=0){
            commandToSend = createCommand(silnik, stopnie, obroty, czas, moc);
        }
        else if(obroty!=0){
            commandToSend = createCommand(silnik, stopnie, obroty, czas, moc);
        }
        else if(czas!=0){
            commandToSend = createCommand(silnik, stopnie, obroty, czas, moc);
        }
        else
        commandToSend = "notepad\n"+path+"\n"+"exit\n";
        //commandToSend = "dir\n" + "exit\n";

        System.out.println(commandToSend);
        Process pr = Runtime.getRuntime().exec(commandToSend+"\n");
        //processInput.write(commandToSend);
        //processInput.flush();

        //int lineCounter = 0;
        //while(true){
        //    String line = processOutput.readLine();
        //    if(line == null) break;
        //    System.out.println(++lineCounter + ": " + line);
        //}

        //processInput.close();
        //processOutput.close();


        //pr.waitFor();
    }
        catch(Exception x)
        {
            x.printStackTrace();
        }
    }    

}
