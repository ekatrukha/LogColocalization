package logcolocalization;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;

import logcolocalization.io.SpimDataLoader;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;


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

		//ij.command().run(ConfigureBVVRenderWindow.class,true).get();
//		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 
//		
//		testBVB.startBVB("");
		
//		ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > valuePair = 
//		testBVB.loadBioFormats( "/home/eugene/Desktop/projects/BrainQuant/test/Composite.tif" );
//		ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > valuePair = 
//				testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BrainQuant/refistered_crop.xml" );
		AbstractSpimData< ? > spimData = SpimDataLoader.loadHDF5( "/home/eugene/Desktop/projects/BrainQuant/refistered_crop.xml" ) ;
		final BasicImgLoader imgLoader = spimData.getSequenceDescription().getImgLoader();
		RandomAccessibleInterval<UnsignedShortType> channel1 = 
				Cast.unchecked(  imgLoader.getSetupImgLoader(0).getImage(0));
		int bins = 512;
		RandomAccessibleInterval<UnsignedShortType> channel2 = 
				Cast.unchecked(  imgLoader.getSetupImgLoader(1).getImage(0));
		double min1 = 20;
		double max1 = 65500;
		double min2 = 20;
		double max2 = 65500;		

     Real1dBinMapper<FloatType> mapper1 = new Real1dBinMapper<>(Math.log10( min1 ), Math.log10(max1), bins, false);
		Real1dBinMapper<FloatType> mapper2 = new Real1dBinMapper<>(Math.log10(min2), Math.log10(max2), bins, false);
//		ArrayList<BinMapper1d<FloatType>> mappers = new ArrayList<>();
//
//		mappers.add (mapper1);
//		mappers.add (mapper2);
//		HistogramNd<FloatType> histogram = new HistogramNd<>(mappers);
//		ArrayList<Iterable<FloatType>> list = new ArrayList<>();
//		RandomAccessibleInterval< FloatType > real1 = Converters.convert( channel1, (i,o) -> {o.set( (float)Math.log10( i.getInteger()));}, new FloatType() );
//		RandomAccessibleInterval< FloatType > real2 = Converters.convert( channel2, (i,o) -> {o.set( (float) Math.log10( i.getInteger()));}, new FloatType() );
//		list.add( real1 );
//		list.add( real2 );
//		histogram.countData( list );
//		
//		RandomAccessibleInterval< FloatType > histFloat = Converters.convert( histogram, (i,o) -> o.set(i.getIntegerLong()), new FloatType() );
//		ImageJFunctions.show( histogram );
//		ImageJFunctions.show(histFloat);
		
		
		
		
		ImagePlus mapImg = IJ.openImage( "/home/eugene/Desktop/projects/BrainQuant/cyto_fluo/cytofluo_map3.tif" );
		//ImagePlus mapImg = IJ.openImage( "/home/eugene/Desktop/projects/BrainQuant/test/map.tif" );
		final ImageProcessor mapIP = mapImg.getProcessor();
		long[] dims = channel1.dimensionsAsLongArray();
		int[] blockSize = { 32, 32, 32 };
		DiskCachedCellImgOptions options = DiskCachedCellImgOptions.options()
			    .cellDimensions(blockSize);
		DiskCachedCellImgFactory<UnsignedShortType> factory = 
			    new DiskCachedCellImgFactory<>(new UnsignedShortType(), options);
		DiskCachedCellImg< UnsignedShortType, ? > out1 = factory.create(dims);
		DiskCachedCellImg< UnsignedShortType, ? > out2 = factory.create(dims);
		
		LoopBuilder.setImages( channel1, channel2, out1, out2 ).multiThreaded().forEachPixel( (c1,c2,co1,co2)-> 
		{
			long x = mapper1.map( new FloatType((float)Math.log10( c1.get())));
			long y = mapper2.map( new FloatType((float)Math.log10( c2.get())));
			if(x>=0 && x<=bins && y>=0 && y<=bins)
			{
				if(mapIP.get( (int)x, (int) y )>0)
				{
					co1.set( c1 );
					co2.set( c2 );
				}
			}
		});
		final ImagePlus ch1 = ImageJFunctions.show( out1 );
		final ImagePlus ch2 = ImageJFunctions.show( out2 );
		ch1.setDimensions( 1, (int)dims[2], 1 );
		ch2.setDimensions( 1, (int)dims[2], 1 );

		Calibration cal = new Calibration ();
		double [] voxDims = spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize().dimensionsAsDoubleArray();
		cal.pixelWidth = voxDims[0];
		cal.pixelHeight = voxDims[1];
		cal.pixelDepth = voxDims[2];
		ch1.setCalibration( cal );
		ch2.setCalibration( cal );
	}



}
