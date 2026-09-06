/*
 */
package es.cristichi.baseapi.exc;

import java.io.IOException;

/**
 *
 * @author Cristichi
 */
public class ResourceNotFoundException extends IOException {

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
