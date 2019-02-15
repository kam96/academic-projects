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
                    sys.destroy(strarray[1]);
                    break;
                case "op": // Opens specified file
                    int status = sys.open(strarray[1]);
                    if (status == -1)
                        System.out.println("error");
                    break;
                case "cl": // Closes specified file
                    sys.close(Integer.parseInt(strarray[1]));
                    break;
                case "rd": // Reads given number of characters from spec. file
                    if (Integer.parseInt(strarray[1]) == 0)
                        System.out.println("error"); // prevent user from access dir
                    else
                    {
                        String str = sys.read(Integer.parseInt(strarray[1]),
                                Integer.parseInt(strarray[2]));
                        System.out.println(str);
                    }
                            // Need length checking
                    break;
                case "wr": // Sequentially writes number of spec. char to spec. file
                    if (Integer.parseInt(strarray[1]) == 0)
                        System.out.println("error"); // prevent user dir access
                    else
                    {
                        int num = sys.write(Integer.parseInt(strarray[1]),
                                strarray[2].charAt(0),
                                Integer.parseInt(strarray[3]));
                        System.out.println(num + " bytes written");
                    }
                        // Need length checking
                    break;
                case "sk": // Seek specified position in spec. file
                    int pos = sys.lseek(Integer.parseInt(strarray[1]),
                            Integer.parseInt(strarray[2]));
                            // Need length checking
                    System.out.println("position is " + pos);
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
                    System.out.println("");
                    break;
            }

        }
    }
}
