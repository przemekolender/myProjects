import java.sql.*;
import java.util.Locale;
import java.util.Scanner;

public class simpleCommands {

     //prints avalable commands
     static void lsCommands(){
        System.out.println("""
                Avalable commands (all are case insensitive):
                ls\t\t\t - print avalable commands
                Students\t - print Students table
                Teachers\t - print Teachers table
                Courses\t\t - print Courses table
                Grades\t\t - print Grades table
                UPDATE\t\t - make aupdate to choosen table
                INSERT\t\t - make insert to choosen table
                DELETE\t\t - delete data from choosen table
                SQLQUERY\t - execute your own SQL command
                REPORT\t\t - create report from database
                EXIT\t\t - close connection and turn off the appilication""");
    }

    //handles printing result of a query
    static void printQuery(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();

        int colCount = rsmd.getColumnCount();
        String format = "%15s";
        for (int i = 1; i <= colCount; i++) System.out.printf(format, rsmd.getColumnLabel(i));
        System.out.print("\n");
        while (rs.next()) {
            for (int i = 1; i <= colCount; i++) {
                String columnValue = rs.getString(i);
                System.out.printf(format, columnValue);
            }
            System.out.print("\n");
        }
    }

    //connects with database and prints table
    static void printTable(Connection con, String query){
        try{
            ResultSet rs = con.createStatement().executeQuery(query);
            printQuery(rs);
            rs.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //allows to process ready queries
    static void proceedQuery(Connection con){
        System.out.println("Type in your query (must be in one line)");
        Scanner s = new Scanner(System.in);
        String input = s.nextLine().toLowerCase(Locale.ROOT);
        if (input.equals("exit")){

        }
        else if (input.startsWith("select")){

            try{
                ResultSet rs = con.createStatement().executeQuery(input);
                printQuery(rs);
                rs.close();

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            try{
                PreparedStatement ps = con.prepareStatement(input);
                ps.executeUpdate();
                ps.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void createReport(Connection con){

         String view =  """
                  CREATE VIEW EndGrades AS (
                  SELECT s.Name, s.Surname, c.CourseName, c.ECTS, ROUND(AVG(Grade), 2) AS CourseMeanGrade,\s
                  ROUND(AVG(Grade*2), 0)/2 AS CourseGrade,
                  (CASE WHEN ROUND(AVG(Grade*2), 0)/2 > 2.5 THEN 1 ELSE 0 END) AS Passed,
                  ROUND(AVG(ROUND(AVG(Grade*2), 0)/2 ) OVER (PARTITION BY Surname),2) AS Average
                  FROM Students s
                  JOIN Grades g ON s.IndexNumber=g.StudentIndexNum
                  JOIN Courses c ON g.CourseID = c.CourseID
                  GROUP BY s.Name, s.Surname, c.CourseName, c.ECTS
                  )
                 """;

        String query1 = """
                SELECT Name, Surname, CourseName, CourseMeanGrade, CourseGrade, Average
                FROM EndGrades ORDER BY Surname
                """;

        String query2 = """
                SELECT Name, Surname, Average, SUM(gainedECTS) AS TotalECTS FROM
                (SELECT *, (Passed * ECTS) AS GainedECTS FROM EndGrades ) AS gt GROUP BY Name, Surname, Average ORDER BY Surname
                """;

        String query3 = """
                SELECT t.Name, t.Surname, c.CourseName, COUNT(*) AS NumberOfStudents, ROUND(AVG(Grade), 2) AS GradesAverage FROM Teachers t
                JOIN Courses c ON t.TeacherID=c.TeacherID
                JOIN Grades g ON c.CourseID = g.CourseID
                GROUP BY t.Name, t.Surname, c.CourseName
                ORDER BY Surname
                """;

        String query4 = """
                SELECT UniversityYear, CourseName, ROUND(AVG(Grade), 2) AS CourseMeanGrade, g.CourseID FROM Students s
                JOIN Grades g on s.IndexNumber=g.StudentIndexNum
                JOIN Courses c ON g.CourseID=c.CourseID
                GROUP BY UniversityYear, CourseName, g.CourseID
                ORDER BY UniversityYear, CourseName
                """;

        String query5 = """
                SELECT TOP 1 Name, Surname, BirthDate, CourseName, ROUND(AVG(Grade), 2) AS GradeAverage FROM Students s
                JOIN Grades g on s.IndexNumber=g.StudentIndexNum
                JOIN Courses c ON g.CourseID=c.CourseID
                WHERE YEAR(BirthDate) < 2000 AND CourseName = 'Databases'
                GROUP BY Name, Surname, BirthDate, CourseName
                ORDER BY ROUND(AVG(Grade), 2) DESC
                """;

        System.out.println("   ###### University Report ######   ");

        try{
            PreparedStatement ps =  con.prepareStatement(view);
            ps.executeUpdate();
            ps.close();

            System.out.println("1. Students average of course grades in each course,\n   course final grades and final grades average:\n");
            simpleCommands.printTable(con, query1);
            System.out.println("\n2. Students final grades average and total number od ECTS:\n");
            simpleCommands.printTable(con, query2);
            System.out.println("\n3. List of courses, teachers taking care of each course, numer of students on course and grades average:\n");
            simpleCommands.printTable(con, query3);
            System.out.println("\n4. Average of grades in every year of University:\n");
            simpleCommands.printTable(con, query4);
            System.out.println("\n5. Student born before 2000 having the best GradeAverage from Databases course\n");
            simpleCommands.printTable(con, query5);

            PreparedStatement ps2 =  con.prepareStatement("DROP VIEW EndGrades");
            ps2.executeUpdate();
            ps2.close();
        }
        catch(Exception e) {e.printStackTrace();}
    }
}