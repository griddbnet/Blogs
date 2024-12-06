package com.galapea.techblog.textstorage.config;

import java.io.Serial;

public class NotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        super();
    }

    public NotFoundException(final String message) {
        super(message);
    }

    public NotFoundException(final Throwable e) {
        super(e);
    }

    public NotFoundException(final String message, final Throwable e) {
        super(message, e);
    }
}
