import java.util.HashMap;
import java.util.Map;

public class SimpleAssembler {
    private static Map<String, Integer> opcodes = new HashMap<>();

    static {
        opcodes.put("HLT", 0);
        opcodes.put("LDR", 1);
    }

    /*
    Holds instruction data for all opcodes (whether needed or not). For example not all opcodes use
    an indirect (like AIR). But it's available if needed. IDK how yet to implement nullable.
    */
    static class Instruction {
        String opcode;
        int register;
        int index;
        int indirect;
        int address;

        public Instruction(String opcode, int register, int index, int indirect, int address) {
            this.opcode = opcode;
            this.register = register;
            this.index = index;
            this.indirect = indirect;
            this.address = address;
        }
    }

    public static void main(String[] args) {
        /*
        Right now this just assembles using predefined address location. The file goes like:
        1    LOC 6       ;BEGIN AT LOCATION 6
        2    Data 10     ;PUT 10 AT LOCATION 6
        3    Data 3      ;PUT 3 AT LOCATION 7
        4    Data End    ;PUT 1024 AT LOCATION 8
        5    Data 0
        6    Data 12
        7    Data 9
        8    Data 18
        9    Data 12
        10   LDX 2,7     ;X2 GETS 3

         So the LDX doesn't happen until line 10. We need a way to break those out.
         */
        String result = assemble(20, "LDR 2,1,8");
        System.out.println(result2);
    }

    public static String assemble(int location, String instructionLine) {
        Instruction inst = parseInstruction(instructionLine);
        String locationOctal = convertToOctal(location, 6);
        String instructionOctal = generateInstruction(inst);

        return locationOctal + " " + instructionOctal + " " + instructionLine;
    }

    public static Instruction parseInstruction(String line) {
        // Remove comments (anything after semicolon)
        if (line.contains(";")) {
            line = line.substring(0, line.indexOf(";"));
        }

        // Split by spaces and commas
        String[] parts = line.trim().split("[ ,]+");

        String opcode = parts[0];

        // Different parsing based on instruction type
        if (opcode.equals("HLT")) {
            return new Instruction(opcode, 0, 0, 0, 0);
        } else if (opcode.equals("AIR") || opcode.equals("SIR")) {
            // Immediate instructions: AIR r,immed
            int register = Integer.parseInt(parts[1]);
            int immediate = Integer.parseInt(parts[2]);
            return new Instruction(opcode, register, 0, 0, immediate);
        } else {
            // Standard format: LDR r,x,address[,I]
            int register = Integer.parseInt(parts[1]);
            int index = Integer.parseInt(parts[2]);
            int address = Integer.parseInt(parts[3]);
            int indirect = 0;

            // Check for indirect addressing (5th parameter)
            if (parts.length > 4) {
                indirect = Integer.parseInt(parts[4]);
            }

            return new Instruction(opcode, register, index, indirect, address);
        }
    }

    public static String generateInstruction(Instruction inst) {
        // Get opcode from map
        Integer opcodeValue = opcodes.get(inst.opcode);
        if (opcodeValue == null) {
            throw new RuntimeException("Unknown opcode: " + inst.opcode);
        }

        // Pack into 16-bit instruction format:
        // |Opcode(6)|R(2)|IX(2)|I(1)|Address(5)|
        int instruction = 0;

        instruction |= (opcodeValue & 0x3F) << 10;  // Opcode: bits 15-10
        instruction |= (inst.register & 0x3) << 8;  // Register: bits 9-8
        instruction |= (inst.index & 0x3) << 6;     // Index: bits 7-6
        instruction |= (inst.indirect & 0x1) << 5;  // Indirect: bit 5
        instruction |= (inst.address & 0x1F);       // Address: bits 4-0

        // Convert to 6-digit octal
        return convertToOctal(instruction, 6);
    }

    public static String convertToOctal(int value, int digits) {
        String octal = Integer.toOctalString(value);

        // Pad with leading zeros
        while (octal.length() < digits) {
            octal = "0" + octal;
        }

        return octal;
    }
}