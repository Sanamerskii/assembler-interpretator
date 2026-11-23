import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Читает программу УВМ из CSV. */
final class AssemblySource {
    private static final int FIELD_COUNT = 2;

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
            throw new IllegalArgumentException("Ожидается число без знака");
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
