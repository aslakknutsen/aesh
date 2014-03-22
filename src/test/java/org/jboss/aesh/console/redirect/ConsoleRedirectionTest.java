/*
  * Copyright 2012 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
package org.jboss.aesh.console.redirect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.aesh.console.BaseConsoleTest;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleOperation;
import org.jboss.aesh.console.operator.ControlOperator;
import org.junit.Ignore;
import org.junit.Test;

/**
  * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
  */
 public class ConsoleRedirectionTest extends BaseConsoleTest {

     @Test
     public void pipeCommands() throws Throwable {
         final AtomicInteger count = new AtomicInteger();
         console()
             .numberOfCommands(2)
             .when((console, out) -> {
                 out.write(("ls | find *. -print"+Config.getLineSeparator()).getBytes());
             })
             .then((console, op) -> {
                 if(count.get() == 0) {
                     assertEquals("ls ", op.getBuffer());
                     count.incrementAndGet();
                 }
                 else if(count.get() == 1) {
                     assertEquals(" find *. -print", op.getBuffer());
                 }
                 return 0;
             })
             .prove();
     }

     @Test @Ignore
     public void redirectionCommands() throws Throwable {
         invokeTestConsole(new Setup() {
             @Override
             public void call(Console console, OutputStream out) throws IOException {
                 if (Config.isOSPOSIXCompatible()) {
                     out.write(("ls >" + Config.getTmpDir() + "/foo\\ bar.txt" + Config.getLineSeparator()).getBytes());
                 } else {
                     out.write(("ls >" + Config.getTmpDir() + "\\foo\\ bar.txt" + Config.getLineSeparator()).getBytes());
                 }
             }
         }, new RedirectionConsoleCallback());
     }

     @Test @Ignore
     public void redirectIn() throws Throwable {
         invokeTestConsole(new Setup() {
                               @Override
                               public void call(Console console, OutputStream out) throws IOException {
                                   if (Config.isOSPOSIXCompatible())
                                       out.write(("ls < " + Config.getTmpDir() + "/foo\\ bar.txt" + Config.getLineSeparator()).getBytes());
                                   else
                                       out.write(("ls < " + Config.getTmpDir() + "\\foo\\ bar.txt" + Config.getLineSeparator()).getBytes());
                                   out.flush();
                               }
                           }, new Verify() {
                               @Override
                               public int call(Console console, ConsoleOperation op) {
                                   assertEquals("ls ", op.getBuffer());
                                   try {
                                       assertTrue(console.getShell().in().getStdIn().available() > 0);
                                   } catch (IOException e) {
                                       fail();
                                   }
                                   assertEquals(ControlOperator.NONE, op);
                                   java.util.Scanner s = new java.util.Scanner(console.getShell().in().getStdIn()).useDelimiter("\\A");
                                   String fileContent = s.hasNext() ? s.next() : "";
                                   assertEquals("CONTENT OF FILE", fileContent);
                                   return 0;
                               }
                           }
         );
     }

     @Test @Ignore
     public void redirectIn2() throws Throwable {
         invokeTestConsole(2, new Setup() {
                     @Override
                     public void call(Console console, OutputStream out) throws IOException {
                         if (Config.isOSPOSIXCompatible())
                             out.write(("ls < " + Config.getTmpDir() + "/foo\\ bar.txt | man" + Config.getLineSeparator()).getBytes());
                         else
                             out.write(("ls < " + Config.getTmpDir() + "\\foo\\ bar.txt | man" + Config.getLineSeparator()).getBytes());
                         out.flush();
                     }
                 }, new Verify() {
                     private int count = 0;

                     @Override
                     public int call(Console console, ConsoleOperation op) {
                         if (count == 0) {
                             assertEquals("ls ", op.getBuffer());
                             try {
                                 assertTrue(console.getShell().in().getStdIn().available() > 0);
                             } catch (IOException e) {
                                 fail();
                             }
                             //assertTrue(output.getStdOut().contains("CONTENT OF FILE"));
                             assertEquals(ControlOperator.PIPE, op);
                             java.util.Scanner s = new java.util.Scanner(console.getShell().in().getStdIn()).useDelimiter("\\A");
                             String fileContent = s.hasNext() ? s.next() : "";
                             assertEquals("CONTENT OF FILE", fileContent);
                         } else if (count == 1) {
                             assertEquals(" man", op.getBuffer());
                             assertEquals(ControlOperator.NONE, op.getControlOperator());
                         }
                         count++;
                         return 0;
                     }
                 }
         );
     }

     class RedirectionConsoleCallback implements Verify {
         private int count = 0;

         @Override
         public int call(Console console, ConsoleOperation output) {
             if(count == 0) {
                 assertEquals("ls ", output.getBuffer());
                 count++;
             }
             else if(count == 1) {
                 assertEquals(" find *. -print", output.getBuffer());
             }
             return 0;
         }
     }
}
