import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class Update extends CUDfuncs{

    Update(Connection con) {
        super(con);
    }

    //function asking for new values
    String getNewValues(String[] cols){
        String names = pasteArray(cols);

        System.out.println(
                "Type in new values in order of modified columns: " +
                names +
                "\nUse commas between values and put in String values in '',\n" +
                "for example: 'John', 'Doe', 4.");

        Scanner s = new Scanner(System.in);
        String input = s.nextLine();
        if (input.equalsIgnoreCase("exit")) run = false;
        return input;
    }
    //function brings together choosen columns to update and thei new values, returns one String made from them
    String createChanges(String newValues, String[] cols){
        String[] dividedValues = divideString(newValues);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < cols.length; i++){
            if(i == cols.length - 1) s.append(cols[i]).append("=").append(dividedValues[i]);
            else s.append(cols[i]).append("=").append(dividedValues[i]).append(", ");
        }
        return s.toString();
    }


    //function updaing data, asks for update condition and processess the operation
    void makeUpdate(String newValues, String table){
        System.out.println("Type in condition do Upddate\n" +
                "for example: grade > 3");
        Scanner s = new Scanner(System.in);
        String input = s.nextLine();
        if (input.equalsIgnoreCase("exit")){
            run = false;
        }
        else{
            try{
                PreparedStatement ps = con.prepareStatement("UPDATE " + table + " SET " + newValues + " WHERE " + input);
                ps.executeUpdate();
                ps.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    }

}
