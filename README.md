The following commands will be used in the test files for this project:

●      cr <name>

o    create a new file with the name <name>

o    Output: <name> created

●      de <name>

o    destroy the named file <name>

o    Output: <name> destroyed



●      op <name>

o    open the named file <name> for reading and writing; display an index value

o    Output: <name> opened <index>



●      cl <index>

o    close the specified file <index>

o    Output: <index> closed



●      rd <index> <count>

o    sequentially read <count> number of characters from the specified file <index> and display them on the terminal

o    Output: <xx...x>



●      wr <index> <char> <count>

o    sequentially write <count> number of <char>s into the specified file <index> at its current position

o    Output: <count> bytes written



●      sk <index> <pos>

o    seek: set the current position of the specified file <index> to <pos>

o    Output: position is <pos>



●      dr

o    directory: list the names of all files

o    Output: <file0> <file1> ... <fileN>



●      in <disk_cont.txt>

o    create a disk using the prescribed dimension parameters and initialize it; also open directory

o    If file does not exist, output: disk initialized

o    If file does exist, output: disk restored



●      sv <disk_cont.txt>

o    close all files and save the contents of the disk in the specified file

o    Output: disk saved



●      If any command fails, output: error