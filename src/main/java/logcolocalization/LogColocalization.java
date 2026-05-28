package logcolocalization;



import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

import ij.plugin.PlugIn;


public class LogColocalization implements PlugIn 
{

	@Override
	public void run( String arg )
	{
    }

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) throws Exception 
	{
		new ImageJ();
		//ImagePlus image = IJ.openImage("/home/eugene/Desktop/projects/IJROIsToSVG/test.tif");
		ImagePlus image = IJ.openImage("/home/eugene/Desktop/projects/IJROIsToSVG/gen_art.tif");
		image.show();
	}



}
