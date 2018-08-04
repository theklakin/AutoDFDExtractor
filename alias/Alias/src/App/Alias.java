/*
This example is created using an existing Java file from the securibench Benchmark, namely ALiasing1.java.
The benchmark can be found: https://suif.stanford.edu/~livshits/securibench/
    @author Benjamin Livshits <livshits@cs.stanford.edu>
    
    $Id: Aliasing1.java,v 1.1 2006/04/21 17:14:27 livshits Exp $

*/

package App;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import securibench.micro.BasicTestCase;
import securibench.micro.MicroTestCase;

import Helper.AliasHelper;

public class Alias {
	
	private static final String FIELD_NAME = "name";

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
       String name = req.getParameter(FIELD_NAME);
       String str = name;
       Test t = new Test();
       String tt = t.getDescription(str);
	   AliasHelper k = new AliasHelper();
	   String test = k.getDescription(str);
	   PrintWriter writer = resp.getWriter();
       writer.println(str);                              /* BAD */
    }
	
	//private void temp(String s){
	//	System.out.println(s);
	//}

}
