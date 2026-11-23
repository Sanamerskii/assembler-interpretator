import java.util.List;

/** Кодирует команды в семибайтовые слова УВМ. */
final class MachineCode {
    static final int INSTRUCTION_BYTES = 7;
    static final int OPCODE_BITS = 5;

    static byte[] encode(List<Instruction> program) {
        byte[] code = new byte[program.size() * INSTRUCTION_BYTES];
        int offset = 0;
        for (Instruction instruction : program) {
            long word = (instruction.operand() << OPCODE_BITS) | instruction.operation().code;
            for (int index = 0; index < INSTRUCTION_BYTES; index++) {
                code[offset++] = (byte) (word >>> (index * Byte.SIZE));
            }
        }
        return code;
    }
}
