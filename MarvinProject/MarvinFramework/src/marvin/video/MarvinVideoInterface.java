package marvin.video;

import marvin.image.MarvinImage;

import com.googlecode.javacv.FrameGrabber;

public interface MarvinVideoInterface {

	// Connection
	void connect(int deviceIndex);
	void connect(int deviceIndex, int width, int height);
	void disconnect();
	
	// Image Width / Height
	int getImageWidth();
	int getImageHeight();
	
	// Frame request
	MarvinImage getFrame();
}
