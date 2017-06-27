/**
Copyright (C) 2017 VONGSALAT Anousone

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rest {

	private String query;
	private String index;
	private String aet;
	private String retrieveAET;
	private String authentication;
	private String url;
	private String ip;
	private String port;
	private String fullAddress;
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");

	public Rest(){
		this.port = "8042";
		this.authentication = null;
		int curDb = jprefer.getInt("current database", 0);
		int typeDb = jprefer.getInt("db type" + curDb, 5);
		if(typeDb == 5){
			if(!jprefer.get("db path" + curDb, "none").equals("none")){
				String pathBrut = jprefer.get("db path" + curDb, "none") + "/";
				int index = ordinalIndexOf(pathBrut, "/", 3);
				this.fullAddress = pathBrut.substring(0, index);
			}else{
				String address = jprefer.get("ODBC" + curDb, "localhost");
				String pattern1 = "@";
				String pattern2 = ":";
				Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
				Matcher m = p.matcher(address);
				while(m.find()){
					this.ip = "http://" + m.group(1);
				}
			}
			if(jprefer.get("db user" + curDb, null) != null && jprefer.get("db pass" + curDb, null) != null){
				authentication = Base64.getEncoder().encodeToString((jprefer.get("db user" + curDb, null) + ":" + jprefer.get("db pass" + curDb, null)).getBytes());
			}
		}else{
			this.ip = jpreferPerso.get("ip", "http://localhost");
			this.port = jpreferPerso.get("port", "8042");
			if(jpreferPerso.get("username", null) != null && jpreferPerso.get("username", null) != null){
				authentication = Base64.getEncoder().encodeToString((jpreferPerso.get("username", null) + ":" + jpreferPerso.get("password", null)).getBytes());
			}
		}if(this.aet != null){
			this.setUrl("modalities/" + this.aet + "/query");
		}
	}


	/*
	 * This method gives the server response as a String.
	 * It opens a new connection with the class' url.
	 * The url is changed depending on the calling method
	 */
	private String getOutputStream() throws IOException{

		//  We set the url connection
		URL url = new URL(this.url);
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

		// Calling method is getAET
		if(stackTraceElements[2].getMethodName() == "getAET"){
			this.setUrl("modalities");
			url = new URL(this.url);
		}

		// Calling method is getIndexContent
		if(stackTraceElements[2].getMethodName() == "getIndexContent"){
			url = new URL(this.url + this.index + "/content/");
		}

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		if((fullAddress != null && fullAddress.contains("https")) || (ip != null && ip.contains("https"))){
			try{
				HttpsTrustModifier.Trust(conn);
			}catch (Exception e){
				throw new IOException("Cannot allow self-signed certificates");
			}
		}
		if(this.authentication != null){
			conn.setRequestProperty("Authorization", "Basic " + this.authentication);
		}

		StringBuilder sb = new StringBuilder();
		String line = "";

		// Putting the query in the output stream if getOutputStream has been called by getQueryID (which was called by getQueryAnswerIndexes
		if((stackTraceElements[2].getMethodName() == "getQueryID" && stackTraceElements[3].getMethodName() == "getQueryAnswerIndexes") || (stackTraceElements[2].getMethodName() == "getSeriesDescriptionID" && stackTraceElements[3].getMethodName() != "getSeriesDescription")){
			OutputStream os = conn.getOutputStream();
			os.write(this.query.getBytes());
			os.flush();			
		}// else, if the calling method is retrieve, we set the aet to retrieve in, in the output stream
		else if(stackTraceElements[2].getMethodName() == "retrieve"){
			OutputStream os = conn.getOutputStream();
			os.write(this.retrieveAET.getBytes());
			os.flush();
		}

		// We create a new BufferedReader that we will store into a StringBuilder
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		while (line != null) {
			sb.append(line);
			line = br.readLine();
		}

		// We close the BufferedReader, disconnect and return the String
		br.close();
		conn.disconnect();
		return sb.toString();
	}


	/*
	 *  This method is usually called within other methods to get an Orthanc query ID
	 */
	private String getQueryID(String level, String name, String id, String studyDate, String modality, String studyDescription, String accessionNumber) throws IOException{
		// We re-define the new query
		this.setQuery(level, name, id, studyDate, modality, studyDescription, accessionNumber);
		String conn;

		// We set the url
		this.setUrl("modalities/" + this.aet + "/query");

		// We can use constant values for the substring, the server's outputStream's length being constant
		if(this.getOutputStream() == null){
			return null;
		}
		conn = this.getOutputStream().substring(12, 48);

		return conn;
	}


	/*
	 * This method gets the answer's indexes to an Orthanc query, as an Object[].
	 * An Object[] should be instantiated to store the values inside it.
	 */
	public Object[] getQueryAnswerIndexes(String level, String name, String id, String studyDate, String modality, String studyDescription, String accessionNumber) throws Exception{
		// We call getQueryID to generate a query ID
		String idURL =  this.getQueryID(level, name, id, studyDate, modality, studyDescription, accessionNumber);
		this.setUrl("queries/" + idURL + "/answers/");
		String conn = this.getOutputStream();
		if(conn.equals("[]")){
			return null;
		}

		// We split the server response in a tab
		String serverResponse = conn.replaceAll("[^0-9,]","");
		String[] indexes = serverResponse.split(",");

		// We convert the ArrayList to an Object[]
		return indexes;
	}

	/*
	 * This method returns the content of a specified index.
	 * The index is obtained by using the getQueryAnswerIndexes.
	 */
	public String getIndexContent(String index) throws IOException{
		if(index.equals("")){
			return null;
		}
		this.index = index;
		String content = this.getOutputStream();
		return content;
	}

	/*
	 * This method returns the desired value, it requires a getIndexContent String, and the desired value.
	 * Some values can only be obtained if the contents correspond with the right query level 
	 * (StudyDescription is obtained via the Series level).
	 * These values may be PatientID, PatientName, StudyDate or StudyDescription.
	 */
	public Object getValue(String contents, String desiredValue){
		if(contents == null){
			return null;
		}
		String s1;
		String value = desiredValue;
		// We build a substring s1 to get the part from the contents to the end
		switch (desiredValue) {
		case "StudyDescription":
			s1 = contents.substring(contents.indexOf(value)+59,contents.length());
			break;
		case "PatientName":
			s1 = contents.substring(contents.indexOf(value)+54,contents.length());
			break;
		case "Modality":
			s1 = contents.substring(contents.indexOf(value)+51,contents.length());
			break;
		case "StudyInstanceUID":
			s1 = contents.substring(contents.indexOf(value)+59,contents.length());
			break;
		case "AccessionNumber":
			s1 = contents.substring(contents.indexOf(value)+58,contents.length());
			break;
		default:
			s1 = contents.substring(contents.indexOf(value)+52,contents.length());
			break;
		}

		// We return a substring of s1 (s1 is cut so that we retain only the part before the ")
		return s1.substring(0,s1.indexOf("\""));
	}

	/*
	 * This method returns the series's descriptions's ID.
	 * It is treated separately because we only need the sole series's descriptions here. 
	 */
	public String getSeriesDescriptionID(String studyInstanceUID) throws Exception{
		// getting the query ID
		this.query = "{ \"Level\" : \"" + "Series" + "\", \"Query\" : "
				+ "{\"Modality\" : \"" + "*" + "\","
				+ "\"ProtocolName\" : \"" + "*" + "\","
				+ "\"SeriesDescription\" : \"" + "*" + "\","
				+ "\"SeriesInstanceUID\" : \"" + "*" + "\","
				+ "\"StudyInstanceUID\" : \"" + studyInstanceUID + "\"}"
				+ "}";
		this.setUrl("modalities/" + this.aet + "/query");
		String idURL = this.getOutputStream().substring(12, 48);
		return idURL;
	}

	/*
	 * This method returns the series's description's values
	 */
	public String[] getSeriesDescriptionValues(String idURL, String index) throws Exception{
		String splittedContent = "";
		String content;

		// getting the ID's answers indexes
		this.setUrl("queries/" + idURL + "/answers/");
		String serverResponse = this.getOutputStream().replaceAll("[^0-9,]","");
		String[] indexes = serverResponse.split(",");
		if(indexes.length == 0){
			throw new Exception("Il n'y a aucune reponse pour cette query.");
		}
		String[] values = new String[indexes.length];
		for(int i = 0; i < indexes.length; i++){
			this.setUrl("queries/" + idURL + "/answers/" + i + "/content");
			content = this.getOutputStream();
			if(index.equals("SeriesDescription")){
				splittedContent = content.substring(content.indexOf("SeriesDescription")+60,content.length());
			}else{
				splittedContent = content.substring(content.indexOf("Modality")+51,content.length());
			}
			values[i] = (splittedContent.substring(0,splittedContent.indexOf("\"")));
		}
		return values;
	}

	/*
	 * This method retrieves an instance, depending on its query ID 
	 */
	public void retrieve(String queryID, String answer, String retrieveAET) throws IOException{
		this.retrieveAET = retrieveAET;
		System.out.println(retrieveAET);
		this.setUrl("queries/" + queryID + "/answers/" + answer + "/retrieve");
		System.out.println(this.url);
		this.getOutputStream();
	}

	/*
	 * This method is similar to getQueryAnswerIndexes, 
	 * except it gives every available AETs instead of queries's answer's indexes.
	 */
	public Object[] getAET() throws IOException{
		this.setUrl("modalities");
		String aet = this.getOutputStream();
		ArrayList<String> indexes = new ArrayList<String>();

		// We store the indexes from serverResponse in the ArrayList indexes
		String pattern1 = "\"";
		String pattern2 = "\"";

		Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
		Matcher m = p.matcher(aet);
		while (m.find()) {
			indexes.add(m.group(1));
		}

		// We convert the ArrayList to an Object[]
		return indexes.toArray();
	}

	/*
	 * This method is similar to getAET, 
	 * except it gives every available dicom AETs.
	 */
	public Object[] getDicomAET() throws IOException{
		ArrayList<String> dicomAETs = new ArrayList<>();
		this.setUrl("system/");
		String content = this.getOutputStream();

		String pattern1 = "DicomAet\" : \"";
		String pattern2 = "\"";

		Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
		Matcher m = p.matcher(content);
		while (m.find()) {
			dicomAETs.add(m.group(1));
		}
		Object[] dicomTab = new Object[dicomAETs.size()];
		dicomTab = dicomAETs.toArray(dicomTab);
		return dicomTab;
	}

	/*
	 * This method sets the aet
	 */
	public void setAET(String aet){
		this.aet = aet;
	}

	/*
	 * This method reset the url to the default one, in order to make new queries 
	 */
	public void resetURL(String aet){
		this.setUrl("modalities/" + aet + "/query");
	}

	private void setQuery(String level, String name, String id, String studyDate, String modality, String studyDescription, String accessionNumber) {
		this.query = "{ \"Level\" : \"" + level + "\", \"Query\" : "
				+ "{\"PatientName\" : \"" + name + "\","
				+ "\"PatientID\" : \"" + id + "\","
				+ "\"StudyDate\" : \"" + studyDate + "\","
				+ "\"ModalitiesInStudy\" : \"" + modality + "\","
				+ "\"StudyDescription\" : \"" + studyDescription + "\","
				+ "\"AccessionNumber\" : \"" + accessionNumber + "\"}"
				+ "}";
	}

	/*
	 * This method sets the url
	 */
	public void setUrl(String url) {
		if(this.fullAddress != null && !this.fullAddress.equals("none")){
			this.url = this.fullAddress + "/" + url;
		}else{
			if(this.ip != null && port != null){
				this.url = ip + ":" + port + "/" + url;
			}
		}
	}
	
	public int ordinalIndexOf(String str, String substr, int n) {
		int pos = str.indexOf(substr);
		while (--n > 0 && pos != -1)
			pos = str.indexOf(substr, pos + 1);
		return pos;
	}
}
