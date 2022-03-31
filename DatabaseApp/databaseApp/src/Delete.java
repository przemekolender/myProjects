import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Delete extends CUDfuncs{

    Delete(Connection con){
        super(con);
    }
    //processes deletes, asks for condition
    void makeDelete(String table) throws SQLException {
        System.out.println("Type in condition to Delete, put strings in '',\n" +
                "for example: name = 'Doe' or grades = 5.\n" +
                "Columns in table " + table + ": " + pasteArray(getCols(table, false)) + ".");

        Scanner s = new Scanner(System.in);
        String input = s.nextLine();
        if (input.equalsIgnoreCase("exit")){
            run = false;
        }
        else{
            try{
                PreparedStatement ps = con.prepareStatement("DELETE FROM " + table + " WHERE " + input);
                ps.executeUpdate();
                ps.close();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
