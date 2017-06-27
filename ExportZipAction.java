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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

public class ExportZipAction extends AbstractAction{
	private static final long serialVersionUID = 1L;

	private StringBuilder ids;
	private ArrayList<String> listeIds;
	private ArrayList<String> shownContentList;
	private JComboBox<Object> shownContent;
	private boolean[] choix = {false};
	private Path path;
	private String url;
	private String authentication;
	private String ip;
	private String port;
	private String fullAddress;
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");

	public ExportZipAction(JComboBox<Object> shownContent, ArrayList<String> shownContentList, ArrayList<String> ids){
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
			this.listeIds = ids;
			this.shownContent = shownContent;
			this.shownContentList = shownContentList;
			this.ids.append("[");
			for(int i = 0; i < ids.size(); i++){
				if(i == ids.size()-1){
					this.ids.append("\"" + ids.get(i) + "\"]");
				}else{
					this.ids.append("\"" + ids.get(i) + "\",");
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.fileChooser();
		try {
			if(!shownContentList.isEmpty() && this.choix[0] == true){
				setUrl("tools/create-archive");
				URL url2 = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				if((fullAddress != null && fullAddress.contains("https")) || (ip != null && ip.contains("https"))){
					try{
						HttpsTrustModifier.Trust(conn);
					}catch (Exception e){
						throw new IOException("Cannot allow self-signed certificates");
					}
				}
				if(authentication != null){
					conn.setRequestProperty("Authorization", "Basic " + authentication);
				}

				OutputStream os = conn.getOutputStream();
				os.write((ids.toString()).getBytes());
				os.flush();

				DateFormat df = new SimpleDateFormat("MMddyyyyhhmmss");
				File f = new File(this.path + df.format(new Date()) + ".zip");
				InputStream is = conn.getInputStream();
				FileOutputStream fos = new FileOutputStream(f);
				int bytesRead = -1;
				byte[] buffer = new byte[1024];
				while ((bytesRead = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
				fos.close();
				is.close();
				listeIds.removeAll(listeIds);
				shownContent.removeAll();
				shownContentList.removeAll(shownContentList);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fileChooser(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(jpreferPerso.get("zipLocation", System.getProperty("user.dir"))));
		chooser.setDialogTitle("Export zip to...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			this.path = chooser.getSelectedFile().toPath();
			jpreferPerso.put("zipLocation", this.path.toString());
			this.choix[0] = true;
		} else {
			this.choix[0] = false;
		}
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
