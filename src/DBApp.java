import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;



public class DBApp {

	static boolean ranOnce = false;

	String MaximumRowsCountinTablePage;
	String MaximumEntriesinOctreeNode;

	public DBApp() {

	}

	public void init() {

	}

	public void readConfig() throws DBAppException {
		File f = new File("src/resources/DBApp.config");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			throw new DBAppException();
		}
		Properties p = new Properties();
		try {
			p.load(fis);
		} catch (IOException e) {
			throw new DBAppException();
		}
		MaximumRowsCountinTablePage = p.getProperty("MaximumRowsCountinTablePage");
		MaximumEntriesinOctreeNode = p.getProperty("MaximumEntriesinOctreeNode");
	}

	public static Vector<Vector<String>> readCSV() throws DBAppException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("src/resources/" + "metadata.csv"));
		} catch (FileNotFoundException e) {
			throw new DBAppException();
		}
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
			throw new DBAppException();
		}
		Vector<Vector<String>> vecvec = new Vector<>();
		int c = 0;
		while (line != null) {
			String[] content = line.split(",");
//			if (c == 0) {
//				c++;
//				try {
//					line = br.readLine();
//				} catch (IOException e) {
//					throw new DBAppException();
//				}
//				continue;
//			}
			Vector<String> vec = new Vector<>(Arrays.asList(content));
			vecvec.add(vec);

			try {
				line = br.readLine();
			} catch (IOException e) {
				throw new DBAppException();
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			throw new DBAppException();
		}
		return vecvec;
	}

	static String getAlphaNumericString() {
		int n = 20;
		// choose a Character random from this String
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
				+ "0123456789"
				+ "abcdefghijklmnopqrstuvxyz";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {

			// generate a random number between
			// 0 to AlphaNumericString variable length
			int index
					= (int) (AlphaNumericString.length()
					* Math.random());

			// add Character one by one in end of sb
			sb.append(AlphaNumericString
					.charAt(index));
		}

		return sb.toString();
	}

	public void createTable(String strTableName, String strClusteringKeyColumn, Hashtable<String, String> htblColNameType,
							Hashtable<String, String> htblColNameMin, Hashtable<String, String> htblColNameMax) throws DBAppException {

		for (Map.Entry<String, String> entry : htblColNameType.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			if (v.toLowerCase().compareTo("java.lang.double") != 0 && v.toLowerCase().compareTo("java.lang.integer") != 0 &&
					v.toLowerCase().compareTo("java.lang.string") != 0 && v.toLowerCase().compareTo("java.util.date") != 0) {
				throw new DBAppException();
			}
		}

		File fq = new File("src/resources/data/" + strTableName + ".class");
		if (fq.exists()) {
			throw new DBAppException();
		}


		String Colname;
		String Coltype;
		String Clusterkey = "false";
		String Min;
		String Max;
		Table table = new Table(strTableName, strClusteringKeyColumn, htblColNameType, htblColNameMin, htblColNameMax);
		serializeTable(table, "src/resources/data/" + strTableName + ".class");
		File f = new File("src/resources/data/" + strTableName);
		f.mkdir();

		Enumeration<String> enu = htblColNameType.keys();
		File q = new File("src/resources/" + "metadata.csv");
		boolean x= true;

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter("src/resources/" + "metadata.csv", true));
		} catch (IOException e) {
			throw new DBAppException();
		}
		StringBuilder sb = new StringBuilder();

		if (!x) {
			ranOnce = true;
			sb.append("Table Name");
			sb.append(",");
			sb.append("Column Name");
			sb.append(",");
			sb.append("Column Type");
			sb.append(",");
			sb.append("Clusterkey");
			sb.append(",");
			sb.append("IndexName");
			sb.append(",");
			sb.append("IndexType");
			sb.append(",");
			sb.append("min");
			sb.append(",");
			sb.append("max");
			sb.append("\r\n");
		}

		while (enu.hasMoreElements()) {
			Colname = enu.nextElement().toString();
			Coltype = htblColNameType.get(Colname);
			if (Colname == strClusteringKeyColumn) {
				Clusterkey = "true";
			}
			Min = htblColNameMin.get(Colname);
			Max = htblColNameMax.get(Colname);
			sb.append(strTableName);
			sb.append(",");
			sb.append(Colname);
			sb.append(",");
			sb.append(Coltype);
			sb.append(",");
			sb.append(Clusterkey);
			sb.append(",");
			sb.append("null");
			sb.append(",");
			sb.append("null");
			sb.append(",");
			sb.append(Min);
			sb.append(",");
			sb.append(Max);
			sb.append("\r\n");
		}
		pw.flush();
		pw.write(sb.toString());
		pw.close();
	}

	public void serialize(Page p, String fileName) throws DBAppException {
		try {
			File f = new File(fileName);
			FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(p);
			objectOutputStream.close();
			fileOutputStream.close();
		} catch (IOException e) {
			throw new DBAppException();
		}
	}



	public void serializeIndex(Octree p, String fileName) throws DBAppException {
		try {
			File f = new File(fileName);
			FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(p);
			objectOutputStream.close();
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new DBAppException();
		}
	}

	public Octree deserializeIndex(String fileName) throws DBAppException {
		Octree p = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(fileName);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			p = (Octree) objectInputStream.readObject();
			objectInputStream.close();
			fileInputStream.close();
		} catch (IOException e) {
			throw new DBAppException();
		} catch (ClassNotFoundException e) {
			throw new DBAppException();
		}
		return p;
	}

	public Page deserialize(String fileName) throws DBAppException {
		Page p = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(fileName);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			p = (Page) objectInputStream.readObject();
			objectInputStream.close();
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new DBAppException();
		} catch (ClassNotFoundException e) {
			throw new DBAppException();
		}
		return p;
	}

	public void serializeTable(Table p, String fileName) throws DBAppException {
		try {
			File f = new File(fileName);
			FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(p);
			objectOutputStream.close();
			fileOutputStream.close();
		} catch (IOException e) {
			throw new DBAppException();
		}
	}

	public Table deserializeTable(String fileName) throws DBAppException {
		Table p = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(fileName);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			p = (Table) objectInputStream.readObject();
			objectInputStream.close();
			fileInputStream.close();
		} catch (IOException | ClassNotFoundException e) {
			throw new DBAppException();
		}
		return p;
	}

	public static int hashString(String str) {
		int hash = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = 31 * hash + str.charAt(i);
		}
		if (hash < 0) return hash * -1;
		return hash;
	}

	public static int hashString2(String str) {
		int hash = 5381;
		for (int i = 0; i < str.length(); i++) {
			hash = ((hash << 5) + hash) + str.charAt(i);
		}
		return hash;
	}
//	public static int hashString(String str) {
//		int hash = 0;
//		for (int i = 0; i < str.length(); i++) {
//			hash = 31 * hash + str.charAt(i);
//		}
//		return hash & 0x7FFFFFFF; // mask the sign bit to get a non-negative int
//	}

	public static int hashDouble(double d) {
		final long prime = 31L;
		long bits = Double.doubleToLongBits(d);
		long hash = 1L;
		hash = prime * hash + (bits ^ (bits >>> 32));
		hash = prime * hash + ((long) Math.floor(d * 1000000));
		return (int) (hash & 0x7FFFFFFF);
	}

	public void createIndex(String strTableName,
							String[] strarrColName) throws DBAppException {
		if (strarrColName.length != 3) throw new DBAppException();

		Hashtable<String, Object> htblColNameValue = new Hashtable<>();
		htblColNameValue.put(strarrColName[0], new Object());
		htblColNameValue.put(strarrColName[1], new Object());
		htblColNameValue.put(strarrColName[2], new Object());



		boolean check = checkcallname(strTableName, htblColNameValue);
		if (!check) throw new DBAppException();

		Table table = deserializeTable("src/resources/data/" + strTableName + ".class");
		Vector<Vector<String>> vecvec = readCSV();
		Vector<Object> mins = new Vector<>();
		Vector<Object> maxs = new Vector<>();
		Vector<String> order = new Vector<>();

		for (int i = 0; i < vecvec.size(); i++) {
//			System.out.println(vecvec.get(i).get(0));
//			System.out.println(strTableName);
			if (vecvec.get(i).get(0).equals(strTableName)) {
				if (vecvec.get(i).get(1).equals(strarrColName[0]) || vecvec.get(i).get(1).equals(strarrColName[1]) || vecvec.get(i).get(1).equals(strarrColName[2])) {
					vecvec.get(i).set(4, strarrColName[0]+strarrColName[1]+strarrColName[2]+"Index");
					vecvec.get(i).set(5, "Octree");
					order.add(vecvec.get(i).get(1));
					switch (vecvec.get(i).get(2).toLowerCase()) {
						case "java.lang.double":
//							String temp="";
//							String work=vecvec.get(i).get(6);
//							for(int k=0;k<work.length();k++){
//								if(work.charAt(k)!='.')
//									temp+=work.charAt(k);
//								else{
//									temp+=work.charAt(k);
//									temp+=work.charAt(k+1);
//									break;
//								}
//							}
							Double temp = Double.parseDouble(vecvec.get(i).get(6));
							mins.add(temp);
//							temp="";
//							work=vecvec.get(i).get(7);
//							for(int k=0;k<work.length();k++){
//								if(work.charAt(k)!='.')
//									temp+=work.charAt(k);
//								else{
//									temp+=work.charAt(k);
//									temp+=work.charAt(k+1);
//									break;
//								}
//							}
							temp = Double.parseDouble(vecvec.get(i).get(7));
							maxs.add(temp);
							break;
						case "java.lang.string":
							mins.add(vecvec.get(i).get(6));
							maxs.add(vecvec.get(i).get(7));
							break;
						case "java.lang.integer":
							mins.add(Integer.parseInt(vecvec.get(i).get(6)));
							maxs.add(Integer.parseInt(vecvec.get(i).get(7)));
							break;
						case "java.util.date":
							SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
							Date d1;
							Date d2;
							try {
								d1 = sdformat.parse(vecvec.get(i).get(6));
								d2 = sdformat.parse(vecvec.get(i).get(7));
							} catch (ParseException e) {
								e.printStackTrace();
								throw new DBAppException();
							}
							mins.add(d1);
							maxs.add(d2);
							break;
						default:
							break;
					}


				}
			}
		}

//		for (int i = 0; i < mins.size(); i++) {
//			if (mins.get(i) instanceof String) {
//				int insert = hashString(mins.get(i).toString());
//				mins.add(i, insert);
//				mins.remove(i + 1);
//			}
//			if (maxs.get(i) instanceof String) {
//				int insert = hashString(maxs.get(i).toString());
//				maxs.add(i, insert);
//				maxs.remove(i + 1);
//			}
//		}
//		System.out.print("Mins: ");
//		System.out.print(mins.size());
//		for (int i = 0; i < mins.size(); i++) {
//
//			System.out.print(mins.get(i) + " ");
//
//		}
//		System.out.println();
//		System.out.print("Maxs: ");
//		for (int i = 0; i < maxs.size(); i++) {
//
//			System.out.print(maxs.get(i) + " ");
//		}

		readConfig();
		String IndexName = "src/resources/data/"+strTableName+"_"+order.get(0)+"_"+order.get(1)+"_"+order.get(2)+"_Index"+".class";

		Octree oct = new Octree(mins.get(0), mins.get(1), mins.get(2), maxs.get(0), maxs.get(1), maxs.get(2), Integer.parseInt(MaximumEntriesinOctreeNode));
		if(!table.rows.isEmpty()) {
			for (int i = 0; i < table.rows.size(); i++) {
				String fil = table.serializedFilesName.get(i);
				Page p = deserialize(fil);
				for (int j = 0; j < p.tuples.size() ; j++) {
					Hashtable<String, Object> tuple = p.tuples.get(j);
					String[] ord = IndexName.split("_");
					Object o1 = tuple.get(ord[1]);
					Object o2 = tuple.get(ord[2]);
					Object o3 = tuple.get(ord[3]);
					oct.insert(o1, o2, o3, fil);
				}
				p = null;
			}
		}
		table.IndexFilesName.add(IndexName);

		serializeIndex(oct, IndexName);
		serializeTable(table, "src/resources/data/" + strTableName + ".class");

		//update csv
		File f = new File("src/resources/" + "metadata.csv");
		f.delete();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter("src/resources/" + "metadata.csv", true));
		} catch (IOException e) {
			throw new DBAppException();
		}
		StringBuilder sb = new StringBuilder();

		sb.append("Table Name");
		sb.append(",");
		sb.append("Column Name");
		sb.append(",");
		sb.append("Column Type");
		sb.append(",");
		sb.append("Clusterkey");
		sb.append(",");
		sb.append("IndexName");
		sb.append(",");
		sb.append("IndexType");
		sb.append(",");
		sb.append("min");
		sb.append(",");
		sb.append("max");
		sb.append("\r\n");

		for (int i = 0; i < vecvec.size() ; i++) {
			sb.append(vecvec.get(i).get(0));
			sb.append(",");
			sb.append(vecvec.get(i).get(1));
			sb.append(",");
			sb.append(vecvec.get(i).get(2));
			sb.append(",");
			sb.append(vecvec.get(i).get(3));
			sb.append(",");
			sb.append(vecvec.get(i).get(4));
			sb.append(",");
			sb.append(vecvec.get(i).get(5));
			sb.append(",");
			sb.append(vecvec.get(i).get(6));
			sb.append(",");
			sb.append(vecvec.get(i).get(7));
			sb.append("\r\n");
		}
		pw.flush();
		pw.write(sb.toString());
		pw.close();

	}

	public void insertIntoTable(String strTableName,
								Hashtable<String, Object> htblColNameValue)
			throws DBAppException {


		Table table = deserializeTable("src/resources/data/" + strTableName + ".class");


//		Vector<Vector<String>> vecvec = readCSV();
		boolean check = checkcallname(strTableName, htblColNameValue);
		boolean check1 = checkDataType(strTableName, htblColNameValue);

		if (!check) {
			throw new DBAppException();
		}
		if (!check1) {
			throw new DBAppException();
		}

		if (!htblColNameValue.containsKey(table.getPK())) {
			throw new DBAppException();
		}
		AtomicBoolean fl = new AtomicBoolean(false);
		table.colNameType.forEach((k, v) -> {
			if (!htblColNameValue.containsKey(k)) {
				fl.set(true);
			}
		});
		if(fl.get()) throw new DBAppException();

		readConfig();

		if (table.rows.isEmpty()) {
			Page page = new Page(Integer.parseInt(MaximumRowsCountinTablePage));
			table.rows.add(page);
			page.tuples.add(htblColNameValue);
			String fileName = "src/resources/data/" + strTableName + "/" + getAlphaNumericString();
			fileName += ".class";
			while (table.serializedFilesName.contains(fileName)) {
				fileName = "src/resources/data/" + strTableName + "/" + getAlphaNumericString();
				fileName += ".class";
			}
			for (int i = 0; i <table.IndexFilesName.size() ; i++) {
				Octree oct = deserializeIndex(table.IndexFilesName.get(i));
				String[] ord = table.IndexFilesName.get(i).split("_");
				Object o1 = htblColNameValue.get(ord[1]);
				Object o2 = htblColNameValue.get(ord[2]);
				Object o3 = htblColNameValue.get(ord[3]);
				oct.insert(o1, o2, o3, fileName);
				serializeIndex(oct, table.IndexFilesName.get(i));
			}
			serialize(page, fileName);
			table.serializedFilesName.add(fileName);
			serializeTable(table, "src/resources/data/" + table.getName() + ".class");
			page = null;
			System.gc();
			return;
		}

		String pk = table.getPK();
		boolean test = false;

		int maxPage = table.rows.size() - 1;
		String f = table.serializedFilesName.get(maxPage);
		readConfig();
		Page max = deserialize(f);
		Hashtable<String, Object> maxTuple = max.tuples.get(max.tuples.size() - 1);
		if (maxTuple.get(pk).toString().compareTo(htblColNameValue.get(pk).toString()) < 0) {
			if (max.tuples.size() == Integer.parseInt(MaximumRowsCountinTablePage)) {
				Page page = new Page(Integer.parseInt(MaximumRowsCountinTablePage));
				page.tuples.add(htblColNameValue);
				table.rows.add(page);
				//int num = newCount1 + 1;/////////////////////////////////////////////////////////////////////////////////////////
				String fileName = "src/resources/data/" + strTableName + "/" + getAlphaNumericString();
				fileName += ".class";
				while (table.serializedFilesName.contains(fileName)) {
					fileName = "src/resources/data/" + strTableName + "/" + getAlphaNumericString();
					fileName += ".class";
				}
				for (int i = 0; i <table.IndexFilesName.size() ; i++) {
					Octree oct = deserializeIndex(table.IndexFilesName.get(i));
					String[] ord = table.IndexFilesName.get(i).split("_");
					Object o1 = htblColNameValue.get(ord[1]);
					Object o2 = htblColNameValue.get(ord[2]);
					Object o3 = htblColNameValue.get(ord[3]);
					oct.insert(o1, o2, o3, fileName);
					serializeIndex(oct, table.IndexFilesName.get(i));
				}
				serialize(page, fileName);
				table.serializedFilesName.add(fileName);
				serializeTable(table, "src/resources/data/" + table.getName() + ".class");
				page = null;
				max = null;
				System.gc();
				return;
			} else {
				max.tuples.add(htblColNameValue);
				for (int i = 0; i <table.IndexFilesName.size() ; i++) {
					Octree oct = deserializeIndex(table.IndexFilesName.get(i));
					String[] ord = table.IndexFilesName.get(i).split("_");
					Object o1 = htblColNameValue.get(ord[1]);
					Object o2 = htblColNameValue.get(ord[2]);
					Object o3 = htblColNameValue.get(ord[3]);
					oct.insert(o1, o2, o3, f);
					serializeIndex(oct, table.IndexFilesName.get(i));
				}
				serialize(max, f);////////////////////////////////////////////////////
				serializeTable(table, "src/resources/data/" + table.getName() + ".class");
				max = null;
				System.gc();
				return;
			}
		}
		max = null;
		System.gc();

		for (int j = 0; j < table.rows.size(); j++) {
			String fil = table.serializedFilesName.get(j);
			Page p = deserialize(fil);
			int count = 0;
			int start = 0;
			int end = p.tuples.size() - 1;
			for (int i = 0; i < p.tuples.size(); i++) {
				int mid = (start + end) / 2;
				Hashtable<String, Object> tuple = p.tuples.get(i);
				if (tuple.get(pk).toString().compareTo(htblColNameValue.get(pk).toString()) < 0) {
					start = mid + 1;
				} else if (tuple.get(pk).toString().compareTo(htblColNameValue.get(pk).toString()) == 0) {
					throw new DBAppException();
				} else {
					if (j == 0) {
						p.tuples.insertElementAt(htblColNameValue, i);
						for (int q = 0; q <table.IndexFilesName.size() ; q++) {
							Octree oct = deserializeIndex(table.IndexFilesName.get(q));
							String[] ord = table.IndexFilesName.get(q).split("_");
							Object o1 = htblColNameValue.get(ord[1]);
							Object o2 = htblColNameValue.get(ord[2]);
							Object o3 = htblColNameValue.get(ord[3]);
							oct.insert(o1, o2, o3, fil);
							serializeIndex(oct, table.IndexFilesName.get(q));
						}
						serialize(p, fil);///////////////////////////////////////
						serializeTable(table, "src/resources/data/" + table.getName() + ".class");
						p = null;
						System.gc();
						redistributeIns(table);
						test = true;
						break;
					} else {
//						int w = newCount-1;//////////////////////////////////////////////////////////////////////////////////
						String fi = table.serializedFilesName.get(j - 1);
						readConfig();
						Page beforeMe = deserialize(fi);
						if (beforeMe.tuples.size() < Integer.parseInt(MaximumRowsCountinTablePage)) {
							beforeMe.tuples.add(htblColNameValue);
							for (int q = 0; q <table.IndexFilesName.size() ; q++) {
								Octree oct = deserializeIndex(table.IndexFilesName.get(q));
								String[] ord = table.IndexFilesName.get(q).split("_");
								Object o1 = htblColNameValue.get(ord[1]);
								Object o2 = htblColNameValue.get(ord[2]);
								Object o3 = htblColNameValue.get(ord[3]);
								oct.insert(o1, o2, o3, fi);
								serializeIndex(oct, table.IndexFilesName.get(q));
							}
							serialize(beforeMe, fi);
							serializeTable(table, "src/resources/data/" + table.getName() + ".class");
							test = true;
							p = null;
							beforeMe = null;
							System.gc();
							break;
						} else {
							p.tuples.insertElementAt(htblColNameValue, i);
							for (int q = 0; q <table.IndexFilesName.size() ; q++) {
								Octree oct = deserializeIndex(table.IndexFilesName.get(q));
								String[] ord = table.IndexFilesName.get(q).split("_");
								Object o1 = htblColNameValue.get(ord[1]);
								Object o2 = htblColNameValue.get(ord[2]);
								Object o3 = htblColNameValue.get(ord[3]);
								oct.insert(o1, o2, o3, fil);
								serializeIndex(oct, table.IndexFilesName.get(q));
							}
							serialize(p, fil);///////////////////////////////////////////////
							serializeTable(table, "src/resources/data/" + table.getName() + ".class");
							redistributeIns(table);
							test = true;
							p = null;
							System.gc();
							break;
						}

					}
				}
			}
			if (test) {
				break;
			}
			p = null;
			System.gc();
		}
	}


	private boolean checkDataType(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
		boolean check = false;
		Vector<Vector<String>> vecvec = readCSV();
		String[] colname = new String[htblColNameValue.keySet().toArray().length];
		for (int j = 0; j < htblColNameValue.keySet().toArray().length; j++) {
			colname[j] = (String) htblColNameValue.keySet().toArray()[j];
			//System.out.println((String) htblColNameValue.keySet().toArray()[j]);
		}
//		String[] colname = (String[]) htblColNameValue.keySet().toArray();

		for (int j = 0; j < colname.length; j++) {
			check = false;
			String current = colname[j];
			Object value = htblColNameValue.get(current);
			for (int k = 0; k < vecvec.size(); k++) {
				if (strTableName.equals(vecvec.get(k).get(0))) {
					if (current.equals(vecvec.get(k).get(1))) {
						switch (vecvec.get(k).get(2).toLowerCase()) {
							case "java.lang.double":
								if (!(value instanceof Double))
									return false;
								if (Double.parseDouble(vecvec.get(k).get(6)) > (Double.parseDouble(value.toString())) || Double.parseDouble(vecvec.get(k).get(7)) < (Double.parseDouble(value.toString()))) {
									return false;
								}
								break;
							case "java.lang.string":
								if (!(value instanceof String))
									return false;
								if ((vecvec.get(k).get(6).compareTo(value.toString().toLowerCase()) > 0) || vecvec.get(k).get(7).compareTo(value.toString().toLowerCase()) < 0) {
									return false;
								}
								break;
							case "java.lang.integer":
								if (!(value instanceof Integer))
									return false;
								if (Integer.parseInt(vecvec.get(k).get(6)) > (Integer.parseInt(value.toString())) || Integer.parseInt(vecvec.get(k).get(7)) < (Integer.parseInt(value.toString()))) {
									return false;
								}
								break;
							case "java.util.date":
								if (!(value instanceof java.util.Date))
									return false;
								SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
								Date min;
								Date max;
								Date val;
								try {
									min = sdformat.parse(vecvec.get(k).get(6));
									max = sdformat.parse(vecvec.get(k).get(7));
									val = ((Date) value);

								} catch (ParseException e) {
									throw new DBAppException();
								}
								if ((min.compareTo(val) > 0) || max.compareTo(val) < 0) {
									return false;
								}
								break;
							default:
								break;
						}
						//break;
					}

				}
			}
//			if(!check)
//				return false;
		}
		return true;
	}

	private boolean checkcallname(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
		boolean check = false;
		Vector<Vector<String>> vecvec = readCSV();
		int i = 0;

		String[] colname = new String[htblColNameValue.keySet().toArray().length];
		for (int j = 0; j < htblColNameValue.keySet().toArray().length; j++) {
			colname[j] = (String) htblColNameValue.keySet().toArray()[j];
			//System.out.println((String) htblColNameValue.keySet().toArray()[j]);
		}
//		String[] colname = (String[]) htblColNameValue.keySet().toArray();

		for (int j = 0; j < colname.length; j++) {
			check = false;
			String current = colname[j];
			for (int k = 0; k < vecvec.size(); k++) {

				if (strTableName.equals(vecvec.get(k).get(0))) {
					if (current.equals(vecvec.get(k).get(1))) {
						check = true;
						break;
					}

				}
			}
			if (!check)
				return false;
		}
		return true;

	}


	public void redistributeIns(Table table) throws DBAppException {
		for (int i = 0; i < table.rows.size(); i++) {
			String f = table.serializedFilesName.get(i);
			Page p = deserialize(f);
			//Page p = deserialize(table.getName()+"page"+i+".class");
			readConfig();
			if (p.tuples.size() > Integer.parseInt(MaximumRowsCountinTablePage)) {
				if (i == table.rows.size() - 1) {
					Page page = new Page(Integer.parseInt(MaximumRowsCountinTablePage));
					table.rows.add(page);
					String fileName = "src/resources/data/" + table.getName() + "/" + getAlphaNumericString();
					fileName += ".class";
					while (table.serializedFilesName.contains(fileName)) {
						fileName = "src/resources/data/" + table.getName() + "/" + getAlphaNumericString();
						fileName += ".class";
					}
//					serialize(page, fileName);
					table.serializedFilesName.add(fileName);
//					page.tuples.add(table.rows.get(i).tuples.get(table.rows.get(i).tuples.size()-1));
//					table.rows.get(i).tuples.remove(table.rows.get(i).tuples.size()-1);

					page.tuples.add(p.tuples.get(p.tuples.size() - 1));


					for (int q = 0; q <table.IndexFilesName.size() ; q++) {
						Octree oct = deserializeIndex(table.IndexFilesName.get(q));
						String[] ord = table.IndexFilesName.get(q).split("_");
						Object o1 = p.tuples.get(p.tuples.size() - 1).get(ord[1]);
						Object o2 = p.tuples.get(p.tuples.size() - 1).get(ord[2]);
						Object o3 = p.tuples.get(p.tuples.size() - 1).get(ord[3]);
						oct.remove(o1, o2, o3, f);
						oct.insert(o1, o2, o3, fileName);
						serializeIndex(oct, table.IndexFilesName.get(q));
					}
					p.tuples.remove(p.tuples.size() - 1);

					//int num = newCount+1;/////////////////////////////////////////////////////////////////////////////////
					//String fi = table.serializedFilesName.get(i+1);
					serialize(page, fileName);

					Page erase = null;
					serialize(erase, f);///////////////////////////////////////////////
					serialize(p, f);//////////////////////////////////////////////////////
					serializeTable(table, "src/resources/data/" + table.getName() + ".class");
					page = null;
					p = null;
					System.gc();
				} else {
					//table.rows.get(i+1).tuples.insertElementAt(table.rows.get(i).tuples.get(table.rows.get(i).tuples.size()-1),0);
					//table.rows.get(i).tuples.remove(table.rows.get(i).tuples.size()-1);
					//int n = newCount+1;//////////////////////////////////////////////////////////////////////////////////////////////////
					String fi = table.serializedFilesName.get(i + 1);
					Page next = deserialize(fi);
					next.tuples.insertElementAt(p.tuples.get(p.tuples.size() - 1), 0);


					for (int q = 0; q <table.IndexFilesName.size() ; q++) {
						Octree oct = deserializeIndex(table.IndexFilesName.get(q));
						String[] ord = table.IndexFilesName.get(q).split("_");
						Object o1 = p.tuples.get(p.tuples.size() - 1).get(ord[1]);
						Object o2 = p.tuples.get(p.tuples.size() - 1).get(ord[2]);
						Object o3 = p.tuples.get(p.tuples.size() - 1).get(ord[3]);
						oct.remove(o1, o2, o3, f);
						oct.insert(o1, o2, o3, fi);
						serializeIndex(oct, table.IndexFilesName.get(q));
					}
					p.tuples.remove(p.tuples.size() - 1);

					Page erase = null;
					serialize(erase, f);////////////////////////////////////////////////////
					serialize(erase, fi);
					serialize(p, f);////////////////////////////////////////////////////////////////////
					serialize(next, fi);
					serializeTable(table, "src/resources/data/" + table.getName() + ".class");
					p = null;
					next = null;
					System.gc();
				}
			}
			p = null;
			System.gc();
		}
	}

	public void updateTable(String strTableName,
							String strClusteringKeyValue,
							Hashtable<String, Object> htblColNameValue)
			throws DBAppException {


		Table table = deserializeTable("src/resources/data/" + strTableName + ".class");

		if (htblColNameValue.containsKey(table.getPK()) || strClusteringKeyValue.isEmpty()) {
			throw new DBAppException();
		}


		boolean check = checkcallname(strTableName, htblColNameValue);
		boolean check1 = checkDataType(strTableName, htblColNameValue);

		if (!check) {
			throw new DBAppException();
		}
		if (!check1) {
			throw new DBAppException();
		}


		String pk = table.getPK();
		boolean done = false;
		boolean updated = false;

		for (int i = 0; i < table.rows.size(); i++) {
			String f = table.serializedFilesName.get(i);
			Page p = deserialize(f);

			int first1 = 0;
			int last1 = p.tuples.size() - 1;
			int mid1 = (first1 + last1) / 2;
			while (first1 <= last1) {
				Hashtable<String, Object> tuple = p.tuples.get(mid1);
				Object val = tuple.get(pk); // tuple elly fy el table
				int compare = 0;
				if (val instanceof Double) {
					Double strClust = Double.parseDouble(strClusteringKeyValue);
					compare = Double.compare((Double) val, strClust);
				} else if (val instanceof Integer) {
					Integer strClust = Integer.parseInt(strClusteringKeyValue);
					compare = Integer.compare((Integer) val, strClust);
				} else if (val instanceof String) {
					compare = val.toString().compareTo(strClusteringKeyValue);
				} else {
					SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
					Date strClust;
					try {
						strClust = formatter2.parse(strClusteringKeyValue);
					} catch (ParseException e) {
						throw new DBAppException();
					}
					compare = ((Date) val).compareTo(strClust);
				}
				if (compare < 0) {
					first1 = mid1 + 1;
				} else if (compare == 0) {
					int finalMid = mid1;
					Page finalP = p;
					for (int q = 0; q <table.IndexFilesName.size() ; q++) {
						Octree oct = deserializeIndex(table.IndexFilesName.get(q));
						String[] ord = table.IndexFilesName.get(q).split("_");
						Object o1 = p.tuples.get(mid1).get(ord[1]);
						Object o2 = p.tuples.get(mid1).get(ord[2]);
						Object o3 = p.tuples.get(mid1).get(ord[3]);
						oct.remove(o1, o2, o3, f);
						serializeIndex(oct, table.IndexFilesName.get(q));
					}
					p.tuples.get(mid1).forEach((k, v) -> {
						htblColNameValue.forEach((k2, v2) -> {
							if (k.compareTo(k2) == 0) {
								finalP.tuples.get(finalMid).replace(k, v2);

							}
						});
					});
					Page erase = null;
					serialize(erase, f);///////////////////////////////////////////////////////////////
					for (int q = 0; q <table.IndexFilesName.size() ; q++) {
						Octree oct = deserializeIndex(table.IndexFilesName.get(q));
						String[] ord = table.IndexFilesName.get(q).split("_");
						Object o1 = p.tuples.get(mid1).get(ord[1]);
						Object o2 = p.tuples.get(mid1).get(ord[2]);
						Object o3 = p.tuples.get(mid1).get(ord[3]);
						oct.insert(o1, o2, o3, f);
						serializeIndex(oct, table.IndexFilesName.get(q));
					}
					serialize(p, f);//////////////////////////////////////////////////////////////////
					serializeTable(table, "src/resources/data/" + table.getName() + ".class");

					p = null;
					System.gc();
					done = true;
					break;
				} else {
					last1 = mid1 - 1;
				}
				mid1 = (first1 + last1) / 2;
			}
			if (done) break;
		}

	}

	public static Vector<Object> uniqueVector(Vector<Object> vec) {
		HashSet<Object> set = new HashSet<>();
		Vector<Object> result = new Vector<>();
		for (Object obj : vec) {
			if (!set.contains(obj)) {
				set.add(obj);
				result.add(obj);
			}
		}
		return result;
	}

	public void deleteFromTable(String strTableName,
								Hashtable<String, Object> htblColNameValue) throws DBAppException {


		Table table = deserializeTable("src/resources/data/" + strTableName + ".class");

		boolean check = checkcallname(strTableName, htblColNameValue);
		boolean check1 = checkDataType(strTableName, htblColNameValue);
		boolean done=false;
		if (!check) {
			throw new DBAppException();
		}
		if (!check1) {
			throw new DBAppException();
		}

		for (int i = 0; i < table.IndexFilesName.size() ; i++) {
			Octree oct = deserializeIndex(table.IndexFilesName.get(i));
			String[] ord = table.IndexFilesName.get(i).split("_");

			Object o1 = htblColNameValue.get(ord[1]);
			Object o2 = htblColNameValue.get(ord[2]);
			Object o3 = htblColNameValue.get(ord[3]);
			if(o1 != null && o2 != null && o3 != null){
				Object CurrPage = oct.get(o1,o2,o3);
				if(CurrPage instanceof Vector<?>){
					Vector<Object> pages = (Vector) CurrPage;
					for(int v=0;v<pages.size();v++){
						Page p=deserialize(pages.get(v).toString());
						for(int j=0;j<p.tuples.size();j++){
							Hashtable<String,Object> tuple = p.tuples.get(j);
							if(tuple.get(ord[1]).equals(o1) && tuple.get(ord[2]).equals(o2) && tuple.get(ord[3]).equals(o3) ){
								AtomicInteger countKeys = new AtomicInteger();
								htblColNameValue.forEach((k, v1) -> {
									if (tuple.get(k).equals(v1)) {
										countKeys.getAndIncrement();
									}
								});
								if(countKeys.get() == htblColNameValue.size()) {
									int indexOfPage = 0;
									for (int k = 0; k < table.serializedFilesName.size() ; k++) {
										if(table.serializedFilesName.get(k).equals(pages.get(v).toString())) {
											indexOfPage = k;
										}
									}
									oct.remove(tuple.get(ord[1]),tuple.get(ord[2]),tuple.get(ord[3]),pages.get(v));

									p.tuples.remove(tuple);
									if (p.tuples.isEmpty()) {
										table.rows.remove(indexOfPage);
										table.serializedFilesName.remove(indexOfPage);
										File fe = new File(pages.get(v).toString());
										fe.delete();
									} else {
										serialize(p, pages.get(v).toString());
									}
									serializeTable(table, "src/resources/data/" + table.getName() + ".class");
									done=true;
									serializeIndex(oct,table.IndexFilesName.get(i));
									System.gc();
								}
							}
						}
					}
				}
				else{
					if(CurrPage==null) continue;
					Page p=deserialize(CurrPage.toString());
					for(int j=0;j<p.tuples.size();j++){
						Hashtable<String,Object> tuple = p.tuples.get(j);
						if(tuple.get(ord[1]).equals(o1) && tuple.get(ord[2]).equals(o2) && tuple.get(ord[3]).equals(o3) ){
							AtomicInteger countKeys = new AtomicInteger();
							htblColNameValue.forEach((k, v) -> {
								if (tuple.get(k).equals(v)) {
									countKeys.getAndIncrement();
								}
							});
							if(countKeys.get() == htblColNameValue.size()) {
								int indexOfPage = 0;
								for (int k = 0; k < table.serializedFilesName.size() ; k++) {
									if(table.serializedFilesName.get(k).equals(CurrPage.toString())) {
										indexOfPage = k;
									}
								}
								oct.remove(tuple.get(ord[1]),tuple.get(ord[2]),tuple.get(ord[3]),CurrPage);
								p.tuples.remove(tuple);
								if (p.tuples.isEmpty()) {
									table.rows.remove(indexOfPage);
									table.serializedFilesName.remove(indexOfPage);
									File fe = new File(CurrPage.toString());
									fe.delete();
								} else {
									serialize(p, CurrPage.toString());
								}
								serializeTable(table, "src/resources/data/" + table.getName() + ".class");
								done=true;
								serializeIndex(oct,table.IndexFilesName.get(i));
								System.gc();
							}
						}
					}
				}

			}
			if(o1 != null && o2 == null && o3 == null){
				Vector<Object> vec1 = Octree.flattenArray(oct.getX(o1));
				Vector<Object> vec = uniqueVector(vec1);
				for(int k=0;k<vec.size();k++){
					Page p=deserialize(vec.get(k).toString());
					for(int j=0;j<p.tuples.size();j++){
						Hashtable<String,Object> tuple = p.tuples.get(j);
						if(p.tuples.get(j).get(ord[1]).equals(o1))
						{
							AtomicInteger countKeys = new AtomicInteger();
							htblColNameValue.forEach((k1, v) -> {
								if (tuple.get(k1).equals(v)) {
									countKeys.getAndIncrement();
								}
							});
							if(countKeys.get() == htblColNameValue.size()) {
								int indexOfPage = 0;
								for (int t = 0; t < table.serializedFilesName.size() ; t++) {
									if(table.serializedFilesName.get(t).equals(vec.get(k).toString())) {
										indexOfPage = t;
									}
								}
								oct.remove(tuple.get(ord[1]),tuple.get(ord[2]),tuple.get(ord[3]),vec.get(k));
								p.tuples.remove(j);
								j--;

								if (p.tuples.isEmpty()) {
									table.rows.remove(indexOfPage);
									table.serializedFilesName.remove(indexOfPage);
									File fe = new File(vec.get(k).toString());
									fe.delete();
								} else {
									serialize(p, vec.get(k).toString());
								}
								serializeTable(table, "src/resources/data/" + table.getName() + ".class");
								done=true;
								serializeIndex(oct,table.IndexFilesName.get(i));
								System.gc();
							}
						}
					}
				}
			}
			if(o1 == null && o2 != null && o3 == null){
				Vector<Object> vec1 = Octree.flattenArray(oct.getY(o2));
				Vector<Object> vec = uniqueVector(vec1);
				for(int k=0;k<vec.size();k++){
					Page p=deserialize(vec.get(k).toString());
					for(int j=0;j<p.tuples.size();j++){
						Hashtable<String,Object> tuple = p.tuples.get(j);
						if(p.tuples.get(j).get(ord[2]).equals(o2))
						{
							AtomicInteger countKeys = new AtomicInteger();
							htblColNameValue.forEach((k1, v) -> {
								if (tuple.get(k1).equals(v)) {
									countKeys.getAndIncrement();
								}
							});
							if(countKeys.get() == htblColNameValue.size()) {
								int indexOfPage = 0;
								for (int t = 0; t < table.serializedFilesName.size() ; t++) {
									if(table.serializedFilesName.get(t).equals(vec.get(k).toString())) {
										indexOfPage = t;
									}
								}
								oct.remove(tuple.get(ord[1]),tuple.get(ord[2]),tuple.get(ord[3]),vec.get(k));
								p.tuples.remove(j);
								j--;

								if (p.tuples.isEmpty()) {
									table.rows.remove(indexOfPage);
									table.serializedFilesName.remove(indexOfPage);
									File fe = new File(vec.get(k).toString());
									fe.delete();
								} else {
									serialize(p, vec.get(k).toString());
								}
								serializeTable(table, "src/resources/data/" + table.getName() + ".class");
								done=true;
								serializeIndex(oct,table.IndexFilesName.get(i));
								System.gc();
							}
						}
					}
				}
			}
			if(o1 == null && o2 == null && o3 != null){
				Vector<Object> vec1 = Octree.flattenArray(oct.getZ(o3));
				Vector<Object> vec = uniqueVector(vec1);
				for(int k=0;k<vec.size();k++){
					Page p=deserialize(vec.get(k).toString());
					for(int j=0;j<p.tuples.size();j++){
						Hashtable<String,Object> tuple = p.tuples.get(j);
						if(p.tuples.get(j).get(ord[3]).equals(o3))
						{
							AtomicInteger countKeys = new AtomicInteger();
							htblColNameValue.forEach((k1, v) -> {
								if (tuple.get(k1).equals(v)) {
									countKeys.getAndIncrement();
								}
							});
							if(countKeys.get() == htblColNameValue.size()) {
								int indexOfPage = 0;
								for (int t = 0; t < table.serializedFilesName.size() ; t++) {
									if(table.serializedFilesName.get(t).equals(vec.get(k).toString())) {
										indexOfPage = t;
									}
								}
								oct.remove(tuple.get(ord[1]),tuple.get(ord[2]),tuple.get(ord[3]),vec.get(k));
								p.tuples.remove(j);
								j--;


								if (p.tuples.isEmpty()) {
									table.rows.remove(indexOfPage);
									table.serializedFilesName.remove(indexOfPage);
									File fe = new File(vec.get(k).toString());
									fe.delete();
								} else {
									serialize(p, vec.get(k).toString());
								}
								serializeTable(table, "src/resources/data/" + table.getName() + ".class");
								done=true;
								serializeIndex(oct,table.IndexFilesName.get(i));
								System.gc();
							}
						}
					}
				}
			}
			if(o1 != null && o2 != null && o3 == null){
				Vector<Object> vec1 = Octree.flattenArray(oct.getXY(o1,o2));
				Vector<Object> vec = uniqueVector(vec1);
				for(int j=0;j<vec.size();j++){
					Page p=deserialize(vec.get(j).toString());
					for(int k=0;k<p.tuples.size();k++){
						Hashtable<String,Object> tuple = p.tuples.get(k);
						if(p.tuples.get(k).get(ord[1]).equals(o1) && p.tuples.get(k).get(ord[2]).equals(o2)){
							AtomicInteger countKeys = new AtomicInteger();
							htblColNameValue.forEach((k1, v) -> {
								if (tuple.get(k1).equals(v)) {
									countKeys.getAndIncrement();
								}
							});
							if(countKeys.get() == htblColNameValue.size()) {
								int indexOfPage = 0;
								for (int t = 0; t < table.serializedFilesName.size() ; t++) {
									if(table.serializedFilesName.get(t).equals(vec.get(j).toString())) {
										indexOfPage = t;
									}
								}
								oct.remove(tuple.get(ord[1]),tuple.get(ord[2]),tuple.get(ord[3]),vec.get(j));

								p.tuples.remove(k);
								k--;

								if (p.tuples.isEmpty()) {
									table.rows.remove(indexOfPage);
									table.serializedFilesName.remove(indexOfPage);
									File fe = new File(vec.get(j).toString());
									fe.delete();
								} else {
									serialize(p, vec.get(j).toString());
								}
								serializeTable(table, "src/resources/data/" + table.getName() + ".class");
								done=true;
								serializeIndex(oct,table.IndexFilesName.get(i));
								System.gc();
							}

						}
					}
				}
			}
			if(o1 != null && o2 == null && o3 != null){
				Vector<Object> vec1 = Octree.flattenArray(oct.getXZ(o1,o3));
				Vector<Object> vec = uniqueVector(vec1);
				for(int j=0;j<vec.size();j++){
					Page p=deserialize(vec.get(j).toString());
					for(int k=0;k<p.tuples.size();k++){
						Hashtable<String,Object> tuple = p.tuples.get(k);
						if(p.tuples.get(k).get(ord[1]).equals(o1) && p.tuples.get(k).get(ord[3]).equals(o3)){
							AtomicInteger countKeys = new AtomicInteger();
							htblColNameValue.forEach((k1, v) -> {
								if (tuple.get(k1).equals(v)) {
									countKeys.getAndIncrement();
								}
							});
							if(countKeys.get() == htblColNameValue.size()) {
								int indexOfPage = 0;
								for (int t = 0; t < table.serializedFilesName.size() ; t++) {
									if(table.serializedFilesName.get(t).equals(vec.get(j).toString())) {
										indexOfPage = t;
									}
								}
								oct.remove(tuple.get(ord[1]),tuple.get(ord[2]),tuple.get(ord[3]),vec.get(j));
								p.tuples.remove(k);
								k--;

								if (p.tuples.isEmpty()) {
									table.rows.remove(indexOfPage);
									table.serializedFilesName.remove(indexOfPage);
									File fe = new File(vec.get(j).toString());
									fe.delete();
								} else {
									serialize(p, vec.get(j).toString());
								}
								serializeTable(table, "src/resources/data/" + table.getName() + ".class");
								done=true;
								serializeIndex(oct,table.IndexFilesName.get(i));
								System.gc();
							}

						}
					}
				}
			}
			if(o1 == null && o2 != null && o3 != null){
				Vector<Object> vec1 = Octree.flattenArray(oct.getYZ(o2,o3));
				Vector<Object> vec = uniqueVector(vec1);
				for(int j=0;j<vec.size();j++){
					Page p=deserialize(vec.get(j).toString());
					for(int k=0;k<p.tuples.size();k++){
						Hashtable<String,Object> tuple = p.tuples.get(k);
						if(p.tuples.get(k).get(ord[2]).equals(o2) && p.tuples.get(k).get(ord[3]).equals(o3)){
							AtomicInteger countKeys = new AtomicInteger();
							htblColNameValue.forEach((k1, v) -> {
								if (tuple.get(k1).equals(v)) {
									countKeys.getAndIncrement();
								}
							});
							if(countKeys.get() == htblColNameValue.size()) {
								int indexOfPage = 0;
								for (int t = 0; t < table.serializedFilesName.size() ; t++) {
									if(table.serializedFilesName.get(t).equals(vec.get(j).toString())) {
										indexOfPage = t;
									}
								}
								oct.remove(tuple.get(ord[1]),tuple.get(ord[2]),tuple.get(ord[3]),vec.get(j));

								p.tuples.remove(k);
								k--;

								if (p.tuples.isEmpty()) {
									table.rows.remove(indexOfPage);
									table.serializedFilesName.remove(indexOfPage);
									File fe = new File(vec.get(j).toString());
									fe.delete();
								} else {
									serialize(p, vec.get(j).toString());
								}
								serializeTable(table, "src/resources/data/" + table.getName() + ".class");
								done=true;
								serializeIndex(oct,table.IndexFilesName.get(i));
								System.gc();
							}

						}
					}
				}
			}
		}
		if(done) return;



		String pk = table.getPK();


		if ((htblColNameValue.containsKey(pk))) {

			for (int i = 0; i < table.rows.size(); i++) {


				String f = table.serializedFilesName.get(i);
				Page p = deserialize(f);

				int first1 = 0;
				int last1 = p.tuples.size() - 1;
				int mid1 = (first1 + last1) / 2;
				while (first1 <= last1) {
					Hashtable<String, Object> tuple = p.tuples.get(mid1);
					Object val = tuple.get(pk); // tuple elly fy el table
					if (val.toString().compareTo(htblColNameValue.get(pk).toString()) < 0) {
						first1 = mid1 + 1;
					} else if (val.toString().compareTo(htblColNameValue.get(pk).toString()) == 0) {
						AtomicInteger countKeys = new AtomicInteger();
						htblColNameValue.forEach((k, v) -> {
							if (tuple.get(k).equals(v)) {
								countKeys.getAndIncrement();
							}
						});

						if (countKeys.get() == htblColNameValue.size()) {

							for (int q = 0; q <table.IndexFilesName.size() ; q++) {
								Octree oct = deserializeIndex(table.IndexFilesName.get(q));
								String[] ord = table.IndexFilesName.get(q).split("_");
								Object o1 = p.tuples.get(mid1).get(ord[1]);
								Object o2 = p.tuples.get(mid1).get(ord[2]);
								Object o3 = p.tuples.get(mid1).get(ord[3]);
								oct.remove(o1, o2, o3, f);
								serializeIndex(oct, table.IndexFilesName.get(q));
							}
							p.tuples.remove(mid1);
							if (p.tuples.isEmpty()) {
								table.rows.remove(i);//////////////////////////////////////////////////////////////////
								table.serializedFilesName.remove(i);
								File fe = new File(f);/////////////////////////////////////////
								fe.delete();
							} else {
								serialize(p, f);////////////////////////////////////////////////////////
							}
							serializeTable(table, "src/resources/data/" + table.getName() + ".class");
							p = null;
							System.gc();

						}
						return;
					} else {
						last1 = mid1 - 1;
					}
					mid1 = (first1 + last1) / 2;
				}
				p = null;
				System.gc();
			}
		}


		for (int i = 0; i < table.rows.size(); i++) {

			String f = table.serializedFilesName.get(i);
			Page p = deserialize(f);


			for (int j = 0; j < p.tuples.size(); j++) {
				int finalJ = j;
				AtomicInteger countKeys = new AtomicInteger();
				Page finalP = p;
				htblColNameValue.forEach((k, v) -> {

					if (finalP.tuples.get(finalJ).get(k).toString().compareTo(v.toString()) == 0) {
						countKeys.getAndIncrement();
					}
				});
				if (countKeys.get() == htblColNameValue.size()) {

					for (int q = 0; q <table.IndexFilesName.size() ; q++) {
						Octree oct = deserializeIndex(table.IndexFilesName.get(q));
						String[] ord = table.IndexFilesName.get(q).split("_");
						Object o1 = p.tuples.get(j).get(ord[1]);
						Object o2 = p.tuples.get(j).get(ord[2]);
						Object o3 = p.tuples.get(j).get(ord[3]);
						oct.remove(o1, o2, o3, f);
						serializeIndex(oct, table.IndexFilesName.get(q));
					}
					p.tuples.remove(j);
					j--;
					if (p.tuples.isEmpty()) {
						table.rows.remove(i);
						table.serializedFilesName.remove(i);
						File fe = new File(f);
						fe.delete();
					} else {
						serialize(p, f);
					}
					serializeTable(table, "src/resources/data/" + table.getName() + ".class");
				}
			}
			p = null;
			System.gc();
		}
	}

	public static String[] deleteElement(String[] arr, int index) {
		if (index < 0 || index >= arr.length) {
			throw new IndexOutOfBoundsException("Index is out of bounds.");
		}
		String[] result = new String[arr.length - 1];
		int resultIndex = 0;
		for (int i = 0; i < arr.length; i++) {
			if (i != index) {
				result[resultIndex] = arr[i];
				resultIndex++;
			}
		}
		return result;
	}





	public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException{

		for (int i = 0; i < arrSQLTerms.length ; i++) {
			SQLTerm s = arrSQLTerms[i];
			Hashtable<String, Object> htbl = new Hashtable<String, Object>();
			htbl.put(s._strColumnName, s._objValue);
			boolean check1 = checkcallname(s._strTableName, htbl);
			if (!check1) {
				throw new DBAppException();
			}

		}

		Table[] tableNames=new Table[arrSQLTerms.length];

		for(int i=0;i<arrSQLTerms.length;i++){
			tableNames[i]=deserializeTable("src/resources/data/" + arrSQLTerms[i]._strTableName + ".class");
		}
		boolean index = false;
		Vector<Vector<Hashtable<String, Object>>> vecvec = new Vector<Vector<Hashtable<String, Object>>>();
		for (int i = 0; i < arrSQLTerms.length ; i++) {
			int w = i+2;
			if(w < arrSQLTerms.length) {
				index = checkIndex(arrSQLTerms[i], arrSQLTerms[i+1], arrSQLTerms[i+2]);
				if(!(strarrOperators[i].equals("AND") && strarrOperators[i+1].equals("AND")))
					index = false;
			}
			if(index) {
				strarrOperators = deleteElement(strarrOperators, i);
				strarrOperators = deleteElement(strarrOperators, i);
				Octree oct = null;
				Object x=null;
				Object y=null;
				Object z=null;
				String col1="";
				String col2="";
				String col3="";
				String[] ord = new String[5];
				for (int j = 0; j < tableNames[i].IndexFilesName.size() ; j++) {
					oct = deserializeIndex(tableNames[i].IndexFilesName.get(j));
					ord = tableNames[i].IndexFilesName.get(i).split("_");
					col1 = ord[1];
					col2 = ord[2];
					col3 = ord[3];


					Object input1 = arrSQLTerms[i]._objValue;
					Object input2 = arrSQLTerms[i+1]._objValue;
					Object input3 = arrSQLTerms[i+2]._objValue;

					Hashtable<String, Object> htbl = new Hashtable<String, Object>();
					htbl.put(arrSQLTerms[i]._strColumnName, arrSQLTerms[i]._objValue);
					htbl.put(arrSQLTerms[i+1]._strColumnName, arrSQLTerms[i+1]._objValue);
					htbl.put(arrSQLTerms[i+2]._strColumnName, arrSQLTerms[i+2]._objValue);

					if(htbl.get(ord[1]) != null && htbl.get(ord[2]) != null && htbl.get(ord[3]) != null ) {
						x = htbl.get(ord[1]);
						y = htbl.get(ord[2]);
						z = htbl.get(ord[3]);
						break;
					}
				}
				Vector<Hashtable<String, Object>> vec = new Vector<>();
				if(arrSQLTerms[i]._strOperator.equals("=") && arrSQLTerms[i+1]._strOperator.equals("=") && arrSQLTerms[i+2]._strOperator.equals("=") ) {
					assert oct != null;
					Object o = oct.get(x, y, z);
					if(o instanceof Vector<?>) {
						Vector<?> v = (Vector) o;
						for (int j = 0; j < v.size() ; j++) {
							Page p = deserialize(v.get(j).toString());
							for (int k = 0; k < p.tuples.size() ; k++) {
								Hashtable<String, Object> tuple = p.tuples.get(k);
								if(tuple.get(col1).equals(x) && tuple.get(col2).equals(y) && tuple.get(col3).equals(z)) {
									vec.add(tuple);
								}
							}

						}

					}
					else {
						Page p = deserialize(o.toString());
						for (int k = 0; k < p.tuples.size() ; k++) {
							Hashtable<String, Object> tuple = p.tuples.get(k);
							if(tuple.get(col1).equals(x) && tuple.get(col2).equals(y) && tuple.get(col3).equals(z)) {
								vec.add(tuple);
							}
						}
					}
				}
				else  {
					SQLTerm[] sq = new SQLTerm[3];
					for (int j = 0; j < arrSQLTerms.length ; j++) {
						if(arrSQLTerms[j]._strColumnName.equals(ord[1])) {
							sq[0] = arrSQLTerms[j];
						}
					}
					for (int j = 0; j < arrSQLTerms.length ; j++) {
						if(arrSQLTerms[j]._strColumnName.equals(ord[2])) {
							sq[1] = arrSQLTerms[j];
						}
					}
					for (int j = 0; j < arrSQLTerms.length ; j++) {
						if(arrSQLTerms[j]._strColumnName.equals(ord[3])) {
							sq[2] = arrSQLTerms[j];
						}
					}

					Vector<Object> pages= oct.RangeOct(x, y, z, sq);
					for (int j = 0; j < pages.size() ; j++) {
						Page p = deserialize(pages.get(j).toString());
						for (int k = 0; k <p.tuples.size() ; k++) {
							Hashtable<String, Object> tuple = p.tuples.get(k);
							boolean ins = true;
							for (int l = 0; l < arrSQLTerms.length ; l++) {
								Object obj = tuple.get(arrSQLTerms[l]._strColumnName);
								switch (arrSQLTerms[l]._strOperator) {
									case "!=":
										if(compareObject1(obj, arrSQLTerms[l]._objValue) == 0)
											ins = false;
										break;
									case "=":
										if(compareObject1(obj, arrSQLTerms[l]._objValue) != 0)
											ins = false;
										break;
									case ">":
										if(!(compareObject1(obj, arrSQLTerms[l]._objValue) > 0))
											ins = false;
										break;
									case ">=":
										if(!(compareObject1(obj, arrSQLTerms[l]._objValue) >= 0))
											ins = false;
										break;
									case "<=":
										if(!(compareObject1(obj, arrSQLTerms[l]._objValue) <= 0))
											ins = false;
										break;
									case "<":
										if(!(compareObject1(obj, arrSQLTerms[l]._objValue) < 0))
											ins = false;
										break;
									default:break;

								}
							}
							if(ins && !vec.contains(tuple)) vec.add(tuple);
						}
					}
				}
				vecvec.add(vec);

				i=i+2;
				index = false;
			}
			else {
				Vector<Hashtable<String, Object>> vec = getTuples(arrSQLTerms[i]);
				vecvec.add(vec);
			}
		}

		int count = 0;
		while (vecvec.size() > 1) {
			Vector<Hashtable<String, Object>> current = vecvec.get(0);
			Vector<Hashtable<String, Object>> next = vecvec.get(1);
			switch (strarrOperators[count]) {
				case "OR":
					for (int i = 0; i < next.size() ; i++) {
						if(!current.contains(next.get(i))) {
							current.add(next.get(i));
						}
					}
					break;
				case "AND":
					for (int i = 0; i < current.size() ; i++) {
						if(!next.contains(current.get(i))) {
							current.remove(current.get(i));
						}
					}
					break;
				case "XOR":
					for (int i = 0; i < current.size() ; i++) {
						if(next.contains(current.get(i))) {
							current.remove(current.get(i));
							next.remove(current.get(i));
						}
					}
					for (int i = 0; i < next.size() ; i++) {
						if(!current.contains(next.get(i))) {
							current.add(next.get(i));
						}
					}
					break;
				default:break;
			}
			vecvec.remove(next);
			count++;
		}
		if(vecvec.isEmpty()) {
			return null;
		}
		Iterator it = vecvec.get(0).iterator();
		return it;
	}

	private boolean checkIndex(SQLTerm arrSQLTerm, SQLTerm arrSQLTerm1, SQLTerm arrSQLTerm2) throws DBAppException {

		if(arrSQLTerm._strTableName.equals(arrSQLTerm1._strTableName) && arrSQLTerm1._strTableName.equals(arrSQLTerm2._strTableName)) {
			Table table = deserializeTable("src/resources/data/" + arrSQLTerm._strTableName + ".class");
			for (int i = 0; i < table.IndexFilesName.size() ; i++) {
				String index = table.IndexFilesName.get(i);
				String[] ord = index.split("_");
				List<String> arr = Arrays.asList(ord);
				if(arr.contains(arrSQLTerm._strColumnName) && arr.contains(arrSQLTerm1._strColumnName) && arr.contains(arrSQLTerm2._strColumnName)) {
					return true;
				}
			}

		} else {
			return false;
		}
		return false;
	}

	private Vector<Hashtable<String, Object>> getTuples(SQLTerm arrSQLTerm) throws DBAppException {
		Table table = deserializeTable("src/resources/data/" + arrSQLTerm._strTableName + ".class");
		String col = arrSQLTerm._strColumnName;
		Object value = arrSQLTerm._objValue;
		String op = arrSQLTerm._strOperator;
		Vector<Hashtable<String, Object>> res = new Vector<Hashtable<String, Object>>();

		for (int i = 0; i < table.rows.size(); i++) {
			String fileName = table.serializedFilesName.get(i);
			Page p = deserialize(fileName);
			for (int j = 0; j < p.tuples.size() ; j++) {
				Hashtable<String, Object> tuple = p.tuples.get(j);
				Object target = tuple.get(col);
				switch (op) {
					case "=" :
						if(target.equals(value)) {
							res.add(tuple);
						}
						break;
					case "!=":
						if(!target.equals(value)) {
							res.add(tuple);
						}
						break;
					case "<":
						if(compareObject1(target, value) < 0) {
							res.add(tuple);
						}
						break;
					case ">":
						if(compareObject1(target, value) > 0) {
							res.add(tuple);
						}
						break;
					case ">=":
						if(compareObject1(target, value) >= 0) {
							res.add(tuple);
						}
						break;
					case "<=":
						if(compareObject1(target, value) <= 0) {
							res.add(tuple);
						}
						break;
					default:break;
				}
			}

		}
		return res;
	}


	public static int compareObject1(Object o1, Object o2) {
		if(o1 instanceof String) {
			String o11 = (String) o1;
			String o22 = (String) o2;
			return (o11.toLowerCase().compareTo(o22.toLowerCase()));
		} else if(o1 instanceof Double) {
			return ((Double) o1).compareTo(((Double)o2));
		} else if(o1 instanceof Integer) {
			return ((Integer) o1).compareTo(((Integer)o2));
		} else {
			return ((Date) o1).compareTo(((Date)o2));
		}
	}
}
