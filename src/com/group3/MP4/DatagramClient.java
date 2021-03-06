package com.group3.MP4;

import java.net.* ;
import java.io.* ;
import java.util.Random ;

/**
 *  A simple datagram client
 *  Shows how to send and receive UDP packets in Java
 */
public class DatagramClient
{
   private final static int PACKETSIZE = 100 ;

   public static void main( String args[] )
   {
      // Check the arguments
      if( args.length != 2 )
      {
         System.out.println( "usage: java DatagramClient host port" ) ;
         return ;
      }

      DatagramSocket socket = null ;

      try
      {
         // Convert the arguments first, to ensure that they are valid

         InetAddress host = InetAddress.getByName( args[0] ) ;
         int port = 0;

         try {
            port = Integer.parseInt(args[1]);
         }
         catch (Exception e) { //Error if it's not a number
            System.out.println("The given port number is not a valid input. Exiting Server...");
            System.exit(1);
         }

         // Construct the socket
         socket = new DatagramSocket(/*port*/) ;

         // Send packet to begin message transmission from server
         byte [] welcome = "Hello Server".getBytes() ;
         DatagramPacket packet = new DatagramPacket( welcome, welcome.length, host, port ) ;
         socket.send(packet);


         //Buffer to receive the message
         byte[] buffer = new byte[65536];
         packet = new DatagramPacket( buffer, buffer.length) ;


         System.out.println("Waiting...");

         //Declarations that are being used in the while loop
         int numMessages = 0;
         int trial = 0;
         int receivedNum = 0;
         String message = "";
         String ACK = "";
         Random rand = new Random();

         boolean to_break = false;
         boolean message_received = false;
         while(true) {
            //Buffer to receive the message
            socket.receive(packet);
            byte[] data = packet.getData();
            //Converting given data to String
            String currentString = new String(data, 0, packet.getLength());

            //The first packet which has the number of messages in it
            if (trial == 0) {
               numMessages = Integer.parseInt(currentString);
            }
            //If it is a real message
            else{
               //Try reading the message
               //The first character should be the framenumber
               try {
                  receivedNum = Integer.parseInt(currentString.substring(0,1));
                  //Delete the first digit
                  currentString = currentString.substring(1,currentString.length());
               }
               catch (NumberFormatException e) {
                  System.out.println("Message must start with a number.\nError: " + e);
               }
            }

            //Fail safe check
            if(!message_received && receivedNum == trial) {
               //Outputs the received data
               System.out.println(packet.getAddress().getHostAddress() + ":" + packet.getPort() + "(" + trial + ") - " + currentString);

               //Add current string to full message if trial is not 0
               if(trial!=0)
                  message = message + currentString;

               //Checks if it's the last message
               if(trial == numMessages){
                  ACK = "ACK: " + trial + " Received all messages.";
                  System.out.println("\nComplete Message: " + message + "\n");
                  message_received = true;
               }
               else {
                  ACK = "ACK: " + trial + " Please send: " + (trial + 1);
                  trial += 1;
               }
            }
            else {
               System.out.println("Message " + trial + " has already been received. Discarding data. ");
            }
            if (message_received)
               to_break = true;

            //CHANGE to true if you want unreliableAck, otherwise change it to false
            if(true) {
               int ran = rand.nextInt(100);

               //50 50 chance
               if(ran > 50) {
                  DatagramPacket newPacket = new DatagramPacket(ACK.getBytes(), ACK.getBytes().length, packet.getAddress(), packet.getPort());
                  socket.send(newPacket);
               }

               else {
                  System.out.println("Ack was not sent for " + trial + ". Please try again.");
                  to_break = false;
               }
            }
            //reliable ACK
            else {
               DatagramPacket newPacket = new DatagramPacket(ACK.getBytes(), ACK.getBytes().length, packet.getAddress(), packet.getPort());
               socket.send(newPacket);
            }

            if (to_break)
               break;
         }
         System.out.println("\nMessage transmission complete. Goodbye!");
      }
      catch( Exception e )
      {
         System.out.println( e ) ;
      }
      finally
      {
         if( socket != null )
            socket.close() ;
      }
   }
}
