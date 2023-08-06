package me.kalmemarq.jgame.common;

public enum MemoryUnit {
    BYTES(1),
    KILOBYTES(1000L),
    MEGABYTES(1000L * 1000L),
    GIGABYTES(1000L * 1000L * 1000L),
    TERABYTES(1000L * 1000L * 1000L * 1000L),
    KIBIBYTES(1024L),
    MEBIBYTES(1024L * 1024L),
    GIBIBYTES(1024L * 1024L * 1024L),
    TEBIBYTES(1024L * 1024L * 1024L * 1024L);

    private final long scale;

    MemoryUnit(long scale) {
        this.scale = scale;
    }

    public double convert(long value, MemoryUnit unit) {
        if (this == BYTES) {
            return value * unit.scale;
        }
        return (value * unit.scale) / (double) this.scale;
    }
    
    public long toBytes(long value) {
        return (long) MemoryUnit.BYTES.convert(value, this);
    }

    public double toKB(long value) {
        return MemoryUnit.KILOBYTES.convert(value, this);
    }

    public double toMB(long value) {
        return MemoryUnit.MEGABYTES.convert(value, this);
    }

    public double toGB(long value) {
        return MemoryUnit.GIGABYTES.convert(value, this);
    }

    public double toTB(long value) {
        return MemoryUnit.TERABYTES.convert(value, this);
    }

    public double toKiB(long value) {
        return MemoryUnit.KIBIBYTES.convert(value, this);
    }

    public double toMiB(long value) {
        return MemoryUnit.MEBIBYTES.convert(value, this);
    }

    public double toGiB(long value) {
        return MemoryUnit.GIBIBYTES.convert(value, this);
    }

    public double toTiB(long value) {
        return MemoryUnit.TEBIBYTES.convert(value, this);
    }
}
