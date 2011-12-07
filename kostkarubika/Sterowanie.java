package kostkarubika;

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
    public static String tablicaKolorow[][] = new String[3][3];

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
        CommandLn.resetMotor(zmien(x));
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
        try{
        CommandLn.resetMotor(zmien(x));
        Thread.currentThread().sleep(200);
        if (q<0) q=q*(-1);
        System.out.println("Obrót silnika "+zmien(x)+" w lewo z mocą "+q);
        CommandLn.CommandLn(zmien(x), 0, 0, -1, q);
        Thread.currentThread().sleep(200);
        } catch (Exception e) {

        }
    }

     public static void rightTurn(int x, int q){
         try{
         CommandLn.resetMotor(zmien(x));
        if (q<0) q=q*(-1);
        System.out.println("Obrót silnika "+zmien(x)+" w prawo z mocą "+q);
        CommandLn.CommandLn(zmien(x), 0, 0, 1, q);
         }catch(Exception e){
             
         }
     }

     public static void engineStop(int x){
         int q=0;
         try{
         CommandLn.CommandLn(zmien(x), 0, 0, 0, q);
         Thread.currentThread().sleep(200);
         System.out.println("Silnik zatrzymany "+x);
         CommandLn.resetMotor(zmien(x));
         Thread.currentThread().sleep(200);
         }catch(Exception e){

         }
     }

     //PRZEWROCENIE KOSTKI
     public static void FlipOver(){
         try{
         Thread actual = Thread.currentThread();
         CommandLn.CommandLn(zmien(2), 0, 9, 0, 65);
         actual.sleep(1000);
         CommandLn.CommandLn(zmien(2), 0, 0, -1, 10);
         actual.sleep(2000);
         engineStop(2);

         }catch(Exception c){
             System.out.println("Ups");
         }
     }

     //wczytanie kolorow gornej scianki
     public static String[][] ReadColor(){
         reset();
         Czujniki.setSensorType(1, 3);
         try{
         //czujnik koloru maksymalnie w lewo
         makeObrot(3,180,0,-20);
         Thread.currentThread().sleep(1500);
         //czujnik koloru nad kostkę
         makeObrot(3,90,0,20);
         Thread.currentThread().sleep(800);
         //maksymalny obrót kostką w lewo
         leftTurn(1,25);
         Thread.currentThread().sleep(2000);
         engineStop(1);
         //sprawdzenie koloru środka 1
         tablicaKolorow[1][1] = Srodek();
         //sprawdzenie koloru skrajnego punktu 2
         tablicaKolorow[0][0] = UpLeft();
         //sprawdzenie kolejnego koloru 3
         tablicaKolorow[0][1] = oneThird();
         //sprawdzenie koloru 4
         tablicaKolorow[0][2] = twoThird();
         //sprawdzenie koloru 5
         tablicaKolorow[1][2] = oneThird();
         //sprawdzenie koloru 6
         tablicaKolorow[2][2] = twoThird();
         //sprawdzenie koloru 7
         tablicaKolorow[2][1] = oneThird();
         //sprawdzenie koloru 8
         tablicaKolorow[2][0] = twoThird();                  
         //ramie z czujnikiem maksymalnie w lewo
         leftTurn(3,20);
         Thread.currentThread().sleep(1200);
         engineStop(1);
         //sprawdzanie koloru 9
         tablicaKolorow[1][0] = MiddleLeft();
         //obrót kostki do punktu wyjsciowego
         leftTurn(1,20);
         Thread.currentThread().sleep(1200);
         engineStop(1);
         //powrot czujnika
         leftTurn(3,20);
         Thread.currentThread().sleep(1500);
         engineStop(3);
         }catch(Exception e){

         }
         return tablicaKolorow;
     }

     //ZCZYTANIE SRODKOWEGO KOLORU
     public static String Srodek(){
         String color ="";
         try{
         leftTurn(3,20);
         Thread.currentThread().sleep(1500);
         engineStop(3);
         Thread.currentThread().sleep(200);
         makeObrot(3, 130, 0, 20);
         Thread.currentThread().sleep(1200);         
         color = Czujniki.readSensor(1);
         Thread.currentThread().sleep(400);
         System.out.print("");
         }catch(Exception e){

         }
         return color;
     }

     public static String UpLeft(){
         String color ="";
         try{ 
         
         leftTurn(3,20);
         Thread.currentThread().sleep(1500);
         engineStop(3);
         Thread.currentThread().sleep(200);
         makeObrot(3, 162, 0, 20);
         Thread.currentThread().sleep(1500);
         color = Czujniki.readSensor(1);
         Thread.currentThread().sleep(400);
         System.out.print("");
         }catch(Exception e){

         }
         return color;
     }

     //ZCZYTANIE KOLORU PIERWSZEJ KOLUMNY DRUGIEGO RZEDU
     public static String MiddleLeft(){
         String color ="";
         CommandLn.resetMotor("C");
         try{
             //ramie z czujnikiem nad ostatni kolor
         Thread.currentThread().sleep(200);
         makeObrot(3,100,0,20);
         Thread.currentThread().sleep(1200);
         //obrot kostki na wlasciwa pozycje
         makeObrot(1,145,0,-20);
         Thread.currentThread().sleep(1400);
         //odczyt wartosci
         color = Czujniki.readSensor(1);
         Thread.currentThread().sleep(400);

         }catch(Exception e){

         }
         return color;
     }

     //OBROT KOSTKI O 33 STOPNIE I ZCZYTANIE KOLORU
     public static String oneThird(){
         String color="";
         try{
         makeObrot(1, 34, 0, 20);
         Thread.currentThread().sleep(600);
         color = Czujniki.readSensor(1);
         Thread.currentThread().sleep(400);
         System.out.print("");
         }catch(Exception e){

         }
         return color;
     }

     //OBROT O 55 STOPNI I ZCZYTANIE KOLORU
     public static String twoThird(){
         String color="";
         try{
         makeObrot(1, 55, 0, 20);
         Thread.currentThread().sleep(600);
         color = Czujniki.readSensor(1);
         Thread.currentThread().sleep(400);         
         }catch(Exception e){

         }
         return color;
     }

     //ZMIANA ORIENTACJI KOSTKI (DOSTEP DO DWOCH 'BOCZNYCH' SCIAN)
     public static void orientacjaKostki(){
         try{
             //czujnik koloru z pozycji blokującej na nieblokującą
         makeObrot(3, 90, 0, 20);
         Thread.currentThread().sleep(500);
            //obrót kostki
         rightTurn(1,50);
         Thread.currentThread().sleep(1500);
         engineStop(1);
         // czujnik koloru do pozycji wyjściowej
         makeObrot(3,90,0,-20);
         Thread.currentThread().sleep(500);
         //przewrócenie kostki
         FlipOver();
         Thread.currentThread().sleep(2000);
         //czujnik do pozycji nieblokującej
         makeObrot(3,90,0,20);
         Thread.currentThread().sleep(500);
            //obrót kostki do pozycji wyjściowej
         leftTurn(1,50);
         Thread.currentThread().sleep(1200);
         engineStop(1);
            //powrót ramienia do pozycji wyjściowej
         makeObrot(3,90,0,-20);
         Thread.currentThread().sleep(500);
         }catch(Exception e){

         }

     }

     //OBROT SCIANKI
     public static void rotateWall(){
         try{
            CommandLn.resetMotor("A");
            Thread.currentThread().sleep(200);
            CommandLn.resetMotor("B");
            Thread.currentThread().sleep(200);
            CommandLn.resetMotor("C");
            Thread.currentThread().sleep(200);

            leftTurn(3,20);
            Thread.currentThread().sleep(400);
            engineStop(3);
            //czujnik koloru z pozycji blokującej na nieblokującą
            makeObrot(3,90,0,20);
            Thread.currentThread().sleep(400);
            //przesunięcie ramienia na pozycję trzymania
            makeObrot(2,50,0,30);
            Thread.currentThread().sleep(600);
            //przekrecenie kostki
            leftTurn(1,20);
            Thread.currentThread().sleep(200);
            engineStop(1);
            Thread.currentThread().sleep(200);
            //OBROT SCIANKI O 270 STOPNI
            rightTurn(1,55);
            Thread.currentThread().sleep(3000);
            engineStop(1);
            Thread.currentThread().sleep(200);
            
            //cofnięcie ramienia robota
            makeObrot(2,50,0,-30);
            Thread.currentThread().sleep(600);
            //kostka obrócona do pozycji domyślnej
            leftTurn(1,40);
            Thread.currentThread().sleep(900);
            engineStop(1);
            //wycofanie czujnika koloru
            makeObrot(3,90,0,-20);
            Thread.currentThread().sleep(500);


         }catch(Exception e){

         }
     }

     public static void reset(){
         try{
         CommandLn.resetMotor("A");
         Thread.currentThread().sleep(200);
         CommandLn.resetMotor("B");
         Thread.currentThread().sleep(200);
         CommandLn.resetMotor("C");
         }catch(Exception e){
             
         }
         for (int x=0; x<3;x++)
             for (int y=0;y<3;y++)
                 tablicaKolorow[x][y] = "nic";
     }


}
