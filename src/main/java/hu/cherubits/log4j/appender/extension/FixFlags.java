package hu.cherubits.log4j.appender.extension;

/**
 * Created by lordoftheflies on 2017.06.01..
 */
public enum FixFlags {
    NDC(0x02),
    MESSAGE(0x04),
    THREADNAME(0x08),
    LOCATION_INFO(0x10),
    USERNAME(0x20),
    DOMAIN(0x40),
    IDENTITY(0x80),
    EXCEPTION(0x100),
    PROPERTIES(0x200),
    NONE(0x0),
    ALL(0xFFFFFFF);

    private final int value;

    FixFlags(final int newValue) {
        value = newValue;
    }

    public int partial() {
        return MESSAGE.value | THREADNAME.value | EXCEPTION.value | DOMAIN.value | PROPERTIES.value;
    }
}
