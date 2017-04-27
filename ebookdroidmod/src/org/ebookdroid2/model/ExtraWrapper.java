package org.ebookdroid2.model;

import java.io.Serializable;

public class ExtraWrapper implements Serializable {
    private static final long serialVersionUID = -5109930164496309305L;

    public Object data;

    public ExtraWrapper(final Object data) {
        super();
        this.data = data;
    }
}
