// ActiveSystemFlag.java
package jmri.jmrix;

/**
 * Lightweight class to check if a system is active.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
abstract public class ActiveSystemFlag {

    /**
     * Chec whether a particular package name is active.
     *
     * @param name Something like "jmri.jmrix.loconet"
     */
    static public boolean isActive(String name)
            throws ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            java.lang.reflect.InvocationTargetException {
        String classname = name + ".ActiveFlag";
        Class<?> c = Class.forName(classname);
        java.lang.reflect.Method m = c.getMethod("isActive", (Class[]) null);
        Object b = m.invoke(null, (Object[]) null); // static object, no args
        return ((Boolean) b).booleanValue();
    }
}


/* @(#)ActiveSystemFlag.java */
