package DB;
   
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.Connection;  
import java.sql.ResultSet;  
import javafx.beans.property.SimpleStringProperty;  
import javafx.beans.value.ObservableValue;  
import javafx.collections.FXCollections;  
import javafx.collections.ObservableList;  
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;  
import javafx.scene.control.TableColumn.CellDataFeatures;  
import javafx.scene.control.TableView;  
import javafx.util.Callback;  
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;



public class DisplayDatabase{  

    public ObservableList<ObservableList> getData() {
        return data;
    }
 
   private  ObservableList<ObservableList> data;  
    
   public  void buildData(TableView tableview,String SQL){  
       if(!tableview.getColumns().isEmpty())
       tableview.getColumns().clear();
      Connection c ;  
        data = FXCollections.observableArrayList();
       
      try{  
       c = DBConnection.connect();  

       ResultSet rs = c.createStatement().executeQuery(SQL);   
       
       for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){  
         final int j = i;          
         TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));  
       
         col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){            
           public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {                                                 
             return new SimpleStringProperty(param.getValue().get(j).toString());              
           }            
         });  
        
            tableview.getColumns().addAll(col); 
        
         
       }  

       while(rs.next()){  
         ObservableList<String> row = FXCollections.observableArrayList();  
         for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){            
           row.add(rs.getString(i));    
         }   
         data.add(row);  
         
        }  
         tableview.setItems(data);
         
       
      }catch(Exception e){  
        System.out.println("Error on Building Data");        
      }  
    }         
}