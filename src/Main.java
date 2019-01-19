import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        File_System sys = new File_System();

        System.out.println("File System Shell");
        Scanner keyboard = new Scanner(System.in);
        String[] strarray;

        while(true)
        {
            strarray = keyboard.nextLine().split(" ");

            switch(strarray[0])
            {
                case "cr": // Create new file with specified name
                    System.out.println("cr is working");
                    break;
                case "de": // Destroy file with given name
                    System.out.println("de is working");
                    break;
                case "op": // Opens specified file
                    System.out.println("op is working");
                    break;
                case "cl": // Closes specified file
                    System.out.println("cl is working");
                    break;
                case "rd": // Reads given number of characters from spec. file
                    System.out.println("rd is working");
                    break;
                case "wr": // Sequentially writes number of spec. char to spec. file
                    System.out.println("wr is working");
                    break;
                case "sk": // Seek specified position in spec. file
                    System.out.println("sk is working");
                    break;
                case "dr": // List the names of all files
                    System.out.println("dr is working");
                    break;
                case "in": // Create disk, initialize it, and open directory
                    sys.init(strarray[1]); // Add length checking here !!!
                    break;
                case "sv": // Save ldisk to specified file
                    sys.save(strarray[1]); // Add length checking here !!!
                    break;
                case "ex": // Exit program
                    System.out.println("Now exiting program...");
                    return;
                default:
                    System.out.println("Unrecognized input...");
                    break;

            }

        }

        /* Testing write_block/read_block functionality
        IO_System a = new IO_System();
        byte[] p = new byte[] {64, 81, 11};
        byte[] q = new byte[] {};

        for (int i = 0; i < 64; i++)
        {
            a.write_block(i,p);
        }

        q = a.read_block(0, q);

        System.out.println(Arrays.toString(q));
        */
    }
}
