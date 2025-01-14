/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.util.*;

/* 
 * Version.java
 * 
 * Created: Wed Aug 11 19:44:46 1999
 * By: Sean Luke
 */

/**
 * Version is a static class which stores version information for this
 * evolutionary computation system.
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Version
    {
    public static final String name = "ECJ";
    public static final String version = "27.1-zvika-5";
    public static final String copyright = "2019";
    public static final String author = "Sean Luke";
    public static final String contributors = "E. Scott, L. Panait, G. Balan, S. Paus, Z. Skolicki,";
    public static final String contributors2 = "R. Kicinger, E. Popovici, K. Sullivan, J. Harrison, J. Bassett,";
    public static final String contributors3 = "R. Hubley, A. Desai, A. Chircop, J. Compton, W. Haddon,";
    public static final String contributors4 = "S. Donnelly, B. Jamil, J. Zelibor, E. Kangas, F. Abidi,";
    public static final String contributors5 = "H. Mooers, J. O'Beirne, L. Manzoni, K. Talukder, S. McKay,";
    public static final String contributors6 = "J. McDermott, J. Zou, A. Rutherford, D. Freelan, E. Wei,";
    public static final String contributors7 = "S. Rajendran, A. Dhawan, B. Brumbac, J. Hilty, A. Kabir";
    public static final String authorEmail0 = "ecj-help";
    public static final String authorEmail1 = "cs.gmu.edu";
    public static final String authorEmail2 = "(better: join ECJ-INTEREST at URL above)";
    public static final String authorURL = "http://cs.gmu.edu/~eclab/projects/ecj/";
    public static final String date = "July 1, 2017";
    public static final String minimumJavaVersion = "1.5";

    public static final String message()
        {
        Properties p = System.getProperties();
        String javaVersion = p.getProperty("java.version");
        String javaVM = p.getProperty("java.vm.name");
        String javaVMVersion = p.getProperty("java.vm.version");
        if (javaVM!=null) javaVersion = javaVersion + " / " + javaVM;
        if (javaVM!=null && javaVMVersion!=null) javaVersion = javaVersion + "-" + javaVMVersion;
        
    
        return 
            "\n| " + name + 
            "\n| An evolutionary computation system (version " + version + ")" +
            //"\n| Copyright " + copyright + */ " By " + author +
            "\n| By " + author + 
            "\n| Contributors: " + contributors +
            "\n|               " + contributors2 +
            "\n|               " + contributors3 +
            "\n|               " + contributors4 +
            "\n|               " + contributors5 +
            "\n|               " + contributors6 +
            "\n|               " + contributors7 +
            "\n| URL: " + authorURL +
            "\n| Mail: " + authorEmail0 + "@" + authorEmail1 +
            "\n|       " + authorEmail2 + 
            "\n| Date: " + date +
            "\n| Current Java: " + javaVersion +
            "\n| Required Minimum Java: " + minimumJavaVersion +
            "\n\n";
        }
    }
