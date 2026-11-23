/** Команды УВМ варианта 7 и размеры их операндов. */
enum Operation {
    LOAD(14, 27),
    READ(22, 16),
    WRITE(12, 32),
    NE(4, 32);

    final int code;
    final long maxOperand;

    Operation(int code, int operandBits) {
        this.code = code;
        this.maxOperand = (1L << operandBits) - 1;
    }
}
