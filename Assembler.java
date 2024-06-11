//import java.io.*;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

public class Assembler
{
    static int vars = 16;       //stores new variables starting from memory location 16
    static int kbd = 24576;     //Memory location of keyboard variable
    static int screen = 16384;  //Memory location of screen
    static int lineNumber = 0;  //Increments with each new line of machine code
    static ArrayList<Tags> tagList = new ArrayList<Tags>();             //Stores tags for loops and iteration
    static ArrayList<Variables> varList = new ArrayList<Variables>();   //Stores variables
    public static class Tags    //Tag class
    {
        private int lineNum;
        private String tag;
        public Tags(int num, String tg)
        {
            lineNum = num;
            tag = tg;
        }

        public int getLineNum()
        {
            return lineNum;
        }

        public String getTag()
        {
            return tag;
        }
    }
    public static class Variables   //Variable class
    {
        private int address;
        private String name;

        public Variables(String n, int add)
        {
            name = n;
            address = add;
        }

        public String getVarName()
        {
            return name;
        }

        public int getAddress()
        {
            return address;
        }
    }
    public static void main(String[] args)
    {
        try 
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.bin"));
            BufferedReader reader = new BufferedReader(new FileReader("input.asm"));
            String line = "";
            //Iteration continues until we reach the end of the file
            while ((line = reader.readLine()) != null)
            {
                if ((line.length() != 0))
                {
                    String machineCode = "0000000000000000";
                    char[] codeBuffer = machineCode.toCharArray();
                    String instruction = parseLine(line);
                    //System.out.println(line.length());
                    if (isAInstruct(instruction))
                    {
                        /*
                        * This section is reserved for dealing with loop varibles...
                        * if a variable tag is a loop, we must set the appropriate flags
                        */
                        boolean isLoop = false;
                        for (int i = 0; i <= tagList.size() - 1; i++)
                        {
                            //System.out.println(instruction.substring(1));
                            if (tagList.get(i).getTag().equals(instruction.substring(1)))
                            {
                                isLoop = true;
                                //System.out.println("Is loop");
                                codeBuffer[0] = '0';
                                char[] binBuffer = String.valueOf((decToBin(tagList.get(i).getLineNum()))).toCharArray();
                                int codeLen = codeBuffer.length - 1;
                                int binLen = binBuffer.length - 1;
                                while (binLen >= 0)
                                {
                                    codeBuffer[codeLen] = binBuffer[binLen];
                                    codeLen--;
                                    binLen--;
                                }
                                break;
                            }
                        }
                        //This section of the code deals with the a instruction
                        if (isLoop == false)
                        {
                            codeBuffer[0] = '0';
                            char[] binBuffer = converter(instruction).toCharArray();
                            int codeLen = codeBuffer.length - 1;
                            int binLen = binBuffer.length - 1;
                            while (binLen >= 0)
                            {
                                codeBuffer[codeLen] = binBuffer[binLen];
                                codeLen--;
                                binLen--;
                            }
                        }
                    } else if (instruction.equals("")) //This solely exists to prevent index exceptions
                    {
                        instruction = "";
                    } else if (instruction.charAt(0) == '(') 
                    {
                        tagList.add(new Tags(lineNumber, instruction.substring(1, instruction.length() - 1)));
                        instruction = "";
                    } else {
                        //deals with C instruction
                        codeBuffer[0] = '1';
                        codeBuffer[1] = '1';
                        codeBuffer[2] = '1';

                        //Dest section
                        if (instruction.contains("="))
                        {
                            if ((instruction.charAt(0) == 'M') || (instruction.charAt(1) == 'M'))
                            {
                                codeBuffer[12] = '1';
                            }
                            if ((instruction.charAt(0) == 'D') || (instruction.charAt(1) == 'D') || (instruction.indexOf('=') != 1 && (instruction.charAt(2) == 'D')))
                            {
                                codeBuffer[11] = '1';
                            }
                            if ((instruction.charAt(0) == 'A') || (instruction.charAt(1) == 'A'))
                            {
                                codeBuffer[10] = '1';
                            }
                        }

                        //Jump conditions
                        if (instruction.contains("JGT"))
                        {
                            codeBuffer[15] = '1';
                        } else if (instruction.contains("JEQ"))
                        {
                            codeBuffer[14] = '1';
                        } else if (instruction.contains("JGE"))
                        {
                            codeBuffer[14] = '1';
                            codeBuffer[15] = '1';
                        } else if (instruction.contains("JLT"))
                        {
                            codeBuffer[13] = '1';
                        } else if (instruction.contains("JNE"))
                        {
                            codeBuffer[13] = '1';
                            codeBuffer[15] = '1';
                        } else if (instruction.contains("JLE"))
                        {
                            codeBuffer[13] = '1';
                            codeBuffer[14] = '1';
                        } else if (instruction.contains("JMP"))
                        {
                            codeBuffer[13] = '1';
                            codeBuffer[14] = '1';
                            codeBuffer[15] = '1';
                        } 
                        //Arithmatic (ALU) operations
                        if (instruction.contains("=") || instruction.contains(";") )
                        {
                            int compStart = 0;
                            int compEnd = instruction.length();
                            if (instruction.contains("="))
                            {
                                compStart = instruction.indexOf("=") + 1;
                            }
                            if (instruction.contains(";"))
                            {
                                compEnd = instruction.indexOf(";");
                            }

                            if (instruction.substring(compStart, compEnd).contains("M"))
                            {
                                codeBuffer[3] = '1';
                            } else 
                            {
                                codeBuffer[3] = '0';
                            }

                            if (instruction.substring(compStart).contains("0"))
                            {
                                codeBuffer[4] = '1';
                                codeBuffer[5] = '0';
                                codeBuffer[6] = '1';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '0';
                            } else if (instruction.substring(compStart, compEnd).equals("1"))
                            {
                                codeBuffer[4] = '1';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '1';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '1';
                            } else if (instruction.substring(compStart, compEnd).equals("-1"))
                            {
                                codeBuffer[4] = '1';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '1';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '0';
                            }
                            //A&M operations
                            else if (instruction.substring(compStart, compEnd).equals("A") || instruction.substring(compStart, compEnd).equals("M"))
                            {
                                codeBuffer[4] = '1';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '0';
                                codeBuffer[9] = '0';
                            } else if (instruction.substring(compStart, compEnd).equals("!A") || instruction.substring(compStart, compEnd).equals("!M"))
                            {
                                codeBuffer[4] = '1';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '0';
                                codeBuffer[9] = '1';
                            } else if (instruction.substring(compStart, compEnd).equals("-A") || instruction.substring(compStart, compEnd).equals("-M"))
                            {
                                codeBuffer[4] = '1';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '1';
                            } else if (instruction.substring(compStart, compEnd).equals("A+1") || instruction.substring(compStart, compEnd).equals("M+1"))
                            {
                                codeBuffer[4] = '1';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '1';
                            } else if (instruction.substring(compStart, compEnd).equals("A-1") || instruction.substring(compStart, compEnd).equals("M-1"))
                            {
                                codeBuffer[4] = '1';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '0';
                            } else if (instruction.substring(compStart, compEnd).equals("D+A") || instruction.substring(compStart, compEnd).equals("D+M"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '0';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '0';
                            } else if (instruction.substring(compStart, compEnd).equals("D-A") || instruction.substring(compStart, compEnd).equals("D-M"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '1';
                            } else if (instruction.substring(compStart, compEnd).equals("A-D") || instruction.substring(compStart, compEnd).equals("M-D"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '0';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '1';
                            } else if (instruction.substring(compStart, compEnd).equals("D&A") || instruction.substring(compStart, compEnd).equals("D&M"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '0';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '0';
                                codeBuffer[8] = '0';
                                codeBuffer[9] = '0';
                            } else if (instruction.substring(compStart, compEnd).equals("D|A") || instruction.substring(compStart, compEnd).equals("D|M"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '0';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '0';
                                codeBuffer[9] = '1';
                            }
                            //D exclusive operations
                            else if (instruction.substring(compStart, compEnd).equals("D"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '0';
                                codeBuffer[6] = '1';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '0';
                                codeBuffer[9] = '0';
                            }  
                            else if (instruction.substring(compStart, compEnd).equals("!D"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '0';
                                codeBuffer[6] = '1';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '0';
                                codeBuffer[9] = '1';
                            }
                            else if (instruction.substring(compStart, compEnd).equals("-D"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '0';
                                codeBuffer[6] = '1';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '1';
                            }else if (instruction.substring(compStart, compEnd).equals("D+1"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '1';
                                codeBuffer[6] = '1';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '1';
                            }else if (instruction.substring(compStart, compEnd).equals("D-1"))
                            {
                                codeBuffer[4] = '0';
                                codeBuffer[5] = '0';
                                codeBuffer[6] = '1';
                                codeBuffer[7] = '1';
                                codeBuffer[8] = '1';
                                codeBuffer[9] = '0';
                            }
                        }
                    }
                    if (instruction.isEmpty() != true)
                    {
                        lineNumber++;
                        machineCode = new String(codeBuffer);
                        writer.write(machineCode + "\n");
                    }
                }
                
            }
            reader.close();
            writer.close(); 
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    //function for checking if it is a or c instruction
    public static boolean isAInstruct(String str)
    {
        try
        {
            if ((str.charAt(0) == '@') || (str.contains("@R")))
            {
                return true;
            } else
            {
                return false;
            }
        } catch (StringIndexOutOfBoundsException e)
        {
            return false;
        }
        
    }

    //function that converts decimal to binary
    public static long decToBin(int dec)
    {
        long binVal = 0;        //use longs instead of ints
        long fact = 1;
        if (dec == 0)
        {
            return 0;
        }
        while (dec >= 1)
        {
            binVal = binVal + fact * (dec % 2);
            fact = fact * 10;
            dec = dec / 2;
        }
        return binVal;
    }
    //Handles A instruction commands - converts the address into string a binary number
    public static String converter(String str)
    {
        int decVal;
        try
        {
            if (str.indexOf('R') == 1)
            {
                decVal = Integer.parseInt(str.substring(2));
            } else 
            {
                decVal = Integer.parseInt(str.substring(1));
            } 
            return String.valueOf(decToBin(decVal));
        } catch(NumberFormatException e)
        {
            int newTag = 16;
            if (str.substring(1).equals("SCREEN") || str.substring(1).equals("KBD"))
            {
                if (str.substring(1).equals("SCREEN"))
                {
                    newTag = screen;
                } else 
                {
                    newTag = kbd;
                }
            } else {
                //creates new variables
                boolean isVar = false;
                for (int i = 0; i <= varList.size() - 1; i++)
                {
                    if (varList.get(i).getVarName().equals(str.substring(1)))
                    {
                        isVar = true;
                        newTag = varList.get(i).getAddress();
                        break;
                    }
                }
                if (isVar == false)
                {
                    varList.add(new Variables(str.substring(1), vars));
                    newTag = vars;
                    vars++;
                }
                
            }
            return String.valueOf(decToBin(newTag));
        }
    }
    //creates the instruction by removing whitespace and making 
    public static String parseLine(String str)
    {
        String command = "";
        if (str.length() > 1)
        {
            if (str.substring(0,2).equals("//"))
            {
                return command;
            }
        }
        for (int i = 0; i < str.length(); i++)
        {
            //excludes whitespace and the beginning of a comment line
            if ((str.charAt(i) != ' ') && (str.charAt(i) != '/'))
            {
                command = command + str.charAt(i);
            }
            //we stop iterating when we reach a comment
            if ((str.charAt(i) == '/') && (str.charAt(i - 1) == '/') && (i > 0))
            {
                break;
            } 
        }
        return command;
    } 
}