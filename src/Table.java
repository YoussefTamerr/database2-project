import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

public class Table implements Serializable {
   String name;
	private static final long serialVersionUID = 1L;

	Vector<Page> rows;
   String PK;
   Hashtable<String, String> colNameType;
   Hashtable<String, String> colNameMin;
   Hashtable<String, String> colNameMax;

   Vector<String> serializedFilesName;
	Vector<String> IndexFilesName;
   public Table(String name,String PK,Hashtable<String, String> colNameType,Hashtable<String,String> colNameMin,Hashtable<String,String> colNameMax)
   {
	   rows = new Vector<Page>();
	   this.name=name;
	   this.PK=PK;
	   this.colNameType=colNameType;
	   this.colNameMax=colNameMax;
	   this.colNameMin=colNameMin;
	   serializedFilesName = new Vector<String>();
	   IndexFilesName = new Vector<String>();
   }
   
   public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public Vector<Page> getRows() {
	return rows;
}

public void setRows(Vector<Page> rows) {
	this.rows = rows;
}

public String getPK() {
	return PK;
}

public void setPK(String pK) {
	PK = pK;
}

public Hashtable<String, String> getColNameType() {
	return colNameType;
}

public void setColNameType(Hashtable<String, String> colNameType) {
	this.colNameType = colNameType;
}

public Hashtable<String, String> getColNameMin() {
	return colNameMin;
}

public void setColNameMin(Hashtable<String, String> colNameMin) {
	this.colNameMin = colNameMin;
}

public Hashtable<String, String> getColNameMax() {
	return colNameMax;
}

public void setColNameMax(Hashtable<String, String> colNameMax) {
	this.colNameMax = colNameMax;
}

public Vector<Page> geTable()
   {
	   return rows;
   }

}
