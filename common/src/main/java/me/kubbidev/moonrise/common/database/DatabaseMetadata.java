package me.kubbidev.moonrise.common.database;

public class DatabaseMetadata {

    // remote
    private Boolean connected;
    private Integer ping;

    // local
    private Long sizeBytes;

    public Boolean connected() {
        return this.connected;
    }

    public void connected(boolean connected) {
        this.connected = connected;
    }

    public Integer ping() {
        return this.ping;
    }

    public void ping(int ping) {
        this.ping = ping;
    }

    public Long sizeBytes() {
        return this.sizeBytes;
    }

    public void sizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
