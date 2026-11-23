import java.util.Map;

/** Записывает результат команды NE по адресу первого операнда. */
final class NotEqual {
    static void execute(Map<Long, Long> memory, long address, long accumulator) {
        long value = memory.getOrDefault(address, 0L);
        memory.put(address, value != accumulator ? 1L : 0L);
    }
}
