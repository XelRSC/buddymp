package com.xel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XPlaylist {

  public void Save(String path, Map<String,String> map)
	{
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			Iterator it = map.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry pair = (Map.Entry)it.next();
				bw.write(pair.getKey() + "##" + pair.getValue());
				bw.newLine();
			}
			bw.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	public HashMap<String, String> Load(String path)
	{
		Map<String, String> tempMap = new HashMap<String, String>();
		try {
				FileInputStream in = new FileInputStream(path);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String str;
			  while((str = br.readLine())!= null)
			  {
				  String[] split = str.split("##");
				  tempMap.put(split[0], split[1]);
			  }
			  br.close();
			  return (HashMap<String, String>) tempMap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
}
