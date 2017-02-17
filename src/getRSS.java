/*
 Name: Mike Peal
 Course: 563
 Assignment: #2
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class getRSS 
{
	
	public static void createFile() throws IOException
	{
		URL url = new URL("http://rss.cnn.com/rss/cnn_latest.rss");
		//set up a reader that reads in input from cnn_latest and writes it into an XML doc
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		FileWriter fw = new FileWriter("cnnxml.txt");
		PrintWriter fp = new PrintWriter(fw);
		String line = "";
		
		//loop to print each line to XML doc while lines exist
		while((line = br.readLine()) != null)
		{
			fp.println(line);
			fp.flush(); //flush after each line (important!)
		}
		br.close(); //close buffered reader
	}

	
	public static void main(String[] args) throws Exception, IOException, ParserConfigurationException, SAXException
	{
		
		try{
		 String jdbcDriver = "com.mysql.jdbc.Driver";
		 String protocolHeader = "jdbc:mysql://db4free.net/compsci463";
		 String user = "compsci463";
		 String password = "love2sql";
		 System.out.println("***Connecting to database***");
		 
		 
		//connect to JDBC class database
		 Class.forName(jdbcDriver);
	     Connection cntn = DriverManager.getConnection(protocolHeader, user, password);
	       
	      //create the statement to be sent to the SQL database
	     Statement query = cntn.createStatement();
	     
//------------------------CREATE DATABASE TABLE-------------------------
  	     DatabaseMetaData md = cntn.getMetaData();
	     ResultSet rs = md.getTables(null, null, "CNN_LATEST", null);
	     
	     if (!rs.next()) //if the table doesn't exist in the database
	     {
	    	 System.out.println("RSS feed table doesn't exist");
	    	//create the String that creates a table
	     	String tableQuery = "create table compsci463.CNN_LATEST"+
		    	        "(TITLE varchar(700) NOT NULL, " +
		    	        "LINK varchar(700) NOT NULL, " +
		    	        "DATE varchar(700) NOT NULL)";
		      
		  //Send the table create statement to the database
		  query.executeUpdate(tableQuery); 
	     }
	     else
	     {
	    	 System.out.println("RSS feed table exists");
	     }
//----------------------------------------------------------------------

//---------------------------POPULATE TABLE-----------------------------
		createFile();
		//build the above resulting XML doc as a file in Java
		File xmlfile = new File("cnnxml.txt");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlfile);
		
		//in cnn xml file, each news item is contained within an <item> tag
		NodeList nList = doc.getElementsByTagName("item");
		
		//loop through each <item> node and grab each child title, link and pubDate
		for(int i=0; i < nList.getLength(); i++)
		{
			Node n = nList.item(i);			
				//store each child in a corresponding string
				Element e = (Element)n;
			      
				String title = e.getElementsByTagName("title").item(0).getTextContent();
				title = title.replace("'", "''");
				
				String link = e.getElementsByTagName("link").item(0).getTextContent();
				
				String date = e.getElementsByTagName("pubDate").item(0).getTextContent();
				
				//send these strings to the database table
				query.executeUpdate("insert into compsci463.CNN_LATEST values('"+title+"', '"+link+"', '"+date+"')");
		}
		System.out.println("XML items added");
		
//--------------------------CREATE NAME TABLE-----------------------------
		 md = cntn.getMetaData();
	     ResultSet nameRS = md.getTables(null, null, "pealm1", null);
	     
	     if (!nameRS.next()) //if the table doesn't exist in the database
	     {
	    	 System.out.println("Second table doesn't exist");
	    	//create the String that creates a table
	     	String nameTableQuery = "create table compsci463.pealm1"+
		    	        "(NAME varchar(700) NOT NULL, " +
		    	        "POPULATED varchar(700) NOT NULL)";
		      
		  //Send the table create statement to the database
		  query.executeUpdate(nameTableQuery);
		  System.out.println("Second table created");
	     }
	     else
	     {
	    	 System.out.println("Second table exists");
	     }
//-------------------------------------------------------------------------
	     
//--------------------------POPULATE NAME TABLE-----------------------------
	     //connect to time-c.nist.gov
	     InetAddress addr = InetAddress.getByName("129.6.15.30");
	     DatagramSocket s = new DatagramSocket();
	     System.out.println("Connected to: "+addr.getHostName());
	     
	     //send a 32-bit packet requesting information
	     byte[] buf = new byte[4];
	     DatagramPacket p = new DatagramPacket(buf, buf.length, addr, 37);
	     s.send(p);
	     
	     //receive 32-bit binary packet from NIST server
	     DatagramPacket dp = new DatagramPacket(buf, buf.length);
	     s.receive(dp);
	     
	     //call a data input stream to properly read the binary packet data
	     DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dp.getData(), 0, dp.getLength()));

	     int time = dis.readInt(); //time in seconds since Jan 1 1900
	     System.out.println("Received: "+time);
	     dis.close(); //close data input stream
	     
	     long timeSince1900 = Integer.toUnsignedLong(time); //the instant of this number is 70 years in the future

	     long yearDiff1970to1900 = 2208988800L; //2208988800L represents time between Jan. 1 1970 and Jan. 1 1900

	     long timeSince1970 = timeSince1900 - yearDiff1970to1900; //subtract difference to pull received number back 70 years to present

	     Instant instant = Instant.ofEpochSecond(timeSince1970); //create instant to convert into proper timestamp
		
		 s.close();
	     query.executeUpdate("insert into compsci463.pealm1 values('CNN_LATEST', '"+instant+"')");
	     System.out.println("Added "+instant+" as timestamp");
//--------------------------------------------------------------------------
		}catch(Exception ex){ex.printStackTrace();}
	
	}//main args close
}//class close