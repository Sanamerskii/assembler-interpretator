import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Читает программу УВМ из CSV. */
public class AssemblySource {
    private static final int REQUIRED_ARGUMENTS = 2;
    private static final int TEST_ARGUMENTS = 3;
    private static final int FIELD_COUNT = 2;

    /** В режиме --test выводит поля команд. */
    public static void main(String[] args) {
        try {
            boolean testMode = getTestMode(args);
            List<Instruction> program = read(Path.of(args[0]));
            if (testMode) {
                for (Instruction instruction : program) {
                    System.out.printf("(A=%d, B=%d)%n", instruction.operation().code, instruction.operand());
                }
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
        throw new IllegalArgumentException("Использование: ./run.sh source.csv program.bin [--test]");
    }

    static List<Instruction> read(Path source) throws IOException {
        List<Instruction> program = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(source)) {
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    program.add(parse(line, lineNumber));
                }
            }
        }
        return program;
    }

    private static Instruction parse(String line, int lineNumber) {
        try {
            String[] fields = line.split(",", -1);
            if (fields.length != FIELD_COUNT) {
                throw new IllegalArgumentException("Ожидается КОМАНДА,операнд");
            }
            Operation operation = Operation.valueOf(fields[0].strip());
            long operand = parseOperand(fields[1].strip(), operation);
            return new Instruction(operation, operand);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Строка " + lineNumber + ": " + error.getMessage(), error);
        }
    }

    private static long parseOperand(String text, Operation operation) {
        if (!text.matches("[0-9]+")) {
            throw new IllegalArgumentException("Ожидается целое десятичное число без знака");
        }
        try {
            long operand = Long.parseLong(text);
            if (operand <= operation.maxOperand) {
                return operand;
            }
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Операнд превышает " + operation.maxOperand, error);
        }
        throw new IllegalArgumentException("Операнд превышает " + operation.maxOperand);
    }
}
