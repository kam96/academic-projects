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
                    sys.create(strarray[1]); // Add length checking !!!
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
                    sys.read(Integer.parseInt(strarray[1]),
                            Integer.parseInt(strarray[2]));
                            // Need length checking
                    break;
                case "wr": // Sequentially writes number of spec. char to spec. file
                    System.out.println("wr is working");
                    break;
                case "sk": // Seek specified position in spec. file
                    sys.lseek(Integer.parseInt(strarray[1]),
                            Integer.parseInt(strarray[2]));
                            // Need length checking
                    break;
                case "dr": // List the names of all files
                    sys.directory();
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
    }
}
