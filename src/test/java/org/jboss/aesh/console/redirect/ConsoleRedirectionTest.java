/*
  * Copyright 2012 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
package org.jboss.aesh.console.redirect;

import org.jboss.aesh.console.AeshConsoleCallback;
 import org.jboss.aesh.console.BaseConsoleTest;
 import org.jboss.aesh.console.Config;
 import org.jboss.aesh.console.Console;
 import org.jboss.aesh.console.ConsoleOperation;
 import org.jboss.aesh.console.operator.ControlOperator;
 import org.junit.Test;

 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;

 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;

/**
  * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
  */
 public class ConsoleRedirectionTest extends BaseConsoleTest {

     @Test
     public void pipeCommands() throws IOException, InterruptedException {
         PipedOutputStream outputStream = new PipedOutputStream();
         PipedInputStream pipedInputStream = new PipedInputStream(outputStream);

         Console console = getTestConsole(pipedInputStream);
         console.setConsoleCallback(new RedirectionConsoleCallback(console));

         console.start();

         outputStream.write(("ls | find *. -print"+Config.getLineSeparator()).getBytes());

         Thread.sleep(100);
         console.stop();
     }

     @Test
     public void redirectionCommands() throws IOException, InterruptedException {
         PipedOutputStream outputStream = new PipedOutputStream();
         PipedInputStream pipedInputStream = new PipedInputStream(outputStream);

         Console console = getTestConsole(pipedInputStream);
         console.setConsoleCallback(new RedirectionConsoleCallback(console));

         console.start();

         if(Config.isOSPOSIXCompatible()) {
             outputStream.write(("ls >"+Config.getTmpDir()+"/foo\\ bar.txt"+Config.getLineSeparator()).getBytes());
         }
         else {
             outputStream.write(("ls >"+Config.getTmpDir()+"\\foo\\ bar.txt"+Config.getLineSeparator()).getBytes());
         }

         Thread.sleep(100);
         console.stop();
     }

     @Test
     public void redirectIn() throws IOException, InterruptedException {
         PipedOutputStream outputStream = new PipedOutputStream();
         PipedInputStream pipedInputStream = new PipedInputStream(outputStream);

         final Console console = getTestConsole(pipedInputStream);
         console.setConsoleCallback(new AeshConsoleCallback() {
             @Override
             public int execute(ConsoleOperation output) {
                 assertEquals("ls ", output.getBuffer());
                 try {
                     assertTrue(console.getShell().in().getStdIn().available() > 0);
                 }
                 catch (IOException e) {
                     fail();
                 }
                 assertEquals(ControlOperator.NONE, output.getControlOperator());
                 java.util.Scanner s = new java.util.Scanner(console.getShell().in().getStdIn()).useDelimiter("\\A");
                 String fileContent = s.hasNext() ? s.next() : "";
                 assertEquals("CONTENT OF FILE", fileContent);
                 return 0;
             }
         });
         console.start();

         if(Config.isOSPOSIXCompatible())
             outputStream.write(("ls < "+Config.getTmpDir()+"/foo\\ bar.txt"+Config.getLineSeparator()).getBytes());
         else
             outputStream.write(("ls < "+Config.getTmpDir()+"\\foo\\ bar.txt"+Config.getLineSeparator()).getBytes());
         outputStream.flush();

         Thread.sleep(200);
         console.stop();
     }

     @Test
     public void redirectIn2() throws IOException, InterruptedException {
         PipedOutputStream outputStream = new PipedOutputStream();
         PipedInputStream pipedInputStream = new PipedInputStream(outputStream);

         final Console console = getTestConsole(pipedInputStream);
         console.setConsoleCallback(new AeshConsoleCallback() {
             private int count = 0;
             @Override
             public int execute(ConsoleOperation output) {
                 if(count == 0) {
                     assertEquals("ls ", output.getBuffer());
                     try {
                         assertTrue(console.getShell().in().getStdIn().available() > 0);
                     }
                     catch (IOException e) {
                         fail();
                     }
                     //assertTrue(output.getStdOut().contains("CONTENT OF FILE"));
                     assertEquals(ControlOperator.PIPE, output.getControlOperator());
                     java.util.Scanner s = new java.util.Scanner(console.getShell().in().getStdIn()).useDelimiter("\\A");
                     String fileContent = s.hasNext() ? s.next() : "";
                     assertEquals("CONTENT OF FILE", fileContent);
                 }
                 else if(count == 1) {
                     assertEquals(" man", output.getBuffer());
                     assertEquals(ControlOperator.NONE, output.getControlOperator());
                 }

                 count++;
                 return 0;
             }
         });
         console.start();

         if(Config.isOSPOSIXCompatible())
             outputStream.write(("ls < "+Config.getTmpDir()+"/foo\\ bar.txt | man"+Config.getLineSeparator()).getBytes());
         else
             outputStream.write(("ls < "+Config.getTmpDir()+"\\foo\\ bar.txt | man"+Config.getLineSeparator()).getBytes());
         outputStream.flush();

         Thread.sleep(200);
         console.stop();
     }

     class RedirectionConsoleCallback extends AeshConsoleCallback {
         private int count = 0;
         Console console;

         RedirectionConsoleCallback(Console console) {
             this.console = console;
         }
         @Override
         public int execute(ConsoleOperation output) {
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


