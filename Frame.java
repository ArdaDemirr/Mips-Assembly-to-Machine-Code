// Arda Demir

import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Frame extends JFrame {

    int Address = 0x00400000;                   // starting address

    HashMap<String, String> opcodeMap;          // a hashmap for opcodes
    HashMap<String,String> registerMap;         // a hashmap for registermaps
    HashMap<String,String> functionMap;         // a hashmap for function codes
    HashMap<String,Integer> labelAddressMap;    // a hashmap for storing label addresses

    private JLabel labelInput;                  // declare input label
    private JLabel labelOutput;                 // declare output label

    private JTextArea inputTextArea;            // declare an input text area
    private JTextArea outputTextArea;           // declare an output text area

    private JScrollPane inputScroll;            // declare an input scroll pane     - will be used to scroll up and down in text areas
    private JScrollPane outputScroll;           // declare an output scroll pane

    private JButton convertButton;              // declare a convert button

    public Frame()                              // main frame
    {
        super("Mips Assembly to Machine Code Converter");      // title
        setLayout(null);                                     // no layout manager
        setResizable(false);                               // not resizeable to prevent bad positioning after resizing

        labelInput = new JLabel("Mips Assembly Code: ");        // initialize input label
        labelOutput = new JLabel("Machine Code: ");             // initialize output label
        inputTextArea = new JTextArea();                             // initialize input text area
        outputTextArea = new JTextArea();                            // initialize output text area
        inputScroll = new JScrollPane(inputTextArea);                // add scroll pane to text area - input
        outputScroll = new JScrollPane(outputTextArea);              // add scroll pane to text area - output
        convertButton = new JButton("Convert");                 // initialize button

        labelInput.setBounds(30, 0, 150, 40);       // set input labels bounds
        inputScroll.setBounds(30, 40, 450, 600);    // set input scrolls bounds
        inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);      // set input scroll as vertical
        labelOutput.setBounds(600, 0, 150, 40);     // set output labels bounds
        outputScroll.setBounds(600, 40, 450, 600);  // set output scrolls bounds
        outputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);     // set output scroll as vertical
        convertButton.setBounds(490, 260, 100, 40); // set buttons bounds

        // ADD all component to main window
        add(labelInput);        
        add(labelOutput);
        add(convertButton);
        add(inputScroll);
        add(outputScroll);

        opcodeMap = new HashMap<String, String>();          // initialize opcode hashmap
        registerMap = new HashMap<String, String>();        // initialize register hashmap
        functionMap = new HashMap<String, String>();        // initialize function code hashmap
        labelAddressMap = new HashMap<String, Integer>();   // initialize label address hashmap

        ButtonHandler handler = new ButtonHandler();        // initialize a event handler for convert button
        convertButton.addActionListener(handler);           // assign handler to buttonds actionlistener

        // I and J type functions opcode
        opcodeMap.put("addi","001000");         opcodeMap.put("beq","000100");      opcodeMap.put("j","000010");
        opcodeMap.put("andi","001100");         opcodeMap.put("bne","000101");      opcodeMap.put("jal","000010");
        opcodeMap.put("lw","100011");           opcodeMap.put("blez","000110");
        opcodeMap.put("sw","101011");           opcodeMap.put("bgtz","000111");

        // codes for all registers
        registerMap.put("$zero", "00000");
        registerMap.put("$t0", "01000");        registerMap.put("$s0", "10000");
        registerMap.put("$t1", "01001");        registerMap.put("$s1", "10001");
        registerMap.put("$t2", "01010");        registerMap.put("$s2", "10010");
        registerMap.put("$t3", "01011");        registerMap.put("$s3", "10011");
        registerMap.put("$t4", "01100");        registerMap.put("$s4", "10100");
        registerMap.put("$t5", "01101");        registerMap.put("$s5", "10101");
        registerMap.put("$t6", "01110");        registerMap.put("$s6", "10110");
        registerMap.put("$t7", "01111");        registerMap.put("$s7", "10111");
        registerMap.put("$t8", "11000");
        registerMap.put("$t9", "11001");     

        registerMap.put("$a0", "00100");        registerMap.put("$gp", "11100");
        registerMap.put("$a1", "00101");        registerMap.put("$sp", "11101"); 
        registerMap.put("$a2", "00110");        registerMap.put("$fp", "11110");
        registerMap.put("$a3", "00111");        registerMap.put("$ra", "11111");

        registerMap.put("$v0", "00010");        registerMap.put("$v1", "00011");
        
        // function codes for R type functions
        functionMap.put("add","100000");        functionMap.put("sll","000000");
        functionMap.put("sub","100010");        functionMap.put("srl","000010");
        functionMap.put("and","100100");        functionMap.put("sllv","000100");
        functionMap.put("or","100101");         functionMap.put("srlv","000110");
    }

    public void extractInput()  // in this part we will try to seperate user input to multiple parts in order to get their machine code equivalent
    {
        String output = "";                             // firstly set our output as empty string
        String inputText = inputTextArea.getText();     // by this command we fetch text from our input text area - user inputted mips machine code in this case
        inputText = inputText.toLowerCase().trim();     // now we will trim it, remove the spaces from start and end of the string
        String inputLines[] = inputText.split("\\r?\\n");   // in this part we split the whole input text to lines, \\r?\\n with this parameter we break them when we occure a line break
        for(int i = 0; i< inputLines.length;i++)        // firstly check all lines for labels
        {
            if(inputLines[i].contains(":"))           // if the line we are currently inspecting contains a ":", this means this line represents a label address that we should copy to label address hashmap in order to use it later
            {
                int labelAddress = Address;             // take starting address store as labelAddress
                String label = "";                      // initially label string is an empty string
                int idx = 0;                            // and index idx is 0
                while (inputLines[i].charAt(idx) != ':')// iterate through line text until we occure ":"
                {
                    label += inputLines[i].charAt(idx); // add the character to label string
                    idx +=1;                            // increment the idx
                }
                labelAddressMap.put(label, labelAddress);    // after we occure : and break the loop, we put finalized label string to our label hashmap with the current address
                labelAddress += 4;                           // increase labelAddress by 4
                inputLines[i] = inputLines[i].substring(idx+1,inputLines[i].length());  // now with this code we delete the "label:" section from our line 
            }
        }

        for(int i = 0; i< inputLines.length;i++)            // now for every line we get from splitting:
        {
            inputLines[i] = inputLines[i].replace(","," ");     // in this part we replace every comma with space
            String lineParts[] = inputLines[i].trim().split("\\s+");         // we split the current line to parts from every space and also ignore extra spaces if exists
            for(int j=0;j<lineParts.length;j++)             // now for every line parts that we just splitted
            {
                lineParts[j] = lineParts[j].trim();         // trim all of them to get the pure code from them
            }

            if(lineParts[0].equals("j") || lineParts[0].equals("jal"))  // now if the first index of line parts is "j" or "jal"
            {
                output = jType(lineParts);  // jump to jType method with lineParts array as parameter and return its value as output string
            }

            else if(opcodeMap.containsKey(lineParts[0]))    // else if the first line parts code contained in opcode hashmap, it measn it is a i type (it cannot be r type because r type have opcode 000000 only)
            {
                output = iType(lineParts);  // jump to iType method with lineParts array as parameter and return its value as output string
            }

            else                                            // if it is both not j and i type, then it is rType,
            {
                output = rType(lineParts);  // jump to rType method with lineParts array as parameter and return its value as output string
            }

            printResult(output);                            // after returning from method, call the printResult method with the newly returned output string
            Address+=4;                                     // add 4 to the address variable
        }
    }

    public String rType(String lineParts[])     // rType method with lineParts[] parameter
    {
        String rTypeOpcode = "000000";          // r types opcode is always 000000
        String output = "";                     // initially out output string is empty

        // if first index (instruction) is "sll" or "srl" or "sllv" or "srlv", it means we need the output for shift cases (is is a little different from aritmetic ones thats why we check it)
        if(lineParts[0].equals("sll") || lineParts[0].equals("srl") || lineParts[0].equals("sllv") || lineParts[0].equals("srlv"))
        {
            // output = 000000(opcode)00000(first source register)(second source register)(destination register)(shift amount)(function code)
            output += rTypeOpcode + "00000" + registerMap.get(lineParts[2]) + registerMap.get(lineParts[1]) + String.format("%5s", Integer.toBinaryString(Integer.parseInt(lineParts[3]))).replace(' ', '0') + functionMap.get(lineParts[0]);
        }
        
        else                                    // if it is not a shift amount then is is the classic aritmetics format
        {
            // output = 000000(opcode)(first source register)(second source register)(destination register)00000(function code)
            output += rTypeOpcode + registerMap.get(lineParts[2]) + registerMap.get(lineParts[3]) + registerMap.get(lineParts[1]) + "00000" + functionMap.get(lineParts[0]);
        }

        return output;                          // return output
    }

    public String iType(String lineParts[])     // iType method with lineParts[] parameter
    {
        String output = "";                     // initially out output string is empty

        // if first index (instruction) is equal to "beq" or "bne" or "blez" or "bgtz"
        if(lineParts[0].equals("beq") || lineParts[0].equals("bne") || lineParts[0].equals("blez") || lineParts[0].equals("bgtz"))
        {
            int addressDiff = labelAddressMap.get(lineParts[3]) - (Address + 4); // calculate addressDiff = current address - the labels address that specified in lineParts[3]
            System.out.println(Integer.toBinaryString((addressDiff/4)));

            // output = (opcode)(first source register)(second source register)(16 bit address of label)                                                            (mask it to only get last 16 bits)
            output += opcodeMap.get(lineParts[0]) + registerMap.get(lineParts[1]) + registerMap.get(lineParts[2]) + String.format("%16s", Integer.toBinaryString((addressDiff/4 & 0xFFFF))).replace(' ', '0');
        }

        else if(lineParts[0].equals("lw") || lineParts[0].equals("sw")) // else if instruction is "lw" or "sw"
        {
            lineParts[2] = lineParts[2].replace("("," ");   // replace "(" with " "
            lineParts[2] = lineParts[2].replace(")","");    // replace ")" with ""
            String offset_address[] = lineParts[2].trim().split("\\s+"); // split it from the " "
            String[] linePartsModified = {lineParts[0], lineParts[1], offset_address[0], offset_address[1]};    // create a new array with instruction, destination register, offset, base address
            
            // output = (opcode)(base addresses register)(destination register)(16 bit offset value)
            output += opcodeMap.get(linePartsModified[0]) + registerMap.get(linePartsModified[3]) + registerMap.get(linePartsModified[1]) + String.format("%16s", Integer.toBinaryString(Integer.parseInt(linePartsModified[2]) & 0xFFFF)).replace(' ', '0');
        }
        
        else    // else classic i type format
        {
            // output = (opcode)(register1)(register2)(16 bit address)
            output += opcodeMap.get(lineParts[0]) + registerMap.get(lineParts[2]) + registerMap.get(lineParts[1]) + String.format("%16s", Integer.toBinaryString(Integer.parseInt(lineParts[3]))).replace(' ', '0');
        }

        return output;  // return output
    }

    public String jType(String lineParts[]) // jType method with lineParts[] parameter
    {
        String output = "";                 // initially out output string is empty
        int dynAddress = labelAddressMap.get(lineParts[1]) >>> 2;   // right shift label address two times store as dynamic address
        System.out.println(Integer.toBinaryString(dynAddress));

        // output = (opcode)(26 bit dynamic address)
        output += opcodeMap.get(lineParts[0]) + String.format("%26s", Integer.toBinaryString(dynAddress)).replace(' ', '0');
        return output;                      // return output
    }

    public void printResult(String output)  // method to print all outputs
    {
        String address = String.format("0x%08X:", Address);                              // for printing the address of the instructions
        String hexaCode = String.format("0x%08X", Long.parseLong(output, 2));      // binary to hexadecimal
        outputTextArea.append(address + " " + output + " || " + hexaCode + "\n");               // append (address)(output-binary) || (output-hexa) to output text area
    }

    private class ButtonHandler implements ActionListener   // button handler method
    {
        public void actionPerformed(ActionEvent event)      // when the button clicked            
        {
            outputTextArea.selectAll();                     // select all from output text area
            outputTextArea.replaceSelection("");    // set them all to "" in order to clean it
            Address = 0x00400000;                           // set address to default
            extractInput();                                 // call our first enrty method -> extractInput()
        }
    }
}