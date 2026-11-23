import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Собирает программу УВМ из CSV в двоичный файл. */
public class Assembler {
    private static final int REQUIRED_ARGUMENTS = 2;
    private static final int TEST_ARGUMENTS = 3;

    /**
     * Записывает машинный код и выводит его размер в байтах.
     *
     * @param args исходник, двоичный файл и необязательный флаг --test
     */
    public static void main(String[] args) {
        try {
            boolean testMode = getTestMode(args);
            List<Instruction> program = AssemblySource.read(Path.of(args[0]));
            byte[] code = MachineCode.encode(program);
            Files.write(Path.of(args[1]), code);
            System.out.println("Размер двоичного файла: " + code.length + " байт");
            if (testMode) {
                printBytes(code);
            }
        } catch (IOException | IllegalArgumentException error) {
            System.err.println(error.getMessage());
            System.exit(1);
        }
    }

    private static boolean getTestMode(String[] args) {
        if (args.length == REQUIRED_ARGUMENTS) {
            return false;
        }
        if (args.length == TEST_ARGUMENTS && "--test".equals(args[REQUIRED_ARGUMENTS])) {
            return true;
        }
        throw new IllegalArgumentException("Использование: ./run.sh assemble source.csv program.bin [--test]");
    }

    private static void printBytes(byte[] code) {
        for (int offset = 0; offset < code.length; offset += MachineCode.INSTRUCTION_BYTES) {
            List<String> bytes = new ArrayList<>();
            for (int index = 0; index < MachineCode.INSTRUCTION_BYTES; index++) {
                bytes.add(String.format("0x%02X", code[offset + index]));
            }
            System.out.println(String.join(", ", bytes));
        }
    }
}
