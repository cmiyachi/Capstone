package video.simpleVideoTest;

import javax.swing.JFrame;

import marvin.gui.MarvinImagePanel;
import marvin.image.MarvinImage;
import marvin.video.MarvinJavaCVAdapter;
import marvin.video.MarvinVideoInterface;

public class SimpleVideoTest extends JFrame implements Runnable{
	
	private MarvinVideoInterface 	videoAdapter;
	private MarvinImage				image;
	private MarvinImagePanel 		videoPanel;
	
	public SimpleVideoTest(){
		super("Simple Video Test");
	
		// Create the VideoAdapter and connect to the camera
		videoAdapter = new MarvinJavaCVAdapter();
		videoAdapter.connect(1);
		
		// Create VideoPanel
		videoPanel = new MarvinImagePanel();
		add(videoPanel);
		
		// Start the thread for requesting the video frames 
		new Thread(this).start();
		
		setSize(800,600);
		setVisible(true);
	}
	

	public static void main(String[] args) {
		SimpleVideoTest t = new SimpleVideoTest();
		t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void run() {
		while(true){
			// Request a video frame and set into the VideoPanel
			image = videoAdapter.getFrame();
			videoPanel.setImage(image);
		}
	}
}


