package marvin.video;

import java.nio.ByteBuffer;

import marvin.image.MarvinImage;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class MarvinJavaCVAdapter implements MarvinVideoInterface{
	
	private FrameGrabber	grabber;
	private IplImage		image;
	private int				width;
	private int				height;
	private boolean			connected;
	private int[]			intArray;
	private MarvinImage 	marvinImage;
	
	@Override
	public void connect(int deviceIndex) {
		connect(deviceIndex, 640,480);
	}
	
	@Override
	public void connect(int deviceIndex, int width, int height) {
		this.width = width;
		this.height = height;
		marvinImage = new MarvinImage(width, height);
		intArray = new int[height*width*4];
		
		try{
			grabber= new VideoInputFrameGrabber(deviceIndex);
			grabber.setImageWidth(width);
			grabber.setImageHeight(height);
			grabber.start();
			connected = true;
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void disconnect() {
		try{
			grabber.stop();
			connected = false;
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public int getImageWidth(){
		return this.width;
	}
	
	@Override
	public int getImageHeight(){
		return this.height;
	}

	@Override
	public MarvinImage getFrame() {
		
		if(connected){
			image=null;
			try
			{
			 image=grabber.grab();
			 convertToIntArray(image, intArray);
			 marvinImage.setIntColorArray(intArray);
			 return marvinImage;
			}
			catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	private void convertToIntArray(IplImage img, int[] arr){
		ByteBuffer buffer = img.getByteBuffer();
		for(int ii=0, bi=0; bi<buffer.limit()-3; ii++, bi+=3){
			arr[ii] = 0xFF000000 + (buffer.get(bi+2) << 16) + (buffer.get(bi+1) << 8) + buffer.get(bi);
		}
	}
}
