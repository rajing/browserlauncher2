//Jomic - a viewer for comic book archives.
//Copyright (C) 2004 Thomas Aglassinger
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
package net.sf.wraplog;

/**
 * Declaration of different logging levels.
 *
 * @author Thomas Aglassinger
 */
public class Level {
    /** Logging level for messages that usually are of no interest for the user. */
    public static final int DEBUG = 0;

    /**
     * Logging level for messages that explain why the desired operation cannot
     * be performed.
     * <p>
     * Note: A simple way for deriving goog error messages is to make them fit
     * the pattern: <blockquote>cannot do something: actual state must match
     * expectation. </blockquote> Just fill in "do something", "actual state"
     * and "expectation".<p> In case your code is just reporting a Java
     * <code>Exception</code> that happens at a lower level on the call stack,
     * use the pattern: <blockquote>cannot do something: &lt;
     * <code>exception.getMessage()</code> &gt;. </blockquote> In practice,
     * the latter  often results in sucky error messages, but there is not much you
     * can do about it apart from making sure that your own
     * <code>Exceptions</code> have a useful <code>getMessage()</code>.
     */
    public static final int ERROR = 3;

    /**
     * Logging level for messages that tell details about normal operations
     * currently going on.
     */
    public static final int INFO = 1;

    /**
     * Logging level for messages that notify about things that can be processed,
     * but the user might want to take a closer look at the current situation.
     * <p>
     * Note: Warnings are a good indicator for bad design. They hint at the
     * developer being to dumb to resolve a situation and therefor delegating
     * the responsibility to the user. Preferrably, the code shoud be changed to
     * either log <code>INFO</code>, throw an exception or log
     * <code>ERROR</code>.
     */
    public static final int WARN = 2;
}