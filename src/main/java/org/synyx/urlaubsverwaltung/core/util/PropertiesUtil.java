/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.util;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;


public final class PropertiesUtil {

    private PropertiesUtil() {

        // Hide constructor for util classes
    }

    /**
     * Load a properties file from the classpath. Thanks to: http://www.rgagnon.com/javadetails/java-0434.html
     *
     * @param  propsName
     *
     * @return  Properties
     *
     * @throws  Exception
     */
    public static Properties load(String propsName) throws IOException {

        Properties props = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(propsName);
        props.load(url.openStream());

        return props;
    }
}
