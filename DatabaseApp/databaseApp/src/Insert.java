import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class Insert extends CUDfuncs{

    Insert(Connection con) {
        super(con);
    }

    //processes insertions, handles typing new records
    void makeInsert(String[] cols, String table){
        String names = pasteArray(cols);

        System.out.println("""
                Type in values to columns, every line will be added to database separately
                Use commas between values and put in String values in '',
                for example: 'John', 'Doe', 4.
                You are updating""" + " " + names);
        Scanner s = new Scanner(System.in);
        while(true){
            String row = s.nextLine();
            if (row.equalsIgnoreCase("exit")) break;
            try{
                PreparedStatement ps = con.prepareStatement("INSERT INTO " + table + "(" + names + ") VALUES (" + row + ")");
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }

        }
    }


}