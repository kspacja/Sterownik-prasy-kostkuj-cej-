
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

    public static void CommandLn(){
    try
    {
        Runtime rt = Runtime.getRuntime();

        String command = "cmd";
        Process pr = rt.exec(command);

        BufferedReader processOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));

        String commandToSend = "notepad\n"+"exit\n";
        //commandToSend = "dir\n" + "exit\n";

        processInput.write(commandToSend);
        processInput.flush();

        int lineCounter = 0;
        while(true)
        {
            String line = processOutput.readLine();
            if(line == null) break;
            System.out.println(++lineCounter + ": " + line);
        }

        processInput.close();
        processOutput.close();
        pr.waitFor();
    }
        catch(Exception x)
        {
            x.printStackTrace();
        }
    }

}
