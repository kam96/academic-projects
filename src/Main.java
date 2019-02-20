import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args) throws FileNotFoundException
    {
        File_System sys = new File_System();
        Scanner input = new Scanner(new File(
                System.getProperty("user.dir") + "\\" + "test.txt"));
        PrintWriter output = new PrintWriter("34768020.txt");
        String[] strarray;

        while(input.hasNext())
        {
            strarray = input.nextLine().split(" ");

            switch(strarray[0])
            {
                case "cr": // Create new file with specified name
                    int test = sys.create(strarray[1]);
                    if (test == -1)
                        output.println("error");
                    else
                        output.println(strarray[1] + " created");
                    break;

                case "de": // Destroy file with given name
                    int check = sys.destroy(strarray[1]);
                    if (check == -1)
                        output.println("error");
                    else
                        output.println(strarray[1] + " destroyed");
                    break;

                case "op": // Opens specified file
                    int status = sys.open(strarray[1]);
                    if (status == -1)
                        output.println("error");
                    else
                        output.println(strarray[1] + " opened " + status);
                    break;

                case "cl": // Closes specified file
                    int index = Integer.parseInt(strarray[1]);
                    if (index <= 0 || index > 3)
                        output.println("error");
                    else
                    {
                        index = sys.close(Integer.parseInt(strarray[1]));
                        output.println(index + ". closed");
                    }
                    break;

                case "rd": // Reads given number of characters from spec. file
                    if (Integer.parseInt(strarray[1]) == 0)
                        output.println("error"); // prevent user from access dir
                    else
                    {
                        String str = sys.read(Integer.parseInt(strarray[1]),
                                Integer.parseInt(strarray[2]));
                        output.println(str.substring(1));
                    }
                    break;

                case "wr": // Sequentially writes number of spec. char to spec. file
                    if (Integer.parseInt(strarray[1]) == 0)
                        output.println("error"); // prevent user dir access
                    else
                    {
                        int num = sys.write(Integer.parseInt(strarray[1]),
                                strarray[2].charAt(0),
                                Integer.parseInt(strarray[3]));
                        if (num == -1)
                            output.println("error");
                        else
                            output.println(num + " bytes written");
                    }
                    break;

                case "sk": // Seek specified position in spec. file
                    int pos = sys.lseek(Integer.parseInt(strarray[1]),
                            Integer.parseInt(strarray[2]));
                    if (pos == -1)
                        output.println("error");
                    else
                        output.println("position is " + pos);
                    break;

                case "dr": // List the names of all files
                    output.println(sys.directory());
                    break;

                case "in": // Create disk, initialize it, and open directory
                    int result;
                    if (strarray.length == 1)
                        result = sys.init();
                    else
                        result = sys.init(strarray[1]);
                    if (result == 0)
                        output.println("disk initialized");
                    else if (result == 1)
                        output.println("disk restored");
                    else
                        output.println("error");
                    break;

                case "sv": // Save ldisk to specified file
                    int saved = sys.save(strarray[1]);
                    if (saved == -1)
                        output.println("error");
                    else
                        output.println("disk saved");
                    break;

                default:
                    output.println();
                    break;
            }

        }
        input.close();
        output.close();
    }
}
