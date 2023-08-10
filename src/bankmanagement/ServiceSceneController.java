package bankmanagement;
import DB.DBConnection;
import DB.DeleteDatabase;
import DB.DisplayDatabase;
import DB.QueryDatabase;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

public class ServiceSceneController implements Initializable {

    @FXML
    private Button addServBtn;
    @FXML
    private Label sWarnMsg;
    @FXML
    private Button srchTranServBtn;

    DisplayDatabase tData = new DisplayDatabase();
    ObservableList<String> accList = FXCollections.observableArrayList();
    @FXML
    private TextField sName;
    @FXML
    private TextField sAmount;
    @FXML
    private TextField sDesc;
    @FXML
    private TextField sAccNum;
    @FXML
    private DatePicker sDate;
    @FXML
    private TextField searchAccNum;
    @FXML
    private TableView<?> sTableView;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
         ResultSet rs = QueryDatabase.query("Select Account_Number from accounttable;");
        if(rs!=null){
            try {
                while(rs.next()){
                    accList.add(rs.getString(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(TransactoinSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }      
      //AutoCompletionBinding<String> autoT = TextFields.bindAutoCompletion(sAccNum,accList);          
      tData.buildData(sTableView, "Select * from serviceTable");
    }    


    @FXML
    private void searchS(ActionEvent event) {
        
        String query = "Select * from servicetable Where ;";
        String accNum = searchAccNum.getText();
        if(accNum == null || accNum.isEmpty()){
         query = "Select * from servicetable;"; 
        }else{
         query+="Account_Num='"+accNum+"';";
        }
        
         tData.buildData(sTableView, query);
    }

    @FXML
    private void addService(ActionEvent event) {
         LocalDate date = sDate.getValue();
        String accNum = sAccNum.getText();
        String name = sName.getText();
        String amount = sAmount.getText();
        String desc = sDesc.getText();
        
        if(date==null){
        sWarnMsg.setText("Lütfen tarih giriniz.");
            sDate.requestFocus();
            return;
        }
        
         if(accNum==null || accNum.isEmpty()){
           sWarnMsg.setText("Lütfen hesap numarasýný giriniz.");
            sAccNum.requestFocus();
            return;
        }
         
          if(name==null || name.isEmpty()){
           sWarnMsg.setText("Lütfen hizmet adýný giriniz.");
            sName.requestFocus();
            return;
        }
          
        if(amount==null || amount.isEmpty()){
           sWarnMsg.setText("Lütfen tutar giriniz.");
            sAmount.requestFocus();
            return;
        }
        
         if(desc==null || desc.isEmpty()){
           sWarnMsg.setText("Lütfen açýklama giriniz.");
            sDesc.requestFocus();
            return;
        }
        
        double amt =  Double.parseDouble(amount);
        double balance = 0;
       
        try{ 
                           
               ResultSet rs = QueryDatabase.query("Select Balance from accounttable where Account_Number ='"+accNum+"';");
               if(rs!=null){
                   if(rs.next()){
                       balance = Double.parseDouble(rs.getString(1));
                     
                         if(balance<=amt){
                            sWarnMsg.setText("Hesapta yeterli bakiye yok.");
                             return;
                       }
                      
                   }else{
                   sWarnMsg.setText("Geçersiz hesap numarasý.");
                   return;
               }
               }else{
                   sWarnMsg.setText("Geçersiz hesap numarasý.");
                    return;
               }
            
             Connection c;
      
        c = DBConnection.connect();
        String query = "INSERT INTO transactiontable (Date,Account_Num,Transaction_Type,Amount)VALUES("+
                            "'"+date+"',\n" +
                            "'"+accNum+"',\n" +
                            "'Borc',\n" +
                            "'"+amt+"');";                   
        PreparedStatement ps = c.prepareStatement(query,PreparedStatement.RETURN_GENERATED_KEYS);
        ps.execute();
         rs = ps.getGeneratedKeys();
        rs.next();
        String tId = rs.getString(1);                
         balance-=amt;               
        query = "Update accounttable set Balance='"+balance+"' where Account_Number='"+accNum+"';";
         c.createStatement().execute(query);
         
          query = "INSERT INTO Servicetable (Date,Account_Num,ServiceName,Description,Amount,TransactionId)VALUES("+
                            "'"+date+"',\n" +
                            "'"+accNum+"',\n" +
                             "'"+name+"',\n" +
                            "'"+desc+"',\n" +
                             "'"+amt+"',\n" +
                            "'"+tId+"');"; 
          c.createStatement().execute(query);
          
          c.close();
          } catch (SQLException ex) {
                Logger.getLogger(TransactoinSceneController.class.getName()).log(Level.SEVERE, null, ex);
          }
       
        
        tData.buildData(sTableView, "Select * from servicetable");
        
   clearFields();
    
}

    private void clearFields() {
        sDate.setValue(LocalDate.now());
        sAccNum.clear();
        sName.clear();
        sAmount.clear();
        sDesc.clear();        
    }

    @FXML
    private void deleteService(ActionEvent event) {
       
        try {
            int index = sTableView.getSelectionModel().getFocusedIndex();
            ObservableList<ObservableList> data = tData.getData();
            ObservableList<String> itemData = data.get(index);            
            int tId = Integer.parseInt(itemData.get(5));                       
            Connection c;            
            c = DBConnection.connect();
            String query="";                        
            query = "Update accounttable set Balance=Balance+"+itemData.get(4)+" where Account_Number='"+itemData.get(1)+"';";                    
            c.createStatement().execute(query);                             
            c.close();
            DeleteDatabase.deleteRecord(tId, "Transactiontable");            
            tData.buildData(sTableView, "Select * from servicetable;");
        } catch (SQLException ex) {
            Logger.getLogger(TransactoinSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }                 
    }
}
