import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Выполняет машинный код УВМ и сохраняет дамп данных. */
public class Interpreter {
    private static final int ARGUMENT_COUNT = 4;
    private static final long MIN_ADDRESS = 0;
    private static final long MAX_ADDRESS = Operation.WRITE.maxOperand;
    private static final long OPCODE_MASK = (1L << MachineCode.OPCODE_BITS) - 1;

    /**
     * Выполняет программу и сохраняет дамп.
     *
     * @param args двоичный файл, файл дампа, начальный и конечный адреса
     */
    public static void main(String[] args) {
        try {
            if (args.length != ARGUMENT_COUNT) {
                throw new IllegalArgumentException("Использование: ./run.sh interpret program.bin dump.csv start end");
            }
            long start = parseAddress(args[2]);
            long end = parseAddress(args[3]);
            if (start > end) {
                throw new IllegalArgumentException("Адреса диапазона заданы в обратном порядке");
            }
            byte[] code = Files.readAllBytes(Path.of(args[0]));
            Map<Long, Long> memory = execute(code);
            writeDump(Path.of(args[1]), memory, start, end);
        } catch (IOException | IllegalArgumentException error) {
            System.err.println(error.getMessage());
            System.exit(1);
        }
    }

    private static long parseAddress(String text) {
        long address = Long.parseLong(text);
        if (address < MIN_ADDRESS || address > MAX_ADDRESS) {
            throw new IllegalArgumentException("Адрес должен быть от 0 до " + MAX_ADDRESS);
        }
        return address;
    }

    private static Map<Long, Long> execute(byte[] code) {
        int instructionCount = code.length / MachineCode.INSTRUCTION_BYTES;
        if (code.length != instructionCount * MachineCode.INSTRUCTION_BYTES) {
            throw new IllegalArgumentException("Неполная команда в двоичном файле");
        }

        Map<Long, Long> memory = new HashMap<>();
        long accumulator = 0;
        for (int offset = 0; offset < code.length; offset += MachineCode.INSTRUCTION_BYTES) {
            Instruction instruction = decode(code, offset);
            long operand = instruction.operand();
            switch (instruction.operation()) {
                case LOAD -> accumulator = operand;
                case READ -> accumulator = memory.getOrDefault(accumulator + operand, 0L);
                case WRITE -> memory.put(operand, accumulator);
                case NE -> NotEqual.execute(memory, operand, accumulator);
            }
        }
        return memory;
    }

    private static Instruction decode(byte[] code, int offset) {
        long word = 0;
        for (int index = 0; index < MachineCode.INSTRUCTION_BYTES; index++) {
            long value = Byte.toUnsignedLong(code[offset + index]);
            word |= value << (index * Byte.SIZE);
        }
        long opcode = word & OPCODE_MASK;
        for (Operation operation : Operation.values()) {
            if (operation.code == opcode) {
                long operand = (word >>> MachineCode.OPCODE_BITS) & operation.maxOperand;
                return new Instruction(operation, operand);
            }
        }
        throw new IllegalArgumentException("Неизвестный код команды: " + opcode);
    }

    private static void writeDump(Path output, Map<Long, Long> memory, long start, long end) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write("address,value");
            writer.newLine();
            for (long address = start; address <= end; address++) {
                writer.write(address + "," + memory.getOrDefault(address, 0L));
                writer.newLine();
            }
        }
    }
}
