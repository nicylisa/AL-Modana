/* Functional Mock-up Interface (FMI) event information.

   Copyright (c) 2012 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

*/

package ecnu.modana.FmiDriver.ptolemy.fmi;

import java.util.HashSet;
import java.util.Set;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMULog;
import org.ptolemy.fmi.NativeSizeT;

/**
 * An interface that is used by Java Native Access (JNA) to handle callbacks.
 *
 * <p>This class contains implementations of methods that are registered
 * with the FMI and then called back from by the FMI.  The callback
 * methods allocate and free memory, handle logging and are sometimes
 * called when the step ends.  For each callback we define an inner class
 * that implements the appropriate interface and has one method that
 * provides the body of the callback.</p>
 *
 * <p>For details about how Callbacks work in JNA, see
 * <a href="http://twall.github.com/jna/3.4.0/javadoc/overview-summary.html#callbacks">http://twall.github.com/jna/3.4.0/javadoc/overview-summary.html#callbacks</a>.</p>
 *
 * <p>This file is based on a file that was autogenerated by
 * <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br> a tool
 * written by <a href="http://ochafik.com/">Olivier Chafik</a> that
 * <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses
 * a few opensource projects.</a>.</p>
 *
 * @author Christopher Brooks
 * @version $Id: FMULibrary.java 66133 2013-04-25 21:59:22Z cxh $
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface FMULibrary extends FMILibrary {

    /** The logging callback function. */
    public class FMULogger implements FMICallbackLogger {
        /** Log a message.
         *  Note that arguments after the message are currently ignored.
         *  @param fmiComponent The component that was instantiated.
         *  @param instanceName The name of the instance of the FMU.
         *  @param status The fmiStatus, see
         *  {@link FMIStatus}
         *  @param category The category of the message,
         *  defined by the tool that created the fmu.  Typical
         *  values are "log" or "error".
         *  @param message The message in printf format
         *  @param parameters The printf style parameters.
         */
        public void apply(Pointer fmiComponent, String instanceName,
                int status, String category, String message, Pointer /*...*/
                parameters) {
            // We place this method in separate file for testing purposes.
            FMULog.log(fmiComponent, instanceName, status, category, message,
                    parameters);
        }
    }

    /** Allocate memory. */
    public class FMUAllocateMemory implements FMICallbackAllocateMemory {

        /** Allocate memory.
         *  @param numberOfObjects The number of objects to allocate.
         *  @param size The size of the object in bytes.
         *  @return a Pointer to the allocated memory.
         */
        public Pointer apply(NativeSizeT numberOfObjects, NativeSizeT size) {
            // For hints, see http://markmail.org/message/6ssggt4q6lkq3hen

            int numberOfObjectsValue = numberOfObjects.intValue();
            if (numberOfObjectsValue <= 0) {
                // instantiateModel() in fmuTemplate.c
                // will try to allocate 0 reals, integers, booleans or
                // strings.
                // However, instantiateModel() later checks to see if
                // any of the allocated spaces are null and fails with
                // "out of memory" if they are null.
                numberOfObjectsValue = 1;
            }
            Memory memory = new Memory(numberOfObjectsValue * size.intValue());
            // FIXME: not sure about alignment.
            Memory alignedMemory = memory.align(4);
            memory.clear();
            Pointer pointer = alignedMemory.share(0);

            // Need to keep a reference so the memory does not get gc'd.
            // See http://osdir.com/ml/java.jna.user/2008-09/msg00065.html
            pointers.add(pointer);

            return pointer;
        }

        /** Keep references to memory that has been allocated and
         *  avoid problems with the memory being garbage collected.
         */
        public static Set<Pointer> pointers = new HashSet<Pointer>();
    }

    /** A callback that frees memory.
     */
    public class FMUFreeMemory implements FMICallbackFreeMemory {
        /** Free memory.
         *  @param object The object to be freed.
         */
        public void apply(Pointer object) {
            FMUAllocateMemory.pointers.remove(object);
        }
    }

    /** A callback for when the step is finished. */
    public class FMUStepFinished implements FMIStepFinished {
        /** The step is finished.
         *  @param fmiComponent The FMI component that was instantiate.
         *  @param status The status flag.  See the FMI documentation.
         */
        public void apply(Pointer fmiComponent, int status) {
            // FIXME: More should be done here.
            System.out.println("Java fmiStepFinished: " + fmiComponent + " "
                    + status);
        }
    };
}
