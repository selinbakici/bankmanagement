package bankmanagement;

import DB.DBConnection;
import DB.DeleteDatabase;
import DB.DisplayDatabase;
import DB.QueryDatabase;
import java.net.URL;
import java.sql.Connection;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

public class TransactoinSceneController implements Initializable {

    @FXML
    private AnchorPane tranServAnchor;
    @FXML
    private TextField tAccNum;
    @FXML
    private Button addTranBtn;
  
    @FXML
    private Button srchTranServBtn1;

     DisplayDatabase tData = new DisplayDatabase();
    ObservableList<String> accList = FXCollections.observableArrayList();
    ObservableList<String> typeList = FXCollections.observableArrayList();
    @FXML
    private TextField tAmount;
    @FXML
    private ComboBox<String> ctType;
    @FXML
    private Label tWarnMsg;
    @FXML
    private DatePicker tDate;
    @FXML
    private TableView<?> tTableView;
    @FXML
    private TextField sAccNum;
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
      
      //AutoCompletionBinding<String> autoT = TextFields.bindAutoCompletion(tAccNum,accList); 
     
      typeList.add("Kredi");
      typeList.add("Borc");
      ctType.setItems(typeList);
      tData.buildData(tTableView, "Select * from transactiontable");
      
    }        
    @FXML
    private void AddTransaction(ActionEvent event) {
        LocalDate date = tDate.getValue();
        String accNum = tAccNum.getText();
        String tType = ctType.getValue();
        String amount = tAmount.getText();
        
        if(date==null){
        tWarnMsg.setText("Lütfen tarihi giriniz.");
            tDate.requestFocus();
            return;
        }
        
         if(accNum==null || accNum.isEmpty()){
           tWarnMsg.setText("Lütfen hesap numarasý giriniz.");
            tAccNum.requestFocus();
            return;
        }
         
          if(tType==null || tType.isEmpty()){
           tWarnMsg.setText("Lütfen iþlem türü giriniz.");
            ctType.requestFocus();
            return;
        }
          
        if(amount==null || amount.isEmpty()){
           tWarnMsg.setText("Lütfen tutar giriniz.");
            tAmount.requestFocus();
            return;
        }
        
        double amt =  Double.parseDouble(amount);
        double balance =0;
        try{ 
           
                
               ResultSet rs = QueryDatabase.query("Select Balance from accounttable where Account_Number ='"+accNum+"';");
               if(rs!=null){
                   if(rs.next()){
                       balance = Double.parseDouble(rs.getString(1));
                       if(tType.equalsIgnoreCase("Borc")){
                         if(balance<=0){
                            tWarnMsg.setText("Hesapta yeterli bakiye yok.");
                             return;
                       }
                       }
                   }else{
                   tWarnMsg.setText("Geçersiz hesap numarasý.");
                   return;
               }
               }else{
                   tWarnMsg.setText("Geçersiz hesap numarasý.");
                    return;
               }
                                                
        Connection c;
      
        c = DBConnection.connect();
        String query = "INSERT INTO transactiontable (Date,Account_Num,Transaction_Type,Amount)VALUES("+
                            "'"+date+"',\n" +
                            "'"+accNum+"',\n" +
                            "'"+tType+"',\n" +
                            "'"+amt+"');";                   
        
         c.createStatement().execute(query);
        if(tType.equalsIgnoreCase("Borc")){
         balance-=amt;
        }else{
         balance+=amt;
        }
        
        query = "Update accounttable set Balance='"+balance+"' where Account_Number='"+accNum+"';";
         c.createStatement().execute(query);
         
          c.close();
          } catch (SQLException ex) {
                Logger.getLogger(TransactoinSceneController.class.getName()).log(Level.SEVERE, null, ex);
          }
       
        
        tData.buildData(tTableView, "Select * from transactiontable");
        
        clearFields();              
    }

    @FXML
    private void searchT(ActionEvent event) {
        String query = "Select * from transactiontable Where ;";
        String accNum = sAccNum.getText();
        if(accNum == null || accNum.isEmpty()){
         query = "Select * from transactiontable;"; 
        }else{
         query+="Account_Num='"+accNum+"';";
        }
        
         tData.buildData(tTableView, query);
    }

    private void clearFields() {

        tDate.setValue(LocalDate.now());
        tAccNum.clear();
        ctType.setValue("");
        tAmount.clear();
        tWarnMsg.setText("");                
    }

    @FXML
    private void deleteTransaction(ActionEvent event) {
        
        try {
            int index = tTableView.getSelectionModel().getFocusedIndex();
            ObservableList<ObservableList> data = tData.getData();
            ObservableList<String> itemData = data.get(index);
            
            int id = Integer.parseInt(itemData.get(0));            
            
            Connection c;
            
            c = DBConnection.connect();
            String query="";
            if(itemData.get(3).equalsIgnoreCase("Borc")){
                
                query = "Update accounttable set Balance=Balance+"+itemData.get(4)+" where Account_Number='"+itemData.get(2)+"';";
            }else{
                query = "Update accounttable set Balance=Balance-"+itemData.get(4)+" where Account_Number='"+itemData.get(2)+"';";
                
            }
            
            c.createStatement().execute(query);
            
            c.close();
            
            DeleteDatabase.deleteRecord(id, "Transactiontable");
            tData.buildData(tTableView, "Select * from transactiontable;");
        } catch (SQLException ex) {
            Logger.getLogger(TransactoinSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }                       
    }    
}
