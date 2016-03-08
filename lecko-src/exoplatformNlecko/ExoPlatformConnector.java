package exoplatformNlecko;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExoPlatformConnector {

	public static void main(String[] args) {
		String login = "simon_legroux";
		//Not my real pass
		String pass = "[pass]"; 
		String address = "https://community.exoplatform.com";
		runExo(login, pass, address,"C:/Temp/exo-community.txt","C:/Temp/exo-community.log.txt");
	}

	public static void runExo(String login, String pass, String address, String activityLog, String Logfile){
		
		JSONArray jsonArray = null;
		JSONObject elementJSON = null, elementJSON2;
		//anonymization MAP
		HashMap<String, String> user_map = new HashMap<String, String>();
		int end_counter=0;
		String idEvent = "";
		String eventType ="";
		String date="";
		String type="";
		String idactor ="";
		String placeName = "";
		HttpGet getRequest ;
		URI uri = null;
		URL url = null;

		String result1 = "";
		StringBuffer sb1 = null;
		InputStream inputStream = null; 
		getRequest = new HttpGet();
		HttpClient client = new DefaultHttpClient();
		client = new DefaultHttpClient();
		int offset = 0;
		PrintWriter out = null;
		PrintWriter out2 = null; 
		boolean end = false;
		try {
			out = new PrintWriter(new FileWriter(activityLog));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			out2 = new PrintWriter(new FileWriter(Logfile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		out2.println("Beginning Extraction");
		while(!end){
			
			//Authentication
			try {
				String webPage = address + "/rest/private/v1/social/activities?offset="+offset+"&limit=20";
				String name = login;
				String password = pass;
				String authString = name + ":" + password;
				byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
				String authStringEnc = new String(authEncBytes);

				url = new URL(webPage);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
				InputStream is = urlConnection.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);

				int numCharsRead;
				char[] charArray = new char[1024];
				sb1 = new StringBuffer();
				while ((numCharsRead = isr.read(charArray)) > 0) {
					sb1.append(charArray, 0, numCharsRead);
				}
				result1 = sb1.toString();


			} catch (MalformedURLException e) {
				out2.println(e.toString());
			} catch (IOException e) {
				out2.println(e.toString());
			}

			byte[] bytes = sb1.toString().getBytes();
			inputStream = new ByteArrayInputStream(bytes);
			try {
				uri = url.toURI();
				getRequest.setURI(uri);

			} catch (URISyntaxException e3) {
				out2.println(e3.toString());
			}

			try {
				//Parsin JSON
				elementJSON = new JSONObject(result1);
				// Getting "activities" array
				jsonArray = elementJSON.getJSONArray("activities");

				for (int a = 0 ; a < jsonArray.length() ; a++){
					placeName="none";
					String type_space = "";
					elementJSON2 = jsonArray.getJSONObject(a);
					String url_comments = "no_url";
					String url_likes = "no_url";
					try {
						idactor=elementJSON.getJSONArray("activities").getJSONObject(a).getJSONObject("owner").getString("id");

						//constuction de la map des users au fur et à mesure pour l'anonymisation
						if(!user_map.containsKey(idactor)){
							user_map.put(idactor, Integer.toString(user_map.size()+1));
							idactor=user_map.get(idactor);
						}else idactor = user_map.get(idactor);
						out.print(idactor + ";");
					} catch(JSONException e){
						out2.println(e.toString());
					}

					try {
						idEvent = elementJSON.getJSONArray("activities").getJSONObject(a).getString("type");
						out.print(idEvent + ";");
						date = elementJSON.getJSONArray("activities").getJSONObject(a).getString("createDate");
						out.print(date + ";");
						type_space= elementJSON.getJSONArray("activities").getJSONObject(a).getJSONObject("activityStream").getString("type");
						out.print(type_space + ";");
						placeName = elementJSON.getJSONArray("activities").getJSONObject(a).getJSONObject("activityStream").getString("id");
						if(type_space.equals("user")){
							placeName="";
						}
						out.print(placeName + ";");
						out.println();
						url_comments = elementJSON.getJSONArray("activities").getJSONObject(a).getString("comments");
						url_likes = elementJSON.getJSONArray("activities").getJSONObject(a).getString("likes");
					}
					catch (JSONException ex){
						out2.println(ex.toString());
					}
					//Getting Comments
					getExoComments(url_comments,login,pass,placeName,out, out2, user_map);
				}
			}catch(NumberFormatException | JSONException e3){
				out2.println(e3.toString());
			}

			try {
				if( elementJSON.getJSONArray("activities").toString().equals("[]") ){
					end_counter++;
				} else { 
					end_counter=0;
				}
			} catch (JSONException e) {
				out2.println(e.toString());
			}
			offset=offset+20;
			//By convention, we will consider that id the JSON is empty 15 times
			//in a row, it is the end, indeed, some JSON are empty when it is not
			//the end. It is more secure to take a margin
			if (end_counter == 15){
				end = true;
				out2.println("Ending Extraction");
			}
		}
		out.flush();
		out2.flush();
	}

	public static void getExoComments (String URL,String login,String pass, String placeName, PrintWriter out, PrintWriter out2, HashMap<String,String> user_map){

		out2.println("Getting Comments");
		JSONArray jsonArray = null;
		JSONObject elementJSON = null, elementJSON2;
		String idEvent = "";
		String date="";
		String type="";
		String idactor ="";
		HttpGet getRequest ; 
		int compteurErreurs=0;
		URI uri = null ;
		URL url = null;
		String result1 = "";
		StringBuffer sb1 = null;
		InputStream inputStream = null; 
		getRequest = new HttpGet();
		HttpClient client = new DefaultHttpClient();
		int startIndex = 0;
		client = new DefaultHttpClient();
		int offset = 0;
		boolean end = false;
		while(!end){

			//Authentication
			try {
				String webPage = URL.replace("rest","rest/private")+"?offset="+offset+"&limit=20";
				String name = login;
				String password = pass;
				String authString = name + ":" + password;
				byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
				String authStringEnc = new String(authEncBytes);

				url = new URL(webPage);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
				InputStream is = urlConnection.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);

				int numCharsRead;
				char[] charArray = new char[1024];
				sb1 = new StringBuffer();
				while ((numCharsRead = isr.read(charArray)) > 0) {
					sb1.append(charArray, 0, numCharsRead);
				}
				result1 = sb1.toString();


			} catch (MalformedURLException e) {
				out2.println(e.toString());
			} catch (IOException e) {
				out2.println(e.toString());
			}

			byte[] bytes = sb1.toString().getBytes();
			inputStream = new ByteArrayInputStream(bytes);
			try {

				uri = url.toURI();
				getRequest.setURI(uri);

			} catch (URISyntaxException e3) {
				out2.println(e3.toString());
			}
			try {
				elementJSON = new JSONObject(result1);
				jsonArray = elementJSON.getJSONArray("comments");

				for (int a = 0 ; a < jsonArray.length() ; a++){
					String type_space = "";
					elementJSON2 = jsonArray.getJSONObject(a);
					try {
						idactor=elementJSON.getJSONArray("comments").getJSONObject(a).getString("identity").split("/")[7];

						if(!user_map.containsKey(idactor)){
							user_map.put(idactor, Integer.toString(user_map.size()+1));
							idactor=user_map.get(idactor);
						}else idactor = user_map.get(idactor);
						out.print(idactor + ";");
					} catch(JSONException e){
						out2.println(e.toString());
					}

					try {
						idEvent = "comment";
						out.print(idEvent + ";");
						date = elementJSON.getJSONArray("comments").getJSONObject(a).getString("createDate");
						out.print(date + ";");
						out.print(placeName + ";");
						out.println();

					}
					catch (JSONException ex){
						out2.println(ex.toString());
					}

				}
			}catch(NumberFormatException | JSONException e3){
				out2.println(e3.toString());
			}
			try {
				if( elementJSON.getJSONArray("comments").toString().equals("[]") ){
					end=true;
					out2.println("End Getting Comments");
				}
			} catch (JSONException e) {
				out2.println(e.toString());
			}
			offset=offset+20;
		}
	}	
}
