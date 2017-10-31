package org.zenframework.easyservices.util.thread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MultiCauseException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Collection<Throwable> causes;

    public MultiCauseException(String message, Collection<Throwable> causes) {
        super(message);
        this.causes = Collections.unmodifiableCollection(new ArrayList<Throwable>(causes));
    }

    public Collection<Throwable> getCauses() {
        return causes;
    }

}
