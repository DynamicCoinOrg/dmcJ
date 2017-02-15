package org.bitcoinj.core;

/**
 * Thrown when something goes wrong with storing a block. Examples: out of disk space.
 */
public class DmcSystemException extends Exception {
    public DmcSystemException(String message) {
        super(message);
    }
    public DmcSystemException(String message, Throwable t) { super(message, t); }
    public DmcSystemException(Throwable t) {
        super(t);
    }
}
