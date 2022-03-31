import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class CUDfuncs {
    Connection con;
    boolean run = true;

    CUDfuncs(Connection con){
        this.con = con;
    }

    //function used to choose table with  which we want to work
    String chooseTable(){
        String choosenTable;
        String[] tabels = {"students", "teachers", "courses", "grades"};
        do{
            Scanner s = new Scanner(System.in);
            System.out.println("Choose table to proceed operation (Students, Teachers, Courses, Grades): ");
            choosenTable = s.nextLine().toLowerCase(Locale.ROOT);
            if (Arrays.asList(tabels).contains(choosenTable)){
                break;
            }
            else if (choosenTable.equalsIgnoreCase("exit")) {
                run = false;
                break;
            }
            else  {
                System.out.println("Choosen table doesn't exists, try again: ");
            }

        }while(true);
        return choosenTable;
    }

    //gets columns names through sql
    String[] getCols(String table, boolean notIncludeIndex) throws SQLException {
        ArrayList<String> cols= new ArrayList<>();
        ResultSet rs = con.createStatement().executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table + "'");
        while (rs.next()){
            cols.add(rs.getString("COLUMN_NAME"));
        }
        rs.close();
        if (!table.equals("grades") && notIncludeIndex){
            cols.remove(0);
        }
        return cols.toArray(new String[0]);
    }

    //allwos to choose column we want to update/insert
    String[] chooseCols(String[] cols)  {
        String names = pasteArray(cols);
        for (int i = 0; i < cols.length; i++) {
            cols[i] = cols[i].toLowerCase(Locale.ROOT);
        }
        System.out.println("Choose columns you want to insert and type in their names\nseparating them with a comma.\nAvalable columns are " + names);
        boolean exit;
        while (true) {
            exit = true;
            Scanner s = new Scanner(System.in);
            String input = s.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                run = false;
                break;
            }
            String[] dividedInput = divideString(input);
            for (String value : dividedInput) {
                if (!Arrays.asList(cols).contains(value)) {
                    exit = false;
                    System.out.println("At least one column name was incorrect, try again.");
                    break;
                }

            }

            if(exit) return dividedInput;
        }
        return null;
    }

    //pastes string array into one string
    String pasteArray(String[] arr){
        StringBuilder paste = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if(i == arr.length - 1){
                paste.append(arr[i]);
            }
            else{
                paste.append(arr[i]).append(", ");
            }
        }
        return paste.toString();
    }

    //divides one string for array
    String[] divideString(String input){
        input = input.replace(" ", "");
        Scanner s2 = new Scanner(input).useDelimiter(",");
        ArrayList<String> a = new ArrayList<>();
        while (s2.hasNext()) {
            a.add(s2.next().toLowerCase(Locale.ROOT));
        }
        return a.toArray(new String[0]);
    }

    public boolean getRun() {
        return run;
    }
}