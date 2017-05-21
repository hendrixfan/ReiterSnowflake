package com.bur.BurReiterSnowflake;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.nd4j.linalg.api.iter.NdIndexIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.Sum;
import org.nd4j.linalg.api.ops.impl.transforms.comparison.CompareAndSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.nd4j.linalg.indexing.functions.Value;
import org.nd4j.linalg.inverse.InvertMatrix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.util.Arrays;

public class Snowflake extends JFrame{

    static int imgx = 10;
    static int imgy = 10;
    static int imgx1 = imgx - 1;
    static int imgy1 = imgy - 1;

    static int maxIt = 100; // growth steps
    static double beta = 0.6;
    static double alpha = 1.0;
    static double gamma = 0.01;
    static int mx = 500; // width  of 2DCA
    static int my = 500; // height of 2DCA

    static int[] dx  = {-1, 0, -1, 1, 0, 1};
    static int[] dy  = {-1, -1, 0, 0, 1, 1};
    
    static double[][] ca = new double[mx][my];
    static double[][] caRep = new double[mx][my];
    static double[][] caNRep = new double[mx][my];
    
    static INDArray ca_fast = Nd4j.zeros(my, mx);
    static INDArray caRep_fast = Nd4j.zeros(my, mx);
    static INDArray caNRep_fast = Nd4j.zeros(my, mx);
    static INDArray caMask_fast = Nd4j.zeros(my, mx);
    static INDArray wsum = Nd4j.zeros(my, mx);
    static INDArray wnsum = Nd4j.zeros(my, mx);
    static INDArray NegcaMask_fast = Nd4j.zeros(my, mx);
    static INDArray dx_fast = Nd4j.create(new double[]{-1, 0, -1, 1, 0, 1},new int[]{6,1});
    static INDArray dy_fast = Nd4j.create(new double[]{-1, -1, 0, 0, 1, 1},new int[]{6,1});
    
    static BufferedImage image;
    private static drawPanel dp;

    private Snowflake() {
        super();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });
    }
    public static void generate_fast(){
    	
    	growth_fast();
    }
    
    
    public static void generate(){
        System.out.println("Start");
        long start = System.currentTimeMillis();
        prepare();
        long growthTime = System.currentTimeMillis();
        growth();
        System.out.println("Duration Growth: " + (System.currentTimeMillis() - growthTime)/(float)1000 + "s");
        growthTime = System.currentTimeMillis();
        growth_fast();
        System.out.println("Duration Growth Nd4j: " + (System.currentTimeMillis() - growthTime)/(float)1000 + "s");
        //printArray();
        drawPicture();
        //savePicture();
        System.out.println("Duration: " + (System.currentTimeMillis() - start)/(float)1000 + "s");
        System.out.println("Ready");
        //Test
    }


    private static void savePicture() {
        try {
            ImageIO.write(image, "bmp", new File("SnowFlake.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void prepare(){
    	
        for (int x = 0; x < mx; x++) {
            for (int y = 0; y < my; y++) {
                ca[x][y] = beta;
                caRep[x][y] = beta;
                caNRep[x][y] = beta;
            }
        }

        image = new BufferedImage(imgx, imgy, BufferedImage.TYPE_INT_RGB);
    }

    private static void drawPicture(){

        double an45 =  -(Math.PI / 4.0);
        double sn45 =  Math.sin(an45);
        double cs45 =  Math.cos(an45);
        double scale = Math.sqrt(3.0);
        double ox = imgx1 / 2.0;
        double oy = imgy1 / 2.0;
        
        dp.clear();

        for (int ky = 0; ky < imgy; ky++) {
            for (int kx = 0; kx < imgx; kx++) { //# apply geometric transformation (scaling and rotation)
            	
            	
                double tx = kx - ox;
                double ty = (ky - oy) * scale;
                double tx0 = tx * cs45 - ty * sn45 + ox; 
                ty = tx * sn45 + ty * cs45 + oy;
                tx = tx0;
                if (tx >= 0 && tx <= imgx1 && ty >= 0 && ty <= imgy1){
                    double c = ca[(int)((mx - 1) * ty / imgx1)][(int)((my - 1) * tx / imgy1)];
                    if (c >= 1.0){
                   
                        dp.setPixel(kx,ky,255,51,153,255);
                    }
                }
            }
        }
        //dp.repaint();

    }

    private static void AddweighedAverages(){
        for (int iy = 0; iy < my; iy++) {
            for (int ix = 0; ix < mx; ix++) {
                double wsum =  (caNRep[ix][iy] * (1.0 - alpha * 6.0 / 12.0));
                for (int j = 0; j < 6; j++) {
                    int jx = ix + dx[j];
                    int jy = iy + dy[j];
                    if (jx >= 0 && jx < mx && jy >= 0 && jy < my){

                        wsum += caNRep[jx][jy] * alpha / 12.0;
                    }
                }
                ca[ix][iy] = caRep[ix][iy] + wsum;
            }
        }
    }


 
	private static void growth_fast(){
		

		ca_fast=ca_fast.add(beta);
		ca_fast.putScalar(mx/2,my/2,1.0);
		for (int i = 0; i < maxIt; i++) {
			BooleanIndexing.assignIf(caMask_fast, ca_fast, Conditions.greaterThanOrEqual(1.0));
			BooleanIndexing.applyWhere(caMask_fast, Conditions.greaterThanOrEqual(1.0), new Value(1.0));
			NdIndexIterator iter = new NdIndexIterator(dx_fast.rows(), 1);
			
			while (iter.hasNext()) {
		        int[] nextIndex = iter.next();
		        double nextValX = dx_fast.getDouble(nextIndex);
		        double nextValY = dy_fast.getDouble(nextIndex);
		        
		        BooleanIndexing.assignIf(caMask_fast, Nd4j_Ex.roll(Nd4j_Ex.roll(ca_fast, 0, (int)nextValY), 1, (int)nextValX),
						Conditions.greaterThanOrEqual(1.0));
				BooleanIndexing.applyWhere(caMask_fast, Conditions.greaterThanOrEqual(1.0), new Value(1.0));

		    }
			caRep_fast = caMask_fast.mul((ca_fast.add(gamma)));
			NegcaMask_fast = Nd4j.ones(my, mx);
			NegcaMask_fast.subi(caMask_fast);
			caNRep_fast = NegcaMask_fast.mul(ca_fast);		
			wsum = caNRep_fast.mul(1.0 - alpha * 6.0 / 12.0);
			iter = new NdIndexIterator(dx_fast.rows(), 1);
			while (iter.hasNext()) {
		        int[] nextIndex = iter.next();
		        double nextValX = dx_fast.getDouble(nextIndex);
		        double nextValY = dy_fast.getDouble(nextIndex);
				wnsum = wnsum.add(Nd4j_Ex.roll(Nd4j_Ex.roll(caNRep_fast, 0, (int)nextValY), 1, (int)nextValX));
			}
			wsum= wsum.add(wnsum.mul(alpha / 12));
			ca_fast = caRep_fast.add(wsum);
		}
    	
    }
    
    private static void growth(){
   
        ca[(mx - 1) / 2][(my - 1) / 2] = 1.0; // ice seed
        for (int i = 0; i < maxIt; i++) {
            // separate the array into receptive and non-receptive arrays
            for (int iy = 0; iy < my; iy++) {
                for (int ix = 0; ix < mx; ix++) {
                	
                    boolean receptive = false;
                    if (ca[ix][iy] >= 1.0){ // ice
                        receptive = true;
                    }else{ // check neighbors
                        for (int j = 0; j < 6; j++) {
                            int jx = ix + dx[j];
                            int jy = iy + dy[j];
                            if (jx >= 0 && jx < mx && jy >= 0 && jy < my){
                                if (ca[jx][jy] >= 1.0) { // ice
                                    receptive = true;
                                    break;
                                }
                            }

                        }    
                    }

                    if (receptive) {
                        caRep[ix][iy] = ca[ix][iy] + gamma;
                        caNRep[ix][iy] = 0.0;
                    }else {
                        caRep[ix][iy] = 0.0;
                        caNRep[ix][iy] = ca[ix][iy];
                    }

                }
            }

            AddweighedAverages();

        }

    }
    
    public static void printArray(){
    	INDArray ca_out = Nd4j.create(ca);
    	System.out.println("Array ohne Nd4j:");
    	System.out.println(ca_out);
    	System.out.println("Array mit Nd4j:");
    	System.out.println(ca_fast);
    	
    	
    }
   
    private static void createWindow() {
        JFrame frame = new JFrame("snow crystal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dp = new drawPanel(500, 500);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(dp, BorderLayout.CENTER);
        frame.getContentPane().setBackground(Color.white);
        frame.pack();
        frame.setLocation(100, 100);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public static void main(String[] args){
    	//createWindow();
        Snowflake.generate();
        //Snowflake.generate_fast();

    }
}
