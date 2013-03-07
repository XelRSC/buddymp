package com.xel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;


import com.rsbuddy.api.gui.Location;
import com.rsbuddy.plugin.WidgetPluginBase;

public class XMain extends WidgetPluginBase{

  public XMain() {
        super("XPLayer", "UP.png","HOVER.png", "DOWN.png");
	}

	
	private Location _defaultLocation;
	private VBox parent;
	private XPlaylist xpl;
	
	//Playback Controls
	private Button PBPrevious, PBPlay, PBPause, PBStop, PBNext;
	
	//Playlist Controls
	private Button PLAdd, PLRemove, PLList, PLSettings;
	private MenuItem PLAddSongs, PLAddFolder, PLRemoveSongs, PLRemoveAll, PLSave, PLLoad;
	
	
	//Displays
	private Label DNowPlaying;
	private Label DTime;
	private ListView<String> DPlayList;
	
	//Player
	private MediaPlayer CurrentPlayer;
	private List<MediaPlayer> Players = new ArrayList<MediaPlayer>();
	private Map<String, String> SongList = new HashMap<String, String>();
	private int CurrentIndex;
	private Slider VolSlider, SeekSlider;
	private double CurrentVolume = 50;
	
	
	@Override
	public void init()
	{
		if(context().get("Left").toString().equals("true"))
			_defaultLocation = Location.LEFT;
		else
			_defaultLocation = Location.RIGHT;
			
	}
	
	@Override
	public void dispose()
	{
		if(CurrentPlayer != null)
			CurrentPlayer.stop();
		
		Players.clear();
		SongList.clear();
	}
	
	@Override
	public EnumSet<Location> supportedLocations()
	{
		return EnumSet.of(_defaultLocation);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Node content(Location arg0) {
		try {
			parent = (VBox)FXMLLoader.load(XMain.class.getResource("FXML.fxml"));
			VBox.setVgrow(parent, Priority.ALWAYS);
			DPlayList = (ListView<String>) parent.lookup("#DPlayList");
			PopulateList();
			DPlayList.setOnMouseClicked(new EventHandler<MouseEvent>(){
				@Override
				public void handle(MouseEvent arg0) {
					if(arg0.getButton().equals(MouseButton.PRIMARY))
						if(arg0.getClickCount() >= 2)
							PlaySong(0);
					}});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
			DNowPlaying = (Label)parent.lookup("#DNowPlaying");
			DTime = (Label)parent.lookup("#DTime");
		
			BTNHandler btnhandler = new BTNHandler();
			PBPrevious = (Button)parent.lookup("#PBPrevious");
			PBPrevious.setOnAction(btnhandler);
			PBPlay = (Button)parent.lookup("#PBPlay");
			PBPlay.setOnAction(btnhandler);
			PBPause = (Button)parent.lookup("#PBPause");
			PBPause.setOnAction(btnhandler);
			PBStop = (Button)parent.lookup("#PBStop");
			PBStop.setOnAction(btnhandler);
			PBNext = (Button)parent.lookup("#PBNext");
			PBNext.setOnAction(btnhandler);
			
			PLAdd = (Button)parent.lookup("#PLAdd");
			PLAdd.setOnAction(btnhandler);
			PLAddSongs = PLAdd.getContextMenu().getItems().get(0);
			PLAddSongs.setOnAction(btnhandler);
			PLAddFolder = PLAdd.getContextMenu().getItems().get(1);
			PLAddFolder.setOnAction(btnhandler);
			
			PLRemove = (Button)parent.lookup("#PLRemove");
			PLRemove.setOnAction(btnhandler);
			PLRemoveSongs = PLRemove.getContextMenu().getItems().get(0);
			PLRemoveSongs.setOnAction(btnhandler);
			PLRemoveAll = PLRemove.getContextMenu().getItems().get(1);
			PLRemoveAll.setOnAction(btnhandler);
			
			PLList = (Button)parent.lookup("#PLList");
			PLList.setOnAction(btnhandler);
			PLSave = PLList.getContextMenu().getItems().get(0);
			PLSave.setOnAction(btnhandler);
			PLLoad = PLList.getContextMenu().getItems().get(1);
			PLLoad.setOnAction(btnhandler);
			
			PLSettings = (Button)parent.lookup("#PLSettings");
			PLSettings.setOnAction(btnhandler);

			VolSlider = (Slider)parent.lookup("#VolSlider");
			VolSlider.valueProperty().addListener(new ChangeListener<Number>(){
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number vol) { SetVolume(vol.doubleValue()); }});
			
			VolSlider.setOnMouseReleased(new EventHandler<MouseEvent>(){
				@Override
				public void handle(MouseEvent arg0) {
					if(CurrentPlayer != null)
						DNowPlaying.setText(DPlayList.getItems().get(CurrentIndex));
				}});
			
			SeekSlider = (Slider)parent.lookup("#SeekSlider");
			SeekSlider.setOnMouseDragged(new EventHandler<MouseEvent>(){
				@Override
				public void handle(MouseEvent arg0) { Seek(new Duration(SeekSlider.getValue() * 1000)); }});
			
		return parent;
	}
	

	private class BTNHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent e) {
			if(e.getSource() == PBPrevious)
			{
				CurrentIndex--;
				if(CurrentIndex < 0)
					CurrentIndex = Players.size() - 1;
				
				PlaySong(2);
			}
			else if(e.getSource() == PBPlay)
			{
				PlaySong(1);
			}
			else if(e.getSource() == PBPause)
			{
				PauseSong();
			}
			else if(e.getSource() == PBStop)
			{
				StopSong();
			}
			else if(e.getSource() == PBNext)
			{
				CurrentIndex++;
				if(CurrentIndex > Players.size() - 1)
					CurrentIndex = 0;
				
				PlaySong(2);
			}
			else if(e.getSource() == PLAdd)
			{
				AddSong(0);
			}
			else if(e.getSource() == PLAddSongs)
			{
				AddSong(0);
			}
			else if(e.getSource() == PLAddFolder)
			{
				AddSong(1);
			}
			else if(e.getSource() == PLRemove)
			{
				RemoveSong(0);
			}
			else if(e.getSource() == PLRemoveSongs)
			{
				RemoveSong(0);
			}
			else if(e.getSource() == PLRemoveAll)
			{
				RemoveSong(1);
			}
			else if(e.getSource() == PLList)
			{
				SavePlaylist();
			}
			else if(e.getSource() == PLSave)
			{
				SavePlaylist();
			}
			else if(e.getSource() == PLLoad)
			{
				LoadPlaylist();
			}
		}	
	}
	
	private void PlaySong(Integer i)
	{
		if(i == 0){
			if(CurrentPlayer != null)
				CurrentPlayer.stop();
			
			CurrentIndex = DPlayList.getSelectionModel().getSelectedIndex();
			CurrentPlayer = Players.get(CurrentIndex);
		}
		else if(i == 2){
			if(CurrentPlayer != null)
				CurrentPlayer.stop();
			
			CurrentPlayer = Players.get(CurrentIndex);
			DPlayList.getSelectionModel().select(CurrentIndex);
		}
		if(CurrentPlayer != null)
		{
			SeekSlider.setMax(CurrentPlayer.getTotalDuration().toSeconds());
			CurrentPlayer.setVolume(CurrentVolume);
			CurrentPlayer.play();
			DNowPlaying.setText(DPlayList.getItems().get(CurrentIndex).toString());
		}
	}
	
	private void PauseSong()
	{
		if(CurrentPlayer != null)
			if(CurrentPlayer.getStatus() == MediaPlayer.Status.PLAYING)
				CurrentPlayer.pause();
			else
				CurrentPlayer.play();
	}
	
	private void StopSong()
	{
		if(CurrentPlayer != null)
			CurrentPlayer.stop();
	}
	
	private void AddSong(Integer i)
	{
		if(i == 0)
		{
			FileChooser fc = new FileChooser();
			fc.setTitle("Select file(s) to add");
			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("MP3 files (*.mp3)", "*.mp3");
			FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("WAV Files (*.WAV)", "*.wav");
			fc.getExtensionFilters().addAll(filter, filter2);
	
			List<File> files = fc.showOpenMultipleDialog(null);
		
			if(files != null)
			{
				for(File f : files)
				{
					SongList.put(f.getName(), f.getAbsolutePath());
					MediaPlayer mp = new MediaPlayer(new Media(new File(f.getAbsolutePath()).toURI().toASCIIString()));
					 mp.currentTimeProperty().addListener(new InvalidationListener() 
				        {
				            public void invalidated(Observable ov) {
				            	MPThread();
				            }
				        });
					 mp.setOnEndOfMedia(new Runnable(){

						@Override
						public void run() {
							CurrentIndex++;
							if(CurrentIndex > Players.size())
								CurrentIndex = 0;
							
							PlaySong(2);
						}});
					Players.add(mp);
					DPlayList.getItems().add(f.getName());
				}
			}
		}
		else if(i == 1)
		{
			DirectoryChooser dc = new DirectoryChooser();
			dc.setTitle("Choose folder");
			File temp = dc.showDialog(null);
			if(temp != null)
			{
				for(String s : temp.list(new FilenameFilter(){
					@Override
					public boolean accept(File dir, String name){
						return name.endsWith(".mp3") || name.endsWith(".wav");
					}
				}))
				{
					String absolute = temp + "\\" + s;
					SongList.put(s, absolute);
					MediaPlayer mp = new MediaPlayer(new Media(new File(absolute).toURI().toASCIIString()));
					mp.currentTimeProperty().addListener(new InvalidationListener() 
			        {
			            public void invalidated(Observable ov) {
			            	MPThread();
			            }
			        });
					 mp.setOnEndOfMedia(new Runnable(){

							@Override
							public void run() {
								CurrentIndex++;
								if(CurrentIndex > Players.size())
									CurrentIndex = 0;
								
								PlaySong(2);
							}});
					Players.add(mp);
					DPlayList.getItems().add(s);
				}
			}
		}
	}
	
	private void RemoveSong(Integer i)
	{
		if(i == 0)
		{
			int selected = DPlayList.getSelectionModel().getSelectedIndex();
			Players.remove(selected);
			SongList.remove(DPlayList.getItems().get(selected).toString());
			DPlayList.getItems().remove(selected);
		}
		else if(i == 1)
		{
			Players.clear();
			SongList.clear();
			DPlayList.getItems().clear();
		}
		
	}

	private void SetVolume(double value)
	{
		DNowPlaying.setText("Volume: " + Math.round(value * 100) + "%");
		CurrentVolume = value;
		if(CurrentPlayer != null)
			CurrentPlayer.setVolume(CurrentVolume);
	}
	
	private void Seek(Duration dur)
	{
		if(CurrentPlayer != null)
			CurrentPlayer.seek(dur);
	}

	private void MPThread()
	{
		
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				Long seconds = Math.round(CurrentPlayer.getTotalDuration().toSeconds() - CurrentPlayer.getCurrentTime().toSeconds());
				Long minute = TimeUnit.SECONDS.toMinutes(seconds);
				Long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
				final String sMinute = Long.toString(minute);
				final String sSecond = Long.toString(second);
				try{
					if(sSecond.length() < 2)
						DTime.setText(sMinute + ":0" + sSecond);
					else
						DTime.setText(sMinute + ":" + sSecond);
					
					SeekSlider.setValue(CurrentPlayer.getCurrentTime().toSeconds());
				}catch(Exception ee){}
			}});
	}
	
	private void SavePlaylist()
	{
		if(xpl == null)
			xpl = new XPlaylist();

		FileChooser fc = new FileChooser();
		fc.setTitle("Save Playlist");
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XMusic Playlist (*.xmp)", "*.xmp");
		fc.getExtensionFilters().addAll(filter);
		File f = fc.showSaveDialog(null);
		if(f != null)
		{
			if(f.toString().contains(".xmp"))
				xpl.Save(f.getAbsolutePath(), SongList);
			else
				xpl.Save(f.getAbsolutePath() + ".xmp", SongList);
					
		}
	}
	
	private void LoadPlaylist()
	{
		if(xpl == null)
			xpl = new XPlaylist();
		
		FileChooser fc = new FileChooser();
		fc.setTitle("Open Playlist");
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XMusic Playlist (*.xmp)", "*.xmp");
		fc.getExtensionFilters().addAll(filter);
		File f = fc.showOpenDialog(null);
		if(f != null)
		{
			Map<String,String> temp = xpl.Load(f.getAbsolutePath());
			Players.clear();
			SongList.clear();
			DPlayList.getItems().clear();
			if(CurrentPlayer != null)
				CurrentPlayer.stop();
			
			Iterator it = temp.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry pair = (Map.Entry)it.next();
				MediaPlayer mp = new MediaPlayer(new Media(new File(pair.getValue().toString()).toURI().toASCIIString()));
				mp.currentTimeProperty().addListener(new InvalidationListener() 
				{
				    public void invalidated(Observable ov) {
				    	MPThread();
				    }
				});
				 mp.setOnEndOfMedia(new Runnable(){

						@Override
						public void run() {
							CurrentIndex++;
							if(CurrentIndex > Players.size())
								CurrentIndex = 0;
							
							PlaySong(2);
						}});
				SongList.put(pair.getKey().toString(), pair.getValue().toString());
				Players.add(mp);
				DPlayList.getItems().add(pair.getKey().toString());
			}
		}
	}

	private void PopulateList()
	{
		if(!Players.isEmpty())
		{
			if(DPlayList.getItems().isEmpty())
			{
				Iterator it = SongList.entrySet().iterator();
				while(it.hasNext())
				{
					Map.Entry pair = (Map.Entry)it.next();
					DPlayList.getItems().add(pair.getKey().toString());
				}
			}
		}
	}
}
