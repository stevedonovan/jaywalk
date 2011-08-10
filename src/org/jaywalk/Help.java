package org.jaywalk;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Help {
	String value();
}
